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
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.utils.TestDataUtil;
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

        // 6.4.0: branching happens in the Copy dialog and leaves the project on the new branch.
        repositoryPage.openProjectDetail(PROJECT_NAME).createBranch(BRANCH_NAME, true);

        // The dialog only reports the protection when there is something to merge, so commit a change
        // on the branch first ("There are changes to merge, but the branch ... is protected").
        repositoryPage.openProjectsList().openProjectDetail(PROJECT_NAME).openFilesTab()
                .uploadFile(TestDataUtil.getFilePathFromResources("TestMergeBranchesNoConflicts_Module6.xlsx"));
        repositoryPage.openProjectsList().saveProject(PROJECT_NAME, BRANCH_NAME + ": add Module6");

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        RepositoriesPageComponent repositories = adminPage.navigateToRepositoriesPage();
        repositories.clickDesignRepositoriesTab()
                .setProtectedBranches(MASTER_BRANCH)
                .applyChangesAndRelogin(User.ADMIN);

        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        // The project is already open on the new branch (createBranch switched onto it), so go straight to the editor.
        EditorPage editor = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editor.getEditorLeftProjectModuleSelectorComponent().selectProject(PROJECT_NAME);
        assertThat(editor.getEditorToolbarPanelComponent().isSyncButtonVisible())
                .as("Sync button should be visible after branching")
                .isTrue();

        editor.getEditorToolbarPanelComponent().clickSync();
        SyncChangesDialogComponent syncDialog = repositoryPage.getSyncChangesDialogComponent();
        syncDialog.waitForDialogToAppear();
        syncDialog.selectBranch(MASTER_BRANCH);

        // 6.4.0 states the reason up front instead of failing the attempt with a 403: the dialog explains
        // that the target branch is protected and leaves both merge buttons disabled.
        assertThat(syncDialog.hasBlockedMessageContaining("protected"))
                .as("The dialog should say the target branch '%s' is protected", MASTER_BRANCH)
                .isTrue();
        assertThat(syncDialog.getBlockedMessages())
                .as("Wording of the protected-branch note")
                .anyMatch(msg -> msg.contains("is protected and you may not merge into it"));
        assertThat(syncDialog.isExportButtonEnabled())
                .as("Send (export) button must be disabled for protected target branch")
                .isFalse();

        syncDialog.clickCancel();
    }
}
