package helpers.service;

import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.pages.LoginPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static configuration.driver.DriverPool.getDriver;

public class LoginService {
    protected static final Logger LOGGER = LogManager.getLogger(LoginService.class);
    private String sessionIdCookieName = "JSESSIONID";

    public EditorPage login(UserData user) {
        LoginPage loginPage = new LoginPage();
        loginPage.open();
        if(!loginPage.isPageOpened())
            loginPage = new InstallWizardService().setUpWebstudio(user);
        return loginPage.login(user);
    }

    public LoginService logout() {
        LOGGER.info("Logging out current user...");
        String currentUrl = getDriver().getCurrentUrl();
        getDriver().manage().deleteCookieNamed(sessionIdCookieName);
        getDriver().get(currentUrl);
        return this;
    }
}
