package tests.ui.webstudio;

import domain.servicemodels.User;
import domain.ui.webstudio.components.CurrentUserComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

public class SimpleTest12 extends BaseTest {

    @Test
    public void simpleTest1() {
        EditorPage editorPage = new LoginService().login(UserService.getUser(User.ADMIN));
        editorPage.currentUserComponent.openDropdownMenuAndSelect(CurrentUserComponent.MenuElements.SIGN_OUT);
    }

    @Test
    public void simpleTest2() {
        EditorPage editorPage = new LoginService().login(UserService.getUser(User.ADMIN));
        editorPage.currentUserComponent.openDropdownMenuAndSelect(CurrentUserComponent.MenuElements.SIGN_OUT);
    }
}
