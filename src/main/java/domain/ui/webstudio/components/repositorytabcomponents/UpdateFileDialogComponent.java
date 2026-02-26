package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

public class UpdateFileDialogComponent extends BaseComponent {

    private WebElement fileInput;
    private WebElement updateButton;
    private WebElement cancelButton;
    private WebElement fileChangedOkBtn;

    public UpdateFileDialogComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public UpdateFileDialogComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        fileInput = createScopedElement("xpath=.//div[@id='updateFileForm:file']//input[@type='file']", "fileInput");
        updateButton = createScopedElement("xpath=.//footer/input[@value='Update']", "updateButton");
        cancelButton = createScopedElement("xpath=.//footer/input[@value='Cancel']", "cancelButton");
        fileChangedOkBtn = new WebElement(page, "xpath=//div[@id='fileChanged_container']//input[@value='OK']", "fileChangedOkBtn");
    }

    public UpdateFileDialogComponent updateFile(String filePath) {
        fileInput.setInputFiles(filePath);
        WaitUtil.sleep(1000, "Waiting for file to be selected");
        return this;
    }

    public boolean isFileChangedWarningVisible() {
        return fileChangedOkBtn.isVisible(500);
    }

    public UpdateFileDialogComponent clickFileChangedOk() {
        fileChangedOkBtn.click();
        return this;
    }

    public void clickUpdateButton() {
        updateButton.click();
        waitForDialogToClose();
    }

    public void clickCancelButton() {
        cancelButton.click();
    }

    public boolean isDialogVisible() {
        try {
            return fileInput.isVisible(1000);
        } catch (Exception e) {
            return false;
        }
    }

    public void waitForDialogToAppear() {
        WaitUtil.waitForCondition(this::isDialogVisible, 5000, 100, "Waiting for Update File dialog to appear");
    }

    public void waitForDialogToClose() {
        WaitUtil.waitForCondition(() -> !isDialogVisible(), 5000, 100, "Waiting for Update File dialog to close");
    }
}

