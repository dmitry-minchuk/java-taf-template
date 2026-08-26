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
import domain.ui.webstudio.pages.mainpages.ProjectDetailPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.GitActionsService;
import helpers.service.GitDaemonInfrastructureService;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.WaitUtil;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestGitSwitchDeletedBranchPreset extends BaseTest {

    private static final String PROJECT_NAME = "Empty Project";
    private static final String TEMPLATE_NAME = "Sample Project";

    private static GitDaemonInfrastructureService gitDaemon;

    @BeforeClass
    public static void beforeClass() {
        gitDaemon = new GitDaemonInfrastructureService();
        gitDaemon.start();
        System.setProperty("git.url", gitDaemon.getHostUrl());
        // The local git daemon is anonymous, but JGit's UsernamePasswordCredentialsProvider
        // requires a non-null password (git.password is not defined in config.properties).
        System.setProperty("git.password", "password");
        GitActionsService.deleteAllRemoteBranchesExceptMaster();
    }

    @AfterClass(alwaysRun = true)
    public static void afterClass() {
        if (gitDaemon != null) {
            gitDaemon.stop();
        }
        System.clearProperty("git.url");
        System.clearProperty("git.password");
    }

    @Override
    protected Map<String, String> additionalContainerConfig() {
        // The in-network URL resolves via the shared Docker network's "git-daemon" alias. BaseTest
        // closes that network after each test method, so this class must keep a single @Test method
        // (the daemon is (re)started per class in @BeforeClass).
        return Map.of("repository.design.uri", gitDaemon.getInNetworkUrl());
    }

    @Test
    @TestCaseId("EPBDS-8520")
    @Description("Git - Verify branch reverts to master after login when preset to deleted branch")
    @AppContainerConfig(startParams = AppContainerStartParameters.STUDIO_GIT)
    public void testGitSwitchDeletedBranchPreset() {
        LoginService loginService = new LoginService(DriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        String projectNameForTest = getOrCreateProject(repositoryPage);
        String deletedBranchName = createBranchAndDeleteIt(repositoryPage, projectNameForTest);

        WaitUtil.sleep(11000, "Waiting for branch deletion to propagate");

        repositoryPage.openUserMenu().signOut();
        editorPage = loginService.login(UserService.getUser(User.ADMIN));
        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        ProjectDetailPage projectDetail = repositoryPage.openProjectsList().openProjectDetail(projectNameForTest);
        assertThat(projectDetail.isBranchPresent(deletedBranchName))
                .as("A deleted branch should no longer be offered by the branch switcher")
                .isFalse();
        assertThat(projectDetail.getCurrentBranch())
                .as("Branch should revert to master after login")
                .isEqualTo("master");
        assertThat(projectDetail.getStatus())
                .as("Status should be Closed or No Changes")
                .isIn("Closed", "No Changes");
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

        // Leave the project sitting on the branch that is about to be deleted.
        repositoryPage.openProjectsList().openProjectDetail(projectName).switchBranch(newBranchName);

        GitActionsService.deleteRemoteBranchDirect(newBranchName);

        return newBranchName;
    }
}
