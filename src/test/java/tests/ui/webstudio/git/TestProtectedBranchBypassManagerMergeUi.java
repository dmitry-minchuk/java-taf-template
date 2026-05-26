package tests.ui.webstudio.git;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.api.AclProjectsMethod;
import domain.api.ProjectBranchesMethod;
import domain.api.ProjectResourcesMethod;
import domain.api.ProjectsMethod;
import domain.api.RepositoryProjectsMethod;
import domain.api.UsersMethod;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.components.common.BypassConfirmDialogComponent;
import domain.ui.webstudio.components.common.SyncChangesDialogComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.utils.TestDataUtil;
import io.restassured.response.Response;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EPBDS-15960 sections G/H — UI bypass merge flow as a Manager with
 * {@code security.allow-bypass-protected-branches=ON}: in-dialog warning,
 * secondary confirmation modal, "Merge Successful" toast. Setup is REST.
 */
public class TestProtectedBranchBypassManagerMergeUi extends BaseTest {

    private static final String DESIGN_REPO = "design";
    private static final String PROTECTED_TARGET = "release-EPBDS-15818";
    private static final String DEV_BRANCH = "EPBDS-15818_dev";
    private static final String MANAGER_LOGIN = "manager_15960";
    private static final String MANAGER_PASSWORD = "manager_15960";
    private static final String PROJECT_NAME = "BypassMergeUiProject";
    private static final String PROJECT_ZIP = "RulesEditor.TestSearchOnProjectLevel.Example1BankRating.zip";
    private static final String DIVERGENT_FILE = "rules.xml";
    private static final String MERGE_SUCCESS_TOAST = "Merge Successful";

    @AfterMethod(alwaysRun = true)
    public void deleteManagerUser() {
        try {
            new UsersMethod().deleteUser(MANAGER_LOGIN);
        } catch (Exception ignored) {
        }
    }

    @Test
    @TestCaseId("EPBDS-15960")
    @Description("Sections G/H: Manager merging into a protected branch with bypass=ON sees "
            + "the in-dialog warning, the secondary confirmation modal, and a Merge Successful toast.")
    @AppContainerConfig(startParams = AppContainerStartParameters.STUDIO_BYPASS_ENABLED_PARAMS)
    public void testManagerBypassMergeFlowUi() {
        // ============ REST setup ============
        configureAdminCommitIdentity();
        String projectId = uploadProject();
        provisionBranches(projectId);
        commitDivergentChangeOnDev(projectId);
        provisionManager(projectId);

        UserData manager = new UserData(MANAGER_LOGIN, MANAGER_PASSWORD);
        // Branch + open state are per-user — switch Manager's workspace to dev so the Sync
        // dialog opens on the divergent branch instead of master.
        assertThat(new ProjectBranchesMethod().switchBranch(projectId, DEV_BRANCH, manager).getStatusCode())
                .as("Manager switches workspace to dev branch").isLessThan(300);

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(manager);

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.refresh();

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);

        repositoryPage.getRepositoryContentButtonsPanelComponent().openProjectAndWait();
        SyncChangesDialogComponent syncDialog = repositoryPage.getSyncChangesDialogComponent();
        helpers.utils.WaitUtil.waitForCondition(() -> {
            repositoryPage.getRepositoryContentButtonsPanelComponent().clickSync();
            return syncDialog.isVisible();
        }, 15_000, 1_000, "Click Sync until the merge dialog appears");
        syncDialog.selectBranch(PROTECTED_TARGET);

        assertThat(syncDialog.isBypassWarningVisible())
                .as("G.1 — Sync dialog must show the bypass warning for a protected target")
                .isTrue();
        assertThat(syncDialog.getBypassWarningText())
                .as("G.2 — bypass warning must name the protected branch")
                .contains("Bypass branch protection?")
                .contains(PROTECTED_TARGET);

        syncDialog.clickSendYourUpdates();
        BypassConfirmDialogComponent confirmDialog = repositoryPage.getBypassConfirmDialogComponent()
                .waitForDialogToAppear();
        assertThat(confirmDialog.getTitle())
                .as("H.1 — confirmation modal title")
                .isEqualTo("Bypass branch protection?");

        confirmDialog.clickConfirmBypassAndMerge();
        assertThat(confirmDialog.isMergeSuccessNoticeVisible())
                .as("H.2 — '%s' toast after confirming bypass", MERGE_SUCCESS_TOAST)
                .isTrue();
    }

    // ============================== private setup helpers ==============================

    // JGit needs a non-null name/email on admin to attribute commits.
    private void configureAdminCommitIdentity() {
        new UsersMethod().setCurrentUserInfo("Test", "Automation", "test-automation@openl.local", "Test Automation");
    }

    private String uploadProject() {
        File zip = new File(TestDataUtil.getFilePathFromResources(PROJECT_ZIP));
        Response upload = new RepositoryProjectsMethod().uploadProject(DESIGN_REPO, PROJECT_NAME, zip);
        assertThat(upload.getStatusCode()).as("upload project %s", PROJECT_NAME).isLessThan(300);
        return resolveProjectId(PROJECT_NAME);
    }

    private void provisionBranches(String projectId) {
        ProjectBranchesMethod branches = new ProjectBranchesMethod();
        assertThat(branches.createBranch(projectId, PROTECTED_TARGET).getStatusCode())
                .as("create protected target branch %s", PROTECTED_TARGET).isLessThan(300);
        assertThat(branches.createBranch(projectId, DEV_BRANCH).getStatusCode())
                .as("create dev branch %s", DEV_BRANCH).isLessThan(300);
        assertThat(branches.switchBranch(projectId, DEV_BRANCH).getStatusCode())
                .as("switch project to dev branch").isLessThan(300);
    }

    private void commitDivergentChangeOnDev(String projectId) {
        ProjectResourcesMethod resources = new ProjectResourcesMethod();
        Response get = resources.getResource(projectId, DIVERGENT_FILE);
        assertThat(get.getStatusCode()).as("download %s on dev", DIVERGENT_FILE).isEqualTo(200);
        try {
            Path tmp = Files.createTempFile("bypass-merge-divergence-", ".xml");
            Files.write(tmp, get.asByteArray());
            Files.write(tmp, "\n<!-- EPBDS-15960 divergence marker -->\n".getBytes(),
                    StandardOpenOption.APPEND);
            assertThat(resources.updateResource(projectId, DIVERGENT_FILE, tmp.toFile()).getStatusCode())
                    .as("PUT modified %s commits the divergent change on dev", DIVERGENT_FILE)
                    .isLessThan(300);
        } catch (Exception e) {
            throw new RuntimeException("failed to commit divergent change on dev", e);
        }
        // PUT leaves an admin lock and OPENED state; reopen+close releases both.
        ProjectsMethod projects = new ProjectsMethod();
        assertThat(projects.openProject(projectId).getStatusCode())
                .as("release exclusive lock after PUT").isLessThan(300);
        assertThat(projects.closeProject(projectId).getStatusCode())
                .as("close admin's workspace claim").isLessThan(300);
    }

    private void provisionManager(String projectId) {
        UsersMethod users = new UsersMethod();
        assertThat(users.createUser(MANAGER_LOGIN, MANAGER_PASSWORD).getStatusCode())
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
