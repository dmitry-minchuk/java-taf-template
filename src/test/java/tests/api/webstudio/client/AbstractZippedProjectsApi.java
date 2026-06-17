package tests.api.webstudio.client;

import configuration.appcontainer.AppContainerPool;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import domain.api.AuthorizedApiMethod;
import domain.api.ProjectStatusMethod;
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
import org.testng.ITest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

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
 *   @Test per project → validate (open, compile-status, run tests + summary)
 *   @AfterClass      → stop container
 *
 * Subclasses are created dynamically via {@link TestZippedProjects} {@code @Factory},
 * one instance per discovered group on disk.
 *
 * Validation uses the 6.1.x project API: {@code GET /rest/projects/{id}/status}
 * (compile state + per-project compilation breakdown) replaces the removed
 * {@code /modules} and {@code /compile/progress} endpoints; tests run project-wide
 * via {@code POST /rest/projects/{id}/tests/run}.
 */
public abstract class AbstractZippedProjectsApi implements ITest {
    protected static final Logger LOGGER = LogManager.getLogger(AbstractZippedProjectsApi.class);
    private static final Duration CONTAINER_STARTUP_TIMEOUT = Duration.ofMinutes(10);
    private static final int TEST_SUMMARY_POLL_INTERVAL_MS = 500;
    private static final int TEST_SUMMARY_POLL_TIMEOUT_MS = 10 * 60 * 1_000;
    private static final int COMPILE_POLL_INTERVAL_MS = 500;
    // Safety net only: tests/run already awaits compilation server-side before it returns,
    // so /status reports a terminal state on the first poll.
    private static final int COMPILE_POLL_TIMEOUT_MS = 60 * 1_000;
    private static final String DESIGN_REPO = "design";

    private final List<File> zipsInGroup;
    private final String groupLabel;
    private final Map<String, Map<String, Object>> projectsByName = new LinkedHashMap<>();
    private final List<String> uploadedProjectNames = new ArrayList<>();
    private final List<String> uploadFailures = new ArrayList<>();
    private final ThreadLocal<String> currentTestName = new ThreadLocal<>();

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
                String msg = String.format("Upload failed for %s (project name [%s]): HTTP %d — %s",
                        zip.getAbsolutePath(), name, upload.getStatusCode(), upload.getBody().asString());
                LOGGER.warn(msg);
                uploadFailures.add(msg);
                continue;
            }
            if (!uploadedProjectNames.contains(name)) {
                uploadedProjectNames.add(name);
            }
        }

        if (uploadedProjectNames.isEmpty()) {
            // Don't throw here — let @Test fail with a clean message instead of
            // surfacing an IllegalStateException via setUp / @BeforeClass.
            LOGGER.warn("Group [{}]: all {} upload(s) failed; @Test will fail with details",
                    groupLabel, zipsInGroup.size());
            return;
        }

        Response listResponse = new ProjectsMethod().getAllProjects(500);
        if (listResponse.getStatusCode() != 200) {
            LOGGER.warn("Failed to list projects for group [{}]: HTTP {} — {}",
                    groupLabel, listResponse.getStatusCode(), listResponse.getBody().asString());
            uploadFailures.add(String.format("GET /rest/projects returned HTTP %d: %s",
                    listResponse.getStatusCode(), listResponse.getBody().asString()));
            return;
        }
        List<Map<String, Object>> projects = extractProjects(listResponse);
        for (Map<String, Object> project : projects) {
            projectsByName.put(String.valueOf(project.get("name")), project);
        }
        LOGGER.info("Group [{}]: uploaded {} zip(s), {} project(s) visible via API",
                groupLabel, uploadedProjectNames.size(), projectsByName.size());
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        AuthorizedApiMethod.clearSession();
        if (AppContainerPool.get() != null) {
            AppContainerPool.closeAppContainer();
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void recordTestName(Method method) {
        // One @Test row per group. The displayed name lists all projects that
        // were uploaded into this group's container, matching the legacy UI test
        // shape: testLocalZippedProjects[proj1, proj2, proj3].
        String params = uploadedProjectNames.isEmpty()
                ? "no-projects"
                : String.join(", ", uploadedProjectNames);
        currentTestName.set(method.getName() + "[" + params + "]");
    }

    @Override
    public String getTestName() {
        String n = currentTestName.get();
        return n != null ? n : "testZippedGroup";
    }

    @Test
    public void testZippedGroup() {
        if (!uploadFailures.isEmpty()) {
            // Fail fast: deployment-group projects share cross-project dependencies,
            // so if even one zip in the group didn't import, the others would just
            // fail compilation with cascading "X is not found" errors that aren't
            // useful signal. Surface the upload errors and stop.
            StringBuilder sb = new StringBuilder(String.format(
                    "Group [%s] — %d/%d zip(s) failed to upload; skipping validation:",
                    groupLabel, uploadFailures.size(), zipsInGroup.size()));
            appendZipPaths(sb);
            sb.append("\n\nUpload errors:");
            for (String f : uploadFailures) {
                sb.append("\n  ").append(f.replace("\n", "\n  "));
            }
            Assert.fail(sb.toString());
        }

        List<String> validationFailures = new ArrayList<>();
        for (String projectName : uploadedProjectNames) {
            Map<String, Object> project = projectsByName.get(projectName);
            if (project == null) {
                validationFailures.add(String.format(
                        "Project [%s] was uploaded but not visible in GET /rest/projects", projectName));
                continue;
            }
            try {
                validateProject(project);
            } catch (AssertionError e) {
                validationFailures.add(e.getMessage());
            }
        }
        if (validationFailures.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder(String.format(
                "Group [%s] — %d/%d uploaded project(s) failed validation",
                groupLabel, validationFailures.size(), uploadedProjectNames.size()));
        appendZipPaths(sb);
        sb.append("\n\nValidation failures:");
        for (String f : validationFailures) {
            sb.append("\n\n").append(f);
        }
        Assert.fail(sb.toString());
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

        // tests/run opens a module, awaits compilation server-side, then runs all tests — so it
        // is also the compile trigger (a plain open leaves compileState 'idle' forever). 404 here
        // means the project has no module to open/compile.
        Response runResponse = new ProjectTestsMethod().runAllTests(projectId);
        int runStatus = runResponse.getStatusCode();
        if (runStatus == 404) {
            LOGGER.info("Project [{}] has no modules to compile/run — skipping", projectName);
            return;
        }
        if (runStatus != 200 && runStatus != 202) {
            Assert.fail(String.format("Failed to compile/run tests for project [%s]: HTTP %d — %s%s",
                    projectName, runStatus, runResponse.getBody().asString(), serverBugNote(runStatus)));
        }

        // Compilation is finished by the time tests/run returns; /status now reports the real state
        // (replaces the removed /modules + /compile/progress endpoints).
        Response statusResp = awaitCompilation(projectId);
        Assert.assertEquals(statusResp.getStatusCode(), 200,
                String.format("Project status failed for %s in group %s: HTTP %d — %s%s",
                        projectName, groupLabel, statusResp.getStatusCode(), statusResp.getBody().asString(),
                        serverBugNote(statusResp.getStatusCode())));
        JsonPath status = statusResp.jsonPath();
        String compileState = status.getString("compileState");
        int compileErrors = intOrZero(status.get("compilation.messages.errors"));
        if ("errors".equalsIgnoreCase(compileState) || compileErrors > 0) {
            Assert.fail(buildCompileErrorReport(projectName, status));
        }

        Response summary = pollTestsSummary(projectId, projectName);
        if (summary == null) {
            Assert.fail(String.format("Tests summary timed out for project [%s]", projectName));
        }
        int code = summary.getStatusCode();
        if (code == 404) {
            LOGGER.info("Project [{}] has no Test tables — compile validated, nothing to run", projectName);
            return;
        }
        if (code != 200) {
            Assert.fail(String.format("Tests summary failed for project [%s]: HTTP %d — %s%s",
                    projectName, code, summary.getBody().asString(), serverBugNote(code)));
        }

        JsonPath summaryJson = summary.jsonPath();
        int total = intOrZero(summaryJson.get("numberOfTests"));
        int failures = intOrZero(summaryJson.get("numberOfFailures"));
        LOGGER.info("Project [{}] tests: total={}, failures={}", projectName, total, failures);
        if (failures > 0) {
            StringBuilder sb = new StringBuilder(String.format(
                    "Project [%s]: %d test failure(s) of %d total", projectName, failures, total));
            appendTestCases(sb, summaryJson);
            Assert.fail(sb.toString());
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
        List<String> errors = new ArrayList<>();
        if (items != null) {
            for (Map<String, Object> msg : items) {
                if ("ERROR".equalsIgnoreCase(stringOrNull(msg.get("severity")))) {
                    String summary = stringOrNull(msg.get("summary"));
                    errors.add(summary != null ? summary : "(empty)");
                }
            }
        }
        StringBuilder sb = new StringBuilder(String.format("Compilation errors detected in project: %s", projectName));
        sb.append(String.format("%nERRORS (%d):", errors.size()));
        for (int i = 0; i < errors.size(); i++) {
            sb.append(String.format("%n  %d. %s", i + 1, errors.get(i)));
        }
        return sb.toString();
    }

    private void appendZipPaths(StringBuilder sb) {
        sb.append("\nProjects location:");
        for (File zip : zipsInGroup) {
            sb.append("\n  ").append(zip.getAbsolutePath());
        }
    }

    @SuppressWarnings("unchecked")
    private void appendTestCases(StringBuilder sb, JsonPath summaryJson) {
        List<Map<String, Object>> testCases = summaryJson.getList("testCases");
        if (testCases == null || testCases.isEmpty()) return;
        for (Map<String, Object> testCase : testCases) {
            Integer caseFailures = toInt(testCase.get("numberOfFailures"));
            if (caseFailures == null || caseFailures == 0) continue;
            String caseName = String.valueOf(testCase.get("name"));
            Integer caseTotal = toInt(testCase.get("numberOfTests"));
            sb.append(String.format("%n  TestCase [%s] — %d/%d failed",
                    caseName, caseFailures, caseTotal == null ? 0 : caseTotal));
            List<Map<String, Object>> testUnits = (List<Map<String, Object>>) testCase.get("testUnits");
            if (testUnits == null) continue;
            for (Map<String, Object> unit : testUnits) {
                String status = stringOrNull(unit.get("status"));
                if (status == null || status.equalsIgnoreCase("TR_OK")) continue;
                appendFailedUnit(sb, unit);
            }
        }
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

    /**
     * Appends a clarifying note when WebStudio returns a 5xx. Typical causes
     * are server-side issues (e.g. an exception inside the tests/summary
     * mapper) or a project that depends on classes/datatypes not available
     * in this WebStudio build (NoClassDefFoundError on a custom calculation
     * step). Either way it's not a test-framework issue — the test surfaces
     * the response body verbatim so investigators can route it correctly.
     */
    private static String serverBugNote(int statusCode) {
        if (statusCode >= 500 && statusCode < 600) {
            return "\n[NOTE] HTTP " + statusCode + " comes from the OpenL server — either a server-side issue "
                    + "or this project is incompatible with the current WebStudio build (missing classes, "
                    + "unexpected internal state). Not a test-framework problem.";
        }
        return "";
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

    private int intOrZero(Object o) {
        Integer v = toInt(o);
        return v == null ? 0 : v;
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
            LOGGER.warn("Unexpected test summary status {} for project [{}]: {}{}",
                    code, projectName, last.getBody().asString(), serverBugNote(code));
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
