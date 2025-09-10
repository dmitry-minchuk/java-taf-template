package domain.ui.webstudio.pages;

import com.microsoft.playwright.Page;
import configuration.core.ui.CorePage;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.common.MessageComponent;
import domain.ui.webstudio.components.common.UserSlidingRightMenuComponent;
import helpers.utils.WaitUtil;

import java.util.List;

// This class is separated from CorePage and created for specific element storage
public abstract class BasePage extends CorePage {

    private WebElement userLogo;
    private List<MessageComponent> messages;
    private WebElement userMenuDrawer;
    private WebElement contentLoadingSpinner;

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
    }

    public void closeAllMessages() {
        LOGGER.debug("messages.size() = {}", messages.size());
        for(int i = 0; i < 3; i++) {
            messages.forEach(MessageComponent::closeMessage);
            WaitUtil.sleep(100);
        }
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
        contentLoadingSpinner.waitForHidden(30000);
    }
}