package helpers.service;

import configuration.driver.DriverPool;
import domain.ui.webstudio.pages.LoginPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;

public class LoginService {

    public EditorPage login(String userName, String userPassword) {
        LoginPage loginPage = new LoginPage(DriverPool.getDriver());
        loginPage.open();
        if(!loginPage.isPageOpened())
            loginPage = new InstallWizardService().setUpWebstudio();
        return loginPage.login(userName, userPassword);
    }
}
