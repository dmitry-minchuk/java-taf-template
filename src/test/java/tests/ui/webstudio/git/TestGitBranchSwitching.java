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
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestGitBranchSwitching extends BaseTest {

    private static final String PROJECT_NAME = "TestGitBranchSwitching";
    private static final String TEMPLATE_NAME = "Sample Project";
    private static final String COPY_BRANCH_NAME = "my_branch";
    private static final String MASTER_BRANCH_NAME = "master";

    @Test
    @TestCaseId("IPBQA-27450")
    @Description("Git - Verify branch switching between master and custom branch")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testGitBranchSwitching() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        // Create project from template
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, PROJECT_NAME, TEMPLATE_NAME);
        repositoryPage.refresh();

        // Copy project to custom branch
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickCopyBtn();

        CopyProjectDialogComponent copyDialog = repositoryPage.getCopyProjectDialogComponent();
        copyDialog.waitForDialogToAppear();
        copyDialog.setNewBranchName(COPY_BRANCH_NAME);
        copyDialog.clickCopyButton();

        // Verify current branch is the custom branch in Properties Tab
        repositoryPage.getLeftRepositoryTreeComponent().selectProjectInTree(PROJECT_NAME);
        RepositoryContentTabPropertiesComponent propertiesTab = repositoryPage.getRepositoryContentTabSwitcherComponent()
                .selectPropertiesTab();
        assertThat(propertiesTab.getSelectedBranch())
                .as("Current branch should be " + COPY_BRANCH_NAME)
                .isEqualTo(COPY_BRANCH_NAME);

        // Switch to Editor tab and select project
        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(PROJECT_NAME);

        // Verify breadcrumb shows the custom branch
        assertThat(editorPage.getEditorToolbarPanelComponent().getCurrentBranch().trim())
                .as("Breadcrumb should show " + COPY_BRANCH_NAME)
                .isEqualTo(COPY_BRANCH_NAME);

        // Switch to master branch via breadcrumb
        editorPage.getEditorToolbarPanelComponent().switchBranch(MASTER_BRANCH_NAME);

        // Return to Repository tab
        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        // Verify current branch is master in Properties Tab
        repositoryPage.getLeftRepositoryTreeComponent().selectProjectInTree(PROJECT_NAME);
        propertiesTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        assertThat(propertiesTab.getSelectedBranch())
                .as("Current branch should be " + MASTER_BRANCH_NAME)
                .isEqualTo(MASTER_BRANCH_NAME);
    }
}
