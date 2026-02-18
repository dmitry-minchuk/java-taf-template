package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.repositorytabcomponents.ElementsTabComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.WorkflowService;
import helpers.utils.TestDataUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestFileAddDelete extends BaseTest {

    @Test
    @TestCaseId("NTC")
    @Description("Upload and delete file in repository: via tree Delete button, and via Elements tab.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testFileAddDelete() {
        String fileName = "TestFileAddDelete.rules.xls";
        String filePath = TestDataUtil.getFilePathFromResources(fileName);

        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        RepositoryPage repositoryPage = new EditorPage().getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        // Cycle 1: upload file → select in tree → delete via Delete button
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickUploadFileBtn();
        repositoryPage.getUploadFileDialogComponent().uploadFile(filePath).clickUploadButton();

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree(projectName)
                .selectItemInFolder(projectName, fileName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickDeleteBtn();
        repositoryPage.getConfirmDeleteDialogComponent().clickDelete();

        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree(fileName))
                .as("File should not exist in tree after deletion via Delete button")
                .isFalse();

        // Cycle 2: refresh → upload again → delete via Elements tab
        repositoryPage.refresh();
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickUploadFileBtn();
        repositoryPage.getUploadFileDialogComponent().uploadFile(filePath).clickUploadButton();

        repositoryPage.getLeftRepositoryTreeComponent().selectProjectInTree(projectName);
        ElementsTabComponent elementsTab = repositoryPage.getRepositoryContentTabSwitcherComponent()
                .selectElementsTab();
        elementsTab.deleteElement(fileName);

        repositoryPage.getRepositoryContentTabSwitcherComponent().selectElementsTab();
        assertThat(elementsTab.isElementPresent(fileName))
                .as("File should not be present in Elements tab after deletion")
                .isFalse();
    }
}
