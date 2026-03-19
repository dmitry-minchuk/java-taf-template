package domain.ui.webstudio.components.repositorytabcomponents;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.WaitUtil;

import java.util.ArrayList;
import java.util.List;

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
    private WebElement updateFileBtn;
    private WebElement exportBtn;
    private WebElement syncBtn;
    private WebElement undeleteBtn;
    private WebElement eraseBtn;
    private WebElement allButtonsLocator;

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
        updateFileBtn = createScopedElement("xpath=.//input[@value='Update file']", "updateFileBtn");
        exportBtn = createScopedElement("xpath=.//input[@value='Export']", "exportBtn");
        syncBtn = createScopedElement("xpath=.//input[@value='Sync']", "syncBtn");
        undeleteBtn = createScopedElement("xpath=.//input[@value='Undelete']", "undeleteBtn");
        eraseBtn = createScopedElement("xpath=.//input[@value='Erase']", "eraseBtn");
        allButtonsLocator = createScopedElement("xpath=.//input[@type='button' or @type='submit']", "allButtonsLocator");
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

    public void clickUpdateFileBtn() {
        updateFileBtn.click();
    }

    public void clickSaveBtn() {
        saveBtn.click();
    }

    public void clickDeleteBtn() {
        deleteBtn.click();
        WaitUtil.sleep(1000, "Waiting for confirmation dialog to open");
    }

    public void clickEraseBtn() {
        eraseBtn.click();
        WaitUtil.sleep(500, "Waiting for erase confirmation dialog to open");
    }

    public void clickAddFolderBtn() {
        addFolderBtn.click();
    }

    public void clickExportBtn() {
        exportBtn.click();
    }

    public void clickSync() {
        syncBtn.click();
        WaitUtil.sleep(500, "Waiting for Sync dialog to open");
    }

    public boolean isSyncButtonVisible() {
        return syncBtn.isVisible(1000);
    }

    public String getSyncButtonTitle() {
        return syncBtn.getAttribute("title");
    }

    public CompareGitRevisionsDialogComponent clickCompareBtn() {
        Page comparePopup = page.waitForPopup(() -> {
            compareBtn.click();
        });
        comparePopup.waitForLoadState();
        return new CompareGitRevisionsDialogComponent(comparePopup);
    }

    public void clickCloseBtn() {
        closeBtn.click();
        WaitUtil.sleep(500, "Waiting after clicking Close button");
    }

    public boolean isSaveBtnVisible() {
        return saveBtn.isVisible();
    }

    public boolean isCopyBtnVisible() {
        return copyBtn.isVisible();
    }

    public boolean isCopyBtnVisible(int millis) {
        return copyBtn.isVisible(millis);
    }

    public boolean isDeleteBtnVisible() {
        return deleteBtn.isVisible();
    }

    public boolean isDeployBtnVisible() {
        return deployBtn.isVisible();
    }

    public boolean isDeployBtnVisible(int millis) {
        return deployBtn.isVisible(millis);
    }

    public boolean isAddFolderBtnVisible() {
        return addFolderBtn.isVisible();
    }

    public boolean isUploadFileBtnVisible() {
        return uploadFileBtn.isVisible();
    }

    public boolean isExportBtnVisible() {
        return exportBtn.isVisible();
    }

    public void clickUndeleteBtn() {
        undeleteBtn.click();
        WaitUtil.sleep(500, "Waiting after clicking Undelete button");
    }

    public boolean isUndeleteBtnVisible() {
        return undeleteBtn.isVisible();
    }

    public boolean isOpenBtnVisible() {
        return openBtn.isVisible();
    }

    public boolean isCloseBtnVisible() {
        return closeBtn.isVisible();
    }

    public List<String> getAllVisibleButtonValues() {
        List<String> values = new ArrayList<>();
        Locator allButtons = allButtonsLocator.getLocator();
        for (int i = 0; i < allButtons.count(); i++) {
            Locator btn = allButtons.nth(i);
            if (btn.isVisible()) {
                String val = btn.getAttribute("value");
                if (val != null && !val.isEmpty()) {
                    values.add(val);
                }
            }
        }
        return values;
    }
}