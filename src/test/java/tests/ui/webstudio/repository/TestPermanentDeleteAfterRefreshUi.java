package tests.ui.webstudio.repository;

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
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestPermanentDeleteAfterRefreshUi extends BaseTest {

    private static final String PROJECTS_FOLDER = "Projects";
    private static final String SAMPLE_TEMPLATE = "Sample Project";
    private static final String SAMPLE_MODULE_FILE = "Main.xlsx";

    @Test
    @TestCaseId("EPBDS-15385")
    @Description("A file deleted in the Elements tab does not reappear after a full browser page refresh.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testDeletedFileDoesNotReturnAfterRefresh() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, SAMPLE_TEMPLATE);
        RepositoryPage repositoryPage = new EditorPage().getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree(PROJECTS_FOLDER)
                .selectItemInFolder(PROJECTS_FOLDER, projectName);
        repositoryPage.getRepositoryContentTabSwitcherComponent().selectElementsTab()
                .deleteElement(SAMPLE_MODULE_FILE);

        repositoryPage.reloadPage();
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree(PROJECTS_FOLDER)
                .selectItemInFolder(PROJECTS_FOLDER, projectName);
        ElementsTabComponent elementsTabAfterRefresh = repositoryPage.getRepositoryContentTabSwitcherComponent()
                .selectElementsTab();

        assertThat(elementsTabAfterRefresh.isElementPresent(SAMPLE_MODULE_FILE))
                .as("Deleted file '%s' must not reappear after a page refresh", SAMPLE_MODULE_FILE)
                .isFalse();
    }

    @Test
    @TestCaseId("EPBDS-15385")
    @Description("A project deleted through the React confirmation modal is permanently removed and does not reappear after a full browser page refresh.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testPermanentlyDeletedProjectDoesNotReturnAfterRefresh() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, SAMPLE_TEMPLATE);
        RepositoryPage repositoryPage = new EditorPage().getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        repositoryPage.deleteProject(projectName)
                .enterDeletionComment("Removed by automated regression test")
                .acknowledgePermanentDeletion()
                .clickDelete();

        repositoryPage.reloadPage();

        assertThat(repositoryPage.isProjectPresent(projectName))
                .as("Permanently deleted project '%s' must not reappear after a page refresh", projectName)
                .isFalse();
    }
}
