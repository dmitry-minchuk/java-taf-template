package domain.ui.webstudio.components.common;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.DriverPool;

import java.util.List;

public class MessageComponent extends BaseComponent {

    private List<WebElement> message;
    private WebElement closeBtn;

    public MessageComponent() {
        super(DriverPool.getPage());
        initializeComponents();
    }

    public MessageComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeComponents();
    }

    private void initializeComponents() {
        message = createScopedElementList("xpath=.//div[contains(@class,'ant-notification-notice-message')]", "Message Content List");
        closeBtn = createScopedElement("xpath=.//a[@aria-label='Close'] | .//button[@class='ant-notification-notice-close']", "Close Message Button");
    }

    public String getMessageText() {
        if(!message.isEmpty()) {
            String text = message.getFirst().getText();
            LOGGER.info("Trying to extract text from the element: {}", text);
            return text;
        }
        return "";
    }

    public String getFullText() {
        return getRootLocator().getText().replaceAll("\\s+", " ").trim();
    }

    public void closeMessage() {
        try {
            closeBtn.click();
        } catch (Exception ignored) {}
    }
}
