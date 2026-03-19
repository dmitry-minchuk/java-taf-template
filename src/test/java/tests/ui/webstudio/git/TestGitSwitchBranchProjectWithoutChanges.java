package tests.ui.webstudio.git;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.repositorytabcomponents.CopyProjectDialogComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

public class TestGitSwitchBranchProjectWithoutChanges extends BaseTest {

    private static final String PROJECT_NAME = "TestGitSwitchBranchProjectWithoutChanges";
    private static final String TEMPLATE_NAME = "Sample Project";
    private static final String MASTER_BRANCH = "master";

    @Test
    @TestCaseId("EPBDS-8453, EPBDS-8424")
    @Description("Git - Verify branch switching with and without unsaved changes")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testGitSwitchBranchProjectWithoutChanges() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        // Create project from template
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, PROJECT_NAME, TEMPLATE_NAME);
        repositoryPage.refresh();

        // Copy project (auto-generated branch name)
        CopyProjectDialogComponent copyDialog = repositoryPage.clickCopyProjectInTable(PROJECT_NAME);
        String copyBranchName = copyDialog.getNewBranchName();
        copyDialog.clickCopyButton();

        // Switch to Editor tab and select project
        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(PROJECT_NAME);

        // Switch to master branch without changes
        editorPage.getEditorToolbarPanelComponent().switchBranch(MASTER_BRANCH);

        // Edit project description
        editorPage.openEditProjectDialog(PROJECT_NAME)
                .setDescription("2")
                .clickUpdateButton();

        // Try switching branch with unsaved changes — confirmation modal expected
        editorPage.getEditorToolbarPanelComponent().selectBranchInDropdown(copyBranchName);
        editorPage.clickModalOkBtn();
        editorPage.waitUntilSpinnerLoaded();

        // Switch to master branch
        editorPage.getEditorToolbarPanelComponent().switchBranch(MASTER_BRANCH);

        // Switch back to copy branch
        editorPage.getEditorToolbarPanelComponent().switchBranch(copyBranchName);
    }
}
