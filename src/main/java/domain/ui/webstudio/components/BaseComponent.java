package domain.ui.webstudio.components;

import com.microsoft.playwright.Page;
import configuration.core.ui.CoreComponent;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.common.MessageComponent;
import helpers.utils.WaitUtil;
import lombok.Getter;

import java.util.List;

// This class is separated from CoreComponent and created for specific element storage
public abstract class BaseComponent extends CoreComponent {

    private WebElement contentLoadingSpinner;
    @Getter
    private List<MessageComponent> messages;
    @Getter
    private WebElement modalOkBtn;

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
        modalOkBtn = new WebElement(page, "xpath=//div[@class='ant-modal-content']//button[./span[contains(text(),'OK')]]", "applyChangesBtn");
    }

    public void waitUntilSpinnerLoaded() {
        contentLoadingSpinner.waitForHidden(30000);
    }

    public void closeAllMessages() {
        LOGGER.info("messages.size() = {}", messages.size());
        for(int i = 0; i < 3; i++) {
            messages.forEach(MessageComponent::closeMessage);
            WaitUtil.sleep(100);
        }
    }
}