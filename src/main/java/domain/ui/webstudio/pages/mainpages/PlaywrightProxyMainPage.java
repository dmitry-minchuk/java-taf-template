package domain.ui.webstudio.pages.mainpages;

import configuration.core.ui.PlaywrightBasePage;
import configuration.core.ui.PlaywrightWebElement;
import domain.ui.webstudio.components.PlaywrightCurrentUserComponent;

public abstract class PlaywrightProxyMainPage extends PlaywrightBasePage {

    private PlaywrightWebElement userLogo;
    private PlaywrightWebElement message;

    public PlaywrightProxyMainPage(String urlAppender) {
        super(urlAppender);
        initializeComponents();
    }

    private void initializeComponents() {
        userLogo = new PlaywrightWebElement(page, "div.user-logo span", "User Logo");
        message = new PlaywrightWebElement(page, "div.message.closable", "Studio Message");
    }

    public String getStudioMessage() {
        if (message.isDisplayed()) {
            return message.getText();
        } else {
            return null;
        }
    }

    public boolean isStudioMessageDisplayed(String text) {
        return message.isDisplayed() && message.getText().contains(text);
    }

    public PlaywrightCurrentUserComponent getCurrentUserComponent() {
        userLogo.click();
        var userMenuDrawer = new PlaywrightWebElement(page, "div.ant-drawer-content-wrapper", "User Menu Drawer");
        userMenuDrawer.waitForVisible();
        return new PlaywrightCurrentUserComponent(userMenuDrawer);
    }
}