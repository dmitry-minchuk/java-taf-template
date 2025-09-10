package helpers.service;

import com.microsoft.playwright.Page;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.LoginPage;
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
        String baseUrl = LocalDriverPool.getAppUrl();
        page.navigate(baseUrl);
        LOGGER.info("Navigated to login page: {}", baseUrl);
        
        return new LoginPage().login(user);
    }
}