package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestWhichIsGoingToFail extends BaseTest {

    @Test
    @TestCaseId("EPBDS-34242")
    @Description("Just to test how failures look like")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testWhichIsGoingToFail() {
        EditorPage editorPage = new LoginService().login(UserService.getUser(User.ADMIN));
        assertThat(4).isGreaterThan(5).as("Some description here");
    }
}
