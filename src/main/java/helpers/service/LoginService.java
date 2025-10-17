package helpers.service;

import com.microsoft.playwright.Page;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.LoginPage;
import helpers.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoginService {
    
    protected static final Logger LOGGER = LogManager.getLogger(LoginService.class);
    private final Page page;
    
    public LoginService(Page page) {
        this.page = page;
    }
    
    public EditorPage login(UserData user) {
        return login(user, LocalDriverPool.getAppUrl());
    }

    public EditorPage login(UserData user, String appUrl) {
        LOGGER.info("Logging in with user: {}", user.getLogin());
        page.navigate(appUrl);
        LOGGER.info("Navigated to login page: {}", appUrl);
        return new LoginPage().login(user);
    }
}