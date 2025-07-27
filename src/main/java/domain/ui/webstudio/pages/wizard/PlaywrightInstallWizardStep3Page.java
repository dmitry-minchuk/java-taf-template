package domain.ui.webstudio.pages.wizard;

import configuration.core.ui.PlaywrightBasePage;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import domain.ui.webstudio.pages.PlaywrightLoginPage;

public class PlaywrightInstallWizardStep3Page extends PlaywrightBasePage {

    private PlaywrightWebElement multiUserRadioBtn;
    private PlaywrightWebElement adminUsersTextField;
    private PlaywrightWebElement finishBtn;

    public PlaywrightInstallWizardStep3Page() {
        super("/faces/pages/modules/install/step3.xhtml");
        initializeElements();
    }

    private void initializeElements() {
        multiUserRadioBtn = new PlaywrightWebElement(page, "//input[@value='multi']", "Multi User Radio Button");
        adminUsersTextField = new PlaywrightWebElement(page, "//input[@name='step3Form:adAdminUsers']", "Admin Users Text Field");
        finishBtn = new PlaywrightWebElement(page, "//input[@value='Finish']", "Finish Button");
    }

    public PlaywrightLoginPage setUpMultiUserMode(String userName) {
        multiUserRadioBtn.click();
        adminUsersTextField.fill(userName);
        finishBtn.click();
        return new PlaywrightLoginPage();
    }
}