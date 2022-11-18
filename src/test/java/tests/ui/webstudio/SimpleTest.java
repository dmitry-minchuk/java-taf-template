package tests.ui.webstudio;

import domain.ui.webstudio.components.CurrentUserComponent;
import domain.ui.webstudio.pages.*;
import org.testng.annotations.Test;
import tests.BaseTest;

public class SimpleTest extends BaseTest {

    @Test
    public void simpleTest() {
        InstallWizardStartPage installWizardStartPage = new InstallWizardStartPage(getDriver());
        installWizardStartPage.open();
        InstallWizardStep1Page installWizardStep1Page = installWizardStartPage.clickStartBtn();
        InstallWizardStep2Page installWizardStep2Page = installWizardStep1Page.fillWorkingDirPathAndClickNext();
        InstallWizardStep3Page installWizardStep3Page = installWizardStep2Page.clickNext();
        LoginPage loginPage = installWizardStep3Page.setUpMultiUserMode("admin");
        EditorPage editorPage = loginPage.login("admin", "admin");
        editorPage.currentUserComponent.openDropdownMenuAndSelect(CurrentUserComponent.MenuElements.SIGN_OUT);
    }

    @Test
    public void simpleTest2() {
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.open();
        EditorPage editorPage = loginPage.login("admin", "admin");
        editorPage.currentUserComponent.openDropdownMenuAndSelect(CurrentUserComponent.MenuElements.SIGN_OUT);
    }
}
