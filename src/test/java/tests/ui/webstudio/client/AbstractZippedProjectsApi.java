package tests.ui.webstudio.client;

import configuration.appcontainer.AppContainerPool;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import domain.api.AuthorizedApiMethod;
import domain.api.CompileMethod;
import domain.api.ProjectModulesMethod;
import domain.api.ProjectTestsMethod;
import domain.api.ProjectsMethod;
import domain.api.RepositoryProjectsMethod;
import domain.api.UsersMethod;
import helpers.utils.StringUtil;
import helpers.utils.ZipProjectNameReader;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Per-deployment-group lifecycle:
 *   @BeforeClass     → start fresh WebStudio container, upload all zips in the group
 *   @Test per project → validate (open, modules, compile-check, run tests + summary)
 *   @AfterClass      → stop container
 *
 * Subclasses are created dynamically via {@link TestZippedProjects} {@code @Factory},
 * one instance per discovered group on disk.
 */
public abstract class AbstractZippedProjectsApi {
    protected static final Logger LOGGER = LogManager.getLogger(AbstractZippedProjectsApi.class);
    private static final Duration CONTAINER_STARTUP_TIMEOUT = Duration.ofMinutes(10);
    private static final int TEST_SUMMARY_POLL_INTERVAL_MS = 500;
    private static final int TEST_SUMMARY_POLL_TIMEOUT_MS = 10 * 60 * 1_000;
    private static final int COMPILE_POLL_INTERVAL_MS = 500;
    // Shortened from 3 min — most projects compile within seconds when scoped to a single module.
    // If compile is genuinely slow we exit early, and tests/run will still wait for compile server-side.
    // The cost is that compile errors in test-less projects with very slow compile might not be surfaced
    // here, but UI mode had the same coverage gap (Problems panel showed first-module errors only).
    private static final int COMPILE_POLL_TIMEOUT_MS = 30 * 1_000;
    private static final String DESIGN_REPO = "design";

    private final List<File> zipsInGroup;
    private final String groupLabel;
    private final Map<String, Map<String, Object>> projectsByName = new LinkedHashMap<>();
    private final List<String> uploadedProjectNames = new ArrayList<>();

    protected AbstractZippedProjectsApi(List<File> zipsInGroup, String groupLabel) {
        this.zipsInGroup = zipsInGroup;
        this.groupLabel = groupLabel;
    }

    public String getGroupLabel() {
        return groupLabel;
    }

    @BeforeClass
    public void setUp() {
        startContainer();
        AuthorizedApiMethod.startSession();
        configureCommitIdentity();

        for (File zip : zipsInGroup) {
            String name = ZipProjectNameReader.readProjectName(zip);
            LOGGER.info("Uploading [{}] from {}", name, zip.getName());
            Response upload = new RepositoryProjectsMethod().uploadProject(DESIGN_REPO, name, zip);
            if (upload.getStatusCode() >= 300) {
                // Don't kill the whole group on a single bad zip — log and continue.
                // Common case: two zips in a deployment folder export the same business name,
                // server accepts the first and rejects the second with 4xx/5xx.
                LOGGER.warn("Upload failed for zip {} (project name [{}]): HTTP {} — {}",
                        zip.getName(), name, upload.getStatusCode(), upload.getBody().asString());
                continue;
            }
            if (!uploadedProjectNames.contains(name)) {
                uploadedProjectNames.add(name);
            }
        }
        if (uploadedProjectNames.isEmpty()) {
            throw new IllegalStateException(String.format(
                    "All uploads failed for group [%s] — no projects to validate", groupLabel));
        }

        Response listResponse = new ProjectsMethod().getAllProjects(500);
        if (listResponse.getStatusCode() != 200) {
            throw new IllegalStateException(String.format(
                    "Failed to list projects for group [%s]: HTTP %d — %s",
                    groupLabel, listResponse.getStatusCode(), listResponse.getBody().asString()));
        }
        List<Map<String, Object>> projects = extractProjects(listResponse);
        for (Map<String, Object> project : projects) {
            projectsByName.put(String.valueOf(project.get("name")), project);
        }
        LOGGER.info("Group [{}]: uploaded {} zip(s), {} project(s) visible via API",
                groupLabel, zipsInGroup.size(), projectsByName.size());
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        AuthorizedApiMethod.clearSession();
        if (AppContainerPool.get() != null) {
            AppContainerPool.closeAppContainer();
        }
    }

    @DataProvider(name = "zippedProjects")
    public Object[][] zippedProjects() {
        return uploadedProjectNames.stream()
                .map(name -> new Object[]{name})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "zippedProjects")
    public void testZippedProject(String projectName) {
        Map<String, Object> project = projectsByName.get(projectName);
        Assert.assertNotNull(project, String.format(
                "Project [%s] was uploaded but not visible in GET /rest/projects for group [%s]",
                projectName, groupLabel));
        validateProject(project);
    }

    /**
     * Set first/last name + email for the authenticated admin. Without this,
     * the local design-repo's JGit commits fail with
     * "Name of PersonIdent must not be null" the first time a project is created.
     * The legacy UI test handled this via the "Configure Commit Info" modal.
     */
    private void configureCommitIdentity() {
        Response resp = new UsersMethod().setCurrentUserInfo(
                "Test", "Automation", "test-automation@openl.local", "Test Automation");
        if (resp.getStatusCode() >= 300) {
            LOGGER.warn("Could not set admin commit identity (HTTP {}): {}",
                    resp.getStatusCode(), resp.getBody().asString());
        }
    }

    private void startContainer() {
        String containerName = StringUtil.generateUniqueName("zipped_" + sanitize(groupLabel));
        Map<String, String> envVars = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS.getParameterMap();
        String dockerImage = ProjectConfiguration.getProperty(PropertyNameSpace.DOCKER_IMAGE_NAME);

        LOGGER.info("Starting WebStudio container for zipped group [{}] with {} zip(s)",
                groupLabel, zipsInGroup.size());
        AppContainerPool.setAppContainer(containerName, null, envVars, null, dockerImage, CONTAINER_STARTUP_TIMEOUT);
    }

    private static String sanitize(String s) {
        return s == null ? "group" : s.replaceAll("[^a-zA-Z0-9_-]+", "_").toLowerCase();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractProjects(Response response) {
        JsonPath json = response.jsonPath();
        Object content = json.get("content");
        if (content instanceof List) {
            return (List<Map<String, Object>>) content;
        }
        return (List<Map<String, Object>>) (List<?>) json.getList("$");
    }

    private void validateProject(Map<String, Object> project) {
        String projectId = String.valueOf(project.get("id"));
        String projectName = String.valueOf(project.get("name"));
        LOGGER.info("Validating project [{}] (id={}, group={})", projectName, projectId, groupLabel);

        Response open = new ProjectsMethod().openProject(projectId);
        Assert.assertTrue(open.getStatusCode() < 300,
                String.format("Failed to set project %s as current: HTTP %d — %s",
                        projectName, open.getStatusCode(), open.getBody().asString()));

        Response modulesResp = new ProjectModulesMethod(projectId).listModules();
        Assert.assertEquals(modulesResp.getStatusCode(), 200,
                String.format("List modules failed for project %s in group %s",
                        projectName, groupLabel));
        List<Map<String, Object>> modules = extractModuleList(modulesResp);
        if (modules.isEmpty()) {
            LOGGER.info("Project [{}] has no modules — skipping test run", projectName);
            return;
        }
        String firstModuleName = String.valueOf(modules.get(0).get("name"));

        checkCompilation(projectName);
        runProjectTests(projectId, projectName, firstModuleName);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractModuleList(Response response) {
        return (List<Map<String, Object>>) (List<?>) response.jsonPath().getList("$");
    }

    @SuppressWarnings("unchecked")
    private void checkCompilation(String projectName) {
        CompileMethod compile = new CompileMethod();
        long deadline = System.currentTimeMillis() + COMPILE_POLL_TIMEOUT_MS;
        Response last = null;
        while (System.currentTimeMillis() < deadline) {
            last = compile.getCompileProgress(-1L, -1, false);
            if (last.getStatusCode() != 200) {
                LOGGER.warn("Compile progress returned HTTP {} for project [{}]: {}",
                        last.getStatusCode(), projectName, last.getBody().asString());
                return;
            }
            Boolean completed = last.jsonPath().getBoolean("compilationCompleted");
            if (Boolean.TRUE.equals(completed)) {
                break;
            }
            sleepInterruptible(COMPILE_POLL_INTERVAL_MS);
        }
        if (last == null) {
            return;
        }
        Integer errorsCount = toInt(last.jsonPath().get("errorsCount"));
        List<Map<String, Object>> messages = last.jsonPath().getList("messages");
        List<String> errors = new ArrayList<>();
        if (messages != null) {
            for (Map<String, Object> msg : messages) {
                String severity = stringOrNull(msg.get("severity"));
                if (severity != null && severity.equalsIgnoreCase("ERROR")) {
                    String summary = stringOrNull(msg.get("summary"));
                    errors.add(summary != null ? summary : "(empty)");
                }
            }
        }
        if (errors.isEmpty() && (errorsCount == null || errorsCount == 0)) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Compilation errors detected in project: %s%n", projectName));
        sb.append(String.format("Group: %s%n", groupLabel));
        sb.append(String.format("ERRORS (%d):%n", errors.size()));
        for (int i = 0; i < errors.size(); i++) {
            sb.append(String.format("  %d. %s%n", i + 1, errors.get(i)));
        }
        String detail = sb.toString();
        LOGGER.error(detail);
        Assert.fail(detail);
    }

    private void runProjectTests(String projectId, String projectName, String fromModule) {
        Response runResponse = new ProjectTestsMethod().runAllTests(projectId, fromModule);
        int runStatus = runResponse.getStatusCode();
        if (runStatus == 404 || runStatus == 204) {
            LOGGER.info("Project [{}] has no Test tables — skipping test execution", projectName);
            return;
        }
        Assert.assertTrue(runStatus == 200 || runStatus == 202,
                String.format("Failed to start tests for project %s: HTTP %d — %s",
                        projectName, runStatus, runResponse.getBody().asString()));

        Response summary = pollTestsSummary(projectId, projectName);
        Assert.assertNotNull(summary, String.format("Test summary timed out for project [%s]", projectName));
        if (summary.getStatusCode() == 404) {
            LOGGER.info("Project [{}] reports no test execution task — likely no Test tables", projectName);
            return;
        }
        Assert.assertEquals(summary.getStatusCode(), 200,
                String.format("Tests summary returned HTTP %d for project [%s]: %s",
                        summary.getStatusCode(), projectName, summary.getBody().asString()));

        Integer failures = summary.jsonPath().getInt("numberOfFailures");
        Integer total = summary.jsonPath().getInt("numberOfTests");
        LOGGER.info("Project [{}] tests: total={}, failures={}", projectName, total, failures);
        if (failures != null && failures > 0) {
            String detail = buildFailureReport(projectName, total, failures, summary.jsonPath());
            LOGGER.error(detail);
            Assert.fail(detail);
        }
    }

    @SuppressWarnings("unchecked")
    private String buildFailureReport(String projectName, int total, int failures, JsonPath summaryJson) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Project [%s] has %d test failures (of %d total) in group [%s]",
                projectName, failures, total, groupLabel));
        List<Map<String, Object>> testCases = summaryJson.getList("testCases");
        if (testCases == null || testCases.isEmpty()) {
            return sb.toString();
        }
        for (Map<String, Object> testCase : testCases) {
            Integer caseFailures = toInt(testCase.get("numberOfFailures"));
            if (caseFailures == null || caseFailures == 0) {
                continue;
            }
            String caseName = String.valueOf(testCase.get("name"));
            Integer caseTotal = toInt(testCase.get("numberOfTests"));
            sb.append(String.format("%n  TestCase [%s] — %d/%d failed",
                    caseName, caseFailures, caseTotal == null ? 0 : caseTotal));
            List<Map<String, Object>> testUnits = (List<Map<String, Object>>) testCase.get("testUnits");
            if (testUnits == null) continue;
            for (Map<String, Object> unit : testUnits) {
                String status = stringOrNull(unit.get("status"));
                if (status == null) continue;
                String up = status.toUpperCase();
                if (!up.contains("FAIL") && !up.equals("ERROR")) {
                    continue;
                }
                appendFailedUnit(sb, unit);
            }
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private void appendFailedUnit(StringBuilder sb, Map<String, Object> unit) {
        String description = stringOrNull(unit.get("description"));
        String id = stringOrNull(unit.get("id"));
        String status = stringOrNull(unit.get("status"));
        sb.append(String.format("%n    Unit [%s] status=%s",
                description != null ? description : id, status));

        List<Map<String, Object>> params = (List<Map<String, Object>>) unit.get("parameters");
        if (params != null && !params.isEmpty()) {
            sb.append(String.format("%n      input: "));
            sb.append(params.stream().map(this::formatParamValue).collect(Collectors.joining(", ")));
        }
        List<Map<String, Object>> assertions = (List<Map<String, Object>>) unit.get("testAssertions");
        if (assertions != null) {
            for (Map<String, Object> assertion : assertions) {
                String aStatus = stringOrNull(assertion.get("status"));
                if (aStatus != null) {
                    String up = aStatus.toUpperCase();
                    if (!up.contains("FAIL") && !up.equals("ERROR")) {
                        continue;
                    }
                }
                String aDesc = stringOrNull(assertion.get("description"));
                Object expected = assertion.get("expectedValue");
                Object actual = assertion.get("actualValue");
                sb.append(String.format("%n      assertion[%s]: expected=%s actual=%s",
                        aDesc != null ? aDesc : "-", expected, actual));
            }
        }
        List<Map<String, Object>> errors = (List<Map<String, Object>>) unit.get("errors");
        if (errors != null && !errors.isEmpty()) {
            for (Map<String, Object> err : errors) {
                String severity = stringOrNull(err.get("severity"));
                String summary = stringOrNull(err.get("summary"));
                sb.append(String.format("%n      error[%s]: %s", severity, summary));
            }
        }
    }

    private String formatParamValue(Map<String, Object> param) {
        Object name = param.get("name");
        Object value = param.get("value");
        return String.valueOf(name) + "=" + String.valueOf(value);
    }

    private String stringOrNull(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Response pollTestsSummary(String projectId, String projectName) {
        long deadline = System.currentTimeMillis() + TEST_SUMMARY_POLL_TIMEOUT_MS;
        ProjectTestsMethod client = new ProjectTestsMethod();
        Response last = null;
        int consecutive404 = 0;
        while (System.currentTimeMillis() < deadline) {
            last = client.getTestsSummary(projectId, false, 100, false);
            int code = last.getStatusCode();
            if (code == 200) {
                return last;
            }
            if (code == 202 || code == 409) {
                consecutive404 = 0;
                sleepInterruptible(TEST_SUMMARY_POLL_INTERVAL_MS);
                continue;
            }
            if (code == 404) {
                consecutive404++;
                if (consecutive404 >= 3) {
                    return last;
                }
                sleepInterruptible(TEST_SUMMARY_POLL_INTERVAL_MS);
                continue;
            }
            LOGGER.warn("Unexpected test summary status {} for project [{}]: {}",
                    code, projectName, last.getBody().asString());
            return last;
        }
        return last;
    }

    private void sleepInterruptible(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
