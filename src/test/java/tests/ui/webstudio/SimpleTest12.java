package tests.ui.webstudio;

import domain.ui.webstudio.components.CurrentUserComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.LoginService;
import org.testng.annotations.Test;
import tests.BaseTest;

public class SimpleTest12 extends BaseTest {

    @Test
    public void simpleTest1() {
        EditorPage editorPage = new LoginService().login("admin", "admin");
        editorPage.currentUserComponent.openDropdownMenuAndSelect(CurrentUserComponent.MenuElements.SIGN_OUT);
    }

    @Test
    public void simpleTest2() {
        EditorPage editorPage = new LoginService().login("admin", "admin");
        editorPage.currentUserComponent.openDropdownMenuAndSelect(CurrentUserComponent.MenuElements.SIGN_OUT);
    }
}
