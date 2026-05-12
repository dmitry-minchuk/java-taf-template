package tests.ui.webstudio.client;

import configuration.appcontainer.AppContainerPool;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import domain.api.AuthorizedApiMethod;
import domain.api.ProjectModulesMethod;
import domain.api.ProjectTestsMethod;
import domain.api.ProjectsMethod;
import helpers.utils.StringUtil;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestGenesisProjectsApi {
    private static final Logger LOGGER = LogManager.getLogger(TestGenesisProjectsApi.class);
    private static final Duration CONTAINER_STARTUP_TIMEOUT = Duration.ofMinutes(60);
    private static final int TEST_SUMMARY_POLL_INTERVAL_MS = 2_000;
    private static final int TEST_SUMMARY_POLL_TIMEOUT_MS = 10 * 60 * 1_000;

    static {
        publishLaunchDescription();
    }

    private SoftAssert softAssert;

    public enum GenesisGroup {
        GROUP_1_RATING_CLAIM(AppContainerStartParameters.GENESIS_GROUP_1_PARAMS, "rating + claim",
                List.of("openl-rating", "openl-claim")),
        GROUP_2_POLICY_BUNDLE(AppContainerStartParameters.GENESIS_GROUP_2_PARAMS, "policy bundle",
                List.of("openl-policy", "openl-policy-life", "openl-financials"));

        final AppContainerStartParameters startParams;
        final String label;
        final List<String> repos;

        GenesisGroup(AppContainerStartParameters startParams, String label, List<String> repos) {
            this.startParams = startParams;
            this.label = label;
            this.repos = repos;
        }
    }

    private static void publishLaunchDescription() {
        if (System.getProperty("rp.description") != null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("## Genesis Projects — API Validation").append("\n\n");
        sb.append("End-to-end check that every project across the selected design repositories ")
                .append("opens, lists modules and passes its test tables. Driven entirely via WebStudio REST ")
                .append("(no UI).").append("\n\n");
        sb.append("**Started:** ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sb.append("**Container image:** `").append(safeProp(PropertyNameSpace.DOCKER_IMAGE_NAME)).append("`").append("\n");
        sb.append("**Branch:** `").append(safeProp(PropertyNameSpace.GITLAB_BRANCH)).append("`").append("\n");
        sb.append("**User:** `").append(safeProp(PropertyNameSpace.GITLAB_USER)).append("`").append("\n\n");
        sb.append("### Repository groups").append("\n\n");
        for (GenesisGroup group : GenesisGroup.values()) {
            sb.append("**").append(group.label).append("**").append("\n");
            for (String repo : group.repos) {
                sb.append("- ").append(repo).append("\n");
            }
            sb.append("\n");
        }
        sb.append("### Per project").append("\n");
        sb.append("- `PATCH /rest/projects/{id}` → status=OPENED").append("\n");
        sb.append("- `GET /rest/projects/{id}/modules`").append("\n");
        sb.append("- `POST /rest/projects/{id}/tests/run` + polled `GET …/tests/summary` (JSESSIONID kept across calls)").append("\n");
        System.setProperty("rp.description", sb.toString());
    }

    private static String safeProp(PropertyNameSpace prop) {
        try {
            String v = ProjectConfiguration.getProperty(prop);
            return v == null ? "(unset)" : v;
        } catch (RuntimeException e) {
            return "(unset)";
        }
    }

    @DataProvider(name = "GenesisGroups")
    public Object[][] genesisGroups() {
        return new Object[][]{
                {GenesisGroup.GROUP_1_RATING_CLAIM},
                {GenesisGroup.GROUP_2_POLICY_BUNDLE}
        };
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(Method method) {
        AuthorizedApiMethod.clearSession();
        if (AppContainerPool.get() != null) {
            AppContainerPool.closeAppContainer();
        }
    }

    @Test(dataProvider = "GenesisGroups")
    public void testGenesisGroup(GenesisGroup group) {
        softAssert = new SoftAssert();
        startContainer(group);
        AuthorizedApiMethod.startSession();

        LOGGER.info("Triggering lazy git clone for group [{}] — this may take up to an hour...", group.label);
        Response listResponse = new ProjectsMethod().getAllProjects(500);
        softAssert.assertEquals(listResponse.getStatusCode(), 200,
                String.format("List projects failed for group %s", group.label));

        if (listResponse.getStatusCode() != 200) {
            softAssert.assertAll();
            return;
        }

        List<Map<String, Object>> projects = extractProjects(listResponse);
        LOGGER.info("Found {} projects in group [{}]", projects.size(), group.label);

        for (Map<String, Object> project : projects) {
            validateProject(project);
        }

        softAssert.assertAll();
    }

    private void startContainer(GenesisGroup group) {
        String containerName = StringUtil.generateUniqueName("genesis_" + group.name().toLowerCase());
        Map<String, String> envVars = group.startParams.getParameterMap();
        envVars.forEach((k, v) -> LOGGER.info("[{}] -> [{}]", k, v));
        String dockerImage = ProjectConfiguration.getProperty(PropertyNameSpace.DOCKER_IMAGE_NAME);

        LOGGER.info("Starting WebStudio container for group [{}]. First boot clones design repos and may take up to {} minutes.",
                group.label, CONTAINER_STARTUP_TIMEOUT.toMinutes());
        AtomicBoolean done = new AtomicBoolean(false);
        long started = System.currentTimeMillis();
        Thread heartbeat = new Thread(() -> {
            try {
                while (!done.get()) {
                    Thread.sleep(30_000);
                    if (done.get()) return;
                    long elapsedSec = (System.currentTimeMillis() - started) / 1000;
                    LOGGER.info("...still cloning/booting [{}] — elapsed {}m {}s",
                            group.label, elapsedSec / 60, elapsedSec % 60);
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }, "genesis-warmup-heartbeat");
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
            softAssert.assertTrue(open.getStatusCode() < 300,
                    String.format("Failed to open project %s: HTTP %d — %s",
                            projectName, open.getStatusCode(), open.getBody().asString()));
        }

        Response modulesResp = new ProjectModulesMethod(projectId).listModules();
        softAssert.assertEquals(modulesResp.getStatusCode(), 200,
                String.format("List modules failed for project %s", projectName));
        if (modulesResp.getStatusCode() != 200) {
            return;
        }
        List<?> modules = modulesResp.jsonPath().getList("$");
        if (modules.isEmpty()) {
            LOGGER.info("Project [{}] has no modules — skipping compile/test checks", projectName);
            return;
        }

        runProjectTests(projectId, projectName);
    }

    private void runProjectTests(String projectId, String projectName) {
        Response runResponse = new ProjectTestsMethod().runAllTests(projectId);
        int runStatus = runResponse.getStatusCode();
        if (runStatus == 404 || runStatus == 204) {
            LOGGER.info("Project [{}] has no Test tables — skipping test execution", projectName);
            return;
        }
        softAssert.assertTrue(runStatus == 200 || runStatus == 202,
                String.format("Failed to start tests for project %s: HTTP %d — %s",
                        projectName, runStatus, runResponse.getBody().asString()));
        if (runStatus != 200 && runStatus != 202) {
            return;
        }

        Response summary = pollTestsSummary(projectId, projectName);
        if (summary == null) {
            softAssert.fail(String.format("Test summary timed out for project [%s]", projectName));
            return;
        }
        if (summary.getStatusCode() == 404) {
            LOGGER.info("Project [{}] reports no test execution task — likely no Test tables", projectName);
            return;
        }
        if (summary.getStatusCode() != 200) {
            softAssert.fail(String.format("Tests summary returned HTTP %d for project [%s]: %s",
                    summary.getStatusCode(), projectName, summary.getBody().asString()));
            return;
        }
        Integer failures = summary.jsonPath().getInt("numberOfFailures");
        Integer total = summary.jsonPath().getInt("numberOfTests");
        LOGGER.info("Project [{}] tests: total={}, failures={}", projectName, total, failures);
        if (failures != null && failures > 0) {
            String detail = buildFailureReport(projectName, total, failures, summary.jsonPath());
            LOGGER.error(detail);
            softAssert.fail(detail);
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
                String status = String.valueOf(unit.get("status"));
                if (status == null || !status.toUpperCase().contains("FAIL") && !status.equalsIgnoreCase("ERROR")) {
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
            sb.append("%n      input: ".formatted());
            sb.append(params.stream().map(this::formatParamValue).collect(java.util.stream.Collectors.joining(", ")));
        }
        List<Map<String, Object>> assertions = (List<Map<String, Object>>) unit.get("testAssertions");
        if (assertions != null) {
            for (Map<String, Object> assertion : assertions) {
                String aStatus = stringOrNull(assertion.get("status"));
                if (aStatus != null && !aStatus.toUpperCase().contains("FAIL") && !aStatus.equalsIgnoreCase("ERROR")) {
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
            last = client.getTestsSummary(projectId, false, 100);
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
            LOGGER.warn("Unexpected test summary status {} for project [{}], stopping poll",
                    code, projectName);
            return last;
        }
        return last;
    }

    private void sleepInterruptible() {
        try {
            Thread.sleep(TEST_SUMMARY_POLL_INTERVAL_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
