package domain.ui.webstudio.components.common;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.WaitUtil;

import java.util.List;

public class MessageComponent extends BaseComponent {

    private List<WebElement> message;
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
        message = createScopedElementList("xpath=.//div[contains(@class,'ant-notification-notice-message')]", "Message Content List");
        closeBtn = createScopedElement("xpath=.//a[@aria-label='Close']", "Close Message Button");
    }

    public String getMessageText() {
        if(!message.isEmpty()) {
            String text = message.getFirst().getText();
            LOGGER.info("Trying to extract text from the element: {}", text);
            return text;
        }
        return "";
    }

    public void closeMessage() {
        try {
            closeBtn.click();
        } catch (Exception ignored) {}
    }
}
