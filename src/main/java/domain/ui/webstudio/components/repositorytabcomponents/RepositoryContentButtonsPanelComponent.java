package domain.ui.webstudio.components.repositorytabcomponents;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;

public class RepositoryContentButtonsPanelComponent extends BaseComponent {

    private WebElement closeBtn;
    private WebElement saveBtn;
    private WebElement copyBtn;
    private WebElement deleteBtn;
    private WebElement deployBtn;
    private WebElement openBtn;
    private WebElement openRevisionBtn;
    private WebElement compareBtn;
    private WebElement addFolderBtn;
    private WebElement uploadFileBtn;
    private WebElement exportBtn;

    public RepositoryContentButtonsPanelComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public RepositoryContentButtonsPanelComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        closeBtn = createScopedElement("xpath=.//input[@value='Close']", "closeBtn");
        saveBtn = createScopedElement("xpath=.//input[@value='Save']", "saveBtn");
        copyBtn = createScopedElement("xpath=.//input[@value='Copy']", "copyBtn");
        deleteBtn = createScopedElement("xpath=.//input[@value='Delete']", "deleteBtn");
        deployBtn = createScopedElement("xpath=.//input[@value='Deploy']", "deployBtn");
        openBtn = createScopedElement("xpath=.//input[@value='Open']", "openBtn");
        openRevisionBtn = createScopedElement("xpath=.//input[@value='Open Revision']", "openRevisionBtn");
        compareBtn = createScopedElement("xpath=.//input[@value='Compare']", "compareBtn");
        addFolderBtn = createScopedElement("xpath=.//input[@value='Add Folder']", "addFolderBtn");
        uploadFileBtn = createScopedElement("xpath=.//input[@value='Upload File']", "uploadFileBtn");
        exportBtn = createScopedElement("xpath=.//input[@value='Export']", "exportBtn");
    }

    // Legacy methods for compatibility
    public void openProject() {
        openBtn.click();
    }

    public void saveDeploy() {
        saveBtn.click();
    }

    public boolean isDeployButtonEnabled() {
        return deployBtn.isEnabled();
    }

    public void clickDeploy() {
        deployBtn.click();
    }

    public void clickCopyBtn() {
        copyBtn.click();
    }

    public void clickUploadFileBtn() {
        uploadFileBtn.click();
    }

    public void clickSaveBtn() {
        saveBtn.click();
    }

    public void clickDeleteBtn() {
        deleteBtn.click();
    }

    public void clickEraseBtn() {
        deleteBtn.click();
    }

    public void clickAddFolderBtn() {
        addFolderBtn.click();
    }

    public void clickExportBtn() {
        exportBtn.click();
    }
}