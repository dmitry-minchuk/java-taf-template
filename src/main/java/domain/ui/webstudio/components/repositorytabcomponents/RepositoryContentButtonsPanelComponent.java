package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.CoreComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;

public class RepositoryContentButtonsPanelComponent extends CoreComponent {

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

    public void clickClose() {
        closeBtn.click();
    }

    public void clickSave() {
        saveBtn.click();
    }

    public void clickCopy() {
        copyBtn.click();
    }

    public void clickDelete() {
        deleteBtn.click();
    }

    public void clickDeploy() {
        deployBtn.click();
    }

    public void clickOpen() {
        openBtn.click();
    }

    public void clickOpenRevision() {
        openRevisionBtn.click();
    }

    public void clickCompare() {
        compareBtn.click();
    }

    public void clickAddFolder() {
        addFolderBtn.click();
    }

    public void clickUploadFile() {
        uploadFileBtn.click();
    }

    public void clickExport() {
        exportBtn.click();
    }

    // Legacy methods for compatibility
    public void openProject() {
        clickOpen();
    }

    public void saveDeploy() {
        clickSave();
    }

    public boolean isDeployButtonEnabled() {
        return deployBtn.isEnabled();
    }
}