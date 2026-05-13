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
import helpers.utils.StringUtil;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public abstract class AbstractStudioCentralProjectsApi {
    protected static final Logger LOGGER = LogManager.getLogger(AbstractStudioCentralProjectsApi.class);
    private static final Duration CONTAINER_STARTUP_TIMEOUT = Duration.ofMinutes(60);
    private static final int TEST_SUMMARY_POLL_INTERVAL_MS = 2_000;
    private static final int TEST_SUMMARY_POLL_TIMEOUT_MS = 10 * 60 * 1_000;
    private static final int COMPILE_POLL_INTERVAL_MS = 1_500;
    private static final int COMPILE_POLL_TIMEOUT_MS = 3 * 60 * 1_000;

    private final Map<String, Map<String, Object>> projectsByName = new LinkedHashMap<>();

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
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        AuthorizedApiMethod.clearSession();
        if (AppContainerPool.get() != null) {
            AppContainerPool.closeAppContainer();
        }
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
        String status = String.valueOf(project.get("status"));
        LOGGER.info("Validating project [{}] (id={}, status={})", projectName, projectId, status);

        if (!"OPENED".equalsIgnoreCase(status)) {
            Response open = new ProjectsMethod().openProject(projectId);
            Assert.assertTrue(open.getStatusCode() < 300,
                    String.format("Failed to open project %s: HTTP %d — %s",
                            projectName, open.getStatusCode(), open.getBody().asString()));
        }

        Response modulesResp = new ProjectModulesMethod(projectId).listModules();
        Assert.assertEquals(modulesResp.getStatusCode(), 200,
                String.format("List modules failed for project %s", projectName));
        List<?> modules = modulesResp.jsonPath().getList("$");
        if (modules.isEmpty()) {
            LOGGER.info("Project [{}] has no modules — skipping test run", projectName);
            return;
        }

        checkCompilation(projectName);
        runProjectTests(projectId, projectName);
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
        List<String> errors = new java.util.ArrayList<>();
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
        sb.append(String.format("ERRORS (%d):%n", errors.size()));
        for (int i = 0; i < errors.size(); i++) {
            sb.append(String.format("  %d. %s%n", i + 1, errors.get(i)));
        }
        String detail = sb.toString();
        LOGGER.error(detail);
        Assert.fail(detail);
    }

    private void runProjectTests(String projectId, String projectName) {
        Response runResponse = new ProjectTestsMethod().runAllTests(projectId);
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
