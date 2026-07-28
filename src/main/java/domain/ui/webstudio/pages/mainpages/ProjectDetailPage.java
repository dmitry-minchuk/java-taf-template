package domain.ui.webstudio.pages.mainpages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.repositorytabcomponents.CompareGitRevisionsDialogComponent;
import domain.ui.webstudio.components.repositorytabcomponents.SyncUpdatesDialogComponent;
import domain.ui.webstudio.components.common.ConfigureCommitInfoComponent;
import domain.ui.webstudio.components.repositorytabcomponents.CopyProjectDialogComponent;
import domain.ui.webstudio.components.repositorytabcomponents.ExportProjectModalComponent;
import domain.ui.webstudio.pages.BasePage;
import helpers.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

// React project-detail view (/projects/<id>) introduced in build 032c60a664ce: Overview / Files /
// History / Branches / Publish tabs. Reached from the projects list via RepositoryPage.openProjectDetail.
public class ProjectDetailPage extends BasePage {

    private static final Logger LOGGER = LogManager.getLogger(ProjectDetailPage.class);

    // Header buttons render with the screen, so a short probe decides bar-vs-overflow.
    private static final int HEADER_ACTION_PROBE_MS = DEFAULT_TIMEOUT_MS / 5;
    // The detail screen can settle after the first tab click, so a switch may need repeating.
    private static final int TAB_SWITCH_ATTEMPTS = 3;
    private static final String COMPARE_SCREEN_PATH = "compare.xhtml";
    private static final int COMPARE_WINDOW_WIDTH = 1280;
    private static final int COMPARE_WINDOW_HEIGHT = 800;

    // Top navigation (shared React shell)
    @Getter
    private TabSwitcherComponent tabSwitcherComponent;
    // Tabs
    private WebElement overviewTab;
    private WebElement filesTab;
    private WebElement historyTab;
    private WebElement headerActionByLabel;
    private WebElement headerMoreBtn;
    private WebElement headerOverflowItem;
    private WebElement branchLabel;
    private WebElement openRevisionSelect;
    private WebElement openRevisionOption;
    private WebElement openRevisionSubmit;
    private List<WebElement> openRevisionOptions;
    private WebElement modifiedValue;
    private WebElement modifiedDate;
    private ExportProjectModalComponent exportProjectModalComponent;
    private WebElement branchSwitcherTrigger;
    private WebElement branchMenuItem;
    private WebElement mergeTargetBranchSelect;
    private WebElement mergeBranchOption;
    private CopyProjectDialogComponent copyProjectDialogComponent;
    private ConfigureCommitInfoComponent configureCommitInfoComponent;
    private WebElement configureCommitInfoShade;
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
    private WebElement detailRoot;
    private WebElement filesAddMenuItem;
    private WebElement fileActionsBtn;
    private WebElement fileActionsMenuItem;
    private WebElement fileDeleteSubmitBtn;
    private WebElement updateFileInput;
    private WebElement updateFileSubmitBtn;
    private WebElement updateFileNameWarning;
    private WebElement filesUploadInput;
    private WebElement filesUploadSubmitBtn;
    private WebElement filesUploadNameField;
    private WebElement filesUploadPathField;
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
        // The header bar also renders a hidden copy of every button to measure widths, so match the visible
        // one by its testid (<actionId>-<projectId>) rather than by label text.
        headerActionByLabel = new WebElement(page, "xpath=//button[starts-with(@data-testid,'%s-')]", "headerAction");
        headerMoreBtn = new WebElement(page, "[data-testid=project-actions-more]", "headerMoreBtn");
        headerOverflowItem = new WebElement(page, "xpath=//div[contains(@class,'ant-dropdown')][not(contains(@class,'ant-dropdown-hidden'))]//button[normalize-space()='%s']", "headerOverflowItem");
        branchLabel = new WebElement(page, "[data-testid=overview-branch]", "branchLabel");
        // In the Overview panel a field is a label <div><span>Name</span></div> followed by its value div;
        // the Modified value holds the author and, in a nested div, the date.
        modifiedValue = new WebElement(page, "xpath=//*[@data-testid='overview-right']//div[./span[normalize-space()='Modified']]/following-sibling::div[1]", "modifiedValue");
        modifiedDate = new WebElement(page, "xpath=//*[@data-testid='overview-right']//div[./span[normalize-space()='Modified']]/following-sibling::div[1]/div[last()]", "modifiedDate");
        openRevisionSelect = new WebElement(page, "[data-testid=open-revision-select]", "openRevisionSelect");
        openRevisionOption = new WebElement(page, "xpath=//div[contains(@class,'ant-select-item-option')][@title='%s']", "openRevisionOption");
        openRevisionSubmit = new WebElement(page, "[data-testid=open-revision-submit]", "openRevisionSubmit");
        openRevisionOptions = createElementList("xpath=//div[contains(@class,'ant-select-item-option')]", "openRevisionOptions");
        exportProjectModalComponent = new ExportProjectModalComponent();
        branchSwitcherTrigger = new WebElement(page, "[data-testid=overview-branch-trigger]", "branchSwitcherTrigger");
        // A switcher entry shows the branch name plus its marks ("master" + a Default tag), so match it by
        // the menu key antd derives from the branch name, falling back to the name held inside the label.
        branchMenuItem = new WebElement(page, "xpath=//div[contains(@class,'ant-dropdown')][not(contains(@class,'ant-dropdown-hidden'))]//li[contains(@class,'ant-dropdown-menu-item')][@data-menu-id='rc-menu-uuid-%s' or .//*[normalize-space()='%s']]", "branchMenuItem");
        // antd prefixes the field id with the form name, so the select is merge_branches_form_targetBranch.
        mergeTargetBranchSelect = new WebElement(page, "input[id$=targetBranch]", "mergeTargetBranchSelect");
        mergeBranchOption = new WebElement(page, "xpath=//*[@data-testid='merge-branch-%s']", "mergeBranchOption");
        copyProjectDialogComponent = new CopyProjectDialogComponent();
        configureCommitInfoComponent = createScopedComponent(ConfigureCommitInfoComponent.class, "xpath=//div[@role='dialog'][.//div[contains(@class,'ant-modal-title') and normalize-space()='Configure Git Commit Info']]", "configureCommitInfoComponent");
        configureCommitInfoShade = new WebElement(page, "xpath=//div[@role='dialog'][.//div[contains(@class,'ant-modal-title') and normalize-space()='Configure Git Commit Info']]", "configureCommitInfoShade");
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
        detailRoot = new WebElement(page, "[data-testid=project-detail]", "detailRoot");
        // antd binds the menu handler to the <li>, so click that rather than the labelled <span> inside it.
        filesAddMenuItem = new WebElement(page, "xpath=//div[contains(@class,'ant-dropdown')][not(contains(@class,'ant-dropdown-hidden'))]//li[contains(@class,'ant-dropdown-menu-item')][.//span[@data-testid='%s']]", "filesAddMenuItem");
        fileActionsBtn = new WebElement(page, "[data-testid=file-actions]", "fileActionsBtn");
        fileActionsMenuItem = new WebElement(page, "xpath=//div[contains(@class,'ant-dropdown')][not(contains(@class,'ant-dropdown-hidden'))]//li[contains(@class,'ant-dropdown-menu-item')][normalize-space()='%s']", "fileActionsMenuItem");
        fileDeleteSubmitBtn = new WebElement(page, "[data-testid=file-delete-submit]", "fileDeleteSubmitBtn");
        // As with the upload dragger, antd puts the testid on the file input itself.
        updateFileInput = new WebElement(page, "input[data-testid=update-file-dragger]", "updateFileInput");
        updateFileSubmitBtn = new WebElement(page, "[data-testid=update-file-submit]", "updateFileSubmitBtn");
        updateFileNameWarning = new WebElement(page, "[data-testid=update-file-name-warning]", "updateFileNameWarning");
        // antd Upload.Dragger forwards unknown props to the file input itself, so the testid lands there.
        filesUploadInput = new WebElement(page, "input[data-testid=files-upload-dragger]", "filesUploadInput");
        filesUploadNameField = new WebElement(page, "[data-testid=files-upload-name]", "filesUploadNameField");
        // The path field is an AutoComplete wrapper, so type into its inner input.
        filesUploadPathField = new WebElement(page, "[data-testid=files-upload-path] input", "filesUploadPathField");
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

    /**
     * Opens the Files tab. Right after a project is created the detail screen finishes loading late and
     * resets to Overview, so the click is repeated until the tab really is open — the Add button exists
     * only there, which makes it a reliable marker.
     */
    public ProjectDetailPage openFilesTab() {
        detailRoot.waitForVisible(DEFAULT_TIMEOUT_MS);
        for (int attempt = 1; attempt <= TAB_SWITCH_ATTEMPTS; attempt++) {
            filesTab.click();
            waitUntilSpinnerLoaded();
            if (filesAddBtn.isVisible(DEFAULT_TIMEOUT_MS / 2)) {
                return this;
            }
        }
        filesAddBtn.waitForVisible(DEFAULT_TIMEOUT_MS);
        return this;
    }

    public ProjectDetailPage openHistoryTab() {
        historyTab.click();
        waitUntilSpinnerLoaded();
        return this;
    }

    // --- Branches tab (React project-detail; create/merge controls require the project to be OPEN) ---

    /**
     * Clicks an action in the project header (Open Revision / Close / Sync / Copy / Delete / Compare /
     * Export / Deploy). The header bar collapses trailing actions into an overflow menu as the window
     * narrows, so look there when the button is not on the bar.
     */
    public ProjectDetailPage clickHeaderAction(String actionLabel) {
        WebElement action = headerActionByLabel.format(actionIdOf(actionLabel));
        if (action.isVisible(HEADER_ACTION_PROBE_MS)) {
            LOGGER.info("Header action '{}' clicked on the bar", actionLabel);
            action.click();
            return this;
        }
        LOGGER.info("Header action '{}' is not on the bar; opening the Actions menu", actionLabel);
        headerMoreBtn.click();
        headerOverflowItem.format(actionLabel).click();
        return this;
    }

    // The header buttons are keyed by the action's own id; the overflow menu still lists them by label.
    private static String actionIdOf(String actionLabel) {
        return switch (actionLabel) {
            case "Open Revision" -> "openRevision";
            case "Delete Branch" -> "deleteBranch";
            default -> actionLabel.substring(0, 1).toLowerCase() + actionLabel.substring(1);
        };
    }

    /**
     * Branches the project. Studio 6.4.0 removed the Branches tab and moved branching into the Copy
     * dialog, which opens in branch mode on a branching repository — so "create a branch" is a copy of the
     * project into a new branch, and it leaves the project on that branch.
     */
    public ProjectDetailPage createBranch(String branchName) {
        return createBranch(branchName, false);
    }

    public ProjectDetailPage createBranch(String branchName, boolean switchAfter) {
        String sourceBranch = getCurrentBranch();
        clickHeaderAction("Copy");
        copyProjectDialogComponent.waitForDialogToAppear().setBranchName(branchName);
        copyProjectDialogComponent.clickCopyButton();
        fillCommitInfoIfShown();
        waitUntilSpinnerLoaded();
        // Studio moves the project onto the branch it just created, so wait for that instead of forcing it.
        WaitUtil.waitForCondition(() -> branchName.equals(getCurrentBranch()), DEFAULT_TIMEOUT_MS, 500,
                "Waiting for the project to land on branch " + branchName);
        if (!switchAfter) {
            switchBranch(sourceBranch);
        }
        return this;
    }

    // Attempts to branch under a name the repository already holds; returns the dialog's error text.
    public String createBranchExpectingError(String branchName) {
        clickHeaderAction("Copy");
        copyProjectDialogComponent.waitForDialogToAppear().setBranchName(branchName);
        copyProjectDialogComponent.clickCopyButton(false);
        return String.join(" ", copyProjectDialogComponent.waitForErrors(DEFAULT_TIMEOUT_MS));
    }

    // Switches the project onto another branch via the branch switcher next to the branch label.
    public ProjectDetailPage switchBranch(String branchName) {
        openOverviewTab();
        branchSwitcherTrigger.click();
        branchMenuItem.format(branchName, branchName).click();
        waitUntilSpinnerLoaded();
        return this;
    }

    /**
     * Whether the repository holds this branch. The switcher offers every branch except the one the project
     * is already on, so the current branch is answered directly.
     */
    public boolean isBranchPresent(String branchName) {
        if (branchName.equals(getCurrentBranch())) {
            return true;
        }
        branchSwitcherTrigger.click();
        boolean present = branchMenuItem.format(branchName, branchName).isVisible(HEADER_ACTION_PROBE_MS);
        page.keyboard().press("Escape");
        return present;
    }

    // The default branch is tagged in the label, and the text comes back glued together ("masterDefault").
    private static final String DEFAULT_BRANCH_TAG = "Default";

    /** Name of the branch the project sits on, without the Default tag the label adds to it. */
    public String getCurrentBranch() {
        openOverviewTab();
        String label = branchLabel.getText().trim();
        if (label.endsWith(DEFAULT_BRANCH_TAG)) {
            label = label.substring(0, label.length() - DEFAULT_BRANCH_TAG.length());
        }
        return label.trim();
    }

    // A first commit by a user raises the "Configure Git Commit Info" modal on top of the flow.
    private void fillCommitInfoIfShown() {
        if (configureCommitInfoShade.isVisible(HEADER_ACTION_PROBE_MS)) {
            configureCommitInfoComponent.fillCommitInfoWithRandomData();
        }
    }

    /**
     * Opens the merge dialog against another branch. 6.4.0 reaches it from the header's Sync action and asks
     * which branch to merge with, instead of the per-branch Merge action the removed Branches tab had.
     */
    public SyncUpdatesDialogComponent openMergeDialog(String targetBranch) {
        clickHeaderAction("Sync");
        mergeTargetBranchSelect.waitForVisible(DEFAULT_TIMEOUT_MS).click();
        mergeBranchOption.format(targetBranch).click();
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

    /** Who last changed the project — the author part of the Overview "Modified" field. */
    public String getModifiedBy() {
        openOverviewTab();
        String whole = modifiedValue.getText().trim();
        String date = modifiedDate.getText().trim();
        return whole.endsWith(date) ? whole.substring(0, whole.length() - date.length()).trim() : whole;
    }

    /** When the project was last changed, as the Overview shows it (e.g. "Jul 27, 2026 10:26 AM"). */
    public String getModifiedAt() {
        openOverviewTab();
        return modifiedDate.getText().trim();
    }

    /**
     * The way a committed revision is named in the export window: "&lt;author&gt;: &lt;date&gt;". Lets a test
     * build the expected entry from the project's own Overview.
     */
    public String getLatestRevisionLabel() {
        return getModifiedBy() + ": " + getModifiedAt();
    }

    /**
     * Opens the project on an earlier revision, replacing the workspace copy — the "Open Revision" action.
     * The revision is named the way the revision lists name it: "&lt;author&gt;: &lt;date&gt;".
     */
    public ProjectDetailPage openRevision(String revisionLabel) {
        clickHeaderAction("Open Revision");
        openRevisionSelect.waitForVisible(DEFAULT_TIMEOUT_MS).click();
        openRevisionOption.format(revisionLabel).click();
        openRevisionSubmit.click();
        waitUntilSpinnerLoaded();
        return this;
    }

    /**
     * Opens the project on the revision at the given position in the list, newest first (1 = newest). Use
     * this rather than a label when the label comes from elsewhere: the editor's export window and this one
     * write the same commit's date differently.
     */
    public ProjectDetailPage openRevisionByPosition(int position) {
        clickHeaderAction("Open Revision");
        openRevisionSelect.waitForVisible(DEFAULT_TIMEOUT_MS).click();
        WaitUtil.waitForCondition(() -> openRevisionOptions.size() >= position, DEFAULT_TIMEOUT_MS, 250,
                "Waiting for the revision list to hold at least " + position + " entries");
        openRevisionOptions.get(position - 1).click();
        openRevisionSubmit.click();
        waitUntilSpinnerLoaded();
        return this;
    }

    /** Opens the Export window for this project (the action sits in the header, or in its Actions menu). */
    public ExportProjectModalComponent openExportDialog() {
        clickHeaderAction("Export");
        exportProjectModalComponent.waitForDialogToAppear();
        return exportProjectModalComponent;
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
    /**
     * Opens the revision comparison. In 6.4.0 the Revisions tab no longer picks two revisions: the header's
     * Compare action opens the comparison screen in a separate window, where the revisions are chosen.
     */
    /**
     * Opens the comparison screen (a separate window) without choosing a revision — for checks that only
     * look at the screen itself, such as how the module dropdowns are ordered.
     */
    public CompareGitRevisionsDialogComponent openCompareWindow() {
        Page compareWindow = openCompareScreen();
        return new CompareGitRevisionsDialogComponent(compareWindow);
    }

    /** Clicks Compare and returns the window it opens (the screen is a popup, so wait for it as one). */
    private Page openCompareScreen() {
        Page compareWindow = page.waitForPopup(() -> clickHeaderAction("Compare"));
        settleCompareWindow(compareWindow);
        return compareWindow;
    }

    /**
     * Finds the window the Compare action opens. Match on the screen's own address — a project's own URL can
     * contain the word "compare" simply because its name does.
     */
    private Page awaitCompareWindow() {
        WaitUtil.waitForCondition(() -> findCompareWindow() != null, DEFAULT_TIMEOUT_MS, 250,
                "Waiting for the compare window to open");
        return findCompareWindow();
    }

    private Page findCompareWindow() {
        return LocalDriverPool.getBrowserContext().pages().stream()
                .filter(candidate -> candidate.url().contains(COMPARE_SCREEN_PATH))
                .reduce((first, second) -> second)
                .orElse(null);
    }

    // The comparison screen is a JSF page that keeps loading after the window appears. It is opened with
    // window.open(width, height), which in a headless run can leave the popup sized so small that nothing
    // in it counts as visible — so give it a viewport of its own first.
    private void settleCompareWindow(Page compareWindow) {
        compareWindow.setViewportSize(COMPARE_WINDOW_WIDTH, COMPARE_WINDOW_HEIGHT);
        compareWindow.waitForLoadState();
        LOGGER.info("Compare window: url={}, title={}", compareWindow.url(), compareWindow.title());
        try {
            compareWindow.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE,
                    new Page.WaitForLoadStateOptions().setTimeout(DEFAULT_TIMEOUT_MS));
        } catch (RuntimeException ignored) {
            // A busy page never reaches network-idle; the element waits below cover that.
        }
    }

    public CompareGitRevisionsDialogComponent openRevisionCompare() {
        Page compareWindow = openCompareScreen();
        CompareGitRevisionsDialogComponent compare = new CompareGitRevisionsDialogComponent(compareWindow);
        // The screen opens with nothing compared yet: pick the revision to compare against and run it.
        // (Its module dropdowns are RichFaces combos and stay hidden, so do not wait on them here.)
        compare.selectRevision(1);
        compare.clickCompareBtn();
        return compare;
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
        openFilesTab();
        fileNodeByName.format(fileName).click();
        fileActionsBtn.waitForVisible(DEFAULT_TIMEOUT_MS).click();
        fileActionsMenuItem.format("Delete").click();
        fileDeleteSubmitBtn.click();
        waitUntilSpinnerLoaded();
        fileNodeByName.format(fileName).waitForHidden(DEFAULT_TIMEOUT_MS);
        return this;
    }

    /**
     * Whether the project holds this file. Opens the Files tab itself: the detail screen can finish loading
     * late and drop back to Overview, which would leave the file tree out of the page.
     */
    public boolean isFilePresent(String fileName) {
        openFilesTab();
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
        return uploadFileAs(filePath, targetName, null);
    }

    /** Uploads into a folder of the project (e.g. "rules"); an empty folder means the project root. */
    public ProjectDetailPage uploadFileInto(String filePath, String targetFolder) {
        return uploadFileAs(filePath, null, targetFolder);
    }

    public ProjectDetailPage uploadFileAs(String filePath, String targetName, String targetFolder) {
        openFilesTab();
        openAddMenuItem("files-upload");
        filesUploadInput.setInputFiles(filePath);
        if (targetName != null && !targetName.isEmpty()) {
            filesUploadNameField.waitForVisible(DEFAULT_TIMEOUT_MS).fill(targetName);
        }
        if (targetFolder != null && !targetFolder.isEmpty()) {
            filesUploadPathField.waitForVisible(DEFAULT_TIMEOUT_MS).fill(targetFolder);
        }
        filesUploadSubmitBtn.click();
        waitUntilSpinnerLoaded();
        return this;
    }

    /**
     * Whether the Files tab offers its Add menu (New folder / New text file / Upload). It is only rendered
     * for a user who may change the project's files, so this is how "can this user edit here?" is checked —
     * it replaces the old toolbar's Upload File / Add Folder buttons.
     */
    public boolean isAddFilesMenuAvailable() {
        openFilesTab();
        return filesAddBtn.isVisible(DEFAULT_TIMEOUT_MS);
    }

    /**
     * Replaces a file's content with another file: select it, Update from its actions menu, pick the new
     * file, confirm. Uploading a file with a different name is allowed but warned about.
     */
    public ProjectDetailPage updateFile(String fileName, String newFilePath) {
        openFilesTab();
        fileNodeByName.format(fileName).click();
        // Selecting a file opens its preview pane, where the actions menu lives — wait for it.
        fileActionsBtn.waitForVisible(DEFAULT_TIMEOUT_MS).click();
        fileActionsMenuItem.format("Update").click();
        updateFileInput.waitForVisible(DEFAULT_TIMEOUT_MS).setInputFiles(newFilePath);
        updateFileSubmitBtn.click();
        waitUntilSpinnerLoaded();
        return this;
    }

    /** Whether the update dialog warns that the chosen file has a different name. */
    public boolean isUpdateFileNameWarningShown() {
        return updateFileNameWarning.isVisible(DEFAULT_TIMEOUT_MS / 2);
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
