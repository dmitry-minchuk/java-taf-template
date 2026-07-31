package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.WebElement;
import helpers.utils.WaitUtil;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.DownloadUtil;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;

@Getter
public class ExportProjectDialogComponent extends BaseComponent {

    private static final Logger LOGGER = LogManager.getLogger(ExportProjectDialogComponent.class);

    private WebElement revisionDropdown;
    private WebElement exportBtn;
    private WebElement cancelBtn;

    public ExportProjectDialogComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public ExportProjectDialogComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        revisionDropdown = createScopedElement("xpath=.//select[@id='exportProjectForm:projectVersionToExport']", "revisionDropdown");
        exportBtn = createScopedElement("xpath=.//input[@value='Export']", "exportBtn");
        cancelBtn = createScopedElement("xpath=.//input[@value='Cancel']", "cancelBtn");
    }

    public void waitForDialogToAppear() {
        revisionDropdown.waitForVisible(5000);
        waitUntilSpinnerLoaded();
        exportBtn.waitForVisible(5000);
        // The popup is autosized: it is attached and "visible" while the ajax response that fills it is still on
        // its way, and measures zero until then - a click landing in that window hits nothing.
        WaitUtil.waitForCondition(exportBtn::hasSize, DEFAULT_TIMEOUT_MS, 200,
                "Waiting for the export dialog to be laid out");
    }

    public boolean isDialogVisible() {
        return revisionDropdown.isVisible(2000);
    }

    public List<String> getAllRevisions() {
        return revisionDropdown.getSelectVisibleTextValues();
    }

    public String getSelectedRevision() {
        return revisionDropdown.getLocator().inputValue();
    }

    public void selectRevision(String revision) {
        revisionDropdown.selectByVisibleText(revision);
    }

    public void clickExport() {
        waitUntilSpinnerLoaded();
        exportBtn.waitForVisible(10000);
        exportBtn.click();
    }

    public File clickExportAndDownload() {
        waitForDialogToAppear();
        File downloadedFile = DownloadUtil.downloadFile(exportBtn.getLocator());
        LOGGER.info("Downloaded file: {} (size: {} bytes)", downloadedFile.getName(), downloadedFile.length());
        return downloadedFile;
    }

    public void clickCancel() {
        cancelBtn.click();
    }
}
