package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.DownloadUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;

/**
 * The "Export project" window of the React projects screen (Studio 6.4.0), opened from the project's Export
 * action. It offers the same choices as the old JSF dialog — which revision to export, or "Viewing" for the
 * current state — so it keeps the same method names.
 *
 * <p>Each revision entry reads "&lt;author&gt;: &lt;date&gt;", e.g. "Jane Doe: Jul 27, 2026 10:26 AM".
 */
public class ExportProjectModalComponent extends BaseComponent {

    private static final Logger LOGGER = LogManager.getLogger(ExportProjectModalComponent.class);

    private WebElement revisionSelect;
    private WebElement selectedRevisionLabel;
    private WebElement revisionOption;
    private WebElement exportBtn;
    private WebElement cancelBtn;
    private List<WebElement> revisionOptions;

    public ExportProjectModalComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public ExportProjectModalComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        revisionSelect = new WebElement(page, "[data-testid=export-project-revision]", "exportRevisionSelect");
        selectedRevisionLabel = new WebElement(page,
                "xpath=//*[@data-testid='export-project-revision']//span[contains(@class,'ant-select-selection-item')]",
                "exportSelectedRevision");
        revisionOption = new WebElement(page,
                "xpath=//div[contains(@class,'ant-select-item-option')][@title='%s']", "exportRevisionOption");
        revisionOptions = createElementList("xpath=//div[contains(@class,'ant-select-item-option')]", "exportRevisionOptions");
        exportBtn = new WebElement(page, "[data-testid=export-project-submit]", "exportSubmitBtn");
        cancelBtn = new WebElement(page,
                "xpath=//div[contains(@class,'ant-modal-footer')]//button[normalize-space()='Cancel']", "exportCancelBtn");
    }

    public void waitForDialogToAppear() {
        revisionSelect.waitForVisible(DEFAULT_TIMEOUT_MS);
        exportBtn.waitForVisible(DEFAULT_TIMEOUT_MS);
    }

    public boolean isDialogVisible() {
        return revisionSelect.isVisible(DEFAULT_TIMEOUT_MS / 5);
    }

    /** Every revision offered, including the "Viewing" entry for the project's current state. */
    public List<String> getAllRevisions() {
        revisionSelect.click();
        List<String> revisions = revisionOptions.stream()
                .map(option -> option.getLocator().getAttribute("title"))
                .filter(title -> title != null && !title.isBlank())
                .toList();
        page.keyboard().press("Escape");
        return revisions;
    }

    public String getSelectedRevision() {
        return selectedRevisionLabel.getText().trim();
    }

    public void selectRevision(String revision) {
        LOGGER.info("Selecting revision to export: {}", revision);
        revisionSelect.click();
        revisionOption.format(revision).click();
    }

    public void clickExport() {
        exportBtn.click();
    }

    public File clickExportAndDownload() {
        waitForDialogToAppear();
        File downloadedFile = DownloadUtil.downloadFile(exportBtn.getLocator());
        LOGGER.info("Downloaded file: {} ({} bytes)", downloadedFile.getName(), downloadedFile.length());
        return downloadedFile;
    }

    public void clickCancel() {
        cancelBtn.click();
    }
}
