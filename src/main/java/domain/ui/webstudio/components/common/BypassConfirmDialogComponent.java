package domain.ui.webstudio.components.common;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * EPBDS-15960 sections G/H: secondary "Bypass branch protection?" confirmation
 * shown above the Sync dialog when a bypass-eligible user tries to send into a
 * protected target branch.
 */
public class BypassConfirmDialogComponent extends BaseComponent {

    private static final Logger LOGGER = LogManager.getLogger(BypassConfirmDialogComponent.class);

    private WebElement title;
    private WebElement confirmBtn;
    private WebElement cancelBtn;
    private WebElement mergeSuccessNotice;

    public BypassConfirmDialogComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    private void initializeElements() {
        confirmBtn = new WebElement(LocalDriverPool.getPage(),
                "xpath=//button[.//span[normalize-space(text())='Confirm bypass and merge']]",
                "bypassConfirmBtn");
        cancelBtn = new WebElement(LocalDriverPool.getPage(),
                "xpath=//button[.//span[normalize-space(text())='Confirm bypass and merge']]/preceding-sibling::button[1]",
                "bypassCancelBtn");
        title = new WebElement(LocalDriverPool.getPage(),
                "xpath=//div[contains(@class,'ant-modal-title') and normalize-space(text())='Bypass branch protection?']",
                "bypassConfirmTitle");
        mergeSuccessNotice = new WebElement(LocalDriverPool.getPage(),
                "xpath=//div[contains(@class,'ant-notification')]//*[normalize-space(text())='Merge Successful']",
                "mergeSuccessNotice");
    }

    public BypassConfirmDialogComponent waitForDialogToAppear() {
        confirmBtn.waitForVisible();
        return this;
    }

    public boolean isVisible() {
        return confirmBtn.isVisible();
    }

    public String getTitle() {
        return title.getText().trim();
    }

    public void clickConfirmBypassAndMerge() {
        LOGGER.info("Clicking 'Confirm bypass and merge' on bypass confirmation dialog");
        confirmBtn.click();
    }

    public void clickCancel() {
        LOGGER.info("Clicking 'Cancel' on bypass confirmation dialog");
        cancelBtn.click();
    }

    public boolean isMergeSuccessNoticeVisible() {
        try {
            mergeSuccessNotice.waitForVisible();
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
