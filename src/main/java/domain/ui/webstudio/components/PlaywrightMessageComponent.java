package domain.ui.webstudio.components;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

public class PlaywrightMessageComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement message;
    private PlaywrightWebElement messageWithText;
    private PlaywrightWebElement closeBtn;

    public PlaywrightMessageComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeComponents();
    }

    public PlaywrightMessageComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeComponents();
    }

    private void initializeComponents() {
        message = createScopedElement("xpath=//div[contains(@class,'ant-message-notice-content')]", "Message Content");
        messageWithText = createScopedElement("xpath=//div[contains(@class,'ant-message-notice-content') and contains(.,'%s')]", "Message With Text");
        closeBtn = createScopedElement("xpath=.//li[contains(@class,'ant-menu-item')]//span[text()='My Settings']", "Close Message Button");
    }

    public boolean isMessageDisplayed(String messageText) {
        return messageWithText.format(messageText).isVisible();
    }

    public String getMessageText() {
        return message.getText();
    }

    public void closeMessage() {
        closeBtn.click();
    }
}
