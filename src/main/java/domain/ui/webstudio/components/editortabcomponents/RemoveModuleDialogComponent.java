package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

public class RemoveModuleDialogComponent extends BaseComponent {

    private WebElement leaveExcelFileCheckbox;
    private WebElement removeButton;
    private WebElement cancelButton;

    public RemoveModuleDialogComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public RemoveModuleDialogComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        leaveExcelFileCheckbox = createScopedElement("xpath=.//input[@id='leaveExcelFile']", "leaveExcelFileCheckbox");
        removeButton = createScopedElement("xpath=.//footer/input[@value='Remove']", "removeButton");
        cancelButton = createScopedElement("xpath=.//footer/input[@value='Cancel']", "cancelButton");
    }

    public void setLeaveFile(boolean leave) {
        if (leave) {
            leaveExcelFileCheckbox.check();
        } else {
            leaveExcelFileCheckbox.uncheck();
        }
    }

    public void clickRemove() {
        removeButton.click();
        WaitUtil.waitForCondition(() -> !removeButton.isVisible(500), 5000, 100, "Waiting for Remove Module dialog to close");
    }

    public void clickCancel() {
        cancelButton.click();
    }

    public boolean isDialogVisible() {
        return removeButton.isVisible(1000);
    }
}
