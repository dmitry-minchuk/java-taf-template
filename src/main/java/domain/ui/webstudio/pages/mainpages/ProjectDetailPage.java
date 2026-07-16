package domain.ui.webstudio.pages.mainpages;

import com.microsoft.playwright.Locator;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.repositorytabcomponents.SyncUpdatesDialogComponent;
import domain.ui.webstudio.pages.BasePage;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

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
    private WebElement branchesTab;
    // Branches tab (only interactive when the project is OPEN): create/merge/delete per branch row
    private WebElement branchesCurrentLabel;    // the current-branch name
    private WebElement branchesCreateBtn;
    private WebElement branchNewNameField;
    private WebElement branchCreateSubmitBtn;
    private WebElement branchSwitchAfterToggle;  // ant-switch "Switch to the new branch" (role=switch)
    private WebElement branchRowByName;          // format(name) → branch-commit-<name> (row presence)
    private WebElement branchMergeByName;        // format(name) → branch-merge-<name>
    private SyncUpdatesDialogComponent syncUpdatesDialogComponent;
    // Header
    private WebElement projectStatus;       // React status: Local / Opened / Editing / Closed / ...
    // Files tab: a file tree on the left, a preview panel with per-file actions on the right
    private WebElement fileNodeByName;      // format(fileName)
    private WebElement fileDeleteBtn;
    private WebElement fileDeleteConfirmOk;
    private WebElement filesUploadInput;
    private WebElement filesNewFolderBtn;
    private WebElement folderPathInput;
    private WebElement folderSubmitBtn;
    // History tab
    private WebElement revisionEntries;     // one per revision (revision-comment-<hash>)
    // Overview tab: assigned tags render as ant-tags "<type> → <value>" in the TAGS section
    private WebElement tagValueForType;     // format(tagType) → the value span

    public ProjectDetailPage() {
        super();
        initializeComponents();
    }

    private void initializeComponents() {
        tabSwitcherComponent = createScopedComponent(TabSwitcherComponent.class, "xpath=//ul[@role='menu' and contains(@class,'ant-menu-horizontal')]", "tabSwitcherComponent");
        overviewTab = new WebElement(page, "xpath=//div[@data-node-key='overview']", "overviewTab");
        filesTab = new WebElement(page, "xpath=//div[@data-node-key='files']", "filesTab");
        historyTab = new WebElement(page, "xpath=//div[@data-node-key='history']", "historyTab");
        branchesTab = new WebElement(page, "xpath=//div[@data-node-key='branches']", "branchesTab");
        branchesCurrentLabel = new WebElement(page, "[data-testid=branches-current]", "branchesCurrentLabel");
        branchesCreateBtn = new WebElement(page, "[data-testid=branches-create]", "branchesCreateBtn");
        branchNewNameField = new WebElement(page, "[data-testid=branches-new-name]", "branchNewNameField");
        branchCreateSubmitBtn = new WebElement(page, "[data-testid=branches-create-submit]", "branchCreateSubmitBtn");
        branchSwitchAfterToggle = new WebElement(page, "[data-testid=branches-switch-after]", "branchSwitchAfterToggle");
        branchRowByName = new WebElement(page, "xpath=//*[@data-testid='branch-commit-%s']", "branchRow");
        branchMergeByName = new WebElement(page, "xpath=//*[@data-testid='branch-merge-%s']", "branchMergeBtn");
        syncUpdatesDialogComponent = new SyncUpdatesDialogComponent();
        projectStatus = new WebElement(page, "[data-testid^=\"status-\"]", "projectStatus");
        revisionEntries = new WebElement(page, "xpath=//*[starts-with(@data-testid,'revision-comment-')]", "revisionEntries");
        fileNodeByName = new WebElement(page, "xpath=//div[@role='treeitem'][.//*[normalize-space()='%s']]", "fileTreeNode");
        fileDeleteBtn = new WebElement(page, "[data-testid=file-delete]", "fileDeleteBtn");
        fileDeleteConfirmOk = new WebElement(page, "xpath=//div[contains(@class,'ant-popover')]//button[.//span[normalize-space()='OK']]", "fileDeleteConfirmOk");
        filesUploadInput = new WebElement(page, "[data-testid=files-upload-input]", "filesUploadInput");
        filesNewFolderBtn = new WebElement(page, "[data-testid=files-new-folder]", "filesNewFolderBtn");
        folderPathInput = new WebElement(page, "[data-testid=files-folder-path]", "folderPathInput");
        folderSubmitBtn = new WebElement(page, "[data-testid=files-folder-submit]", "folderSubmitBtn");
        tagValueForType = new WebElement(page, "xpath=//*[@data-testid='overview-left']//span[contains(@class,'ant-tag')][./span[1][normalize-space()='%s']]/span[last()]", "tagValueForType");
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

    // --- Branches tab (React project-detail; create/merge controls require the project to be OPEN) ---

    public ProjectDetailPage openBranchesTab() {
        branchesTab.click();
        waitUntilSpinnerLoaded();
        return this;
    }

    // Creates a branch off the current one via the "New branch" dialog. Mirrors the legacy
    // CopyProjectDialogComponent.setNewBranchName flow (which left you on the new branch → switchAfter=true).
    public ProjectDetailPage createBranch(String branchName) {
        return createBranch(branchName, false);
    }

    public ProjectDetailPage createBranch(String branchName, boolean switchAfter) {
        openBranchesTab();
        branchesCreateBtn.click();
        branchNewNameField.fill(branchName);
        if (switchAfter && !"true".equals(branchSwitchAfterToggle.getAttribute("aria-checked"))) {
            branchSwitchAfterToggle.click();
        }
        branchCreateSubmitBtn.click();
        waitUntilSpinnerLoaded();
        return this;
    }

    public boolean isBranchPresent(String branchName) {
        openBranchesTab();
        return branchRowByName.format(branchName).isVisible(DEFAULT_TIMEOUT_MS);
    }

    public String getCurrentBranch() {
        openBranchesTab();
        return branchesCurrentLabel.getText().trim();
    }

    // Opens the "Sync updates" dialog for merging the current branch with the given target branch (the target
    // is implicit in the branch row's Merge action).
    public SyncUpdatesDialogComponent openMergeDialog(String targetBranch) {
        openBranchesTab();
        branchMergeByName.format(targetBranch).click();
        return syncUpdatesDialogComponent.waitForVisible();
    }

    // Reads the value assigned for a tag type from the Overview TAGS section (each tag renders as an
    // ant-tag "<type> → <value>"); returns the value span's text.
    public String getTagValueForType(String tagType) {
        openOverviewTab();
        return tagValueForType.format(tagType).getText().trim();
    }

    // React status vocabulary (Local / Opened / Editing / Closed / ...) — differs from the legacy
    // "No Changes" / "In Editing" wording, so migrated tests must assert against the React values.
    public String getStatus() {
        return projectStatus.getText().trim();
    }

    // Revision comments on the History tab (each revision-comment-<hash>), newest first. Replaces the
    // legacy RepositoryContentRevisionsTabComponent.getRevisionDescription(i) loop.
    public List<String> getRevisionDescriptions() {
        openHistoryTab();
        Locator entries = revisionEntries.getLocator();
        entries.first().waitFor();
        List<String> descriptions = new ArrayList<>();
        int count = entries.count();
        for (int i = 0; i < count; i++) {
            descriptions.add(entries.nth(i).textContent().trim());
        }
        return descriptions;
    }

    public int getRevisionsCount() {
        openHistoryTab();
        // The History tab loads its revision list asynchronously after the spinner clears, so wait for
        // the first entry to render before counting (every project has at least the creation revision).
        // waitForVisible would trip strict mode on the multi-match, so wait on the first entry directly.
        revisionEntries.getLocator().first().waitFor();
        return revisionEntries.getLocator().count();
    }

    // Selecting a file reveals the preview panel with its delete action; deletion is confirmed via a popconfirm.
    // The tree node is removed asynchronously after the popconfirm, so wait it out before returning — otherwise
    // a following isFilePresent check races the removal and still sees the node.
    public ProjectDetailPage deleteFile(String fileName) {
        fileNodeByName.format(fileName).click();
        fileDeleteBtn.click();
        fileDeleteConfirmOk.click();
        waitUntilSpinnerLoaded();
        fileNodeByName.format(fileName).waitForHidden(DEFAULT_TIMEOUT_MS);
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

    // Creates a folder (or path/to/folder) in the project via the Files tab.
    public ProjectDetailPage createFolder(String folderPath) {
        openFilesTab();
        filesNewFolderBtn.click();
        folderPathInput.fill(folderPath);
        folderSubmitBtn.click();
        waitUntilSpinnerLoaded();
        return this;
    }

    // Folders and files are both tree nodes, so this also answers "is this folder present?".
    public boolean isFolderPresent(String folderName) {
        return fileNodeByName.format(folderName).isVisible(DEFAULT_TIMEOUT_MS);
    }

    public ProjectDetailPage reloadPage() {
        page.reload();
        waitUntilSpinnerLoaded();
        return this;
    }
}
