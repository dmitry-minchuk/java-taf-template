package tests.ui.webstudio;

import domain.ui.webstudio.components.CurrentUserComponent;
import domain.ui.webstudio.pages.LoginPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.wizard.InstallWizardStartPage;
import domain.ui.webstudio.pages.wizard.InstallWizardStep1Page;
import domain.ui.webstudio.pages.wizard.InstallWizardStep2Page;
import domain.ui.webstudio.pages.wizard.InstallWizardStep3Page;
import helpers.service.LoginService;
import org.testng.annotations.Test;
import tests.BaseTest;

public class SimpleTest34 extends BaseTest {

    @Test
    public void simpleTest2() {
        EditorPage editorPage = new LoginService().login("admin", "admin");
        editorPage.currentUserComponent.openDropdownMenuAndSelect(CurrentUserComponent.MenuElements.SIGN_OUT);
    }

    @Test
    public void simpleTest4() {
        EditorPage editorPage = new LoginService().login("admin", "admin");
        editorPage.currentUserComponent.openDropdownMenuAndSelect(CurrentUserComponent.MenuElements.SIGN_OUT);
    }
}
