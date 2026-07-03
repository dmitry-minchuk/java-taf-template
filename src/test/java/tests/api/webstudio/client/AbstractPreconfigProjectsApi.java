package tests.api.webstudio.client;

import configuration.appcontainer.AppContainerPool;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.network.NetworkPool;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import domain.api.AuthorizedApiMethod;
import domain.api.DeploymentsMethod;
import domain.api.GetWsServicesMethod;
import domain.api.ProjectStatusMethod;
import domain.api.ProjectTestsMethod;
import domain.api.ProjectsMethod;
import domain.api.RepositoryProjectsMethod;
import domain.api.UsersMethod;
import helpers.service.DeployInfrastructureService;
import helpers.service.PreconfigSourcesService.PreconfigProject;
import helpers.utils.StringUtil;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Per-preconfig-project lifecycle:
 *   @BeforeClass → start deploy infrastructure (PostgreSQL production repo + ruleservice WS
 *                  container) and a fresh WebStudio wired to it, upload the project ZIP
 *   @Test        → open, compile (errors == 0), deploy to the production repository, verify the
 *                  service is served by ruleservice (GET /admin/services is 200 and lists it)
 *   @AfterClass  → stop all containers
 *
 * Subclasses are created dynamically via {@link TestPreconfigProjects} {@code @Factory}, one
 * instance per OpenL project discovered in the local Mercurial preconfig clones. The compile
 * part mirrors {@link AbstractZippedProjectsApi}; the deploy part goes through
 * {@code POST /rest/deployments} (same operation as the UI DeployModal) and asserts the
 * ruleservice side, which the zip and central regressions do not cover.
 */
public abstract class AbstractPreconfigProjectsApi implements ITest {
    protected static final Logger LOGGER = LogManager.getLogger(AbstractPreconfigProjectsApi.class);
    private static final Duration CONTAINER_STARTUP_TIMEOUT = Duration.ofMinutes(10);
    private static final int COMPILE_POLL_INTERVAL_MS = 500;
    private static final int COMPILE_POLL_TIMEOUT_MS = 5 * 60 * 1_000;
    private static final int WS_SERVICE_POLL_INTERVAL_MS = 3_000;
    private static final int WS_SERVICE_POLL_TIMEOUT_MS = 5 * 60 * 1_000;
    private static final int WS_PORT = 8080;
    private static final String DESIGN_REPO = "design";
    private static final String PRODUCTION_REPO_ID = "production";

    private final PreconfigProject project;
    private DeployInfrastructureService deployInfra;
    private String uploadFailure;

    protected AbstractPreconfigProjectsApi(PreconfigProject project) {
        this.project = project;
    }

    public String getProjectLabel() {
        return project.label();
    }

    @BeforeClass
    public void setUp() {
        startDeployInfraWithRetry();
        startStudioContainer();
        AuthorizedApiMethod.startSession();
        configureCommitIdentity();

        LOGGER.info("Uploading preconfig [{}] (project [{}]) from {}",
                project.label(), project.projectName(), project.zip().getName());
        Response upload = new RepositoryProjectsMethod()
                .uploadProject(DESIGN_REPO, project.projectName(), project.zip());
        if (upload.getStatusCode() >= 300) {
            uploadFailure = String.format("Upload failed for %s (project name [%s]): HTTP %d — %s",
                    project.zip().getAbsolutePath(), project.projectName(),
                    upload.getStatusCode(), upload.getBody().asString());
            LOGGER.warn(uploadFailure);
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        AuthorizedApiMethod.clearSession();
        if (AppContainerPool.get() != null) {
            AppContainerPool.closeAppContainer();
        }
        if (deployInfra != null) {
            deployInfra.cleanup();
        }
    }

    @Override
    public String getTestName() {
        return "testPreconfigProject[" + project.label() + "]";
    }

    @Test
    public void testPreconfigProject() {
        if (uploadFailure != null) {
            Assert.fail(String.format("Preconfig [%s] — upload failed:%n  %s%nSources: %s",
                    project.label(), uploadFailure, project.zip().getAbsolutePath()));
        }

        String projectId = findProjectId();
        LOGGER.info("Validating preconfig [{}] (id={})", project.label(), projectId);

        Response open = new ProjectsMethod().openProject(projectId);
        Assert.assertTrue(open.getStatusCode() < 300,
                String.format("Failed to open project [%s]: HTTP %d — %s",
                        project.projectName(), open.getStatusCode(), open.getBody().asString()));

        // tests/run opens the module and awaits compilation server-side — it is the compile
        // trigger; a plain open leaves compileState 'idle' forever.
        Response run = new ProjectTestsMethod().runAllTests(projectId);
        Assert.assertTrue(run.getStatusCode() == 200 || run.getStatusCode() == 202 || run.getStatusCode() == 404,
                String.format("Failed to trigger compile for [%s]: HTTP %d — %s",
                        project.projectName(), run.getStatusCode(), run.getBody().asString()));

        Response statusResp = awaitCompilation(projectId);
        Assert.assertNotNull(statusResp, String.format("No /status response for [%s]", project.projectName()));
        Assert.assertEquals(statusResp.getStatusCode(), 200,
                String.format("Project status failed for [%s]: HTTP %d — %s",
                        project.projectName(), statusResp.getStatusCode(), statusResp.getBody().asString()));
        JsonPath status = statusResp.jsonPath();
        String compileState = status.getString("compileState");
        int compileErrors = intOrZero(status.get("compilation.messages.errors"));
        if ("errors".equalsIgnoreCase(compileState) || compileErrors > 0) {
            Assert.fail(buildCompileErrorReport(status));
        }
        LOGGER.info("Preconfig [{}] compiled: state={}, errors=0", project.label(), compileState);

        deployAndVerifyService(projectId);
    }

    private void deployAndVerifyService(String projectId) {
        String deploymentName = StringUtil.generateUniqueName("preconfig-deploy");
        LOGGER.info("Deploying [{}] to production repository [{}] as [{}]",
                project.projectName(), PRODUCTION_REPO_ID, deploymentName);
        Response deploy = new DeploymentsMethod()
                .deploy(PRODUCTION_REPO_ID, deploymentName, projectId, "Preconfig regression deploy");
        Assert.assertTrue(deploy.getStatusCode() < 300,
                String.format("Deploy failed for [%s]: HTTP %d — %s",
                        project.projectName(), deploy.getStatusCode(), deploy.getBody().asString()));

        GetWsServicesMethod wsServices = new GetWsServicesMethod(deployInfra.getWsContainer(), WS_PORT);
        long deadline = System.currentTimeMillis() + WS_SERVICE_POLL_TIMEOUT_MS;
        List<String> lastSeen = List.of();
        while (System.currentTimeMillis() < deadline) {
            try {
                lastSeen = wsServices.getServiceNames();
                if (lastSeen.stream().anyMatch(this::matchesThisProject)) {
                    LOGGER.info("Preconfig [{}] is served by ruleservice: services={}", project.label(), lastSeen);
                    return;
                }
            } catch (RuntimeException e) {
                LOGGER.info("Ruleservice not ready yet for [{}]: {}", project.label(), e.getMessage());
            }
            sleepInterruptible(WS_SERVICE_POLL_INTERVAL_MS);
        }
        Assert.fail(String.format(
                "Preconfig [%s] was deployed (HTTP<300) but did not appear in ruleservice within %d s.%n"
                        + "Expected a service matching serviceName [%s] or project [%s]; /admin/services returned: %s",
                project.label(), WS_SERVICE_POLL_TIMEOUT_MS / 1000,
                project.serviceName(), project.projectName(), lastSeen));
    }

    /** Ruleservice names the service from rules-deploy.xml serviceName, or falls back to project naming. */
    private boolean matchesThisProject(String serviceName) {
        if (serviceName == null) {
            return false;
        }
        return (project.serviceName() != null && serviceName.contains(project.serviceName()))
                || serviceName.contains(project.projectName());
    }

    private String findProjectId() {
        Response list = new ProjectsMethod().getAllProjects(100);
        Assert.assertEquals(list.getStatusCode(), 200,
                String.format("GET /rest/projects failed: HTTP %d — %s",
                        list.getStatusCode(), list.getBody().asString()));
        List<Map<String, Object>> projects = list.jsonPath().getList("content");
        Assert.assertNotNull(projects, "GET /rest/projects returned no content array");
        return projects.stream()
                .filter(p -> project.projectName().equals(String.valueOf(p.get("name"))))
                .map(p -> String.valueOf(p.get("id")))
                .findFirst()
                .orElseGet(() -> {
                    Assert.fail(String.format("Project [%s] was uploaded but is not listed in GET /rest/projects",
                            project.projectName()));
                    return null;
                });
    }

    /** Parallel container starts occasionally flake on the Docker daemon — one clean retry fixes it. */
    private void startDeployInfraWithRetry() {
        for (int attempt = 1; ; attempt++) {
            deployInfra = DeployInfrastructureService.builder()
                    .withPostgres()
                    .withWsContainer()
                    .build();
            try {
                deployInfra.start();
                return;
            } catch (RuntimeException e) {
                deployInfra.cleanup();
                if (attempt >= 2) {
                    throw e;
                }
                LOGGER.warn("Deploy infrastructure failed to start for [{}] (attempt {}): {} — retrying",
                        project.label(), attempt, e.getMessage());
                sleepInterruptible(5_000);
            }
        }
    }

    private void startStudioContainer() {
        String containerName = StringUtil.generateUniqueName("preconfig_" + sanitize(project.moduleName()));
        Map<String, String> envVars = AppContainerStartParameters.DEPLOY_STUDIO_PARAMS.getParameterMap();
        String dockerImage = ProjectConfiguration.getProperty(PropertyNameSpace.DOCKER_IMAGE_NAME);
        LOGGER.info("Starting WebStudio container for preconfig [{}]", project.label());
        // Same network as the deploy infra (registered in NetworkPool by deployInfra.start()) so the
        // studio reaches PostgreSQL by alias; production-repo config arrives as a copied .properties.
        AppContainerPool.setAppContainer(containerName, NetworkPool.getNetwork(), envVars,
                deployInfra.getFilesToCopy(), dockerImage, CONTAINER_STARTUP_TIMEOUT);
    }

    private void configureCommitIdentity() {
        Response resp = new UsersMethod().setCurrentUserInfo(
                "Test", "Automation", "test-automation@openl.local", "Test Automation");
        if (resp.getStatusCode() >= 300) {
            LOGGER.warn("Could not set admin commit identity (HTTP {}): {}",
                    resp.getStatusCode(), resp.getBody().asString());
        }
    }

    private Response awaitCompilation(String projectId) {
        ProjectStatusMethod statusApi = new ProjectStatusMethod();
        long deadline = System.currentTimeMillis() + COMPILE_POLL_TIMEOUT_MS;
        Response last = null;
        while (System.currentTimeMillis() < deadline) {
            last = statusApi.getStatus(projectId, false);
            if (last.getStatusCode() == 200) {
                String state = last.jsonPath().getString("compileState");
                if (state != null && !state.equalsIgnoreCase("idle")
                        && !state.equalsIgnoreCase("compiling") && !state.equalsIgnoreCase("inProgress")) {
                    return last;
                }
            }
            sleepInterruptible(COMPILE_POLL_INTERVAL_MS);
        }
        return last;
    }

    @SuppressWarnings("unchecked")
    private String buildCompileErrorReport(JsonPath status) {
        List<Map<String, Object>> items = status.getList("compilation.messages.items");
        List<String> errors = new ArrayList<>();
        if (items != null) {
            for (Map<String, Object> msg : items) {
                if ("ERROR".equalsIgnoreCase(String.valueOf(msg.get("severity")))) {
                    errors.add(String.valueOf(msg.get("summary")));
                }
            }
        }
        StringBuilder sb = new StringBuilder(String.format(
                "Compilation errors detected in preconfig [%s] (project [%s])",
                project.label(), project.projectName()));
        sb.append(String.format("%nSources zip: %s", project.zip().getAbsolutePath()));
        sb.append(String.format("%nERRORS (%d):", errors.size()));
        for (int i = 0; i < errors.size(); i++) {
            sb.append(String.format("%n  %d. %s", i + 1, errors.get(i)));
        }
        return sb.toString();
    }

    private static int intOrZero(Object value) {
        if (value instanceof Number n) {
            return n.intValue();
        }
        try {
            return value == null ? 0 : Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static void sleepInterruptible(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while polling", e);
        }
    }

    private static String sanitize(String s) {
        return s == null ? "project" : s.replaceAll("[^a-zA-Z0-9_-]+", "_").toLowerCase();
    }
}
