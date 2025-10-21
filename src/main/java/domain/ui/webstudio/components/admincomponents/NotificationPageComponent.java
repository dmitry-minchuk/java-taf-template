package domain.ui.webstudio.components.admincomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;

public class NotificationPageComponent extends BaseComponent {

    private WebElement messageTextarea;
    private WebElement notifyButton;
    private WebElement clearButton;

    public NotificationPageComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public NotificationPageComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        messageTextarea = createScopedElement("xpath=.//textarea[contains(@class,'ant-input')]", "Message Textarea");
        notifyButton = createScopedElement("xpath=.//button[contains(.,'Notify')]", "Notify Button");
        clearButton = createScopedElement("xpath=.//button[contains(.,'Clear')]", "Clear Button");
    }

    public void sendNotification(String message) {
        messageTextarea.clear();
        messageTextarea.fill(message);
        notifyButton.sleep(100).click();
    }

    public void clearNotification() {
        clearButton.click();
    }
}