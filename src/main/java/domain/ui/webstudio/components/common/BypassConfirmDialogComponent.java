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
        // Anchor inside the ant-modal whose title is "Bypass branch protection?" — that
        // scopes Cancel + Confirm to this specific confirmation, away from the parent
        // Sync dialog and any other modals.
        String modalRoot = "//div[contains(@class,'ant-modal') and "
                + ".//div[contains(@class,'ant-modal-title') and "
                + "normalize-space(text())='Bypass branch protection?']]";
        confirmBtn = new WebElement(LocalDriverPool.getPage(),
                "xpath=" + modalRoot + "//button[.//span[normalize-space(text())='Confirm bypass and merge']]",
                "bypassConfirmBtn");
        cancelBtn = new WebElement(LocalDriverPool.getPage(),
                "xpath=" + modalRoot + "//button[normalize-space(.)='Cancel']",
                "bypassCancelBtn");
        title = new WebElement(LocalDriverPool.getPage(),
                "xpath=" + modalRoot + "//div[contains(@class,'ant-modal-title')]",
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

    /** Waits for the modal to disappear (with the modal's animation grace period) and
     *  returns true if it did so within that window, false otherwise. */
    public boolean waitForDialogToDisappear() {
        try {
            confirmBtn.waitForHidden(2_000);
            return true;
        } catch (RuntimeException e) {
            return !confirmBtn.isVisible(200);
        }
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

    /** True if the success toast is NOT visible within a short window (negative assertion). */
    public boolean isMergeSuccessNoticeAbsent() {
        try {
            mergeSuccessNotice.waitForHidden(2_000);
            return true;
        } catch (RuntimeException e) {
            return !mergeSuccessNotice.isVisible(200);
        }
    }
}
