package domain.ui.webstudio.pages.mainpages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.repositorytabcomponents.CompareGitRevisionsDialogComponent;
import domain.ui.webstudio.components.repositorytabcomponents.SyncUpdatesDialogComponent;
import domain.ui.webstudio.pages.BasePage;
import helpers.utils.WaitUtil;
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
    private WebElement branchCreateErrorNotice;  // React ant-notification shown when a branch create fails
    private SyncUpdatesDialogComponent syncUpdatesDialogComponent;
    // Header
    private WebElement projectStatus;       // React status: Local / Opened / Editing / Closed / ...
    // Files tab: a file tree on the left, a preview panel with per-file actions on the right
    private WebElement fileNodeByName;      // format(fileName)
    private WebElement filesAddBtn;
    private WebElement filesAddMenuItem;
    private WebElement fileActionsBtn;
    private WebElement fileActionsMenuItem;
    private WebElement fileDeleteSubmitBtn;
    private WebElement filesUploadInput;
    private WebElement filesUploadSubmitBtn;
    private WebElement filesUploadNameField;
    private WebElement folderPathInput;
    private WebElement folderSubmitBtn;
    // History tab
    private WebElement revisionEntries;     // one per revision (revision-comment-<hash>)
    private WebElement revisionCompareToggles;  // per-revision "select for comparison" toggles
    private WebElement revisionsCompareBtn;     // "Compare (n/2)" button
    private WebElement revisionCompareSubmit;   // submit in the "Compare" modal (opens the diff tab)
    // Overview tab: assigned tags render as ant-tags "<type> → <value>" in the TAGS section
    private WebElement tagValueForType;     // format(tagType) → the value span
    private WebElement overviewRight;       // the right column of the Overview tab (Status/Repository/Path/Branch/Revision/Last change/Comment)

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
        branchCreateErrorNotice = new WebElement(page, "xpath=//div[contains(@class,'ant-notification')]//div[@role='alert']", "branchCreateErrorNotice");
        syncUpdatesDialogComponent = new SyncUpdatesDialogComponent();
        projectStatus = new WebElement(page, "[data-testid^=\"status-\"]", "projectStatus");
        revisionEntries = new WebElement(page, "xpath=//*[starts-with(@data-testid,'revision-comment-')]", "revisionEntries");
        revisionCompareToggles = new WebElement(page, "xpath=//*[starts-with(@data-testid,'revision-compare-') and not(@data-testid='revision-compare-file') and not(@data-testid='revision-compare-submit')]", "revisionCompareToggles");
        revisionsCompareBtn = new WebElement(page, "[data-testid=revisions-compare]", "revisionsCompareBtn");
        revisionCompareSubmit = new WebElement(page, "[data-testid=revision-compare-submit]", "revisionCompareSubmit");
        fileNodeByName = new WebElement(page, "xpath=//div[@role='treeitem'][.//*[normalize-space()='%s']]", "fileTreeNode");
        filesAddBtn = new WebElement(page, "[data-testid=files-add]", "filesAddBtn");
        // antd binds the menu handler to the <li>, so click that rather than the labelled <span> inside it.
        filesAddMenuItem = new WebElement(page, "xpath=//div[contains(@class,'ant-dropdown')][not(contains(@class,'ant-dropdown-hidden'))]//li[contains(@class,'ant-dropdown-menu-item')][.//span[@data-testid='%s']]", "filesAddMenuItem");
        fileActionsBtn = new WebElement(page, "[data-testid=file-actions]", "fileActionsBtn");
        fileActionsMenuItem = new WebElement(page, "xpath=//div[contains(@class,'ant-dropdown')][not(contains(@class,'ant-dropdown-hidden'))]//li[contains(@class,'ant-dropdown-menu-item')][normalize-space()='%s']", "fileActionsMenuItem");
        fileDeleteSubmitBtn = new WebElement(page, "[data-testid=file-delete-submit]", "fileDeleteSubmitBtn");
        // antd Upload.Dragger forwards unknown props to the file input itself, so the testid lands there.
        filesUploadInput = new WebElement(page, "input[data-testid=files-upload-dragger]", "filesUploadInput");
        filesUploadNameField = new WebElement(page, "[data-testid=files-upload-name]", "filesUploadNameField");
        filesUploadSubmitBtn = new WebElement(page, "[data-testid=files-upload-submit]", "filesUploadSubmitBtn");
        // files-folder-path is an antd AutoComplete wrapper (a DIV); the typeable field is its inner input.
        folderPathInput = new WebElement(page, "[data-testid=files-folder-path] input", "folderPathInput");
        folderSubmitBtn = new WebElement(page, "[data-testid=files-folder-submit]", "folderSubmitBtn");
        tagValueForType = new WebElement(page, "xpath=//*[@data-testid='overview-left']//span[contains(@class,'ant-tag')][./span[1][normalize-space()='%s']]/span[last()]", "tagValueForType");
        overviewRight = new WebElement(page, "[data-testid=overview-right]", "overviewRight");
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

    // Attempts to create a branch whose name is expected to be rejected (e.g. it already exists in the
    // repository). Returns the React error notification text (e.g. "Branch 'x' already exists in repository.").
    public String createBranchExpectingError(String branchName) {
        openBranchesTab();
        branchesCreateBtn.click();
        branchNewNameField.fill(branchName);
        branchCreateSubmitBtn.click();
        branchCreateErrorNotice.waitForVisible(DEFAULT_TIMEOUT_MS);
        return branchCreateErrorNotice.getText().trim();
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

    /**
     * The project's status. 6.4.0 restored the legacy wording — No Changes / In Editing / Viewing Revision
     * / Local / Closed / Deleted — and dropped the testid from the status pill, so it is read from the
     * Overview panel by its label.
     */
    public String getStatus() {
        return extractOverviewField("Status", "Repository");
    }

    // The Overview-right column concatenates its labelled fields into one text blob, in the order
    // Status / Repository / Path / Branch / Revision ID / Modified / Comment. Extracts one field's value.
    private String extractOverviewField(String label, String nextLabel) {
        openOverviewTab();
        String blob = overviewRight.getText();
        int start = blob.indexOf(label);
        if (start < 0) {
            return "";
        }
        start += label.length();
        int end = blob.indexOf(nextLabel, start);
        return (end < 0 ? blob.substring(start) : blob.substring(start, end)).trim();
    }

    // Replaces the legacy Properties-tab getRevision() — the React Overview shows the FULL commit hash.
    public String getOverviewRevision() {
        return extractOverviewField("Revision ID", "Modified");
    }

    // Replaces the legacy Properties-tab getModifiedBy()+getModifiedAt() — the Overview combines the author
    // and timestamp into a single "Modified" field (e.g. "German HarberJul 27, 2026 6:51 AM").
    public String getOverviewLastChange() {
        return extractOverviewField("Modified", "Comment");
    }

    // Replaces the legacy Properties-tab getPath() — the project's path-in-repository from the Overview.
    public String getOverviewPath() {
        return extractOverviewField("Path", "Branch");
    }

    // Replaces the legacy Properties-tab getRepository() — the design repository name from the Overview.
    public String getOverviewRepository() {
        return extractOverviewField("Repository", "Path");
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

    // Opens the repository revision comparison: selects the two newest revisions on the History tab, submits
    // the Compare dialog (which opens the diff in a NEW browser tab, the legacy showDiff.xhtml JSF page), and
    // returns a CompareGitRevisionsDialogComponent bound to that tab.
    public CompareGitRevisionsDialogComponent openRevisionCompare() {
        openHistoryTab();
        Locator toggles = revisionCompareToggles.getLocator();
        toggles.first().waitFor();
        toggles.nth(0).click();
        toggles.nth(1).click();
        revisionsCompareBtn.click();
        revisionCompareSubmit.waitForVisible(DEFAULT_TIMEOUT_MS);
        int pagesBefore = LocalDriverPool.getBrowserContext().pages().size();
        revisionCompareSubmit.click();
        WaitUtil.waitForCondition(
                () -> LocalDriverPool.getBrowserContext().pages().size() > pagesBefore,
                DEFAULT_TIMEOUT_MS, 250, "Waiting for the compare diff tab to open");
        List<Page> pages = LocalDriverPool.getBrowserContext().pages();
        Page diffTab = pages.get(pages.size() - 1);
        WaitUtil.waitForCondition(() -> diffTab.url().contains("showDiff"),
                DEFAULT_TIMEOUT_MS, 250, "Waiting for the diff tab to load showDiff.xhtml");
        diffTab.waitForLoadState();
        return new CompareGitRevisionsDialogComponent(diffTab);
    }

    // The newest revision's git hash, parsed from the first History entry's data-testid
    // ("revision-comment-<hash>"). Lets callers assert a new revision was committed (id changes).
    public String getLatestRevisionId() {
        openHistoryTab();
        Locator first = revisionEntries.getLocator().first();
        first.waitFor();
        String testId = first.getAttribute("data-testid");
        return testId == null ? "" : testId.substring(testId.lastIndexOf('-') + 1);
    }

    public int getRevisionsCount() {
        openHistoryTab();
        // The History tab loads its revision list asynchronously after the spinner clears, so wait for
        // the first entry to render before counting (every project has at least the creation revision).
        // waitForVisible would trip strict mode on the multi-match, so wait on the first entry directly.
        revisionEntries.getLocator().first().waitFor();
        return revisionEntries.getLocator().count();
    }

    /**
     * Deletes a file: select it in the tree, then Delete from the preview pane's "More actions" menu and
     * confirm in the delete dialog. The tree node goes away asynchronously, so wait it out — otherwise a
     * following isFilePresent check races the removal and still sees the node.
     */
    public ProjectDetailPage deleteFile(String fileName) {
        fileNodeByName.format(fileName).click();
        fileActionsBtn.click();
        fileActionsMenuItem.format("Delete").click();
        fileDeleteSubmitBtn.click();
        waitUntilSpinnerLoaded();
        fileNodeByName.format(fileName).waitForHidden(DEFAULT_TIMEOUT_MS);
        return this;
    }

    public boolean isFilePresent(String fileName) {
        return fileNodeByName.format(fileName).isVisible(DEFAULT_TIMEOUT_MS);
    }

    // Uploads a file into the project: Files tab -> Add -> Upload -> pick the file -> submit.
    public ProjectDetailPage uploadFile(String filePath) {
        return uploadFileAs(filePath, null);
    }

    /**
     * Uploads a file and stores it under a different name. A single-file upload exposes a Name field
     * (pre-filled from the picked file), which is how the legacy upload dialog's source→target rename maps
     * onto the React Files tab.
     */
    public ProjectDetailPage uploadFileAs(String filePath, String targetName) {
        openFilesTab();
        openAddMenuItem("files-upload");
        filesUploadInput.setInputFiles(filePath);
        if (targetName != null && !targetName.isEmpty()) {
            filesUploadNameField.waitForVisible(DEFAULT_TIMEOUT_MS).fill(targetName);
        }
        filesUploadSubmitBtn.click();
        waitUntilSpinnerLoaded();
        return this;
    }

    // The Files tab gathers New folder / New text file / Upload behind a single Add menu. That menu is an
    // antd Dropdown with the default HOVER trigger, so clicking the button toggles it shut again — hover it.
    private void openAddMenuItem(String itemTestId) {
        filesAddBtn.hover();
        filesAddMenuItem.format(itemTestId).click();
    }

    // Creates a folder (or path/to/folder) in the project via the Files tab.
    public ProjectDetailPage createFolder(String folderPath) {
        openFilesTab();
        openAddMenuItem("files-new-folder");
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
