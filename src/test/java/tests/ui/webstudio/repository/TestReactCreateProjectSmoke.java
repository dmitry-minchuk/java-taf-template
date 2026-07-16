package tests.ui.webstudio.repository;

import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.ProjectDetailPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

// Temporary validation of the React create-from-template gateway (build 032c60a664ce). Remove once
// the repository domain migration is validated by the real suites.
public class TestReactCreateProjectSmoke extends BaseTest {

    @Test
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void createFromTemplateWorks() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        assertThat(projectName).as("project name returned by the React create-from-template flow").isNotBlank();
    }

    @Test
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void createFromExcelWorks() {
        String projectName = WorkflowService.loginCreateProjectFromExcelFile(User.ADMIN, "TestNavigationToTable.xlsx");
        assertThat(projectName).as("project name returned by the React create-from-Excel flow").isNotBlank();
    }

    @Test
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void createFromZipWorks() {
        String projectName = WorkflowService.loginCreateProjectFromZip(User.ADMIN, "TestClickDatatypeNotFoundError.zip");
        assertThat(projectName).as("project name returned by the React create-from-Zip flow").isNotBlank();
    }

    @Test
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void copyProjectWorks() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        RepositoryPage repositoryPage = new EditorPage().getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        String copyName = projectName + "Copy";
        repositoryPage.copyProject(projectName, copyName);
        assertThat(repositoryPage.isProjectPresent(copyName))
                .as("Copied project '%s' should appear in the list", copyName).isTrue();
    }

    @Test
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void listProjectsShowsCreatedProject() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        RepositoryPage repositoryPage = new EditorPage().getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        assertThat(repositoryPage.getAllVisibleProjectsInTable())
                .as("getAllVisibleProjectsInTable should list the created project from the React rows")
                .contains(projectName);
    }

    @Test
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void historyShowsCreationRevision() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        RepositoryPage repositoryPage = new EditorPage().getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        ProjectDetailPage projectDetail = repositoryPage.openProjectDetail(projectName);
        assertThat(projectDetail.getRevisionDescriptions())
                .as("History tab should list the project-creation revision comment")
                .anyMatch(d -> d.contains("is created"));
    }
}
