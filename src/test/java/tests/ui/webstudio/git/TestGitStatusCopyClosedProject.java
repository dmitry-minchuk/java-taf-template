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

public class TestGitStatusCopyClosedProject extends BaseTest {

    private static final String PROJECT_NAME = "TestGitStatusCopyClosedProject";
    private static final String TEMPLATE_NAME = "Sample Project";

    @Test
    @TestCaseId("IPBQA-27562")
    @Description("Git - Verify project status after copying a closed project")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testGitStatusCopyClosedProject() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        // Create project from template
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, PROJECT_NAME, TEMPLATE_NAME);

        // Close the project, then copy it — the copy dialog opens in branch mode and suggests a branch name.
        repositoryPage.closeProject(PROJECT_NAME);

        CopyProjectDialogComponent copyDialog = repositoryPage.clickCopyAction(PROJECT_NAME);
        String copyBranch = copyDialog.getNewBranchName();
        copyDialog.clickCopyButton();
        repositoryPage.fillCommitInfo();
        repositoryPage.waitUntilSpinnerLoaded();

        // Copying a closed project leaves it closed in the workspace (the old UI opened it as "No Changes").
        assertThat(repositoryPage.openProjectsList().getProjectStatusFromDetail(PROJECT_NAME))
                .as("A copied closed project stays closed")
                .isEqualTo("Closed");

        // EPBDS-8469: the row shows the branch the copy was made on.
        assertThat(repositoryPage.openProjectsList().getProjectBranchFromTable(PROJECT_NAME))
                .as("The row should show branch " + copyBranch)
                .isEqualTo(copyBranch);

        // It is still closed, and still on that branch.
        assertThat(repositoryPage.openProjectsList().openProjectDetail(PROJECT_NAME).getCurrentBranch())
                .as("The project should still sit on branch " + copyBranch)
                .isEqualTo(copyBranch);
    }
}
