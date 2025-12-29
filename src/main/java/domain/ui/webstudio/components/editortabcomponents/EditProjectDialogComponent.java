package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

public class EditProjectDialogComponent extends BaseComponent {

    private WebElement descriptionField;
    private WebElement updateBtn;
    private WebElement cancelBtn;

    public EditProjectDialogComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public EditProjectDialogComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        descriptionField = createScopedElement("xpath=//textarea[@id='projectDescription']", "descriptionField");
        updateBtn = createScopedElement("xpath=//input[@value='Update']", "updateBtn");
        cancelBtn = createScopedElement("xpath=//input[@value='Cancel']", "cancelBtn");
    }

    public EditProjectDialogComponent setDescription(String description) {
        descriptionField.fillSequentially(description);
        return this;
    }

    public String getDescription() {
        return descriptionField.getAttribute("value");
    }

    public void clickUpdateButton() {
        updateBtn.click();
        waitForDialogToClose();
    }

    public void clickCancelButton() {
        cancelBtn.click();
    }

    public boolean isDialogVisible() {
        return descriptionField.isVisible(1000);
    }

    public void waitForDialogToAppear() {
        WaitUtil.waitForCondition(this::isDialogVisible, 5000, 100, "Waiting for Edit Project dialog to appear");
    }

    public void waitForDialogToClose() {
        WaitUtil.waitForCondition(() -> !isDialogVisible(), 5000, 100, "Waiting for Edit Project dialog to close");
    }
}
