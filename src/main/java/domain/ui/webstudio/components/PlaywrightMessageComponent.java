package domain.ui.webstudio.components;

import configuration.core.ui.CoreComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.LocalDriverPool;

public class PlaywrightMessageComponent extends CoreComponent {

    private PlaywrightWebElement message;
    private PlaywrightWebElement closeBtn;

    public PlaywrightMessageComponent() {
        super(LocalDriverPool.getPage());
        initializeComponents();
    }

    public PlaywrightMessageComponent(PlaywrightWebElement rootLocator) {
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
