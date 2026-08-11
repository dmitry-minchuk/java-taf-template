package tests.ui.webstudio.git;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
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

import static org.assertj.core.api.Assertions.assertThat;

public class TestGitCopyProjectIntoExistingBranchViaEditorTab extends BaseTest {

    private static final String PROJECT_NAME = "TestProject";
    private static final String SECOND_PROJECT_NAME = "TestProject2";
    private static final String BRANCH_NAME = "myBranch";
    private static final String EXPECTED_ERROR_MESSAGE = "Branch '" + BRANCH_NAME + "' already exists in repository.";

    @Test
    @TestCaseId("EPBDS-8495")
    @Description("Git - Copy project into existing branch via Editor Tab should display error and verify branch value. "
            + "Opening a project puts the editor tree into single-project mode, so the second project is reached "
            + "the way a user reaches it - through the breadcrumb project dropdown.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testGitCopyProjectIntoExistingBranchEditorTab() {
        LoginService loginService = new LoginService(DriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, PROJECT_NAME, "Sample Project");
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, SECOND_PROJECT_NAME, "Sample Project");

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(PROJECT_NAME);
        assertThat(editorPage.getEditorToolbarPanelComponent().isCopyProjectBtnVisible())
                .as("Copy button must be present in the Editor toolbar")
                .isTrue();

        editorPage.getEditorToolbarPanelComponent().clickCopyProjectBtn();
        CopyProjectDialogComponent copyDialog = repositoryPage.getCopyProjectDialogComponent();
        copyDialog.waitForDialogToAppear();
        copyDialog.setNewBranchName(BRANCH_NAME);
        copyDialog.clickCopyButton();

        editorPage.getEditorToolbarPanelComponent().clickCopyProjectBtn();
        copyDialog.waitForDialogToAppear();
        assertThat(copyDialog.getCurrentBranch())
                .as("Current branch should be set to the newly created branch")
                .isEqualTo(BRANCH_NAME);
        copyDialog.clickCancelButton();

        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(SECOND_PROJECT_NAME);
        editorPage.getEditorToolbarPanelComponent().clickCopyProjectBtn();
        copyDialog.waitForDialogToAppear();
        copyDialog.setNewBranchName(BRANCH_NAME);
        copyDialog.clickCopyButton(false);
        assertThat(copyDialog.waitForErrors(5000))
                .as("Error message about existing branch should be displayed")
                .anyMatch(e -> e.contains(EXPECTED_ERROR_MESSAGE));
    }
}
