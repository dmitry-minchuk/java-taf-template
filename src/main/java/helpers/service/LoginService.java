package helpers.service;

import configuration.driver.ContainerizedDriverPool;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.pages.LoginPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;

public class LoginService {

    public EditorPage login(UserData user) {
        LoginPage loginPage = new LoginPage(ContainerizedDriverPool.getDriver());
        loginPage.open();
        if(!loginPage.isPageOpened())
            loginPage = new InstallWizardService().setUpWebstudio(user);
        return loginPage.login(user);
    }
}
