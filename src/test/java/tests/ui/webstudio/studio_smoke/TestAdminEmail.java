package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.TabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

public class TestAdminEmail extends BaseTest {

    @Test
    @TestCaseId("IPBQA-32798")
    @Description("New Admin UI 'Email' page")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAdminEmail() {
        EditorPage editorPage = new LoginService().login(UserService.getUser(User.ADMIN));
        AdminPage adminPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.ADMIN);
        System.out.println();
    }
}
