package tests.ui.webstudio.git;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.api.AuthenticationSettingsMethod;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.RepositoriesPageComponent;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.SyncChangesDialogComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.repositorytabcomponents.CopyProjectDialogComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestProtectedBranchMergeProtection extends BaseTest {

    private static final String PROJECT_NAME = "ProtectedBranchProject";
    private static final String BRANCH_NAME = "MyBranch";
    private static final String MASTER_BRANCH = "master";

    @Test
    @TestCaseId("EPBDS-15753")
    @Description("Git - Cannot merge into protected branch: UI shows server 409 error in merge dialog")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCannotMergeIntoProtectedBranch() {
        // EPBDS-15818 introduced `security.allow-bypass-protected-branches`. When ON, an
        // Admin (Manager-eligible) merging into a protected branch sees a "Bypass branch
        // protection?" confirm modal instead of a hard error — see EPBDS-15960 section H.1.
        // This test verifies the legacy "hard 403 + error alert" behavior, so pin the flag
        // to OFF before logging in. The PATCH triggers an application restart and
        // invalidates the browser session, so authentication happens after the toggle.
        new AuthenticationSettingsMethod().setAllowBypassProtectedBranches(false);

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, PROJECT_NAME, "TestMergeBranchesNoConflicts_NoConflicts.zip");
        repositoryPage.refresh();

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickCopyBtn();

        CopyProjectDialogComponent copyDialog = repositoryPage.getCopyProjectDialogComponent();
        copyDialog.waitForDialogToAppear()
                .setNewBranchName(BRANCH_NAME)
                .clickCopyButton();
        repositoryPage.refresh();

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        RepositoriesPageComponent repositories = adminPage.navigateToRepositoriesPage();
        repositories.clickDesignRepositoriesTab()
                .setProtectedBranches(MASTER_BRANCH)
                .applyChangesAndRelogin(User.ADMIN);

        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);
        assertThat(repositoryPage.getRepositoryContentButtonsPanelComponent().isSyncButtonVisible())
                .as("Sync button should be visible after branching")
                .isTrue();

        repositoryPage.getRepositoryContentButtonsPanelComponent().clickSync();
        SyncChangesDialogComponent syncDialog = repositoryPage.getSyncChangesDialogComponent();
        syncDialog.waitForDialogToAppear();
        syncDialog.selectBranch(MASTER_BRANCH);

        // EPBDS-15818 changed the error copy. The merge endpoint returns HTTP 403
        // (`openl.error.403.default.message`) and the React UI renders it as
        // "You do not have the required privileges to do that." (no longer mentions the branch name).
        assertThat(syncDialog.hasErrorMessageContaining("privileges"))
                .as("Error alert should appear when merging into protected branch '%s'", MASTER_BRANCH)
                .isTrue();
        assertThat(syncDialog.getErrorMessages())
                .as("Error alert should match the legacy 403 protected-branch copy")
                .anyMatch(msg -> msg.contains("You do not have the required privileges"));
        assertThat(syncDialog.isExportButtonEnabled())
                .as("Send (export) button must be disabled for protected target branch")
                .isFalse();

        syncDialog.clickCancel();
    }
}
