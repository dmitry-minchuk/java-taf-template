package helpers.service;

import configuration.driver.DriverPool;
import domain.ui.webstudio.pages.LoginPage;
import domain.ui.webstudio.pages.wizard.InstallWizardStartPage;
import domain.ui.webstudio.pages.wizard.InstallWizardStep1Page;
import domain.ui.webstudio.pages.wizard.InstallWizardStep2Page;
import domain.ui.webstudio.pages.wizard.InstallWizardStep3Page;

public class InstallWizardService {

    public LoginPage setUpWebstudio() {
        InstallWizardStep1Page installWizardStep1Page = new InstallWizardStartPage(DriverPool.getDriver()).clickStartBtn();
        InstallWizardStep2Page installWizardStep2Page = installWizardStep1Page.fillWorkingDirPathAndClickNext();
        InstallWizardStep3Page installWizardStep3Page = installWizardStep2Page.clickNext();
        return installWizardStep3Page.setUpMultiUserMode("admin");
    }
}
