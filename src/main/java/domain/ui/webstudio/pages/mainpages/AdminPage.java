package domain.ui.webstudio.pages.mainpages;

import domain.ui.webstudio.components.admincpmponents.AdminNavigationComponent;
import domain.ui.webstudio.components.admincpmponents.EmailPageComponent;
import org.openqa.selenium.support.FindBy;

public class AdminPage extends ProxyMainPage {

    @FindBy(xpath = "//div[@id='main-menu']")
    private AdminNavigationComponent adminNavigationComponent;

    @FindBy(xpath = "//form[./h4[text()='Email server configuration']]")
    private EmailPageComponent emailPageComponent;

    public AdminPage() {
        super("");
    }

    public EmailPageComponent navigateToEmailPage() {
        adminNavigationComponent.clickMail();
        return emailPageComponent;
    }
}
