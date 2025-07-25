package domain.ui.webstudio.pages;

import configuration.core.ui.PlaywrightBasePage;
import configuration.core.ui.PlaywrightWebElement;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.utils.PlaywrightExpectUtil;
import com.microsoft.playwright.Page;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Playwright-based LoginPage implementation
 * Phase 2.3: Demonstrates expect() patterns for state verification
 */
public class PlaywrightLoginPage extends PlaywrightBasePage {
    
    private static final Logger LOGGER = LogManager.getLogger(PlaywrightLoginPage.class);
    
    private PlaywrightWebElement loginTextField;
    private PlaywrightWebElement passwordTextField;
    private PlaywrightWebElement signInBtn;
    
    public PlaywrightLoginPage() {
        super("/");
        initializeElements();
    }
    
    protected void initializeElements() {
        Page page = getPage();
        
        // Initialize elements with Playwright locators
        loginTextField = new PlaywrightWebElement(page, "#loginName");
        passwordTextField = new PlaywrightWebElement(page, "#loginPassword");
        signInBtn = new PlaywrightWebElement(page, "#loginSubmit");
    }
    
    public Page getPage() {
        return this.page;
    }
    
    /**
     * Login method using Playwright expect patterns
     * Demonstrates Phase 2.3: expect() patterns for state verification
     */
    public EditorPage login(UserData user) {
        LOGGER.info("Logging in with user: {}", user.getLogin());
        
        // PLAYWRIGHT MIGRATION: Use expect patterns to verify page state
        Page page = getPage();
        
        // Wait for page to be ready
        PlaywrightExpectUtil.expectPageReady(page);
        
        // Verify login form is visible
        if (!PlaywrightExpectUtil.expectVisible(page, "#loginName")) {
            throw new RuntimeException("Login form is not visible");
        }
        
        // Fill login form with state verification
        loginTextField.fill(user.getLogin());
        
        // Verify login field contains expected text
        if (!PlaywrightExpectUtil.expectText(page, "#loginName", user.getLogin())) {
            LOGGER.warn("Login field text verification failed");
        }
        
        passwordTextField.fill(user.getPassword());
        
        // Verify sign-in button is stable (visible and enabled)
        if (!PlaywrightExpectUtil.expectElementStable(page, "#loginSubmit")) {
            throw new RuntimeException("Sign-in button is not stable for interaction");
        }
        
        signInBtn.click();
        
        // Wait for navigation to complete
        PlaywrightExpectUtil.expectPageReady(page);
        
        LOGGER.info("Login completed successfully");
        return new EditorPage();
    }
    
    /**
     * Verify login page is fully loaded and ready
     * Demonstrates comprehensive state verification using expect patterns
     */
    public boolean isLoginPageReady() {
        Page page = getPage();
        
        // Comprehensive page readiness check using expect patterns
        return PlaywrightExpectUtil.expectVisible(page, "#loginName") &&
               PlaywrightExpectUtil.expectVisible(page, "#loginPassword") &&
               PlaywrightExpectUtil.expectElementStable(page, "#loginSubmit") &&
               PlaywrightExpectUtil.expectElementCount(page, "form", 1);
    }
    
    /**
     * Verify error message appears (if login fails)
     */
    public boolean hasErrorMessage() {
        return PlaywrightExpectUtil.expectVisible(getPage(), ".error-message, .alert-danger");
    }
    
    /**
     * Clear login form fields
     */
    public void clearForm() {
        Page page = getPage();
        
        if (PlaywrightExpectUtil.expectVisible(page, "#loginName")) {
            loginTextField.clear();
        }
        
        if (PlaywrightExpectUtil.expectVisible(page, "#loginPassword")) {
            passwordTextField.clear();
        }
        
        LOGGER.debug("Login form cleared");
    }
}