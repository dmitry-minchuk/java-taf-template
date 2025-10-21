package domain.ui.webstudio.pages;

import com.microsoft.playwright.Page;
import configuration.core.ui.CorePage;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.common.MessageComponent;
import domain.ui.webstudio.components.common.UserSlidingRightMenuComponent;
import helpers.utils.WaitUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

// This class is separated from CorePage and created for specific element storage
public abstract class BasePage extends CorePage {

    private WebElement userLogo;
    @Getter
    private List<MessageComponent> messages;
    private WebElement userMenuDrawer;
    private WebElement contentLoadingSpinner;
    @Getter
    private WebElement modalOkBtn;
    private WebElement notificationPanel;

    public BasePage() {
        super();
        initializeComponents();
    }

    public BasePage(Page page) {
        super(page);
        initializeComponents();
    }

    private void initializeComponents() {
        userLogo = new WebElement(page, "xpath=//div[contains(@class,'user-logo')][not(ancestor::div[contains(@class, 'ant-drawer-right')])]//span", "User Logo");
        messages = createComponentList(MessageComponent.class, "xpath=//div[contains(@class,'ant-notification-notice-wrapper')]", "Studio Messages");
        userMenuDrawer = new WebElement(page, "xpath=//div[contains(@class,'ant-drawer-content-wrapper')]", "User Menu Drawer");
        contentLoadingSpinner = new WebElement(page, "xpath=//div[@id='loadingPanel']", "contentLoadingSpinner");
        modalOkBtn = new WebElement(page, "xpath=//div[@class='ant-modal-content']//button[./span[contains(text(),'OK')]]", "applyChangesBtn");
        notificationPanel = new WebElement(page, "xpath=//div[@data-show='true' and contains(@class, 'ant-alert-banner')]", "Notification Panel");
    }

    public void closeAllMessages() {
        LOGGER.info("Messages currently open: {}", messages.size());
        for(int i = 0; i < 3; i++) {
            messages.forEach(MessageComponent::closeMessage);
            WaitUtil.sleep(100, "Waiting between message close attempts");
        }
    }

    public List<String> getAllMessages() {
        List<String> messagesTextList = new ArrayList<>();
        for(int i = 0; i < 30; i++) {
            messages.forEach(m -> {
                if(!messagesTextList.contains(m.getMessageText()))
                    messagesTextList.add(m.getMessageText());
            });
            WaitUtil.sleep(50, "Waiting between message get_text attempts");
        }
        return messagesTextList;
    }

    public boolean isStudioMessageDisplayed(String text) {
        return messages.stream().anyMatch(m -> m.getMessageText().contains(text));
    }

    public UserSlidingRightMenuComponent openUserMenu() {
        closeAllMessages();
        userLogo.click();
        userMenuDrawer.waitForVisible();
        return new UserSlidingRightMenuComponent(userMenuDrawer);
    }

    public void waitUntilSpinnerLoaded() {
        contentLoadingSpinner.waitForHidden(DEFAULT_TIMEOUT_MS * 100L);
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