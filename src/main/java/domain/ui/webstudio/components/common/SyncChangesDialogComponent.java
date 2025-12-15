package domain.ui.webstudio.components.common;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

public class SyncChangesDialogComponent extends BaseComponent {

    private static final Logger LOGGER = LogManager.getLogger(SyncChangesDialogComponent.class);

    private WebElement dialogHeader;
    private WebElement importTheirChangesBtn;
    private WebElement exportYourChangesBtn;
    private WebElement cannotImportMessage;
    private WebElement cannotExportMessage;
    private WebElement cancelBtn;

    public SyncChangesDialogComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public SyncChangesDialogComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        dialogHeader = createScopedElement("xpath=.//div[@id='modalMergeBranches_header_content']", "dialogHeader");
        importTheirChangesBtn = createScopedElement("xpath=.//input[@value='Receive their updates']", "importTheirChangesBtn");
        exportYourChangesBtn = createScopedElement("xpath=.//input[@value='Send your updates']", "exportYourChangesBtn");
        cannotImportMessage = createScopedElement("xpath=.//div[@id='mergeBranchesForm:cannotImportMessage']", "cannotImportMessage");
        cannotExportMessage = createScopedElement("xpath=.//div[@id='mergeBranchesForm:cannotExportMessage']", "cannotExportMessage");
        cancelBtn = createScopedElement("xpath=.//input[@value='Cancel']", "cancelBtn");
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
