package domain.ui.webstudio.pages.wizard;

import domain.ui.BasePage;
import domain.ui.webstudio.pages.LoginPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class InstallWizardStep3Page extends BasePage {

    @FindBy(xpath = "//input[@value='multi']")
    private WebElement multiUserRadioBtn;

    @FindBy(xpath = "//input[@name='step3Form:adAdminUsers']")
    private WebElement adminUsersTextField;

    @FindBy(xpath = "//input[@value='Finish']")
    private WebElement finishBtn;

    public InstallWizardStep3Page(WebDriver driver) {
        super(driver, "/faces/pages/modules/install/step3.xhtml");
    }

    public LoginPage setUpMultiUserMode(String userName) {
        multiUserRadioBtn.click();
        adminUsersTextField.sendKeys(userName);
        finishBtn.click();
        return new LoginPage(driver);
    }
}
