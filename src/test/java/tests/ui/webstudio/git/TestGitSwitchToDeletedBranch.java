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
import domain.ui.webstudio.components.repositorytabcomponents.RepositoryContentTabPropertiesComponent;
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

        repositoryPage.getLeftRepositoryTreeComponent().selectProjectInTree(projectNameForTest);
        RepositoryContentTabPropertiesComponent propertiesTab = repositoryPage.getRepositoryContentTabSwitcherComponent()
                .selectPropertiesTab();

        assertThat(propertiesTab.getSelectOprions())
                .as("Deleted branch should not be listed in available select_options")
                .doesNotContain(deletedBranchName);
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
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickCopyBtn();

        CopyProjectDialogComponent copyDialog = repositoryPage.getCopyProjectDialogComponent();
        copyDialog.waitForDialogToAppear();
        String newBranchName = copyDialog.getNewBranchName();
        copyDialog.clickCopyButton(false);
        repositoryPage.fillCommitInfo();

        repositoryPage.refresh();

        repositoryPage.getLeftRepositoryTreeComponent().selectProjectInTree(projectName);
        RepositoryContentTabPropertiesComponent propertiesTab = repositoryPage.getRepositoryContentTabSwitcherComponent()
                .selectPropertiesTab();
        propertiesTab.selectBranch("master");

        GitActionsService.deleteRemoteBranchDirect(newBranchName);

        return newBranchName;
    }
}
