package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

public class TestWithJiraTicket extends BaseTest {

    @Test
    @TestCaseId("EPBDS-12345")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testWithJiraTicket() {
        EditorPage editorPage = new LoginService().login(UserService.getUser(User.ADMIN));
        System.out.println("This test is linked to a Jira ticket.");
    }
}
