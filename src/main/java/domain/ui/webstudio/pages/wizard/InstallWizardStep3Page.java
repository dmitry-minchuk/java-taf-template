package domain.ui.webstudio.pages.wizard;

import configuration.core.SmartWebElement;
import domain.ui.BasePage;
import domain.ui.webstudio.pages.LoginPage;
import org.openqa.selenium.support.FindBy;

public class InstallWizardStep3Page extends BasePage {

    @FindBy(xpath = "//input[@value='multi']")
    private SmartWebElement multiUserRadioBtn;

    @FindBy(xpath = "//input[@name='step3Form:adAdminUsers']")
    private SmartWebElement adminUsersTextField;

    @FindBy(xpath = "//input[@value='Finish']")
    private SmartWebElement finishBtn;

    public InstallWizardStep3Page() {
        super("/faces/pages/modules/install/step3.xhtml");
    }

    public LoginPage setUpMultiUserMode(String userName) {
        multiUserRadioBtn.click();
        adminUsersTextField.sendKeys(userName);
        finishBtn.click();
        return new LoginPage();
    }
}
