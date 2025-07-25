package domain.ui.webstudio.components.admincomponents;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import configuration.core.ui.PlaywrightBasePageComponent;
import helpers.utils.PlaywrightExpectUtil;

/**
 * Playwright version of EmailPageComponent for Admin UI Email verification configuration
 * Migrated from Selenium-based EmailPageComponent to use native Playwright wait strategies
 */
public class PlaywrightEmailPageComponent extends PlaywrightBasePageComponent {

    // Playwright locators using CSS selectors (more reliable than XPath)
    private final Locator emailVerificationCheckbox;
    private final Locator emailUrlField;
    private final Locator emailUsernameField;
    private final Locator emailPasswordField;
    private final Locator applyBtn;
    private final Locator showPasswordBtn;

    public PlaywrightEmailPageComponent(Page page) {
        super(page);
        
        // Initialize locators with CSS selectors optimized for Playwright
        this.emailVerificationCheckbox = page.locator("input[type='checkbox']");
        this.emailUrlField = page.locator("div:has(label[title='URL']) input");
        this.emailUsernameField = page.locator("div:has(label[title='Username']) input");
        this.emailPasswordField = page.locator("input#password");
        this.applyBtn = page.locator("button:has(span:text('Apply'))");
        this.showPasswordBtn = page.locator("span[aria-label*='eye']");
    }

    /**
     * Enable email verification by clicking the checkbox if not already enabled
     */
    public void enableEmailVerification() {
        if (!isEmailVerificationEnabled()) {
            PlaywrightExpectUtil.expectVisible(page, emailVerificationCheckbox);
            emailVerificationCheckbox.click();
        }
    }

    /**
     * Disable email verification by clicking the checkbox if currently enabled
     */
    public void disableEmailVerification() {
        if (isEmailVerificationEnabled()) {
            PlaywrightExpectUtil.expectVisible(page, emailVerificationCheckbox);
            emailVerificationCheckbox.click();
        }
    }

    /**
     * Check if email verification is enabled by checking if URL field is visible
     * @return true if email verification is enabled (URL field is visible)
     */
    public boolean isEmailVerificationEnabled() {
        try {
            return PlaywrightExpectUtil.expectVisible(page, emailUrlField, 2000);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Set the email URL in the URL field
     * @param url SMTP server URL
     */
    public void setEmailUrl(String url) {
        PlaywrightExpectUtil.expectVisible(page, emailUrlField);
        emailUrlField.clear();
        emailUrlField.fill(url);
    }

    /**
     * Set the email username in the username field
     * @param username Email username
     */
    public void setEmailUsername(String username) {
        PlaywrightExpectUtil.expectVisible(page, emailUsernameField);
        emailUsernameField.clear();
        emailUsernameField.fill(username);
    }

    /**
     * Set the email password in the password field
     * @param password Email password
     */
    public void setEmailPassword(String password) {
        PlaywrightExpectUtil.expectVisible(page, emailPasswordField);
        emailPasswordField.clear();
        emailPasswordField.fill(password);
    }

    /**
     * Get the current value of the email URL field
     * @return Current URL field value
     */
    public String getEmailUrl() {
        PlaywrightExpectUtil.expectVisible(page, emailUrlField);
        return emailUrlField.inputValue();
    }

    /**
     * Get the current value of the email username field
     * @return Current username field value
     */
    public String getEmailUsername() {
        PlaywrightExpectUtil.expectVisible(page, emailUsernameField);
        return emailUsernameField.inputValue();
    }

    /**
     * Get the current value of the email password field
     * @return Current password field value (usually masked)
     */
    public String getEmailPassword() {
        PlaywrightExpectUtil.expectVisible(page, emailPasswordField);
        return emailPasswordField.inputValue();
    }

    /**
     * Set email credentials and apply the configuration
     * @param url SMTP server URL
     * @param username Email username  
     * @param password Email password
     */
    public void setEmailCredentials(String url, String username, String password) {
        setEmailUrl(url);
        setEmailUsername(username);
        setEmailPassword(password);
        
        PlaywrightExpectUtil.expectVisible(page, applyBtn);
        applyBtn.click();
        
        // Handle confirmation popup using Playwright's built-in dialog handling
        page.onDialog(dialog -> {
            if (dialog.type().equals("confirm")) {
                dialog.accept();
            }
        });
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
        PlaywrightExpectUtil.expectVisible(page, showPasswordBtn);
        showPasswordBtn.click();
    }
}