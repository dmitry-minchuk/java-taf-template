package tests.ui.webstudio.git;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
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

        assertThat(syncDialog.hasErrorMessageContaining("protected"))
                .as("Error alert should appear when merging into protected branch '%s'", MASTER_BRANCH)
                .isTrue();
        assertThat(syncDialog.getErrorMessages())
                .as("Error alert should reference protected branch name")
                .anyMatch(msg -> msg.contains(MASTER_BRANCH) && msg.contains("protected"));
        assertThat(syncDialog.isExportButtonEnabled())
                .as("Send (export) button must be disabled for protected target branch")
                .isFalse();

        syncDialog.clickCancel();
    }
}
