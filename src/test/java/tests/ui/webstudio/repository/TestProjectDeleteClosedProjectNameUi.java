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

    @Test
    @TestCaseId("EPBDS-16231")
    @Description("EPBDS-16231: the Confirm Delete dialog for a CLOSED project shows the plain project name, not the name with a hash appended. Fixed in the React projects UI (032c60a664ce) — now passes.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testConfirmDeleteShowsCleanNameForClosedProject() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        RepositoryPage repositoryPage = new EditorPage().getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        repositoryPage.closeProject(projectName);
        repositoryPage.waitUntilSpinnerLoaded();

        assertThat(repositoryPage.isProjectActionAvailable(projectName, "Open"))
                .as("Precondition: the project must be in Closed state before the Confirm Delete check")
                .isTrue();
        ProjectDeleteConfirmModalComponent deleteModal = repositoryPage.deleteProject(projectName);

        assertThat(deleteModal.getMessage())
                .as("Confirm Delete dialog for a closed project must not append a hash to the project name")
                .doesNotContainPattern(":[0-9a-f]{16,}");
    }
}
