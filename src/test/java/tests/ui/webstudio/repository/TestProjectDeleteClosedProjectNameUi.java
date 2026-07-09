package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.repositorytabcomponents.ProjectDeleteConfirmModalComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestProjectDeleteClosedProjectNameUi extends BaseTest {

    private static final String PROJECTS_FOLDER = "Projects";

    @Test
    @TestCaseId("EPBDS-16231")
    @Description("Known-failing regression for EPBDS-16231: the Confirm Delete dialog for a CLOSED project must show the plain project name, not the name with a hash appended. Stays red until EPBDS-16231 is fixed.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testConfirmDeleteShowsCleanNameForClosedProject() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        RepositoryPage repositoryPage = new EditorPage().getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree(PROJECTS_FOLDER)
                .selectItemInFolder(PROJECTS_FOLDER, projectName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickCloseBtn();
        repositoryPage.waitUntilSpinnerLoaded();

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree(PROJECTS_FOLDER)
                .selectItemInFolder(PROJECTS_FOLDER, projectName);
        assertThat(repositoryPage.getRepositoryContentButtonsPanelComponent().isOpenBtnVisible())
                .as("Precondition: the project must be in Closed state before the Confirm Delete check")
                .isTrue();
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickDeleteBtn();
        ProjectDeleteConfirmModalComponent deleteModal =
                repositoryPage.getProjectDeleteConfirmModalComponent().waitForVisible();

        assertThat(deleteModal.getMessage())
                .as("Confirm Delete dialog for a closed project must not append a hash to the project name")
                .doesNotContainPattern(":[0-9a-f]{16,}");
    }
}
