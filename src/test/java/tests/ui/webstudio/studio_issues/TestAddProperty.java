package tests.ui.webstudio.studio_issues;

import configuration.annotations.AppContainerConfig;
import configuration.annotations.JiraTicket;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.CurrentUserComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

public class TestAddProperty extends BaseTest {

    @Test
    @JiraTicket("EPBDS-6964")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAddProperty() {
        EditorPage editorPage = new LoginService().login(UserService.getUser(User.ADMIN));
        editorPage.currentUserComponent.openDropdownMenuAndSelect(CurrentUserComponent.MenuElements.SIGN_OUT);
    }

}
