package domain.ui.webstudio.components.admincomponents;

import com.microsoft.playwright.Page;
import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

public class PlaywrightEmailPageComponent extends PlaywrightBasePageComponent {
    
    private PlaywrightWebElement emailVerificationCheckbox;
    private PlaywrightWebElement emailUrlField;
    private PlaywrightWebElement emailUsernameField;
    private PlaywrightWebElement emailPasswordField;
    private PlaywrightWebElement applyBtn;
    private PlaywrightWebElement showPasswordBtn;

    public PlaywrightEmailPageComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeEmailComponents();
    }
    
    public PlaywrightEmailPageComponent(PlaywrightWebElement rootLocator) {
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
        return emailPasswordField.getAttribute("value");
    }

    public void setEmailCredentials(String url, String username, String password) {
        setEmailUrl(url);
        setEmailUsername(username);
        setEmailPassword(password);

        // Handle browser confirmation dialogs using Playwright's native dialog handling
        page.onDialog(dialog -> {
            if (dialog.type().equals("confirm")) {
                dialog.accept();
            }
        });
        
        applyBtn.click();
    }

    public void enableEmailVerificationWithCredentials(String url, String username, String password) {
        enableEmailVerification();
        setEmailCredentials(url, username, password);
    }

    public void togglePasswordVisibility() {
        showPasswordBtn.click();
    }
}