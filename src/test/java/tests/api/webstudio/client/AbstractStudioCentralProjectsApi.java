package tests.api.webstudio.client;

import configuration.appcontainer.AppContainerPool;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import domain.api.AuthorizedApiMethod;
import domain.api.ProjectStatusMethod;
import domain.api.ProjectTestsMethod;
import domain.api.ProjectsMethod;
import helpers.utils.StringUtil;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public abstract class AbstractStudioCentralProjectsApi implements ITest {
    protected static final Logger LOGGER = LogManager.getLogger(AbstractStudioCentralProjectsApi.class);
    private static final Duration CONTAINER_STARTUP_TIMEOUT = Duration.ofMinutes(60);
    private static final int TEST_SUMMARY_POLL_INTERVAL_MS = 2_000;
    private static final int TEST_SUMMARY_POLL_TIMEOUT_MS = 10 * 60 * 1_000;
    private static final int COMPILE_POLL_INTERVAL_MS = 1_500;
    // Safety net only: tests/run already awaits compilation server-side before it returns.
    private static final int COMPILE_POLL_TIMEOUT_MS = 60 * 1_000;

    private final Map<String, Map<String, Object>> projectsByName = new LinkedHashMap<>();
    // Per-invocation test name so ReportPortal shows the project instead of the bare method name.
    private final ThreadLocal<String> currentTestName = new ThreadLocal<>();

    protected abstract AppContainerStartParameters params();

    protected abstract String groupLabel();

    @BeforeClass
    public void setUp() {
        startContainer();
        AuthorizedApiMethod.startSession();

        LOGGER.info("Listing projects for group [{}] — triggers lazy git clone if not yet done...", groupLabel());
        Response listResponse = new ProjectsMethod().getAllProjects(500);
        if (listResponse.getStatusCode() != 200) {
            throw new IllegalStateException(String.format(
                    "Failed to list projects for group [%s]: HTTP %d — %s",
                    groupLabel(), listResponse.getStatusCode(), listResponse.getBody().asString()));
        }
        List<Map<String, Object>> projects = extractProjects(listResponse);
        for (Map<String, Object> project : projects) {
            projectsByName.put(String.valueOf(project.get("name")), project);
        }
        LOGGER.info("Found {} projects in group [{}]", projectsByName.size(), groupLabel());

        openAllProjects();
    }

    /**
     * Bulk-open every discovered project so cross-project dependencies resolve before
     * any per-project compilation/test check runs. A project that depends on another
     * CLOSED project would otherwise fail to compile.
     */
    private void openAllProjects() {
        ProjectsMethod projects = new ProjectsMethod();
        int opened = 0;
        int alreadyOpen = 0;
        int failed = 0;
        for (Map.Entry<String, Map<String, Object>> entry : projectsByName.entrySet()) {
            String name = entry.getKey();
            Map<String, Object> project = entry.getValue();
            String status = String.valueOf(project.get("status"));
            String id = String.valueOf(project.get("id"));
            if ("OPENED".equalsIgnoreCase(status)) {
                alreadyOpen++;
                continue;
            }
            Response resp = projects.openProject(id);
            if (resp.getStatusCode() < 300) {
                opened++;
                project.put("status", "OPENED");
            } else {
                failed++;
                LOGGER.warn("Failed to open project [{}]: HTTP {} — {}",
                        name, resp.getStatusCode(), resp.getBody().asString());
            }
        }
        LOGGER.info("Bulk-open for group [{}]: opened={}, alreadyOpen={}, failed={}",
                groupLabel(), opened, alreadyOpen, failed);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        AuthorizedApiMethod.clearSession();
        if (AppContainerPool.get() != null) {
            AppContainerPool.closeAppContainer();
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void recordTestName(Method method, Object[] params) {
        String projectName = (params != null && params.length > 0) ? String.valueOf(params[0]) : "";
        currentTestName.set(method.getName() + "[" + projectName + "]");
    }

    @Override
    public String getTestName() {
        String n = currentTestName.get();
        return n != null ? n : "testStudioCentralProject";
    }

    @DataProvider(name = "studioCentralProjects")
    public Object[][] studioCentralProjects() {
        return projectsByName.keySet().stream()
                .map(name -> new Object[]{name})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "studioCentralProjects")
    public void testStudioCentralProject(String projectName) {
        Map<String, Object> project = projectsByName.get(projectName);
        Assert.assertNotNull(project, "Project not found in discovered set: " + projectName);
        validateProject(project);
    }

    private void startContainer() {
        AppContainerStartParameters startParams = params();
        String containerName = StringUtil.generateUniqueName("studio_central_" + startParams.name().toLowerCase());
        Map<String, String> envVars = startParams.getParameterMap();
        envVars.forEach((k, v) -> LOGGER.info("[{}] -> [{}]", k, v));
        String dockerImage = ProjectConfiguration.getProperty(PropertyNameSpace.DOCKER_IMAGE_NAME);

        LOGGER.info("Starting WebStudio container for group [{}]. First boot clones design repos and may take up to {} minutes.",
                groupLabel(), CONTAINER_STARTUP_TIMEOUT.toMinutes());
        AtomicBoolean done = new AtomicBoolean(false);
        long started = System.currentTimeMillis();
        Thread heartbeat = new Thread(() -> {
            try {
                while (!done.get()) {
                    Thread.sleep(30_000);
                    if (done.get()) return;
                    long elapsedSec = (System.currentTimeMillis() - started) / 1000;
                    LOGGER.info("...still cloning/booting [{}] — elapsed {}m {}s",
                            groupLabel(), elapsedSec / 60, elapsedSec % 60);
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }, "studio-central-warmup-heartbeat");
        heartbeat.setDaemon(true);
        heartbeat.start();
        try {
            AppContainerPool.setAppContainer(containerName, null, envVars, null, dockerImage, CONTAINER_STARTUP_TIMEOUT);
        } finally {
            done.set(true);
            heartbeat.interrupt();
        }
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
        LOGGER.info("Validating project [{}] (id={})", projectName, projectId);

        // Re-anchor "current project" in session even if already OPENED.
        Response open = new ProjectsMethod().openProject(projectId);
        Assert.assertTrue(open.getStatusCode() < 300,
                String.format("Failed to set project %s as current: HTTP %d — %s",
                        projectName, open.getStatusCode(), open.getBody().asString()));

        // tests/run opens a module, awaits compilation server-side, then runs all tests — so it is
        // also the compile trigger (a plain open leaves compileState 'idle'). 404 means the project
        // has no module to open/compile.
        Response runResponse = new ProjectTestsMethod().runAllTests(projectId);
        int runStatus = runResponse.getStatusCode();
        if (runStatus == 404 || runStatus == 204) {
            LOGGER.info("Project [{}] has no modules to compile/run — skipping", projectName);
            return;
        }
        Assert.assertTrue(runStatus == 200 || runStatus == 202,
                String.format("Failed to compile/run tests for project %s: HTTP %d — %s",
                        projectName, runStatus, runResponse.getBody().asString()));

        // Compilation is finished by the time tests/run returns; /status now reports the real state
        // (replaces the removed /modules + /compile/progress endpoints).
        Response statusResp = awaitCompilation(projectId);
        Assert.assertEquals(statusResp.getStatusCode(), 200,
                String.format("Project status failed for project %s: HTTP %d — %s",
                        projectName, statusResp.getStatusCode(), statusResp.getBody().asString()));
        JsonPath status = statusResp.jsonPath();
        String compileState = status.getString("compileState");
        int compileErrors = intOrZero(status.get("compilation.messages.errors"));
        if ("errors".equalsIgnoreCase(compileState) || compileErrors > 0) {
            String detail = buildCompileErrorReport(projectName, status);
            LOGGER.error(detail);
            Assert.fail(detail);
        }

        Response summary = pollTestsSummary(projectId, projectName);
        Assert.assertNotNull(summary, String.format("Test summary timed out for project [%s]", projectName));
        int code = summary.getStatusCode();
        if (code == 404) {
            LOGGER.info("Project [{}] has no Test tables — compile validated, nothing to run", projectName);
            return;
        }
        Assert.assertEquals(code, 200,
                String.format("Tests summary returned HTTP %d for project [%s]: %s",
                        code, projectName, summary.getBody().asString()));

        Integer failures = summary.jsonPath().getInt("numberOfFailures");
        Integer total = summary.jsonPath().getInt("numberOfTests");
        LOGGER.info("Project [{}] tests: total={}, failures={}", projectName, total, failures);
        if (failures != null && failures > 0) {
            String detail = buildFailureReport(projectName, total, failures, summary.jsonPath());
            LOGGER.error(detail);
            Assert.fail(detail);
        }
    }

    // Poll /status until the project reaches a terminal compile state (ok/warnings/errors).
    private Response awaitCompilation(String projectId) {
        ProjectStatusMethod statusApi = new ProjectStatusMethod();
        long deadline = System.currentTimeMillis() + COMPILE_POLL_TIMEOUT_MS;
        Response last = null;
        while (System.currentTimeMillis() < deadline) {
            last = statusApi.getStatus(projectId, false);
            if (last.getStatusCode() == 200) {
                String state = last.jsonPath().getString("compileState");
                if (state != null && !state.equalsIgnoreCase("idle") && !state.equalsIgnoreCase("compiling")) {
                    return last;
                }
            }
            sleepInterruptible(COMPILE_POLL_INTERVAL_MS);
        }
        return last;
    }

    @SuppressWarnings("unchecked")
    private String buildCompileErrorReport(String projectName, JsonPath status) {
        List<Map<String, Object>> items = status.getList("compilation.messages.items");
        List<String> errors = new java.util.ArrayList<>();
        if (items != null) {
            for (Map<String, Object> msg : items) {
                if ("ERROR".equalsIgnoreCase(stringOrNull(msg.get("severity")))) {
                    String summary = stringOrNull(msg.get("summary"));
                    errors.add(summary != null ? summary : "(empty)");
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Compilation errors detected in project: %s%n", projectName));
        sb.append(String.format("ERRORS (%d):%n", errors.size()));
        for (int i = 0; i < errors.size(); i++) {
            sb.append(String.format("  %d. %s%n", i + 1, errors.get(i)));
        }
        return sb.toString();
    }

    private int intOrZero(Object o) {
        Integer v = toInt(o);
        return v == null ? 0 : v;
    }

    @SuppressWarnings("unchecked")
    private String buildFailureReport(String projectName, int total, int failures, JsonPath summaryJson) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Project [%s] has %d test failures (of %d total)", projectName, failures, total));
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
            sb.append(String.format("%n  TestCase [%s] — %d/%d failed", caseName, caseFailures, caseTotal == null ? 0 : caseTotal));
            List<Map<String, Object>> testUnits = (List<Map<String, Object>>) testCase.get("testUnits");
            if (testUnits == null) continue;
            for (Map<String, Object> unit : testUnits) {
                String status = stringOrNull(unit.get("status"));
                if (status == null || status.equalsIgnoreCase("TR_OK")) continue;
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
                if (aStatus != null && aStatus.equalsIgnoreCase("TR_OK")) {
                    continue;
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
                sleepInterruptible();
                continue;
            }
            if (code == 404) {
                consecutive404++;
                if (consecutive404 >= 3) {
                    return last;
                }
                sleepInterruptible();
                continue;
            }
            LOGGER.warn("Unexpected test summary status {} for project [{}]: {}",
                    code, projectName, last.getBody().asString());
            return last;
        }
        return last;
    }

    private void sleepInterruptible() {
        sleepInterruptible(TEST_SUMMARY_POLL_INTERVAL_MS);
    }

    private void sleepInterruptible(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
