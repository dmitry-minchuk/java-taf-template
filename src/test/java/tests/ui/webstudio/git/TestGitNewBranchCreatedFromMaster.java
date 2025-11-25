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
import domain.ui.webstudio.components.repositorytabcomponents.ElementsTabComponent;
import domain.ui.webstudio.components.repositorytabcomponents.UploadFileDialogComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
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

        //Copying project to branch BRANCH_MASTER2
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);
        repositoryPage.getRepositoryContentButtonsPanelComponent()
                .clickCopyBtn();

        CopyProjectDialogComponent copyDialog = repositoryPage.getCopyProjectDialogComponent();
        copyDialog.waitForDialogToAppear();
        copyDialog.setNewBranchName(BRANCH_MASTER2)
                  .clickCopyButton();

        //Uploading file
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);
        repositoryPage.getRepositoryContentButtonsPanelComponent()
                .clickUploadFileBtn();

        UploadFileDialogComponent uploadDialog = repositoryPage.getUploadFileDialogComponent();
        uploadDialog.waitForDialogToAppear();
        uploadDialog.uploadFile(TestDataUtil.getFilePathFromResources(NEW_FILE_NAME))
                    .setFileName(NEW_FILE_NAME)
                    .clickUploadButton();

        //Saving changes
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickSaveBtn();
        repositoryPage.getSaveChangesComponent().getSaveBtn().click();
        repositoryPage.refresh();

        //Copying project to branch BRANCH_MASTER3
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);
        repositoryPage.getRepositoryContentButtonsPanelComponent()
                .clickCopyBtn();

        copyDialog = repositoryPage.getCopyProjectDialogComponent();
        copyDialog.waitForDialogToAppear();
        copyDialog.setNewBranchName(BRANCH_MASTER3)
                  .clickCopyButton();

        //Verifying files in project
        repositoryPage.getLeftRepositoryTreeComponent()
                .selectProjectInTree(PROJECT_NAME);

        ElementsTabComponent elementsTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectElementsTab();
        assertThat(elementsTab.getElementsTable().getCell(2, 2).getText().equalsIgnoreCase(NEW_FILE_NAME))
                .as("File " + NEW_FILE_NAME + " should be present in the project")
                .isTrue();

        assertThat(elementsTab.getElementsTable().getCell(1, 2).getText().equalsIgnoreCase(EXISTING_FILE_NAME))
                .as("File " + EXISTING_FILE_NAME + " should be present in the project")
                .isTrue();
    }
}
