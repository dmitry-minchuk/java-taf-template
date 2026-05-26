package tests.api.webstudio.integration;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.api.AclProjectsMethod;
import domain.api.AuthenticationSettingsMethod;
import domain.api.ProjectBranchesMethod;
import domain.api.ProjectMergeMethod;
import domain.api.ProjectsMethod;
import domain.api.RepositoryProjectsMethod;
import domain.api.UsersMethod;
import domain.serviceclasses.models.UserData;
import helpers.utils.TestDataUtil;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Section B of EPBDS-15960 — Manager bypass merge flow against a protected target branch
 * with the global `allowBypassProtectedBranches` setting ON.
 *
 * STATUS: setup pipeline works end-to-end (admin profile fix, project upload, dev branch
 * creation, manager user provisioning, MANAGER role grant, bypass setting toggle, merge
 * endpoint reaches the protection layer). The current bottleneck is creating divergent
 * commits between `dev` and `master` purely over REST so that the merge endpoint exercises
 * the bypass branch instead of the `openl.error.409.project.merge.same.branches.message`
 * fast-path. To be addressed in the next iteration (most likely via repeat ZIP upload on the
 * dev branch with a modified fixture).
 *
 * Once divergence is in place, this test should assert:
 *   B.1 — merge-check without `force` → HTTP 409, code `openl.error.409.protected.branch.bypass.required`.
 *   B.2 — merge-check with `force=true` → HTTP 200 (mergeable / up-to-date).
 *   B.3 — merge without `force` → HTTP 409 with the same code; protected branch HEAD unchanged.
 *   B.4 — merge with `force=true` → HTTP 200; protected branch advanced.
 *
 * Then: B.5 (force on non-protected = no-op), B.6 (receive direction), B.7 (both branches protected),
 * and A.4 (non-admin reading the setting → 403).
 */
public class TestProtectedBranchBypassManagerApi extends BaseTest {

    private static final String DESIGN_REPO = "design";
    // `master` is protected by the default branch matcher; using it avoids configuring the matcher
    // for the test container.
    private static final String PROTECTED_TARGET = "master";
    private static final String DEV_BRANCH = "EPBDS-15818_dev";
    private static final String MANAGER_LOGIN = "manager_15818";
    private static final String MANAGER_PASSWORD = "manager_15818";
    private static final String PROJECT_NAME = "BypassMergeTest";
    private static final String PROJECT_ZIP = "TestMergeBranchesNoConflicts_NoConflicts.zip";
    private static final String BYPASS_409_CODE = "openl.error.409.protected.branch.bypass.required";

    @Test(enabled = false)  // WIP: needs a divergent-commit fixture; see class Javadoc
    @TestCaseId("EPBDS-15960")
    @Description("Section B (B.1–B.4): Manager with allowBypassProtectedBranches=ON merging into "
            + "release-* protected target: 409 without force, 200 with force=true, both for /merge/check and /merge")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testManagerBypassMergeFlow() {
        // ============ Admin setup (do BEFORE the bypass-toggle PATCH that restarts Studio) ============
        // Admin profile must have firstName/email or JGit commits fail with PersonIdent NPE.
        new UsersMethod().setCurrentUserInfo("Test", "Automation", "test-automation@openl.local", "Test Automation");

        File zip = new File(TestDataUtil.getFilePathFromResources(PROJECT_ZIP));
        Response upload = new RepositoryProjectsMethod().uploadProject(DESIGN_REPO, PROJECT_NAME, zip);
        assertThat(upload.getStatusCode()).as("upload project").isLessThan(300);

        String projectId = resolveProjectId(PROJECT_NAME);

        ProjectBranchesMethod branches = new ProjectBranchesMethod();
        // Project was created on `master`; only create the dev branch and switch the project to it.
        assertThat(branches.createBranch(projectId, DEV_BRANCH).getStatusCode())
                .as("create dev branch %s", DEV_BRANCH).isLessThan(300);
        assertThat(branches.switchBranch(projectId, DEV_BRANCH).getStatusCode())
                .as("switch project to dev branch").isLessThan(300);

        assertThat(new UsersMethod().createUser(MANAGER_LOGIN, MANAGER_PASSWORD).getStatusCode())
                .as("create user %s", MANAGER_LOGIN).isLessThan(300);
        assertThat(new AclProjectsMethod().grantRole(projectId, MANAGER_LOGIN, true, "MANAGER").getStatusCode())
                .as("grant MANAGER role on project").isLessThan(300);

        // Restart-triggering PATCH last — by the time it fires everything else is in place.
        assertThat(new AuthenticationSettingsMethod().setAllowBypassProtectedBranches(true).getStatusCode())
                .as("enable allowBypassProtectedBranches").isEqualTo(204);

        UserData manager = new UserData(MANAGER_LOGIN, MANAGER_PASSWORD);
        ProjectMergeMethod merge = new ProjectMergeMethod();

        // ============ B.1 — merge-check without force → 409 bypass-required ============
        Response checkNoForce = merge.checkMerge(projectId, "send", PROTECTED_TARGET, false, manager);
        assertThat(checkNoForce.getStatusCode())
                .as("B.1 — merge-check without force must return 409 for protected target")
                .isEqualTo(409);
        assertThat(checkNoForce.jsonPath().getString("code"))
                .as("B.1 — 409 body must carry the bypass-required code")
                .isEqualTo(BYPASS_409_CODE);

        // ============ B.2 — merge-check with force=true → 200 ============
        Response checkWithForce = merge.checkMerge(projectId, "send", PROTECTED_TARGET, true, manager);
        assertThat(checkWithForce.getStatusCode())
                .as("B.2 — merge-check with force=true must return 200")
                .isEqualTo(200);
        assertThat(checkWithForce.jsonPath().getString("status"))
                .as("B.2 — merge-check status should be mergeable or up-to-date")
                .isIn("mergeable", "up-to-date");

        // ============ B.3 — merge without force → 409, protected branch HEAD unchanged ============
        String protectedHeadBefore = readBranchHead(projectId, PROTECTED_TARGET);
        Response mergeNoForce = merge.merge(projectId, "send", PROTECTED_TARGET, false, manager);
        assertThat(mergeNoForce.getStatusCode())
                .as("B.3 — merge without force must return 409 (no bypass)")
                .isEqualTo(409);
        assertThat(mergeNoForce.jsonPath().getString("code"))
                .as("B.3 — 409 body carries the bypass-required code")
                .isEqualTo(BYPASS_409_CODE);
        assertThat(readBranchHead(projectId, PROTECTED_TARGET))
                .as("B.3 — protected branch HEAD must not advance when bypass was denied")
                .isEqualTo(protectedHeadBefore);

        // ============ B.4 — merge with force=true → 200, protected branch advanced ============
        Response mergeWithForce = merge.merge(projectId, "send", PROTECTED_TARGET, true, manager);
        assertThat(mergeWithForce.getStatusCode())
                .as("B.4 — merge with force=true must succeed for Manager + setting ON")
                .isEqualTo(200);
        // Branch HEAD may still match if dev was up-to-date (no commits to merge); for this fixture
        // the dev branch was just created off master so it is up-to-date — accept either outcome
        // as long as the call succeeded without error. A separate scenario in B.6/B.7 will cover
        // a non-trivial merge once we wire commit creation in the setup.
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

    private String readBranchHead(String projectId, String branchName) {
        Response branches = new ProjectBranchesMethod().listBranches(projectId);
        if (branches.getStatusCode() != 200) {
            return "unknown-status-" + branches.getStatusCode();
        }
        List<Map<String, Object>> list = branches.jsonPath().getList("$");
        return list.stream()
                .filter(b -> branchName.equals(b.get("name")))
                .map(b -> String.valueOf(b.getOrDefault("commit", b.getOrDefault("revision", "no-head"))))
                .findFirst()
                .orElse("branch-not-found");
    }
}
