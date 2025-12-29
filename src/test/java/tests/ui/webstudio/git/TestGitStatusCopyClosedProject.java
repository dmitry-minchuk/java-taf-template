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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestGitStatusCopyClosedProject extends BaseTest {

    private static final String PROJECT_NAME = TestGitStatusCopyClosedProject.class.getSimpleName();
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

        // Select project in tree and close it
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickCloseBtn();

        // Refresh and copy project via icon in table
        repositoryPage.refresh();
        CopyProjectDialogComponent copyDialog = repositoryPage.clickCopyProjectInTable(PROJECT_NAME);
        String copyBranch = copyDialog.getNewBranchName();
        copyDialog.clickCopyButton();

        // Verify status in Properties tab
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);
        String statusInProperties = repositoryPage.getRepositoryContentTabSwitcherComponent()
                .selectPropertiesTab()
                .getStatus();
        assertThat(statusInProperties)
                .as("Status in Properties tab should be 'No Changes'")
                .isEqualTo("No Changes");

        // Verify status and branch in table after refresh
        repositoryPage.refresh();
        Map<String, String> projectInfo = repositoryPage.getProjectInfoFromTable(PROJECT_NAME);
        assertThat(projectInfo.get("Status"))
                .as("Status in table should be 'No Changes'")
                .isEqualTo("No Changes");

        // Verification of EPBDS-8469
        assertThat(projectInfo.get("Branch"))
                .as("Branch in table should be " + copyBranch)
                .isEqualTo(copyBranch);

        // Close project again and verify branch selector
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickCloseBtn();

        String selectedBranch = repositoryPage.getRepositoryContentTabSwitcherComponent()
                .selectPropertiesTab()
                .getSelectedBranch();
        assertThat(selectedBranch)
                .as("Selected branch should be " + copyBranch)
                .isEqualTo(copyBranch);

        // Final verification after refresh
        repositoryPage.refresh();
        Map<String, String> finalProjectInfo = repositoryPage.getProjectInfoFromTable(PROJECT_NAME);
        assertThat(finalProjectInfo.get("Branch"))
                .as("Branch in table should still be " + copyBranch)
                .isEqualTo(copyBranch);
    }
}
