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
    // JSF closable messages: <div class="message closable error/info/warning"> at top of page
    // Legacy locator: //div[contains(@class, 'message closable')] | //div[@class='messages']
    private WebElement closableMessage;

    public BasePage() {
        super();
        initializeComponents();
    }

    public BasePage(Page page) {
        super(page);
        initializeComponents();
    }

    private void initializeComponents() {
        userLogo = new WebElement(page, "xpath=//header//span[contains(@class,'ant-avatar')][.//span[@aria-label='user']]", "User Logo");
        messages = createComponentList(MessageComponent.class, "xpath=//div[contains(@class,'ant-notification-notice-wrapper')]", "Studio Messages");
        userMenuDrawer = new WebElement(page, "xpath=//div[contains(@class,'ant-drawer') and contains(@class,'ant-drawer-open')]//div[contains(@class,'ant-drawer-section')]", "User Menu Drawer");
        contentLoadingSpinner = new WebElement(page, "xpath=//div[@id='loadingPanel']", "contentLoadingSpinner");
        modalOkBtn = new WebElement(page, "xpath=//div[@class='ant-modal-content']//button[./span[contains(text(),'OK')]]", "applyChangesBtn");
        notificationPanel = new WebElement(page, "xpath=//div[@data-show='true' and contains(@class, 'ant-alert-banner')]", "Notification Panel");
        closableMessage = new WebElement(page, "xpath=//div[contains(@class, 'message closable')]", "closableMessage");
    }

    public void clickModalOkBtn() {
        modalOkBtn.waitForVisible();
        modalOkBtn.click();
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

    public boolean isStudioMessageDisplayed(String text) {
        try {
            return messages.stream().anyMatch(m -> m.getMessageText().contains(text));
        } catch (Exception e) {
            LOGGER.debug("Ignoring exception during message check (likely due to DOM update): {}", e.getMessage());
            return false;
        }
    }

    public UserSlidingRightMenuComponent openUserMenu() {
        closeAllMessages();
        userLogo.click();
        userMenuDrawer.waitForVisible();
        return new UserSlidingRightMenuComponent(userMenuDrawer);
    }

    public void waitUntilSpinnerLoaded() {
        contentLoadingSpinner.waitForHidden(DEFAULT_TIMEOUT_MS * 100L);
        // Also wait out the new React full-screen loading overlay (EPBDS-16241 replaced #loadingPanel).
        WebElement.waitForAppReady(page);
    }

    // Debounced settle: waits until the React loading overlay stays absent continuously, so a following
    // click (e.g. a tab switch right after a recompile) isn't intercepted by the overlay flickering back.
    // Bounded (never throws): if the app is still churning it just proceeds and relies on click retries.
    public void waitUntilAppIdle() {
        WebElement.waitForAppIdle(page, 30000L);
    }

    public boolean isNotificationVisible() {
        return notificationPanel.sleep(500).isVisible();
    }

    public boolean isNotificationVisible(int timeoutMillis) {
        return notificationPanel.isVisible(timeoutMillis);
    }

    public String getNotificationText() {
        WaitUtil.waitForCondition(() -> notificationPanel.isVisible(), 100, 1000, "Waiting for notification to be visible");
        if (isNotificationVisible()) {
            return notificationPanel.getText();
        }
        throw new RuntimeException("No Notification text found!");
    }

    public boolean isClosableMessageVisible() {
        return closableMessage.isVisible(1000);
    }

    public String getClosableMessageText() {
        closableMessage.waitForVisible(DEFAULT_TIMEOUT_MS);
        return closableMessage.getText().trim();
    }

    public void closeClosableMessage() {
        if (isClosableMessageVisible()) {
            closableMessage.click();
        }
    }
}