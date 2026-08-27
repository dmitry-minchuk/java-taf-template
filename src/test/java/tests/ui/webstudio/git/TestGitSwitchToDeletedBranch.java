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
import helpers.service.GitActionsService;
import helpers.service.GitContainerService;
import helpers.service.GitRemote;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.WaitUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestGitSwitchToDeletedBranch extends BaseTest {

    private static final String PROJECT_NAME = "Empty Project";
    private static final String TEMPLATE_NAME = "Sample Project";
    private static final String GIT_CONTAINER_ALIAS = "git-container-switch-to-deleted";

    private GitContainerService gitContainer;
    private GitRemote gitRemote;

    @Override
    protected void startAuxiliaryContainers() {
        gitContainer = new GitContainerService(GIT_CONTAINER_ALIAS);
        gitContainer.start();
        gitRemote = gitContainer.asRemote();
        GitActionsService.deleteAllRemoteBranchesExceptMaster(gitRemote);
    }

    @Override
    protected void stopAuxiliaryContainers() {
        if (gitContainer != null) {
            gitContainer.stop();
            gitContainer = null;
        }
        gitRemote = null;
    }

    @Override
    protected Map<String, String> additionalContainerConfig() {
        return Map.of(
                "repository.design.uri", gitContainer.getInNetworkUrl(),
                "repository.design.login", GitRemote.ANONYMOUS,
                "repository.design.password", GitRemote.ANONYMOUS
        );
    }

    @Test
    @TestCaseId("EPBDS-8505")
    @Description("Git - Verify validation error when switching to deleted branch")
    @AppContainerConfig(startParams = AppContainerStartParameters.STUDIO_GIT)
    public void testGitSwitchToDeletedBranch() {
        LoginService loginService = new LoginService(DriverPool.getPage());
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
        CopyProjectDialogComponent copyDialog = repositoryPage.openProjectsList().clickCopyAction(projectName);
        String newBranchName = copyDialog.getNewBranchName();
        copyDialog.clickCopyButton();
        repositoryPage.fillCommitInfo();
        repositoryPage.waitUntilSpinnerLoaded();

        repositoryPage.openProjectsList().openProjectDetail(projectName).switchBranch("master");

        GitActionsService.deleteRemoteBranchDirect(gitRemote, newBranchName);

        return newBranchName;
    }
}
