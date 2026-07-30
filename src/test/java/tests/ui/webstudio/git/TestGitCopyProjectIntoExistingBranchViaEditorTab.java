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

import static org.assertj.core.api.Assertions.assertThat;

public class TestGitCopyProjectIntoExistingBranchViaEditorTab extends BaseTest {

    private static final String PROJECT_NAME = "TestProject";
    private static final String SECOND_PROJECT_NAME = "TestProject2";
    private static final String BRANCH_NAME = "myBranch";
    // React surfaces the collision as an ant-notification: "Branch 'myBranch' already exists in repository."
    private static final String EXPECTED_ERROR_MESSAGE = "Branch '" + BRANCH_NAME + "' already exists in repository.";

    @Test
    @TestCaseId("EPBDS-8495")
    @Description("Git - Copy project into existing branch via Editor Tab should display error and verify branch value")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testGitCopyProjectIntoExistingBranchEditorTab() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        // Create two projects
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, PROJECT_NAME, "Sample Project");
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, SECOND_PROJECT_NAME, "Sample Project");

        // Known-failing (product regression, fix in progress): EPBDS-9535 removed the legacy repository JSF
        // and took the Editor toolbar's Copy button (added by EPBDS-6878) with it, so the Editor offers no
        // way to copy at all. This guards the button itself; red until the button is back.
        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(PROJECT_NAME);
        assertThat(editorPage.getEditorToolbarPanelComponent().isCopyProjectBtnVisible())
                .as("Copy button must be present in the Editor toolbar")
                .isTrue();

        // Copy the first project into a new branch from the Editor.
        editorPage.getEditorToolbarPanelComponent().clickCopyProjectBtn();
        CopyProjectDialogComponent copyDialog = repositoryPage.getCopyProjectDialogComponent();
        copyDialog.waitForDialogToAppear();
        copyDialog.setNewBranchName(BRANCH_NAME);
        copyDialog.clickCopyButton();

        // Re-open the dialog: the working branch must follow the copy (EPBDS-10629).
        editorPage.getEditorToolbarPanelComponent().clickCopyProjectBtn();
        copyDialog.waitForDialogToAppear();
        assertThat(copyDialog.getCurrentBranch())
                .as("Current branch should be set to the newly created branch")
                .isEqualTo(BRANCH_NAME);
        copyDialog.clickCancelButton();

        // Copying the second project into the same branch must be rejected — branches are repo-wide.
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(SECOND_PROJECT_NAME);
        editorPage.getEditorToolbarPanelComponent().clickCopyProjectBtn();
        copyDialog.waitForDialogToAppear();
        copyDialog.setNewBranchName(BRANCH_NAME);
        copyDialog.clickCopyButton(false);
        assertThat(copyDialog.waitForErrors(5000))
                .as("Error message about existing branch should be displayed")
                .anyMatch(e -> e.contains(EXPECTED_ERROR_MESSAGE));
    }
}
