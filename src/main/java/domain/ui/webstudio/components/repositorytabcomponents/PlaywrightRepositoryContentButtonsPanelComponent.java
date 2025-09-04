package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.CoreComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

public class PlaywrightRepositoryContentButtonsPanelComponent extends CoreComponent {

    private PlaywrightWebElement closeBtn;
    private PlaywrightWebElement saveBtn;
    private PlaywrightWebElement copyBtn;
    private PlaywrightWebElement deleteBtn;
    private PlaywrightWebElement deployBtn;
    private PlaywrightWebElement openBtn;
    private PlaywrightWebElement openRevisionBtn;
    private PlaywrightWebElement compareBtn;
    private PlaywrightWebElement addFolderBtn;
    private PlaywrightWebElement uploadFileBtn;
    private PlaywrightWebElement exportBtn;

    public PlaywrightRepositoryContentButtonsPanelComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightRepositoryContentButtonsPanelComponent(PlaywrightWebElement rootLocator) {
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