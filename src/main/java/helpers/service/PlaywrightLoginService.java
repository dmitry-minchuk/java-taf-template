package helpers.service;

import com.microsoft.playwright.Page;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.pages.mainpages.PlaywrightAdminPage;
import domain.ui.webstudio.pages.mainpages.PlaywrightEditorPage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Playwright version of LoginService for user authentication
 * Provides login functionality using Playwright page automation
 */
public class PlaywrightLoginService {
    
    protected static final Logger LOGGER = LogManager.getLogger(PlaywrightLoginService.class);
    private final Page page;
    
    public PlaywrightLoginService(Page page) {
        this.page = page;
    }
    
    public PlaywrightEditorPage login(UserData user) {
        LOGGER.info("Logging in with user: {}", user.getLogin());
        
        // Navigate to login page using proper URL resolution (LOCAL vs DOCKER mode aware)
        String loginUrl = configuration.driver.PlaywrightDriverPool.getAppUrl();
        page.navigate(loginUrl);
        LOGGER.info("Navigated to login page: {}", loginUrl);
        
        // Wait for login form to be visible and fill credentials
        var usernameField = page.locator("input#loginName");
        var passwordField = page.locator("input#loginPassword");
        var loginButton = page.locator("input#loginSubmit");
        
        usernameField.waitFor();
        usernameField.fill(user.getLogin());
        
        passwordField.fill(user.getPassword());
        
        loginButton.click();
        
        // Wait for successful login - editor page should load
        page.waitForURL("**/", new Page.WaitForURLOptions().setTimeout(10000));
        
        LOGGER.info("Successfully logged in as: {}", user.getLogin());
        return new PlaywrightEditorPage();
    }
    
    // Navigate to Administration via User Menu: user-logo span → ant-drawer → Administration → Admin Page
    public PlaywrightAdminPage navigateToAdministration() {
        LOGGER.info("Navigating to Administration via User Menu");
        
        // Step 1: Click the user logo span to open the user menu
        // Original Selenium: //div[@class='user-logo']/span
        var userLogo = page.locator("div.user-logo span");
        
        userLogo.waitFor();
        userLogo.click();
        LOGGER.info("User logo clicked successfully");
        
        var userMenuDrawer = page.locator("div.ant-drawer-content-wrapper");
        userMenuDrawer.waitFor();
        LOGGER.info("User menu drawer opened successfully");
        
        var administrationMenuItem = page.locator("li.ant-menu-item:has(span:text('Administration'))");
        administrationMenuItem.waitFor();
        administrationMenuItem.click();
        LOGGER.info("Administration menu item clicked");
        
        page.locator("div#main-menu, [data-menu-id]").first().waitFor();
        
        return new PlaywrightAdminPage();
    }
}