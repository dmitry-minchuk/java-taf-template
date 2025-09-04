package domain.ui.webstudio.components.common;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.WaitUtil;

public class MessageComponent extends BaseComponent {

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
        WaitUtil.sleep(1000);
    }
}
