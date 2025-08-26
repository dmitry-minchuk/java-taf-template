package domain.ui.webstudio.pages.mainpages;

import configuration.core.ui.PlaywrightBasePage;
import configuration.core.ui.PlaywrightWebElement;
import domain.ui.webstudio.components.PlaywrightCurrentUserComponent;
import domain.ui.webstudio.components.PlaywrightMessageComponent;

import java.util.List;

public abstract class PlaywrightProxyMainPage extends PlaywrightBasePage {

    private PlaywrightWebElement userLogo;
    private List<PlaywrightMessageComponent> messages;
    private PlaywrightWebElement userMenuDrawer;

    public PlaywrightProxyMainPage(String urlAppender) {
        super(urlAppender);
        initializeComponents();
    }

    private void initializeComponents() {
        userLogo = new PlaywrightWebElement(page, "xpath=//div[contains(@class,'user-logo')][not(ancestor::div[contains(@class, 'ant-drawer-right')])]//span", "User Logo");
        messages = createComponentList(PlaywrightMessageComponent.class, "xpath=//div[contains(@class,'ant-notification-notice-wrapper')]", "Studio Messages");
        userMenuDrawer = new PlaywrightWebElement(page, "xpath=//div[contains(@class,'ant-drawer-content-wrapper')]", "User Menu Drawer");
    }

    public void closeAllMessages() {
        LOGGER.debug("messages.size() = {}", messages.size());
        for(PlaywrightMessageComponent msg : messages) {
            msg.closeMessage();
        }
    }

    public boolean isStudioMessageDisplayed(String text) {
        return messages.stream().anyMatch(m -> m.getMessageText().contains(text));
    }

    public PlaywrightCurrentUserComponent getCurrentUserComponent() {
        closeAllMessages();
        userLogo.click();
        userMenuDrawer.waitForVisible();
        return new PlaywrightCurrentUserComponent(userMenuDrawer);
    }
}