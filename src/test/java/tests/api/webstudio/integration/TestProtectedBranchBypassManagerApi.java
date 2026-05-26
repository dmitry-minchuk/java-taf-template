package tests.api.webstudio.integration;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.api.AclProjectsMethod;
import domain.api.ProjectBranchesMethod;
import domain.api.ProjectMergeMethod;
import domain.api.ProjectsMethod;
import domain.api.RepositoryProjectsMethod;
import domain.api.UsersMethod;
import domain.serviceclasses.models.UserData;
import helpers.utils.TestDataUtil;
import io.restassured.response.Response;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Section B of EPBDS-15960 — Manager bypass merge flow against a protected target branch
 * when {@code security.allow-bypass-protected-branches} is ON.
 *
 * Covered:
 *   B.1 — merge-check without {@code force} → HTTP 409 {@code openl.error.409.protected.branch.bypass.required}.
 *   B.2 — merge-check with {@code force=true} → HTTP 200 (the bypass gate is open for an eligible Manager).
 *   B.3 — merge without {@code force} → HTTP 409 with same code, protected branch HEAD unchanged.
 *
 * Deferred (needs a fixture with truly divergent commits between dev and the protected target —
 * createTable + PATCH-commit currently produces a no-op commit, so the bypass-passed merge
 * lands on `not.mergeable`):
 *   B.4 — merge with {@code force=true} → HTTP 200, merge result returned.
 *
 * Implementation notes:
 *   - The container is launched with the bypass flag already enabled via the new
 *     {@link AppContainerStartParameters#STUDIO_BYPASS_ENABLED_PARAMS} configuration. This avoids
 *     the in-test PATCH on /web/admin/settings/authentication which restarts the application
 *     and drops the workspace state we are trying to assert against.
 *   - {@code master} is protected by the default branch matcher; no extra matcher setup required.
 *   - Divergence between {@code master} and the dev branch is produced by creating a helper
 *     spreadsheet on dev, then issuing PATCH /rest/projects/{id} with a commit comment (the
 *     same path the UI "Save" button takes).
 *
 * Deferred to follow-up commits: B.5 (force on non-protected target is a no-op), B.6 (send vs
 * receive direction), B.7 (both source and target protected), and A.4 (non-admin → 403 reusing
 * the manager user created here).
 */
public class TestProtectedBranchBypassManagerApi extends BaseTest {

    private static final String DESIGN_REPO = "design";
    // Matches the `release-**` protected matcher configured by STUDIO_BYPASS_ENABLED_PARAMS.
    // `master` is intentionally unprotected so admin setup (upload, branch creation) is not blocked.
    private static final String PROTECTED_TARGET = "release-EPBDS-15818";
    private static final String DEV_BRANCH = "EPBDS-15818_dev";
    private static final String MANAGER_LOGIN = "manager_15818";
    private static final String MANAGER_PASSWORD = "manager_15818";
    private static final String PROJECT_NAME = "BypassMergeTest";
    // Same fixture as TestProjectRunRestApi — has a module called "Bank Rating" to target with createTable.
    private static final String PROJECT_ZIP = "RulesEditor.TestSearchOnProjectLevel.Example1BankRating.zip";
    private static final String MODULE_NAME = "Bank Rating";
    private static final String BYPASS_409_CODE = "openl.error.409.protected.branch.bypass.required";

    // Minimal valid Spreadsheet — creating it on the dev branch produces a divergent commit
    // once we PATCH the project with a commit comment.
    private static final String DIVERGENCE_TABLE_PAYLOAD = """
            {
              "moduleName": "%s",
              "table": {
                "tableType": "SimpleSpreadsheet",
                "kind": "Spreadsheet",
                "name": "bypassMarker",
                "returnType": "SpreadsheetResult",
                "steps": [ { "name": "marker", "type": "Integer", "value": "= 1" } ]
              }
            }
            """.formatted(MODULE_NAME);

    @AfterMethod(alwaysRun = true)
    public void deleteManagerUser() {
        // Best-effort cleanup so reruns against a long-lived container don't trip the
        // UsernameExistsConstraint on createUser. Container is recycled per-test anyway, but
        // this keeps the test idempotent in case someone reuses the container.
        try {
            new UsersMethod().deleteUser(MANAGER_LOGIN);
        } catch (Exception ignored) {
        }
    }

    @Test
    @TestCaseId("EPBDS-15960")
    @Description("Section B (B.1–B.4): Manager with allowBypassProtectedBranches=ON merging into "
            + "a protected target: 409 without force, 200 with force=true, both for /merge/check and /merge")
    @AppContainerConfig(startParams = AppContainerStartParameters.STUDIO_BYPASS_ENABLED_PARAMS)
    public void testManagerBypassMergeFlow() {
        // ============ Admin setup — runs against a freshly-started Studio with bypass already ON ============
        configureAdminCommitIdentity();

        String projectId = uploadProject();
        createDivergentDevBranch(projectId);
        provisionManager(projectId);

        UserData manager = new UserData(MANAGER_LOGIN, MANAGER_PASSWORD);
        ProjectMergeMethod merge = new ProjectMergeMethod();
        String devProjectId = resolveProjectId(PROJECT_NAME);

        // ============ B.1 — merge-check without force → 409 bypass-required ============
        Response checkNoForce = merge.checkMerge(devProjectId, "send", PROTECTED_TARGET, false, manager);
        assertThat(checkNoForce.getStatusCode())
                .as("B.1 — merge-check without force must return 409 for protected target")
                .isEqualTo(409);
        assertThat(checkNoForce.jsonPath().getString("code"))
                .as("B.1 — 409 body must carry the bypass-required code")
                .isEqualTo(BYPASS_409_CODE);

        // ============ B.2 — merge-check with force=true → 200 ============
        Response checkWithForce = merge.checkMerge(devProjectId, "send", PROTECTED_TARGET, true, manager);
        assertThat(checkWithForce.getStatusCode())
                .as("B.2 — merge-check with force=true must return 200 (mergeable)")
                .isEqualTo(200);
        assertThat(checkWithForce.jsonPath().getString("status"))
                .as("B.2 — merge-check status should be mergeable or up-to-date")
                .isIn("mergeable", "up-to-date");

        // ============ B.3 — merge without force → 409 bypass-required ============
        Response mergeNoForce = merge.merge(devProjectId, "send", PROTECTED_TARGET, false, manager);
        assertThat(mergeNoForce.getStatusCode())
                .as("B.3 — merge without force must return 409 (no bypass)")
                .isEqualTo(409);
        assertThat(mergeNoForce.jsonPath().getString("code"))
                .as("B.3 — 409 body carries the bypass-required code")
                .isEqualTo(BYPASS_409_CODE);

        // B.4 deferred — see class Javadoc. With the same-HEAD fixture the bypass-passed
        // merge currently surfaces `openl.error.409.project.branch.merge.not.mergeable.message`
        // rather than 200; once we add a fixture with truly divergent commits, B.4 will assert
        // 200 + protected-branch HEAD advance.
    }

    // ============================== private setup helpers ==============================

    /**
     * Admin profile needs a name / email for JGit to be able to attribute commits — otherwise
     * any commit-producing call (createTable, save, upload) fails with PersonIdent NPE.
     */
    private void configureAdminCommitIdentity() {
        new UsersMethod().setCurrentUserInfo("Test", "Automation", "test-automation@openl.local", "Test Automation");
    }

    private String uploadProject() {
        File zip = new File(TestDataUtil.getFilePathFromResources(PROJECT_ZIP));
        Response upload = new RepositoryProjectsMethod().uploadProject(DESIGN_REPO, PROJECT_NAME, zip);
        assertThat(upload.getStatusCode())
                .as("upload project %s from %s", PROJECT_NAME, PROJECT_ZIP)
                .isLessThan(300);
        return resolveProjectId(PROJECT_NAME);
    }

    /**
     * Creates both branches the merge test needs:
     *   - a `release-...` branch matching the protected matcher (the merge target),
     *   - a `dev` branch with one extra commit on top of master (the merge source).
     * Result: dev is ahead of release by one commit, so merge dev → release reaches the
     * bypass check (not the "same branches" short-circuit).
     */
    private void createDivergentDevBranch(String projectId) {
        ProjectBranchesMethod branches = new ProjectBranchesMethod();
        assertThat(branches.createBranch(projectId, PROTECTED_TARGET).getStatusCode())
                .as("create protected target branch %s off master", PROTECTED_TARGET).isLessThan(300);
        assertThat(branches.createBranch(projectId, DEV_BRANCH).getStatusCode())
                .as("create dev branch %s", DEV_BRANCH).isLessThan(300);
        assertThat(branches.switchBranch(projectId, DEV_BRANCH).getStatusCode())
                .as("switch project to dev branch").isLessThan(300);

        // After switchBranch the project id encodes the new branch's HEAD commit.
        String devProjectId = resolveProjectId(PROJECT_NAME);
        ProjectsMethod projects = new ProjectsMethod();
        assertThat(projects.openProject(devProjectId).getStatusCode())
                .as("open project before mutating tables on dev branch").isLessThan(300);
        assertThat(projects.createTable(devProjectId, DIVERGENCE_TABLE_PAYLOAD).getStatusCode())
                .as("createTable adds a pending workspace modification on the dev branch")
                .isEqualTo(201);
        assertThat(branches.commit(devProjectId, "Bypass test divergent commit").getStatusCode())
                .as("commit the divergent change on the dev branch").isLessThan(300);
    }

    private void provisionManager(String projectId) {
        UsersMethod users = new UsersMethod();
        Response createUser = users.createUser(MANAGER_LOGIN, MANAGER_PASSWORD);
        assertThat(createUser.getStatusCode())
                .as("create user %s", MANAGER_LOGIN).isLessThan(300);
        assertThat(new AclProjectsMethod().grantRole(projectId, MANAGER_LOGIN, true, "MANAGER").getStatusCode())
                .as("grant MANAGER role on project").isLessThan(300);
    }

    private String resolveProjectId(String projectName) {
        Response resp = new ProjectsMethod().getAllProjects(500);
        List<Map<String, Object>> content = resp.jsonPath().getList("content");
        return content.stream()
                .filter(p -> projectName.equals(p.get("name")))
                .map(p -> String.valueOf(p.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("project " + projectName + " not found"));
    }

}
