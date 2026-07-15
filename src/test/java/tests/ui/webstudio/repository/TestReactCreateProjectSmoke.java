package tests.ui.webstudio.repository;

import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
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
}
