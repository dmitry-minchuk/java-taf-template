package helpers.service;

import com.microsoft.playwright.Page;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoginService {
    
    protected static final Logger LOGGER = LogManager.getLogger(LoginService.class);
    private final Page page;
    
    public LoginService(Page page) {
        this.page = page;
    }
    
    public EditorPage login(UserData user) {
        LOGGER.info("Logging in with user: {}", user.getLogin());
        
        // Navigate to login page using proper URL resolution (LOCAL vs DOCKER mode aware)
        String loginUrl = LocalDriverPool.getAppUrl();
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
        return new EditorPage();
    }
}