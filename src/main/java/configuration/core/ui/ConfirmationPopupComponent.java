package configuration.core.ui;

import com.microsoft.playwright.Page;
import org.openqa.selenium.support.FindBy;


public class ConfirmationPopupComponent {

    // Main Confirmation Dialog Elements
    @FindBy(xpath = "//div[contains(@class,'ant-modal-confirm-confirm')]//button[./span[text()='OK']]")
    private SmartWebElement confirmOkBtn;

    @FindBy(xpath = "//div[contains(@class,'ant-modal-confirm-confirm')]//button[./span[text()='Cancel']]")
    private SmartWebElement confirmCancelBtn;

    // Alternative confirmation dialog patterns (for different modal types)
    @FindBy(xpath = "//div[contains(@class,'ant-modal-confirm')]//button[./span[text()='OK'] or ./span[text()='Yes'] or ./span[text()='Confirm']]")
    private SmartWebElement confirmBtn;

    @FindBy(xpath = "//div[contains(@class,'ant-modal-confirm')]//button[./span[text()='Cancel'] or ./span[text()='No']]")
    private SmartWebElement cancelBtn;

    // Generic confirmation dialog detection
    @FindBy(xpath = "//div[contains(@class,'ant-modal-confirm')]")
    private SmartWebElement confirmationDialog;

    // Dialog content elements
    @FindBy(xpath = "//div[contains(@class,'ant-modal-confirm-title')]")
    private SmartWebElement confirmationTitle;

    @FindBy(xpath = "//div[contains(@class,'ant-modal-confirm-content')]")
    private SmartWebElement confirmationMessage;

    // Warning and error specific dialogs
    @FindBy(xpath = "//div[contains(@class,'ant-modal-confirm-warning')]")
    private SmartWebElement warningDialog;

    @FindBy(xpath = "//div[contains(@class,'ant-modal-confirm-error')]")
    private SmartWebElement errorDialog;

    @FindBy(xpath = "//div[contains(@class,'ant-modal-confirm-info')]")
    private SmartWebElement infoDialog;

    private Page page; // For Playwright mode
    
    public ConfirmationPopupComponent() {
        // No longer extends BasePageComponent - no super() call needed
    }
    
    /**
     * PLAYWRIGHT MIGRATION: Initialize with Playwright page
     */
    public void initPlaywright(Page page, String rootSelector) {
        this.page = page;
        // Note: rootSelector ignored for global confirmation popup
        // Components will be initialized by PlaywrightPageFactory separately
    }

    // Core Confirmation Methods
    
    public void confirm() {
        if (confirmOkBtn.isDisplayed(2)) {
            confirmOkBtn.click();
        } else if (confirmBtn.isDisplayed(2)) {
            confirmBtn.click();
        }
        // PLAYWRIGHT MIGRATION: Removed WaitUtil.sleep() - waitForDialogToDisappear() provides proper waiting
        waitForDialogToDisappear();
    }

    
    public void cancel() {
        if (confirmCancelBtn.isDisplayed(2)) {
            confirmCancelBtn.click();
        } else if (cancelBtn.isDisplayed(2)) {
            cancelBtn.click();
        }
        // PLAYWRIGHT MIGRATION: Removed WaitUtil.sleep() - waitForDialogToDisappear() provides proper waiting
        waitForDialogToDisappear();
    }

    
    public void accept() {
        confirm();
    }

    
    public void dismiss() {
        cancel();
    }

    // Dialog State Methods
    
    public boolean isDisplayed() {
        return confirmationDialog.isDisplayed(3);
    }

    
    public boolean isDisplayed(int timeoutSeconds) {
        return confirmationDialog.isDisplayed(timeoutSeconds);
    }

    
    public boolean waitForDialog(int timeoutSeconds) {
        return confirmationDialog.isDisplayed(timeoutSeconds);
    }

    
    public boolean waitForDialogToDisappear(int timeoutSeconds) {
        return !confirmationDialog.isDisplayed(timeoutSeconds);
    }

    
    public void waitForDialogToDisappear() {
        waitForDialogToDisappear(5);
    }

    // Dialog Content Methods
    
    public String getTitle() {
        if (confirmationTitle.isDisplayed(2)) {
            return confirmationTitle.getText();
        }
        return "";
    }

    
    public String getMessage() {
        if (confirmationMessage.isDisplayed(2)) {
            return confirmationMessage.getText();
        }
        return "";
    }

    
    public String getDialogContent() {
        String title = getTitle();
        String message = getMessage();
        if (!title.isEmpty() && !message.isEmpty()) {
            return title + ": " + message;
        } else if (!title.isEmpty()) {
            return title;
        } else {
            return message;
        }
    }

    // Dialog Type Detection Methods
    
    public boolean isWarningDialog() {
        return warningDialog.isDisplayed(2);
    }

    
    public boolean isErrorDialog() {
        return errorDialog.isDisplayed(2);
    }

    
    public boolean isInfoDialog() {
        return infoDialog.isDisplayed(2);
    }

    
    public String getDialogType() {
        if (isWarningDialog()) return "warning";
        if (isErrorDialog()) return "error";
        if (isInfoDialog()) return "info";
        return "confirm";
    }

    // Button State Methods
    
    public boolean isConfirmButtonAvailable() {
        return confirmOkBtn.isDisplayed(2) || confirmBtn.isDisplayed(2);
    }

    
    public boolean isCancelButtonAvailable() {
        return confirmCancelBtn.isDisplayed(2) || cancelBtn.isDisplayed(2);
    }

    
    public boolean isConfirmButtonEnabled() {
        if (confirmOkBtn.isDisplayed(2)) {
            return confirmOkBtn.isEnabled();
        } else if (confirmBtn.isDisplayed(2)) {
            return confirmBtn.isEnabled();
        }
        return false;
    }

    // Utility Methods
    
    public void confirmIfDisplayed() {
        if (isDisplayed(2)) {
            confirm();
        }
    }

    
    public void cancelIfDisplayed() {
        if (isDisplayed(2)) {
            cancel();
        }
    }

    
    public boolean waitAndConfirm(int timeoutSeconds) {
        if (waitForDialog(timeoutSeconds)) {
            confirm();
            return true;
        }
        return false;
    }

    
    public boolean waitAndCancel(int timeoutSeconds) {
        if (waitForDialog(timeoutSeconds)) {
            cancel();
            return true;
        }
        return false;
    }

    // Validation Methods
    
    public boolean validateDialogText(String expectedText) {
        String dialogContent = getDialogContent().toLowerCase();
        return dialogContent.contains(expectedText.toLowerCase());
    }

    
    public boolean validateDialogType(String expectedType) {
        return expectedType.equalsIgnoreCase(getDialogType());
    }

    
    public String getDialogInfo() {
        if (!isDisplayed()) {
            return "No confirmation dialog is displayed";
        }
        
        return String.format("Confirmation Dialog - Type: %s | Title: %s | Message: %s | Confirm Available: %s | Cancel Available: %s",
                getDialogType(),
                getTitle(),
                getMessage(),
                isConfirmButtonAvailable(),
                isCancelButtonAvailable());
    }
}