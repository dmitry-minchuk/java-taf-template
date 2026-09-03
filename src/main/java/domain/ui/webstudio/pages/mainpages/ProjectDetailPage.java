package domain.ui.webstudio.pages.mainpages;

import com.microsoft.playwright.Page;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.repositorytabcomponents.CompareGitRevisionsDialogComponent;
import domain.ui.webstudio.components.repositorytabcomponents.DeleteBranchModalComponent;
import domain.ui.webstudio.components.repositorytabcomponents.SyncUpdatesDialogComponent;
import domain.ui.webstudio.components.common.ConfigureCommitInfoComponent;
import domain.ui.webstudio.components.projectdetail.ProjectFilesTabComponent;
import domain.ui.webstudio.components.projectdetail.ProjectHeaderActionsComponent;
import domain.ui.webstudio.components.projectdetail.ProjectHistoryTabComponent;
import domain.ui.webstudio.components.projectdetail.ProjectOverviewTabComponent;
import domain.ui.webstudio.components.repositorytabcomponents.CopyProjectDialogComponent;
import domain.ui.webstudio.components.repositorytabcomponents.ExportProjectModalComponent;
import domain.ui.webstudio.pages.BasePage;
import helpers.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import lombok.Getter;

import java.util.List;

public class ProjectDetailPage extends BasePage {

    private static final Logger LOGGER = LogManager.getLogger(ProjectDetailPage.class);

    private static final int HEADER_ACTION_PROBE_MS = DEFAULT_TIMEOUT_MS / 5;
    private static final int TAB_SWITCH_ATTEMPTS = 3;
    private static final int COMPARE_WINDOW_WIDTH = 1280;
    private static final int COMPARE_WINDOW_HEIGHT = 800;

    @Getter
    private TabSwitcherComponent tabSwitcherComponent;
    private WebElement overviewTab;
    private WebElement filesTab;
    private WebElement historyTab;
    private ProjectHeaderActionsComponent headerActions;
    private ProjectOverviewTabComponent overview;
    private ProjectFilesTabComponent files;
    private ProjectHistoryTabComponent history;
    private WebElement openRevisionSelect;
    private WebElement openRevisionOption;
    private WebElement openRevisionSubmit;
    private List<WebElement> openRevisionOptions;
    private ExportProjectModalComponent exportProjectModalComponent;
    private WebElement mergeTargetBranchSelect;
    private List<WebElement> mergeBranchOptions;
    private CopyProjectDialogComponent copyProjectDialogComponent;
    private ConfigureCommitInfoComponent configureCommitInfoComponent;
    private WebElement configureCommitInfoShade;
    private SyncUpdatesDialogComponent syncUpdatesDialogComponent;
    private WebElement detailRoot;
    private WebElement errorNotification;

    public ProjectDetailPage() {
        super();
        initializeComponents();
    }

    private void initializeComponents() {
        tabSwitcherComponent = createScopedComponent(TabSwitcherComponent.class, "xpath=//ul[@role='menu' and contains(@class,'ant-menu-horizontal')]", "tabSwitcherComponent");
        overviewTab = new WebElement(page, "xpath=//div[@data-node-key='overview']", "overviewTab");
        filesTab = new WebElement(page, "xpath=//div[@data-node-key='files']", "filesTab");
        historyTab = new WebElement(page, "xpath=//div[@data-node-key='history']", "historyTab");
        headerActions = new ProjectHeaderActionsComponent(page);
        overview = new ProjectOverviewTabComponent(page);
        files = new ProjectFilesTabComponent(page);
        history = new ProjectHistoryTabComponent(page);
        openRevisionSelect = new WebElement(page, "[data-testid=open-revision-select]", "openRevisionSelect");
        openRevisionOption = new WebElement(page, "xpath=//div[contains(@class,'ant-select-item-option')][@title='%s']", "openRevisionOption");
        openRevisionSubmit = new WebElement(page, "[data-testid=open-revision-submit]", "openRevisionSubmit");
        openRevisionOptions = createElementList("xpath=//div[contains(@class,'ant-select-item-option')]", "openRevisionOptions");
        exportProjectModalComponent = new ExportProjectModalComponent();
        mergeTargetBranchSelect = new WebElement(page, "[data-testid=merge-target-branch]", "mergeTargetBranchSelect");
        mergeBranchOptions = createElementList("xpath=//div[contains(@class,'ant-select-item-option')]//div[contains(@class,'ant-select-item-option-content')]", "mergeBranchOptions");
        copyProjectDialogComponent = new CopyProjectDialogComponent();
        configureCommitInfoComponent = createScopedComponent(ConfigureCommitInfoComponent.class, "xpath=//div[@role='dialog'][.//div[contains(@class,'ant-modal-title') and normalize-space()='Configure Git Commit Info']]", "configureCommitInfoComponent");
        configureCommitInfoShade = new WebElement(page, "xpath=//div[@role='dialog'][.//div[contains(@class,'ant-modal-title') and normalize-space()='Configure Git Commit Info']]", "configureCommitInfoShade");
        syncUpdatesDialogComponent = new SyncUpdatesDialogComponent();
        detailRoot = new WebElement(page, "[data-testid=project-detail]", "detailRoot");
        errorNotification = new WebElement(page, "xpath=(//div[contains(@class,'ant-notification-notice')])[last()]", "errorNotification");
    }

    public ProjectDetailPage openOverviewTab() {
        overviewTab.click();
        waitUntilSpinnerLoaded();
        return this;
    }

    public ProjectDetailPage openFilesTab() {
        detailRoot.waitForVisible(DEFAULT_TIMEOUT_MS);
        for (int attempt = 1; attempt <= TAB_SWITCH_ATTEMPTS; attempt++) {
            filesTab.click();
            waitUntilSpinnerLoaded();
            if (files.isOpen(DEFAULT_TIMEOUT_MS / 2)) {
                return this;
            }
        }
        files.waitForOpen(DEFAULT_TIMEOUT_MS);
        return this;
    }

    public ProjectDetailPage openHistoryTab() {
        historyTab.click();
        waitUntilSpinnerLoaded();
        return this;
    }

    public ProjectDetailPage clickHeaderAction(String actionLabel) {
        headerActions.clickAction(actionLabel);
        return this;
    }

    public boolean isHeaderActionAvailable(String actionLabel) {
        return headerActions.isActionAvailable(actionLabel);
    }

    public DeleteBranchModalComponent openDeleteBranchDialog() {
        clickHeaderAction("Delete Branch");
        return new DeleteBranchModalComponent().waitForVisible();
    }

    public String getErrorNotification() {
        errorNotification.waitForVisible(DEFAULT_TIMEOUT_MS);
        return errorNotification.getText().trim();
    }

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
        WaitUtil.waitForCondition(() -> branchName.equals(getCurrentBranch()), DEFAULT_TIMEOUT_MS, 500,
                "Waiting for the project to land on branch " + branchName);
        if (!switchAfter) {
            switchBranch(sourceBranch);
        }
        return this;
    }

    public String createBranchExpectingError(String branchName) {
        clickHeaderAction("Copy");
        copyProjectDialogComponent.waitForDialogToAppear().setBranchName(branchName);
        copyProjectDialogComponent.clickCopyButton(false);
        return String.join(" ", copyProjectDialogComponent.waitForErrors(DEFAULT_TIMEOUT_MS));
    }

    public ProjectDetailPage switchBranch(String branchName) {
        openOverviewTab();
        overview.switchBranch(branchName);
        return this;
    }

    public boolean isBranchPresent(String branchName) {
        openOverviewTab();
        return overview.isBranchPresent(branchName);
    }

    public String getCurrentBranch() {
        openOverviewTab();
        return overview.getCurrentBranch();
    }

    private void fillCommitInfoIfShown() {
        if (configureCommitInfoShade.isVisible(HEADER_ACTION_PROBE_MS)) {
            configureCommitInfoComponent.fillCommitInfoWithRandomData();
        }
    }

    public SyncUpdatesDialogComponent openMergeDialog(String targetBranch) {
        clickHeaderAction("Sync");
        mergeTargetBranchSelect.waitForVisible(DEFAULT_TIMEOUT_MS).click();
        selectMergeBranch(targetBranch);
        return syncUpdatesDialogComponent.waitForVisible();
    }

    private void selectMergeBranch(String targetBranch) {
        WaitUtil.waitForCondition(() -> !mergeBranchOptions.isEmpty(), DEFAULT_TIMEOUT_MS, 200,
                "Waiting for the merge branch options to appear");
        for (WebElement option : mergeBranchOptions) {
            String optionText = option.getText().trim();
            if (optionText.equals(targetBranch) || optionText.startsWith(targetBranch)) {
                option.click();
                return;
            }
        }
        throw new IllegalStateException("Branch '" + targetBranch + "' is not offered by the merge select");
    }

    public String getTagValueForType(String tagType) {
        openOverviewTab();
        return overview.getTagValueForType(tagType);
    }

    public String getStatus() {
        return extractOverviewField("Status", "Repository");
    }

    private String extractOverviewField(String label, String nextLabel) {
        openOverviewTab();
        return overview.extractField(label, nextLabel);
    }

    public List<String> getOverviewModuleNames() {
        openOverviewTab();
        return overview.getModuleNames();
    }

    public List<String> getOverviewMatchedModuleNames() {
        openOverviewTab();
        return overview.getMatchedModuleNames();
    }

    public ProjectDetailPage migrateOverviewDescriptor() {
        openOverviewTab();
        overview.migrateAndWaitUntilEditable();
        return this;
    }

    public boolean isOverviewMigrateOffered() {
        openOverviewTab();
        return overview.isMigrateOffered();
    }

    public boolean isOverviewEditOffered() {
        openOverviewTab();
        return overview.isEditOffered();
    }

    public boolean isOverviewMigrateEnabled() {
        openOverviewTab();
        return overview.isMigrateEnabled();
    }

    public ProjectDetailPage editOverviewAndSave() {
        openOverviewTab();
        overview.editAndSave();
        return this;
    }

    public ProjectDetailPage editOverviewDescriptionAndSave(String description) {
        openOverviewTab();
        overview.editDescriptionAndSave(description);
        return this;
    }

    public String getOverviewRevision() {
        return extractOverviewField("Revision ID", "Modified");
    }

    public String getOverviewLastChange() {
        return extractOverviewField("Modified", "Comment");
    }

    public String getModifiedBy() {
        openOverviewTab();
        return overview.getModifiedBy();
    }

    public String getModifiedAt() {
        openOverviewTab();
        return overview.getModifiedAt();
    }

    public String getLatestRevisionLabel() {
        return getModifiedBy() + ": " + getModifiedAt();
    }

    public ProjectDetailPage openRevision(String revisionLabel) {
        clickHeaderAction("Open Revision");
        openRevisionSelect.waitForVisible(DEFAULT_TIMEOUT_MS).click();
        openRevisionOption.format(revisionLabel).click();
        openRevisionSubmit.click();
        waitUntilSpinnerLoaded();
        return this;
    }

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

    public ExportProjectModalComponent openExportDialog() {
        clickHeaderAction("Export");
        exportProjectModalComponent.waitForDialogToAppear();
        return exportProjectModalComponent;
    }

    public String getOverviewPath() {
        return extractOverviewField("Path", "Branch");
    }

    public String getOverviewRepository() {
        return extractOverviewField("Repository", "Path");
    }

    public List<String> getRevisionDescriptions() {
        openHistoryTab();
        return history.getRevisionDescriptions();
    }

    public List<String> getRevisionAuthors() {
        openHistoryTab();
        return history.getRevisionAuthors();
    }

    public CompareGitRevisionsDialogComponent openCompareWindow() {
        Page compareWindow = openCompareScreen();
        return new CompareGitRevisionsDialogComponent(compareWindow);
    }

    private Page openCompareScreen() {
        Page compareWindow = page.waitForPopup(() -> clickHeaderAction("Compare"));
        settleCompareWindow(compareWindow);
        return compareWindow;
    }

    private void settleCompareWindow(Page compareWindow) {
        compareWindow.setViewportSize(COMPARE_WINDOW_WIDTH, COMPARE_WINDOW_HEIGHT);
        compareWindow.waitForLoadState();
        LOGGER.info("Compare window: url={}, title={}", compareWindow.url(), compareWindow.title());
        try {
            compareWindow.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE,
                    new Page.WaitForLoadStateOptions().setTimeout(DEFAULT_TIMEOUT_MS));
        } catch (RuntimeException ignored) {
        }
    }

    public CompareGitRevisionsDialogComponent openRevisionCompare() {
        Page compareWindow = openCompareScreen();
        CompareGitRevisionsDialogComponent compare = new CompareGitRevisionsDialogComponent(compareWindow);
        compare.selectRevision(0);
        compare.clickCompareBtn();
        return compare;
    }

    public String getLatestRevisionId() {
        openHistoryTab();
        return history.getLatestRevisionId();
    }

    public int getRevisionsCount() {
        openHistoryTab();
        return history.getRevisionsCount();
    }

    public ProjectDetailPage deleteFile(String fileName) {
        openFilesTab();
        files.deleteFile(fileName);
        return this;
    }

    public boolean isFilePresent(String fileName) {
        openFilesTab();
        return files.isNodePresent(fileName);
    }

    public ProjectDetailPage selectFile(String fileName) {
        openFilesTab();
        files.selectFile(fileName);
        return this;
    }

    public boolean isFilesTabOpen() {
        return files.isOpen(DEFAULT_TIMEOUT_MS);
    }

    public boolean isResourceNotFoundShown() {
        return files.isResourceNotFoundShown();
    }

    public boolean waitForFileSelectionDropped(String fileName) {
        return files.waitForFileSelectionDropped(fileName);
    }

    public long waitForFileTreeToList(String fileName) {
        return files.waitForTreeToList(fileName);
    }

    public String getSelectedFileFromUrl() {
        return files.selectedFileParam();
    }

    public String describeFilePaneState(String expectedFileName) {
        return files.describeFilePaneState(expectedFileName);
    }

    public boolean isFilePreviewEmptyShown() {
        return files.isFilePreviewEmptyShown();
    }

    public ProjectDetailPage uploadFile(String filePath) {
        return uploadFileAs(filePath, null);
    }

    public ProjectDetailPage uploadFileAs(String filePath, String targetName) {
        return uploadFileAs(filePath, targetName, null);
    }

    public ProjectDetailPage uploadFileInto(String filePath, String targetFolder) {
        return uploadFileAs(filePath, null, targetFolder);
    }

    public ProjectDetailPage uploadFileAs(String filePath, String targetName, String targetFolder) {
        openFilesTab();
        files.uploadFileAs(filePath, targetName, targetFolder);
        return this;
    }

    public boolean isAddFilesMenuAvailable() {
        openFilesTab();
        return files.isAddMenuAvailable();
    }

    public ProjectDetailPage updateFile(String fileName, String newFilePath) {
        pickUpdateFile(fileName, newFilePath);
        confirmUpdateFile();
        return this;
    }

    public ProjectDetailPage pickUpdateFile(String fileName, String newFilePath) {
        openFilesTab();
        files.pickUpdateFile(fileName, newFilePath);
        return this;
    }

    public ProjectDetailPage confirmUpdateFile() {
        files.confirmUpdateFile();
        return this;
    }

    public boolean isUpdateFileNameWarningShown() {
        return files.isUpdateFileNameWarningShown();
    }

    public ProjectDetailPage createFolder(String folderPath) {
        openFilesTab();
        files.createFolder(folderPath);
        return this;
    }

    public boolean isFolderPresent(String folderName) {
        openFilesTab();
        return files.isNodePresent(folderName);
    }

    public ProjectDetailPage reloadPage() {
        page.reload();
        waitUntilSpinnerLoaded();
        return this;
    }
}
