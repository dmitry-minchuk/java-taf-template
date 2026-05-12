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
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestGenesisProjectsApi {
    private static final Logger LOGGER = LogManager.getLogger(TestGenesisProjectsApi.class);
    private static final Duration CONTAINER_STARTUP_TIMEOUT = Duration.ofMinutes(60);
    private static final int TEST_SUMMARY_POLL_INTERVAL_MS = 2_000;
    private static final int TEST_SUMMARY_POLL_TIMEOUT_MS = 10 * 60 * 1_000;

    private SoftAssert softAssert;

    public enum GenesisGroup {
        GROUP_1_RATING_CLAIM(AppContainerStartParameters.GENESIS_GROUP_1_PARAMS, "rating,claim"),
        GROUP_2_POLICY_BUNDLE(AppContainerStartParameters.GENESIS_GROUP_2_PARAMS, "policy,policy_life,financials");

        final AppContainerStartParameters startParams;
        final String label;

        GenesisGroup(AppContainerStartParameters startParams, String label) {
            this.startParams = startParams;
            this.label = label;
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
        softAssert.assertEquals(failures, Integer.valueOf(0),
                String.format("Project [%s] has %d test failures (of %d total)", projectName, failures, total));
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
