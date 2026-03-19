package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

public class AddFolderDialogComponent extends BaseComponent {

    private WebElement folderNameInput;
    private WebElement addBtn;
    private WebElement cancelBtn;

    public AddFolderDialogComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public AddFolderDialogComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        folderNameInput = createScopedElement("xpath=//form[@id='newFolderForm']//input[@id='newFolderForm:folderName']", "folderNameInput");
        addBtn = createScopedElement("xpath=//form[@id='newFolderForm']//input[@id='newFolderForm:addButton']", "addBtn");
        cancelBtn = createScopedElement("xpath=//form[@id='newFolderForm']//input[@value='Cancel']", "cancelBtn");
    }

    public AddFolderDialogComponent setFolderName(String name) {
        folderNameInput.fillSequentially(name);
        WaitUtil.sleep(300, "Waiting for folder name validation");
        return this;
    }

    public void clickAdd() {
        addBtn.click();
        WaitUtil.sleep(500, "Waiting for folder creation");
    }

    public void clickCancel() {
        cancelBtn.click();
    }
}
