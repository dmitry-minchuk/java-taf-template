package domain.ui.webstudio.pages.mainpages;

import com.microsoft.playwright.Page;
import configuration.core.ui.CorePage;
import configuration.core.ui.PlaywrightWebElement;
import domain.ui.webstudio.components.PlaywrightCurrentUserComponent;
import domain.ui.webstudio.components.PlaywrightMessageComponent;
import helpers.utils.WaitUtil;

import java.util.List;

// This class is separated from BasePage and created for specific element storage
public abstract class BasePage extends CorePage {

    private PlaywrightWebElement userLogo;
    private List<PlaywrightMessageComponent> messages;
    private PlaywrightWebElement userMenuDrawer;
    private PlaywrightWebElement contentLoadingSpinner;

    public BasePage() {
        super();
        initializeComponents();
    }

    public BasePage(Page page) {
        super(page);
        initializeComponents();
    }

    private void initializeComponents() {
        userLogo = new PlaywrightWebElement(page, "xpath=//div[contains(@class,'user-logo')][not(ancestor::div[contains(@class, 'ant-drawer-right')])]//span", "User Logo");
        messages = createComponentList(PlaywrightMessageComponent.class, "xpath=//div[contains(@class,'ant-notification-notice-wrapper')]", "Studio Messages");
        userMenuDrawer = new PlaywrightWebElement(page, "xpath=//div[contains(@class,'ant-drawer-content-wrapper')]", "User Menu Drawer");
        contentLoadingSpinner = new PlaywrightWebElement(page, "//div[@id='loadingPanel']", "contentLoadingSpinner");
    }

    public void closeAllMessages() {
        LOGGER.debug("messages.size() = {}", messages.size());
        for(PlaywrightMessageComponent msg : messages) {
            msg.closeMessage();
        }
    }

    public boolean isStudioMessageDisplayed(String text) {
        return messages.stream().anyMatch(m -> m.getMessageText().contains(text));
    }

    public PlaywrightCurrentUserComponent getCurrentUserComponent() {
        closeAllMessages();
        userLogo.click();
        userMenuDrawer.waitForVisible();
        return new PlaywrightCurrentUserComponent(userMenuDrawer);
    }

    public void waitUntilPageContentLoaded() {
        while(contentLoadingSpinner.isVisible()) {
            LOGGER.info("Waiting until loading spinner to disappear...");
            WaitUtil.sleep(100);
        }
    }
}