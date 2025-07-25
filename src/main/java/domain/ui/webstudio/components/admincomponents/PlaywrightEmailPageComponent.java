package domain.ui.webstudio.components.admincomponents;

import com.microsoft.playwright.Page;
import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import org.openqa.selenium.support.FindBy;

/**
 * Playwright version of EmailPageComponent for Admin UI Email verification configuration
 * Migrated from Selenium-based EmailPageComponent to use native Playwright wait strategies
 */
public class PlaywrightEmailPageComponent extends PlaywrightBasePageComponent {

    private final Page page;

    public PlaywrightEmailPageComponent() {
        // Get the Page from PlaywrightDriverPool to access Playwright features
        this.page = PlaywrightDriverPool.getPage();
    }

    // Using @FindBy annotations with specific IDs from actual HTML structure
    @FindBy(xpath = "//input[@id='isActive']")
    private PlaywrightWebElement emailVerificationCheckbox;

    @FindBy(xpath = "//input[@id='url']")
    private PlaywrightWebElement emailUrlField;

    @FindBy(xpath = "//input[@id='username']")
    private PlaywrightWebElement emailUsernameField;

    @FindBy(xpath = "//input[@id='password']")
    private PlaywrightWebElement emailPasswordField;

    @FindBy(xpath = "//button[./span[text()='Apply']]")
    private PlaywrightWebElement applyBtn;

    @FindBy(xpath = "//span[contains(@aria-label,'eye')]")
    private PlaywrightWebElement showPasswordBtn;

    /**
     * Enable email verification by clicking the checkbox if not already enabled
     */
    public void enableEmailVerification() {
        if (!isEmailVerificationEnabled()) {
            emailVerificationCheckbox.click();
        }
    }

    /**
     * Disable email verification by clicking the checkbox if currently enabled
     */
    public void disableEmailVerification() {
        if (isEmailVerificationEnabled()) {
            emailVerificationCheckbox.click();
        }
    }

    /**
     * Check if email verification is enabled by checking if URL field is visible
     * Uses Playwright's visibility checking with timeout
     * @return true if email verification is enabled (URL field is visible)
     */
    public boolean isEmailVerificationEnabled() {
        try {
            // Use Playwright's native visibility check with 2 second timeout (like Selenium version)
            return emailUrlField.isVisible(2000);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Set the email URL in the URL field
     * Uses Playwright's clear() and fill() methods
     * @param url SMTP server URL
     */
    public void setEmailUrl(String url) {
        emailUrlField.clear();
        emailUrlField.fill(url);
    }

    /**
     * Set the email username in the username field
     * Uses Playwright's clear() and fill() methods
     * @param username Email username
     */
    public void setEmailUsername(String username) {
        emailUsernameField.clear();
        emailUsernameField.fill(username);
    }

    /**
     * Set the email password in the password field
     * Uses Playwright's clear() and fill() methods
     * @param password Email password
     */
    public void setEmailPassword(String password) {
        emailPasswordField.clear();
        emailPasswordField.fill(password);
    }

    /**
     * Get the current value of the email URL field
     * Uses Playwright's inputValue() method
     * @return Current URL field value
     */
    public String getEmailUrl() {
        return emailUrlField.inputValue();
    }

    /**
     * Get the current value of the email username field
     * Uses Playwright's inputValue() method
     * @return Current username field value
     */
    public String getEmailUsername() {
        return emailUsernameField.inputValue();
    }

    /**
     * Get the current value of the email password field
     * Uses Playwright's inputValue() method
     * @return Current password field value (usually masked)
     */
    public String getEmailPassword() {
        return emailPasswordField.inputValue();
    }

    /**
     * Set email credentials and apply the configuration
     * Uses Playwright's dialog handling with onDialog
     * @param url SMTP server URL
     * @param username Email username  
     * @param password Email password
     */
    public void setEmailCredentials(String url, String username, String password) {
        setEmailUrl(url);
        setEmailUsername(username);
        setEmailPassword(password);
        
        // Set up Playwright's dialog handler before clicking Apply
        page.onDialog(dialog -> {
            if (dialog.type().equals("confirm")) {
                dialog.accept();
            }
        });
        
        applyBtn.click();
    }

    /**
     * Enable email verification and set credentials in one operation
     * @param url SMTP server URL
     * @param username Email username
     * @param password Email password
     */
    public void enableEmailVerificationWithCredentials(String url, String username, String password) {
        enableEmailVerification();
        setEmailCredentials(url, username, password);
    }

    /**
     * Toggle the password visibility by clicking the eye icon
     */
    public void togglePasswordVisibility() {
        showPasswordBtn.click();
    }
}