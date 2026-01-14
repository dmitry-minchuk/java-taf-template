package domain.ui.webstudio.components;

import com.microsoft.playwright.Page;
import configuration.core.ui.CoreComponent;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.common.MessageComponent;
import helpers.utils.WaitUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

// This class is separated from CoreComponent and created for specific element storage
public abstract class BaseComponent extends CoreComponent {

    private WebElement contentLoadingSpinner;
    @Getter
    private List<MessageComponent> messages;
    @Getter
    private WebElement modalOkBtn;
    private WebElement notificationPanel;

    public BaseComponent(Page page) {
        super(page);
        initializeElements();
    }

    public BaseComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        contentLoadingSpinner = new WebElement(page, "xpath=//div[@id='loadingPanel']", "contentLoadingSpinner");
        messages = createComponentList(MessageComponent.class, "xpath=//div[contains(@class,'ant-notification-notice-wrapper')]", "Studio Messages");
        modalOkBtn = new WebElement(page, "xpath=//div[@class='ant-modal-container']//button[./span[contains(text(),'OK')]]", "applyChangesBtn");
        notificationPanel = new WebElement(page, "xpath=//div[@data-show='true' and contains(@class, 'ant-alert-banner')]", "Notification Panel");
    }

    public void waitUntilSpinnerLoaded() {
        contentLoadingSpinner.waitForHidden(30000);
    }

    public void closeAllMessages() {
        try {
            LOGGER.info("Messages currently open: {}", messages.size());
        } catch (Exception e) {
            LOGGER.debug("Could not get messages size (likely due to DOM update)");
        }
        for(int i = 0; i < 3; i++) {
            try {
                messages.forEach(MessageComponent::closeMessage);
            } catch (Exception e) {
                LOGGER.debug("Ignoring exception during message closing (likely due to DOM update): {}", e.getMessage());
            }
            WaitUtil.sleep(100, "Waiting between message close attempts");
        }
    }

    public List<String> getAllMessages() {
        List<String> messagesTextList = new ArrayList<>();
        for(int i = 0; i < 30; i++) {
            try {
                messages.forEach(m -> {
                    if(!messagesTextList.contains(m.getMessageText()))
                        messagesTextList.add(m.getMessageText());
                });
            } catch (Exception e) {
                LOGGER.debug("Ignoring exception during message collection (likely due to DOM update): {}", e.getMessage());
            }
            WaitUtil.sleep(50, "Waiting between message get_text attempts");
        }
        return messagesTextList;
    }

    public boolean isNotificationVisible() {
        return notificationPanel.sleep(500).isVisible();
    }

    public String getNotificationText() {
        WaitUtil.waitForCondition(() -> notificationPanel.isVisible(), 100, 1000, "Waiting for notification to be visible");
        if (isNotificationVisible()) {
            return notificationPanel.getText();
        }
        throw new RuntimeException("No Notification text found!");
    }
}