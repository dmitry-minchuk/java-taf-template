package helpers.service;

import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.pages.LoginPage;
import domain.ui.webstudio.pages.wizard.InstallWizardStartPage;
import domain.ui.webstudio.pages.wizard.InstallWizardStep1Page;
import domain.ui.webstudio.pages.wizard.InstallWizardStep2Page;
import domain.ui.webstudio.pages.wizard.InstallWizardStep3Page;

public class InstallWizardService {

    public LoginPage setUpWebstudio(UserData user) {
        InstallWizardStep1Page installWizardStep1Page = new InstallWizardStartPage().clickStartBtn();
        InstallWizardStep2Page installWizardStep2Page = installWizardStep1Page.fillWorkingDirPathAndClickNext();
        InstallWizardStep3Page installWizardStep3Page = installWizardStep2Page.clickNext();
        return installWizardStep3Page.setUpMultiUserMode(user.getLogin());
    }
}
