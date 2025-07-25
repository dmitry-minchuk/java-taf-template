package helpers.service;

import com.microsoft.playwright.Page;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.pages.mainpages.PlaywrightAdminPage;
import domain.ui.webstudio.pages.mainpages.PlaywrightEditorPage;
import helpers.utils.PlaywrightExpectUtil;
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
    
    /**
     * Login with the specified user credentials
     * @param user UserData containing username and password
     * @return PlaywrightEditorPage after successful login (same as Selenium version)
     */
    public PlaywrightEditorPage login(UserData user) {
        LOGGER.info("Logging in with user: {}", user.getLogin());
        
        // Navigate to login page using localhost URL for LOCAL mode (Playwright runs on host)
        configuration.appcontainer.AppContainerData appData = configuration.appcontainer.AppContainerPool.get();
        int mappedPort = appData.getAppContainer().getMappedPort(8080); // APP_PORT from AppContainerFactory
        String loginUrl = "http://localhost:" + mappedPort + "/";
        page.navigate(loginUrl);
        LOGGER.info("Navigated to login page: {}", loginUrl);
        
        // Wait for login form to be visible - using specific IDs from Selenium LoginPage
        var usernameField = page.locator("input#loginName");
        var passwordField = page.locator("input#loginPassword");  
        var loginButton = page.locator("input#loginSubmit");
        
        // Wait for login form elements to be visible
        PlaywrightExpectUtil.expectVisible(page, usernameField);
        PlaywrightExpectUtil.expectVisible(page, passwordField);
        PlaywrightExpectUtil.expectVisible(page, loginButton);
        
        // Fill credentials
        usernameField.clear();
        usernameField.fill(user.getLogin());
        
        passwordField.clear();
        passwordField.fill(user.getPassword());
        
        // Click login button
        loginButton.click();
        
        // Wait for successful login - look for editor page elements
        PlaywrightExpectUtil.expectVisible(page, page.locator("body"));
        
        LOGGER.info("Successfully logged in as: {}", user.getLogin());
        return new PlaywrightEditorPage(); // Return EditorPage as in Selenium version
    }
    
    /**
     * Navigate to Administration page by opening User Menu first, then clicking Administration
     * Based on getCurrentUserComponent().navigateToAdministration() implementation
     * Flow: Click user-logo span → Wait for ant-drawer → Click Administration → Navigate to Admin Page
     * @return PlaywrightAdminPage for admin operations
     */
    public PlaywrightAdminPage navigateToAdministration() {
        LOGGER.info("Navigating to Administration via User Menu");
        
        // Step 1: Click the user logo span to open the user menu
        // Original Selenium: //div[@class='user-logo']/span
        var userLogo = page.locator("div.user-logo span");
        
        PlaywrightExpectUtil.expectVisible(page, userLogo);
        userLogo.click();
        LOGGER.info("User logo clicked successfully");
        
        // Step 2: Wait for Ant Design drawer to appear
        // Original Selenium: //div[@class='ant-drawer-content-wrapper']
        var userMenuDrawer = page.locator("div.ant-drawer-content-wrapper");
        PlaywrightExpectUtil.expectVisible(page, userMenuDrawer);
        LOGGER.info("User menu drawer opened successfully");
        
        // Step 3: Click Administration menu item within the drawer
        // Original Selenium: .//li[@class='ant-menu-item' and ./span[text()='Administration']]
        var administrationMenuItem = page.locator("li.ant-menu-item:has(span:text('Administration'))");
        
        PlaywrightExpectUtil.expectVisible(page, administrationMenuItem);
        administrationMenuItem.click();
        LOGGER.info("Administration menu item clicked");
        
        // Step 4: Wait for admin page to load - look for admin navigation menu
        PlaywrightExpectUtil.expectVisible(page, page.locator("div#main-menu, [data-menu-id]"));
        
        return new PlaywrightAdminPage(page);
    }
}