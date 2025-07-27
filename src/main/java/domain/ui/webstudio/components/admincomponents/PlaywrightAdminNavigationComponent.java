package domain.ui.webstudio.components.admincomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;

public class PlaywrightAdminNavigationComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement mailMenuItem;

    public PlaywrightAdminNavigationComponent() {
        super(configuration.driver.PlaywrightDriverPool.getPage());
        initializeNavigationComponents();
    }

    public PlaywrightAdminNavigationComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeNavigationComponents();
    }

    private void initializeNavigationComponents() {
        mailMenuItem = createScopedElement("li.ant-menu-item:has(span:text('Mail'))", "mailMenuItem");
    }

    public void clickMail() {
        mailMenuItem.click();
    }
}