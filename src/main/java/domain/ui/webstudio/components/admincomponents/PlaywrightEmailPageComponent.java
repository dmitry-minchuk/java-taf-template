package domain.ui.webstudio.components.admincomponents;

import com.microsoft.playwright.Page;
import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

public class PlaywrightEmailPageComponent extends PlaywrightBasePageComponent {

    private final Page page;
    
    private PlaywrightWebElement emailVerificationCheckbox;
    private PlaywrightWebElement emailUrlField;
    private PlaywrightWebElement emailUsernameField;
    private PlaywrightWebElement emailPasswordField;
    private PlaywrightWebElement applyBtn;
    private PlaywrightWebElement showPasswordBtn;

    public PlaywrightEmailPageComponent() {
        super(PlaywrightDriverPool.getPage());
        this.page = PlaywrightDriverPool.getPage();
        initializeEmailComponents();
    }

    private void initializeEmailComponents() {
        emailVerificationCheckbox = new PlaywrightWebElement(page, "//input[@id='isActive']");
        emailUrlField = new PlaywrightWebElement(page, "//input[@id='url']");
        emailUsernameField = new PlaywrightWebElement(page, "//input[@id='username']");
        emailPasswordField = new PlaywrightWebElement(page, "//input[@id='password']");
        applyBtn = new PlaywrightWebElement(page, "//button[./span[text()='Apply']]");
        showPasswordBtn = new PlaywrightWebElement(page, "//span[contains(@aria-label,'eye')]");
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
        try {
            return emailUrlField.isVisible();
        } catch (Exception e) {
            return false;
        }
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