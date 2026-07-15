package domain.ui.webstudio.pages.mainpages;

import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.pages.BasePage;
import lombok.Getter;

// React project-detail view (/projects/<id>) introduced in build 032c60a664ce: Overview / Files /
// History / Branches / Publish tabs. Reached from the projects list via RepositoryPage.openProjectDetail.
public class ProjectDetailPage extends BasePage {

    // Top navigation (shared React shell)
    @Getter
    private TabSwitcherComponent tabSwitcherComponent;
    // Tabs
    private WebElement overviewTab;
    private WebElement filesTab;
    private WebElement historyTab;
    // Header
    private WebElement projectStatus;       // React status: Local / Opened / Editing / Closed / ...
    // Files tab: a file tree on the left, a preview panel with per-file actions on the right
    private WebElement fileNodeByName;      // format(fileName)
    private WebElement fileDeleteBtn;
    private WebElement fileDeleteConfirmOk;
    private WebElement filesUploadInput;
    // History tab
    private WebElement revisionEntries;     // one per revision (revision-comment-<hash>)

    public ProjectDetailPage() {
        super();
        initializeComponents();
    }

    private void initializeComponents() {
        tabSwitcherComponent = createScopedComponent(TabSwitcherComponent.class, "xpath=//ul[@role='menu' and contains(@class,'ant-menu-horizontal')]", "tabSwitcherComponent");
        overviewTab = new WebElement(page, "xpath=//div[@data-node-key='overview']", "overviewTab");
        filesTab = new WebElement(page, "xpath=//div[@data-node-key='files']", "filesTab");
        historyTab = new WebElement(page, "xpath=//div[@data-node-key='history']", "historyTab");
        projectStatus = new WebElement(page, "[data-testid^=\"status-\"]", "projectStatus");
        revisionEntries = new WebElement(page, "xpath=//*[starts-with(@data-testid,'revision-comment-')]", "revisionEntries");
        fileNodeByName = new WebElement(page, "xpath=//div[@role='treeitem'][.//*[normalize-space()='%s']]", "fileTreeNode");
        fileDeleteBtn = new WebElement(page, "[data-testid=file-delete]", "fileDeleteBtn");
        fileDeleteConfirmOk = new WebElement(page, "xpath=//div[contains(@class,'ant-popover')]//button[.//span[normalize-space()='OK']]", "fileDeleteConfirmOk");
        filesUploadInput = new WebElement(page, "[data-testid=files-upload-input]", "filesUploadInput");
    }

    public ProjectDetailPage openOverviewTab() {
        overviewTab.click();
        waitUntilSpinnerLoaded();
        return this;
    }

    public ProjectDetailPage openFilesTab() {
        filesTab.click();
        waitUntilSpinnerLoaded();
        return this;
    }

    public ProjectDetailPage openHistoryTab() {
        historyTab.click();
        waitUntilSpinnerLoaded();
        return this;
    }

    // React status vocabulary (Local / Opened / Editing / Closed / ...) — differs from the legacy
    // "No Changes" / "In Editing" wording, so migrated tests must assert against the React values.
    public String getStatus() {
        return projectStatus.getText().trim();
    }

    public int getRevisionsCount() {
        openHistoryTab();
        return revisionEntries.getLocator().count();
    }

    // Selecting a file reveals the preview panel with its delete action; deletion is confirmed via a popconfirm.
    public ProjectDetailPage deleteFile(String fileName) {
        fileNodeByName.format(fileName).click();
        fileDeleteBtn.click();
        fileDeleteConfirmOk.click();
        waitUntilSpinnerLoaded();
        return this;
    }

    public boolean isFilePresent(String fileName) {
        return fileNodeByName.format(fileName).isVisible(DEFAULT_TIMEOUT_MS);
    }

    // Uploads a file into the project via the Files tab's upload input (adds/overwrites the module).
    public ProjectDetailPage uploadFile(String filePath) {
        openFilesTab();
        filesUploadInput.setInputFiles(filePath);
        waitUntilSpinnerLoaded();
        return this;
    }

    public ProjectDetailPage reloadPage() {
        page.reload();
        waitUntilSpinnerLoaded();
        return this;
    }
}
