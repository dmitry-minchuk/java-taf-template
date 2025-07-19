package domain.ui.webstudio.components.admincomponents;

import configuration.core.ui.BasePageComponent;
import configuration.core.ui.SmartWebElement;
import org.openqa.selenium.support.FindBy;

public class EmailPageComponent extends BasePageComponent {

    @FindBy(xpath = ".//input[@type='checkbox']")
    private SmartWebElement emailVerificationCheckbox;

    @FindBy(xpath = ".//div[./div/label[@title='URL']]//div/input")
    private SmartWebElement emailUrlField;

    @FindBy(xpath = ".//div[./div/label[@title='Username']]//div/input")
    private SmartWebElement emailUsernameField;

    @FindBy(xpath = ".//input[@id='password']")
    private SmartWebElement emailPasswordField;

    @FindBy(xpath = ".//button[./span[text()='Apply']]")
    private SmartWebElement applyBtn;


    @FindBy(xpath = ".//span[contains(@aria-label,'eye')]")
    private SmartWebElement showPasswordBtn;

    public void enableEmailVerification() {
        if (!isEmailVerificationEnabled()) {
            emailVerificationCheckbox.click();
        }
    }

    public void disableEmailVerification() {
        if (isEmailVerificationEnabled()) {
            emailVerificationCheckbox.click();
        }
    }

    public boolean isEmailVerificationEnabled() {
        return emailUrlField.isDisplayed(1);
    }

    public void setEmailUrl(String url) {
        emailUrlField.sendKeys(url);
    }

    public void setEmailUsername(String username) {
        emailUsernameField.sendKeys(username);
    }

    public void setEmailPassword(String password) {
        emailPasswordField.sendKeys(password);
    }

    public String getEmailUrl() {
        return emailUrlField.getAttribute("value");
    }

    public String getEmailUsername() {
        return emailUsernameField.getAttribute("value");
    }

    public String getEmailPassword() {
        return emailPasswordField.getAttribute("value");
    }

    public void setEmailCredentials(String url, String username, String password) {
        setEmailUrl(url);
        setEmailUsername(username);
        setEmailPassword(password);
        applyBtn.click();
        getConfirmationPopup().confirm();
    }

    public void enableEmailVerificationWithCredentials(String url, String username, String password) {
        enableEmailVerification();
        setEmailCredentials(url, username, password);
    }

    public void togglePasswordVisibility() {
        showPasswordBtn.click();
    }
}