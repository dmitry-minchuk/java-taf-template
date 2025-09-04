package domain.ui.webstudio.components.common;

import configuration.core.ui.CoreComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;

public class MessageComponent extends CoreComponent {

    private WebElement message;
    private WebElement closeBtn;

    public MessageComponent() {
        super(LocalDriverPool.getPage());
        initializeComponents();
    }

    public MessageComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeComponents();
    }

    private void initializeComponents() {
        message = createScopedElement("xpath=.//div[contains(@class,'ant-notification-notice-message')]", "Message Content");
        closeBtn = createScopedElement("xpath=.//a[@aria-label='Close']", "Close Message Button");
    }

    public String getMessageText() {
        return message.getText();
    }

    public void closeMessage() {
        closeBtn.click();
    }
}
