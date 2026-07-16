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
import domain.ui.webstudio.pages.mainpages.ProjectDetailPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.TestDataUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestGitNewBranchCreatedFromMaster extends BaseTest {

    private static final String PROJECT_NAME = "Empty Project";
    private static final String TEMPLATE_NAME = "Sample Project";
    private static final String NEW_FILE_NAME = "rules.xlsx";
    private static final String EXISTING_FILE_NAME = "Main.xlsx";
    private static final String BRANCH_MASTER2 = "master2";
    private static final String BRANCH_MASTER3 = "master3";

    @Test
    @TestCaseId("EPBDS-8421")
    @Description("Git - Verify new branch created from master contains all files")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testGitNewBranchCreatedFromMaster() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, PROJECT_NAME, TEMPLATE_NAME);

        // React has no copy-into-branch: create master2 off master and switch to it
        ProjectDetailPage projectDetail = repositoryPage.openProjectDetail(PROJECT_NAME);
        projectDetail.createBranch(BRANCH_MASTER2, true);

        // Upload a new module on master2 and commit it
        projectDetail.uploadFile(TestDataUtil.getFilePathFromResources(NEW_FILE_NAME));
        repositoryPage.openProjectsList();
        repositoryPage.saveProject(PROJECT_NAME, "Add " + NEW_FILE_NAME);

        // Create master3 off master2 (which now has the uploaded module) and switch to it
        projectDetail = repositoryPage.openProjectDetail(PROJECT_NAME);
        projectDetail.createBranch(BRANCH_MASTER3, true);

        // The new branch must contain both the uploaded and the pre-existing module
        projectDetail.openFilesTab();
        assertThat(projectDetail.isFilePresent(NEW_FILE_NAME))
                .as("File %s should be present in the new branch", NEW_FILE_NAME)
                .isTrue();
        assertThat(projectDetail.isFilePresent(EXISTING_FILE_NAME))
                .as("File %s should be present in the new branch", EXISTING_FILE_NAME)
                .isTrue();
    }
}
