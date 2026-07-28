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
import helpers.service.GitActionsService;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.WaitUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestGitSwitchToDeletedBranch extends BaseTest {

    private static final String PROJECT_NAME = "Empty Project";
    private static final String TEMPLATE_NAME = "Sample Project";
    private static final String EXPECTED_ERROR_MESSAGE = "branchSelector: Validation Error: Value is not valid";

    @BeforeClass
    public static void beforeClass() {
        GitActionsService.deleteAllRemoteBranchesExceptMaster();
    }

    @Test
    @TestCaseId("EPBDS-8505")
    @Description("Git - Verify validation error when switching to deleted branch")
    @AppContainerConfig(startParams = AppContainerStartParameters.STUDIO_GIT)
    public void testGitSwitchToDeletedBranch() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        String projectNameForTest = getOrCreateProject(repositoryPage);
        String deletedBranchName = createBranchAndDeleteIt(repositoryPage, projectNameForTest);

        WaitUtil.sleep(11000, "Waiting for branch deletion to propagate");

        assertThat(repositoryPage.openProjectsList().openProjectDetail(projectNameForTest)
                        .isBranchPresent(deletedBranchName))
                .as("A deleted branch should no longer be offered by the branch switcher")
                .isFalse();
    }

    private String getOrCreateProject(RepositoryPage repositoryPage) {
        java.util.List<String> visibleProjects = repositoryPage.getAllVisibleProjectsInTable();

        if (!visibleProjects.isEmpty()) {
            String projectName = visibleProjects.getFirst();
            LOGGER.info("Using existing project: {}", projectName);
            return projectName;
        }

        LOGGER.info("Creating new project: {}", PROJECT_NAME);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, PROJECT_NAME, TEMPLATE_NAME);
        return PROJECT_NAME;
    }

    private String createBranchAndDeleteIt(RepositoryPage repositoryPage, String projectName) {
        // Branching happens in the Copy dialog, which suggests the new branch name.
        CopyProjectDialogComponent copyDialog = repositoryPage.openProjectsList().clickCopyAction(projectName);
        String newBranchName = copyDialog.getNewBranchName();
        copyDialog.clickCopyButton();
        repositoryPage.fillCommitInfo();
        repositoryPage.waitUntilSpinnerLoaded();

        // Move the project back onto master before the branch is removed underneath it.
        repositoryPage.openProjectsList().openProjectDetail(projectName).switchBranch("master");

        GitActionsService.deleteRemoteBranchDirect(newBranchName);

        return newBranchName;
    }
}
