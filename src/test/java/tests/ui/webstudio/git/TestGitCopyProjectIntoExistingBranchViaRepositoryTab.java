package tests.ui.webstudio.git;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestGitCopyProjectIntoExistingBranchViaRepositoryTab extends BaseTest {

    private static final String PROJECT_NAME = "TestProject";
    private static final String SECOND_PROJECT_NAME = "TestProject2";
    private static final String BRANCH_NAME = "myBranch";
    // React surfaces the collision as an ant-notification: "Branch 'myBranch' already exists in repository."
    private static final String EXPECTED_ERROR_MESSAGE = "Branch '" + BRANCH_NAME + "' already exists in repository.";

    @Test
    @TestCaseId("EPBDS-8495")
    @Description("Git - Copy project into existing branch via Repository Tab should display error")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testGitCopyProjectIntoExistingBranchRepositoryTab() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        // Create two projects
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, PROJECT_NAME, "Sample Project");
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, SECOND_PROJECT_NAME, "Sample Project");

        // React: "copy a project into a new branch" is now the project-detail Branches tab "New branch" action
        // (mirrors the legacy CopyProjectDialogComponent.setNewBranchName flow). Create the branch on the first project.
        repositoryPage.openProjectDetail(PROJECT_NAME).createBranch(BRANCH_NAME);

        // Creating the same branch name for the second project must be rejected — branches are repo-wide.
        String error = repositoryPage.openProjectsList()
                .openProjectDetail(SECOND_PROJECT_NAME)
                .createBranchExpectingError(BRANCH_NAME);

        assertThat(error)
                .as("Error message about existing branch should be displayed")
                .contains(EXPECTED_ERROR_MESSAGE);
    }
}
