package domain.ui.webstudio.components.admincomponents;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.WaitUtil;

import java.util.Objects;

public class EmailPageComponent extends BaseComponent {
    
    private WebElement emailVerificationCheckbox;
    private WebElement emailUrlField;
    private WebElement emailUsernameField;
    private WebElement emailPasswordField;
    private WebElement applyBtn;
    private WebElement showPasswordBtn;

    public EmailPageComponent() {
        super(LocalDriverPool.getPage());
        initializeEmailComponents();
    }
    
    public EmailPageComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeEmailComponents();
    }

    private void initializeEmailComponents() {
        // EXACT SAME locators as legacy EmailPageComponent
        emailVerificationCheckbox = createScopedElement("xpath=.//input[@type='checkbox']", "Email Verification Checkbox");
        emailUrlField = createScopedElement("xpath=.//div[./div/label[@title='URL']]//div/input", "Email URL Field");
        emailUsernameField = createScopedElement("xpath=.//div[./div/label[@title='Username']]//div/input", "Email Username Field");
        emailPasswordField = createScopedElement("xpath=.//input[@id='password']", "Email Password Field");
        applyBtn = createScopedElement("xpath=.//button[./span[text()='Apply']]", "Apply Button");
        showPasswordBtn = createScopedElement("xpath=.//span[contains(@aria-label,'eye')]", "Show Password Button");
    }

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
        return emailUrlField.isVisible();
    }

    public void setEmailUrl(String url) {
        emailUrlField.clear();
        emailUrlField.fill(url);
    }

    public void setEmailUsername(String username) {
        emailUsernameField.clear();
        emailUsernameField.fill(username);
    }

    public void setEmailPassword(String password) {
        emailPasswordField.clear();
        emailPasswordField.fill(password);
    }

    public String getEmailUrl() {
        return emailUrlField.getAttribute("value");
    }

    public String getEmailUsername() {
        return emailUsernameField.getAttribute("value");
    }

    public String getEmailPassword() {
        return Objects.requireNonNullElse(emailPasswordField.getAttribute("value"), "");
    }

    public void setEmailCredentials(String url, String username, String password) {
        setEmailUrl(url);
        setEmailUsername(username);
        setEmailPassword(password);
        applyBtn.click();
        getModalOkBtn().click();
        WaitUtil.sleep(2000, "Waiting for page to stabilize after modal close");
        WaitUtil.waitForCondition(() -> getAllMessages().contains("Email server configuration saved"), 10000, 100, "Waiting for success message");
    }

    public void enableEmailVerificationWithCredentials(String url, String username, String password) {
        enableEmailVerification();
        setEmailCredentials(url, username, password);
    }

    public void togglePasswordVisibility() {
        showPasswordBtn.click();
    }

    public String getPasswordFieldType() {
        return emailPasswordField.getAttribute("type");
    }

    public boolean isPasswordVisible() {
        String type = getPasswordFieldType();
        return type != null && !type.equals("password");
    }
}