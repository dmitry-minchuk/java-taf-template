package domain.ui.webstudio.components.common;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

public class SyncChangesDialogComponent extends BaseComponent {

    private static final Logger LOGGER = LogManager.getLogger(SyncChangesDialogComponent.class);

    private WebElement dialogHeader;
    private WebElement importTheirChangesBtn;
    private WebElement exportYourChangesBtn;
    private WebElement cannotImportMessage;
    private WebElement cannotExportMessage;
    private WebElement cancelBtn;
    private WebElement branchSelector;
    private List<WebElement> selectorOptions;

    public SyncChangesDialogComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public SyncChangesDialogComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        dialogHeader = createScopedElement("xpath=.//div[@class='ant-modal-header']", "dialogHeader");
        importTheirChangesBtn = createScopedElement("xpath=.//button[.//span[text()='Receive their updates']]", "importTheirChangesBtn");
        exportYourChangesBtn = createScopedElement("xpath=.//button[.//span[text()='Send your updates']]", "exportYourChangesBtn");
        cannotImportMessage = createScopedElement("xpath=.//div[@id='mergeBranchesForm:cannotImportMessage']", "cannotImportMessage");
        cannotExportMessage = createScopedElement("xpath=.//div[@id='mergeBranchesForm:cannotExportMessage']", "cannotExportMessage");
        cancelBtn = createScopedElement("xpath=.//span[@aria-label='close']", "cancelBtn");

        branchSelector = createScopedElement("xpath=.//input[@id='merge_branches_form_targetBranch']", "branchSelector");
        selectorOptions = createElementList("xpath=.//div[@class='ant-select-item-option-content']", "branchSelector");
    }

    public void waitForDialogToAppear() {
        WaitUtil.waitForCondition(() -> dialogHeader.isVisible(), 5000, 100, "Waiting for Sync dialog to appear");
    }

    public SyncChangesDialogComponent clickImportTheirChanges() {
        LOGGER.info("Clicking 'Receive their updates' button");
        importTheirChangesBtn.click();
        WaitUtil.sleep(2000, "Waiting for import operation to complete");
        return this;
    }

    public SyncChangesDialogComponent clickExportYourChanges() {
        LOGGER.info("Clicking 'Send your updates' button");
        exportYourChangesBtn.click();
        WaitUtil.sleep(2000, "Waiting for export operation to complete");
        return this;
    }

    public void clickCancel() {
        LOGGER.info("Clicking Cancel button");
        cancelBtn.click();
    }

    public boolean isImportButtonEnabled() {
        return importTheirChangesBtn.isEnabled();
    }

    public boolean isExportButtonEnabled() {
        return exportYourChangesBtn.isEnabled();
    }

    public String getCannotImportMessage() {
        return WaitUtil.waitForResult(() -> {
            if (cannotImportMessage.isVisible())
                return Optional.of(cannotImportMessage.getText().trim());
            return Optional.empty();
        }, 2000, 100, "Waiting for cannot import message element").orElse("");
    }

    public String getCannotExportMessage() {
        return WaitUtil.waitForResult(() -> {
            if (cannotExportMessage.isVisible())
                return Optional.of(cannotExportMessage.getText().trim());
            return Optional.empty();
        }, 2000, 100, "Waiting for cannot export message element").orElse("");
    }

    public SyncChangesDialogComponent selectBranch(String branchName) {
        LOGGER.info("Selecting branch: {}", branchName);
        branchSelector.click();  // Click on parent div, safer than input
        WaitUtil.waitForCondition(() -> !selectorOptions.isEmpty(), 2000, 100, "Waiting for selector options to appear");
        for (WebElement option : selectorOptions) {
            if (option.getText().trim().equals(branchName)) {
                option.click();
                return this;
            }
        }
        throw new RuntimeException("Branch '" + branchName + "' not found in selector options");
    }


    public String getDialogHeader() {
        return dialogHeader.getText().trim();
    }

    public String getImportButtonTitle() {
        return importTheirChangesBtn.getAttribute("title");
    }

    public String getExportButtonTitle() {
        return exportYourChangesBtn.getAttribute("title");
    }
}
