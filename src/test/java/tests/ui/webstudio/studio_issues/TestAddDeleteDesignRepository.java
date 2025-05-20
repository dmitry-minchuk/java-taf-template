package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static domain.ui.webstudio.components.CreateNewProjectComponent.TabName.TEMPLATE;

public class TestAddDeleteDesignRepository extends BaseTest {

    @Test
    @TestCaseId("IPBQA-30682")
    @Description("Multiple Design Repositories: flat folder git repository, JDBC; Repository filter and sorting")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAddDeleteDesignRepository() {
        String projectName = WorkflowService.loginCreateProjectOpenEditor(User.ADMIN, TEMPLATE, "Sample Project");
        //Finish after new Admin UI is ready
    }
}
