package domain.ui.webstudio.pages.mainpages;

import configuration.core.ui.PlaywrightWebElement;
import domain.ui.webstudio.components.admincomponents.PlaywrightAdminNavigationComponent;
import domain.ui.webstudio.components.admincomponents.PlaywrightEmailPageComponent;
import lombok.Getter;

public class PlaywrightAdminPage extends PlaywrightProxyMainPage {

    @Getter
    private PlaywrightAdminNavigationComponent adminNavigationComponent;
    private PlaywrightEmailPageComponent emailPageComponent;
    private PlaywrightWebElement adminNavigation;
    private PlaywrightWebElement emailPageContainer;

    public PlaywrightAdminPage() {
        super("/");
        initializeAdminComponents();
    }

    private void initializeAdminComponents() {
        // Define root locators for component scoping
        adminNavigation = new PlaywrightWebElement(page, ".ant-layout-sider, .admin-navigation", "Admin Navigation Container");
        emailPageContainer = new PlaywrightWebElement(page, ".ant-layout-content, .email-page-content", "Email Page Container");
        
        // Initialize components with proper root locators
        adminNavigationComponent = new PlaywrightAdminNavigationComponent(adminNavigation);
        emailPageComponent = new PlaywrightEmailPageComponent(emailPageContainer);
    }

    public PlaywrightEmailPageComponent navigateToEmailPage() {
        adminNavigationComponent.clickMail();
        return emailPageComponent;
    }
}