package domain.ui.webstudio.pages.mainpages;

import configuration.core.ui.PlaywrightBasePage;
import configuration.core.ui.PlaywrightWebElement;
import domain.ui.webstudio.components.PlaywrightCurrentUserComponent;

public abstract class PlaywrightProxyMainPage extends PlaywrightBasePage {

    private PlaywrightCurrentUserComponent currentUserComponent;
    private PlaywrightWebElement userLogo;
    private PlaywrightWebElement message;

    public PlaywrightProxyMainPage(String urlAppender) {
        super(urlAppender);
        initializeComponents();
    }

    private void initializeComponents() {
        userLogo = new PlaywrightWebElement(page, "div.user-logo span");
        message = new PlaywrightWebElement(page, "div.message.closable");
        currentUserComponent = new PlaywrightCurrentUserComponent();
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
        page.locator("div.ant-drawer-content-wrapper").waitFor();
        return currentUserComponent;
    }
}