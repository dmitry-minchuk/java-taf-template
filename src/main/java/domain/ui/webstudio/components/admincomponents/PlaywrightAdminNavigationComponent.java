package domain.ui.webstudio.components.admincomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;

public class PlaywrightAdminNavigationComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement mailMenuItem;

    public PlaywrightAdminNavigationComponent() {
        super(configuration.driver.PlaywrightDriverPool.getPage());
        initializeNavigationComponents();
    }

    private void initializeNavigationComponents() {
        mailMenuItem = new PlaywrightWebElement(page, "li.ant-menu-item:has(span:text('Mail'))");
    }

    public void clickMail() {
        mailMenuItem.click();
    }
}