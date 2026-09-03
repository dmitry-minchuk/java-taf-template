package domain.ui.webstudio.components.common;

import configuration.core.ui.WebElement;
import configuration.driver.DriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicReference;

public class BypassConfirmDialogComponent extends BaseComponent {

    private static final Logger LOGGER = LogManager.getLogger(BypassConfirmDialogComponent.class);
    private static final int MERGE_TIMEOUT_MS = DEFAULT_TIMEOUT_MS * 3;

    private WebElement title;
    private WebElement confirmBtn;
    private WebElement cancelBtn;
    private WebElement mergeSuccessNotice;
    private WebElement errorNotice;

    public BypassConfirmDialogComponent() {
        super(DriverPool.getPage());
        initializeElements();
    }

    private void initializeElements() {
        String modalRoot = "//div[contains(@class,'ant-modal') and "
                + ".//div[contains(@class,'ant-modal-title') and "
                + "normalize-space(text())='Bypass branch protection?']]";
        confirmBtn = new WebElement(DriverPool.getPage(),
                "xpath=" + modalRoot + "//button[.//span[normalize-space(text())='Confirm bypass and merge']]",
                "bypassConfirmBtn");
        cancelBtn = new WebElement(DriverPool.getPage(),
                "xpath=" + modalRoot + "//button[normalize-space(.)='Cancel']",
                "bypassCancelBtn");
        title = new WebElement(DriverPool.getPage(),
                "xpath=" + modalRoot + "//div[contains(@class,'ant-modal-title')]",
                "bypassConfirmTitle");
        mergeSuccessNotice = new WebElement(DriverPool.getPage(),
                "xpath=//div[contains(@class,'ant-notification')]//*[normalize-space(text())='Merge Successful']",
                "mergeSuccessNotice");
        errorNotice = new WebElement(DriverPool.getPage(),
                "xpath=//div[contains(@class,'ant-notification-notice-error')] | //div[contains(@class,'ant-modal-container')]//div[contains(@class,'ant-alert-error')]",
                "mergeErrorNotice");
    }

    public BypassConfirmDialogComponent waitForDialogToAppear() {
        confirmBtn.waitForVisible();
        return this;
    }

    public boolean isVisible() {
        return confirmBtn.isVisible();
    }

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
        closeAllMessages();
        confirmBtn.click();
    }

    public void clickCancel() {
        LOGGER.info("Clicking 'Cancel' on bypass confirmation dialog");
        cancelBtn.click();
    }

    public String waitForMergeOutcome() {
        AtomicReference<String> outcome = new AtomicReference<>();
        WaitUtil.waitForCondition(() -> {
            if (mergeSuccessNotice.exists()) {
                outcome.set("");
                return true;
            }
            if (errorNotice.exists()) {
                outcome.set(errorNotice.getText().replaceAll("\\s+", " ").trim());
                return true;
            }
            return false;
        }, MERGE_TIMEOUT_MS, 250, "Waiting for the merge to finish with a success toast or an error");
        String result = outcome.get();
        if (result == null) {
            return "No merge outcome within " + MERGE_TIMEOUT_MS + " ms";
        }
        if (!result.isEmpty()) {
            LOGGER.warn("Merge reported an error: {}", result);
        }
        return result;
    }

    public boolean isMergeSuccessNoticeVisible() {
        return waitForMergeOutcome().isEmpty();
    }

    public boolean isMergeSuccessNoticeAbsent() {
        try {
            mergeSuccessNotice.waitForHidden(2_000);
            return true;
        } catch (RuntimeException e) {
            return !mergeSuccessNotice.isVisible(200);
        }
    }
}
