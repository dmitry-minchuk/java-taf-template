package domain.ui.webstudio.pages.mainpages;

import domain.ui.webstudio.components.admincomponents.PlaywrightAdminNavigationComponent;
import domain.ui.webstudio.components.admincomponents.PlaywrightEmailPageComponent;
import lombok.Getter;

public class PlaywrightAdminPage extends PlaywrightProxyMainPage {

    @Getter
    private PlaywrightAdminNavigationComponent adminNavigationComponent;
    private PlaywrightEmailPageComponent emailPageComponent;

    public PlaywrightAdminPage() {
        super("/");
        initializeAdminComponents();
    }

    private void initializeAdminComponents() {
        adminNavigationComponent = new PlaywrightAdminNavigationComponent();
        emailPageComponent = new PlaywrightEmailPageComponent();
    }

    public PlaywrightEmailPageComponent navigateToEmailPage() {
        adminNavigationComponent.clickMail();
        return emailPageComponent;
    }
}