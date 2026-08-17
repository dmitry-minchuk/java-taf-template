package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.DriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.DownloadUtil;
import helpers.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;

public class ExportProjectModalComponent extends BaseComponent {

    private static final Logger LOGGER = LogManager.getLogger(ExportProjectModalComponent.class);

    private static final String MODAL =
            "//div[contains(@class,'ant-modal')][.//*[@data-testid='export-project-revision']]";
    private static final String OPEN_DROPDOWN = "css=.ant-select-dropdown:not(.ant-select-dropdown-hidden)";

    private WebElement revisionSelect;
    private WebElement selectedRevisionLabel;
    private WebElement revisionOption;
    private WebElement exportBtn;
    private WebElement cancelBtn;
    private WebElement openDropdown;
    private List<WebElement> revisionOptions;

    public ExportProjectModalComponent() {
        super(DriverPool.getPage());
        initializeElements();
    }

    public ExportProjectModalComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        revisionSelect = new WebElement(page, "[data-testid=export-project-revision]", "exportRevisionSelect");
        selectedRevisionLabel = new WebElement(page,
                "css=[data-testid=export-project-revision] .ant-select-content", "exportSelectedRevision");
        revisionOption = new WebElement(page,
                "xpath=//div[contains(@class,'ant-select-item-option')][@title='%s']", "exportRevisionOption");
        revisionOptions = createElementList(
                "xpath=//div[contains(@class,'ant-select-dropdown') and not(contains(@class,'ant-select-dropdown-hidden'))]"
                        + "//div[contains(@class,'ant-select-item-option')]",
                "exportRevisionOptions");
        exportBtn = new WebElement(page, "[data-testid=export-project-submit]", "exportSubmitBtn");
        cancelBtn = new WebElement(page,
                "xpath=" + MODAL + "//div[contains(@class,'ant-modal-footer')]//button[normalize-space()='Cancel']",
                "exportCancelBtn");
        openDropdown = new WebElement(page, OPEN_DROPDOWN, "exportRevisionDropdown");
    }

    public void waitForDialogToAppear() {
        revisionSelect.waitForVisible(DEFAULT_TIMEOUT_MS);
        exportBtn.waitForVisible(DEFAULT_TIMEOUT_MS);
    }

    public boolean isDialogVisible() {
        return revisionSelect.isVisible(DEFAULT_TIMEOUT_MS / 5);
    }

    public List<String> getAllRevisions() {
        revisionSelect.click();
        List<String> revisions = revisionOptions.stream()
                .map(option -> option.getLocator().getAttribute("title"))
                .filter(title -> title != null && !title.isBlank())
                .toList();
        closeRevisionDropdown();
        return revisions;
    }

    private void closeRevisionDropdown() {
        if (!openDropdown.exists()) {
            return;
        }
        page.keyboard().press("Escape");
        if (dropdownClosed()) {
            return;
        }
        LOGGER.warn("Escape left a select dropdown open; clicking the revision select to close it");
        revisionSelect.click();
        if (!dropdownClosed()) {
            LOGGER.warn("A select dropdown is still open and may cover the dialog buttons");
        }
    }

    private boolean dropdownClosed() {
        return WaitUtil.waitForCondition(() -> !openDropdown.exists(), DEFAULT_TIMEOUT_MS / 2, 200,
                "Waiting for the revision list to close");
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
        closeRevisionDropdown();
        cancelBtn.click();
        revisionSelect.waitForHidden(DEFAULT_TIMEOUT_MS);
    }
}
