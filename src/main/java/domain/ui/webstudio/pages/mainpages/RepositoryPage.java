package domain.ui.webstudio.pages.mainpages;

import com.microsoft.playwright.Locator;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.common.*;
import domain.ui.webstudio.components.createnewproject.ExcelFilesComponent;
import domain.ui.webstudio.components.createnewproject.OpenApiComponent;
import domain.ui.webstudio.components.createnewproject.TemplateTabComponent;
import domain.ui.webstudio.components.createnewproject.WorkspaceComponent;
import domain.ui.webstudio.components.createnewproject.ZipArchiveComponent;
import domain.ui.webstudio.components.editortabcomponents.ExportProjectDialogComponent;
import domain.ui.webstudio.components.repositorytabcomponents.*;
import domain.ui.webstudio.components.repositorytabcomponents.ConfirmEraseDialogComponent;
import domain.ui.webstudio.pages.BasePage;
import helpers.utils.WaitUtil;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
public class RepositoryPage extends BasePage {

    private static final Logger LOGGER = LogManager.getLogger(RepositoryPage.class);

    private TabSwitcherComponent tabSwitcherComponent;
    // top menu elements:
    private WebElement refreshBtn;
    private WebElement createProjectLink;
    // Inline row buttons render with the row itself, so a short probe decides inline-vs-overflow.
    private static final int ROW_ACTION_PROBE_MS = DEFAULT_TIMEOUT_MS / 5;
    // aria-label of the row's overflow trigger — a menu opener, not an action of its own.
    private static final String OVERFLOW_TRIGGER_LABEL = "Actions";
    // Filter-rail group ids (6.4.0): each group collapses, and its rows leave the DOM while collapsed.
    private static final String REPOSITORY_FILTER_GROUP = "repository";
    private static final String STATUS_FILTER_GROUP = "status";

    // React projects list (build 032c60a664ce+): rows are <tr data-testid=project-row-...> with
    // per-row action buttons keyed by aria-label. Format placeholders with the project name.
    private WebElement projectRowByName;
    private WebElement projectNameInRow;
    private WebElement projectActionByName;
    private WebElement projectRowMoreBtn;
    private WebElement projectActionByNameAndState;
    private WebElement projectRowMoreBtnByState;
    private WebElement overflowMenuItem;
    private WebElement discardCloseConfirmBtn;
    private WebElement copyProjectNameField;
    private WebElement copyProjectSubmitBtn;
    private WebElement filterByNameInput;
    private WebElement filterGroupToggle;
    private WebElement filterGroupShow;
    private WebElement filterRepoCheckbox;
    private WebElement filterStatusCheckbox;
    private WebElement clearFilterBtn;
    private WebElement advancedFilterBtn;
    private WebElement hideDeletedCheckbox;
    private WebElement applyFilterBtn;
    // other components:
    private CreateNewProjectComponent createNewProjectComponent;
    private ConfigureCommitInfoComponent configureCommitInfoComponent;
    private WebElement configureCommitInfoComponentShade;
    private LeftRepositoryTreeComponent leftRepositoryTreeComponent;
    private RepositoryContentButtonsPanelComponent repositoryContentButtonsPanelComponent;
    private RepositoryContentTabSwitcherComponent repositoryContentTabSwitcherComponent;
    private DeployModalComponent deployModalComponent;
    private TagsPopupComponent tagsPopupComponent;
    private MissingTagsPopupComponent missingTagsPopupComponent;
    private CopyProjectDialogComponent copyProjectDialogComponent;
    private UploadFileDialogComponent uploadFileDialogComponent;
    private UpdateFileDialogComponent updateFileDialogComponent;
    private CompareGitRevisionsDialogComponent compareGitRevisionsDialogComponent;
    private WebElement confirmOpeningDialogBtn;
    private WebElement confirmOpeningDialogShade;
    private WebElement messagePopupText;
    private WebElement messagePopupOkBtn;
    private WebElement inlineMessage;
    private SaveChangesComponent saveChangesComponent;
    private SyncChangesDialogComponent syncChangesDialogComponent;
    private BypassConfirmDialogComponent bypassConfirmDialogComponent;
    private ResolveConflictsDialogComponent resolveConflictsDialogComponent;
    private ConfirmDeleteDialogComponent confirmDeleteDialogComponent;
    private ConfirmUndeleteDialogComponent confirmUndeleteDialogComponent;
    private ConfirmCloseProjectDialogComponent confirmCloseProjectDialogComponent;
    private FileChangedWarningComponent fileChangedWarningComponent;
    private TableComponent projectsTable;
    private ExportProjectDialogComponent exportProjectDialogComponent;
    private ConfirmEraseDialogComponent confirmEraseDialogComponent;
    private AddFolderDialogComponent addFolderDialogComponent;
    private ProjectDeleteConfirmModalComponent projectDeleteConfirmModalComponent;
    private SaveProjectDialogComponent saveProjectDialogComponent;
    private WebElement projectDeployAction;

    public RepositoryPage() {
        super();
        initializeComponents();
    }

    private void initializeComponents() {
        createProjectLink = new WebElement(page, "[data-testid=projects-new]", "createProjectLink");
        refreshBtn = new WebElement(page, "xpath=//a[@id='designRepoRefresh']", "refreshBtn");
        projectRowByName = new WebElement(page, "xpath=//tr[starts-with(@data-testid,'project-row')][.//span[normalize-space()='%s']]", "projectRow");
        // Click the name, not the row: a row also holds a branch switcher, and a row-wide click can hit it.
        projectNameInRow = new WebElement(page, "xpath=//tr[starts-with(@data-testid,'project-row')]//span[normalize-space()='%s']", "projectNameInRow");
        projectActionByName = new WebElement(page, "xpath=//tr[starts-with(@data-testid,'project-row')][.//span[normalize-space()='%s']]//button[@aria-label='%s']", "projectRowAction");
        projectDeployAction = new WebElement(page, "xpath=//tr[starts-with(@data-testid,'project-row')][.//span[normalize-space()='%s']]//button[starts-with(@data-testid,'project-action-deploy-')]", "projectDeployAction");
        projectRowMoreBtn = new WebElement(page, "xpath=//tr[starts-with(@data-testid,'project-row')][.//span[normalize-space()='%s']]//button[starts-with(@data-testid,'project-actions-')]", "projectRowMoreBtn");
        projectActionByNameAndState = new WebElement(page, "xpath=//tr[starts-with(@data-testid,'project-row')][.//span[normalize-space()='%s']][.//button[@aria-label='%s']]//button[@aria-label='%s']", "projectRowActionByState");
        projectRowMoreBtnByState = new WebElement(page, "xpath=//tr[starts-with(@data-testid,'project-row')][.//span[normalize-space()='%s']][.//button[@aria-label='%s']]//button[starts-with(@data-testid,'project-actions-')]", "projectRowMoreBtnByState");
        overflowMenuItem = new WebElement(page, "xpath=//div[contains(@class,'ant-dropdown')][not(contains(@class,'ant-dropdown-hidden'))]//li[contains(@class,'ant-dropdown-menu-item')][normalize-space()='%s']", "overflowMenuItem");
        discardCloseConfirmBtn = new WebElement(page, "[data-testid=discard-close-confirm]", "discardCloseConfirmBtn");
        copyProjectNameField = new WebElement(page, "[data-testid=copy-project-name]", "copyProjectNameField");
        copyProjectSubmitBtn = new WebElement(page, "[data-testid=copy-project-submit]", "copyProjectSubmitBtn");

        filterByNameInput = new WebElement(page, "[data-testid=projects-search]", "filterByNameInput");
        filterGroupToggle = new WebElement(page, "[data-testid=filter-toggle-%s]", "filterGroupToggle");
        filterGroupShow = new WebElement(page, "[data-testid=filter-show-%s]", "filterGroupShow");
        filterRepoCheckbox = new WebElement(page, "[data-testid=filter-repo-%s]", "filterRepoCheckbox");
        filterStatusCheckbox = new WebElement(page, "[data-testid=filter-status-%s]", "filterStatusCheckbox");
        clearFilterBtn = new WebElement(page, "xpath=//span[@id='clearFilter']", "clearFilterBtn");
        advancedFilterBtn = new WebElement(page, "xpath=//a[@id='filterButton']", "advancedFilterBtn");
        hideDeletedCheckbox = new WebElement(page, "xpath=//input[@id='filterForm:hideDeleted']", "hideDeletedCheckbox");
        applyFilterBtn = new WebElement(page, "xpath=//form[@id='filterForm']//input[@value='Apply']", "applyFilterBtn");

        createNewProjectComponent = createScopedComponent(CreateNewProjectComponent.class, "xpath=//div[@id='modalNewProject_container']", "createNewProjectComponent");
        tabSwitcherComponent = createScopedComponent(TabSwitcherComponent.class, "xpath=//ul[@role='menu' and contains(@class,'ant-menu-horizontal')]", "tabSwitcherComponent");
        configureCommitInfoComponentShade = new WebElement(page, "xpath=//div[@role='dialog'][.//div[contains(@class,'ant-modal-title') and normalize-space()='Configure Git Commit Info']]", "modalShade");
        configureCommitInfoComponent = createScopedComponent(ConfigureCommitInfoComponent.class, "xpath=//div[@role='dialog'][.//div[contains(@class,'ant-modal-title') and normalize-space()='Configure Git Commit Info']]", "configureCommitInfoComponent");
        leftRepositoryTreeComponent = createScopedComponent(LeftRepositoryTreeComponent.class, "xpath=//div[@id='left']", "leftRepositoryTreeComponent");
        repositoryContentButtonsPanelComponent = createScopedComponent(RepositoryContentButtonsPanelComponent.class, "xpath=//div[@class='nav-panel']", "repositoryContentButtonsPanelComponent");
        repositoryContentTabSwitcherComponent = createScopedComponent(RepositoryContentTabSwitcherComponent.class, "xpath=//div[@id='nodeTabPanel']", "repositoryContentTabSwitcherComponent");
        deployModalComponent = new DeployModalComponent();
        tagsPopupComponent = createScopedComponent(TagsPopupComponent.class, "xpath=//div[@id='modalCreateProjectTags_container']", "tagsPopupComponent");
        missingTagsPopupComponent = createScopedComponent(MissingTagsPopupComponent.class, "xpath=//div[@id='modalConfirmIgnoreNonApplicableTags_container']", "tagsPopupComponent");
        projectsTable = createScopedComponent(TableComponent.class, "xpath=//table[contains(@class,'rf-dt table filtered-table')]", "projectsTable");
        copyProjectDialogComponent = createScopedComponent(CopyProjectDialogComponent.class, "xpath=//div[@id='modalCopyProject_container']", "copyProjectDialogComponent");
        uploadFileDialogComponent = createScopedComponent(UploadFileDialogComponent.class, "xpath=//div[@id='modalNewFile_container']", "uploadFileDialogComponent");
        updateFileDialogComponent = createScopedComponent(UpdateFileDialogComponent.class, "xpath=//div[@id='modalUpdateFile_container']", "updateFileDialogComponent");
        saveChangesComponent = createScopedComponent(SaveChangesComponent.class, "xpath=//div[@id='modalSave_container']", "Save Changes Component");
        syncChangesDialogComponent = createScopedComponent(SyncChangesDialogComponent.class, "xpath=//div[@role='dialog' and .//form[@id='merge_branches_form']]", "syncChangesDialogComponent");
        bypassConfirmDialogComponent = new BypassConfirmDialogComponent();
        resolveConflictsDialogComponent = createScopedComponent(ResolveConflictsDialogComponent.class, "xpath=//div[@role='dialog' and .//span[text()='Resolve Conflicts']]", "resolveConflictsDialogComponent");
        confirmDeleteDialogComponent = new ConfirmDeleteDialogComponent();
        addFolderDialogComponent = createScopedComponent(AddFolderDialogComponent.class, "xpath=//div[@id='modalNewFolder_container']", "addFolderDialogComponent");
        confirmUndeleteDialogComponent = createScopedComponent(ConfirmUndeleteDialogComponent.class, "xpath=//div[@id='modalUndeleteProject_container']", "confirmUndeleteDialogComponent");
        confirmCloseProjectDialogComponent = createScopedComponent(ConfirmCloseProjectDialogComponent.class, "xpath=//div[@id='modalCloseProject_container']", "confirmCloseProjectDialogComponent");
        fileChangedWarningComponent = createScopedComponent(FileChangedWarningComponent.class, "xpath=//div[@id='fileChanged_content']", "fileChangedWarningComponent");
        exportProjectDialogComponent = createScopedComponent(ExportProjectDialogComponent.class, "xpath=//div[@id='exportProject_container']", "exportProjectDialogComponent");
        confirmEraseDialogComponent = createScopedComponent(ConfirmEraseDialogComponent.class, "xpath=//div[@id='modalEraseProject_container']", "confirmEraseDialogComponent");
        projectDeleteConfirmModalComponent = new ProjectDeleteConfirmModalComponent();
        saveProjectDialogComponent = new SaveProjectDialogComponent();

        confirmOpeningDialogBtn = new WebElement(page, "//div[@id='modalOpenProject_container' and not(ancestor::div[contains(@style, 'display: none;')])]//input[@value='Open Project']", "confirmOpeningDialogBtn");
        confirmOpeningDialogShade = new WebElement(page, "xpath=//div[@id='modalOpenProject_shade']", "confirmOpeningDialogShade");
        messagePopupText = new WebElement(page, "xpath=//div[@id='messagePopup_container']//span[@id='messagePopupText']", "messagePopupText");
        messagePopupOkBtn = new WebElement(page, "xpath=//div[@id='messagePopup_container']//input[@value='OK']", "messagePopupOkBtn");
        inlineMessage = new WebElement(page, "xpath=//div[@id='top']//div[@class='messages']", "inlineMessage");
    }

    public void createProject(CreateNewProjectComponent.TabName projectType, String projectName, String sourceName) {
        createProject(projectType, projectName, sourceName, true);
    }

    public void createProject(CreateNewProjectComponent.TabName projectType, String projectName, String sourceName, boolean finalize) {
        createProjectLink.click();
        switch (projectType) {
            case EXCEL_FILES:
                createNewProjectComponent.createProjectFromExcel(sourceName, projectName);
                break;
            case ZIP_ARCHIVE:
                createNewProjectComponent.createProjectFromZip(sourceName, projectName);
                break;
            case TEMPLATE:
                createNewProjectComponent.createProjectFromTemplate(sourceName, projectName, finalize);
                break;
            case OPEN_API:
                OpenApiComponent openApiComponent = createNewProjectComponent.selectTab(projectType);
                openApiComponent.uploadOpenApiFile(sourceName);
                openApiComponent.setProjectName(projectName);
                openApiComponent.clickCreate();
                break;
            default:
                throw new IllegalArgumentException("Unsupported project type: " + projectType);
        }

        if(finalize) {
            fillCommitInfo();
            waitUntilSpinnerLoaded();
            // 6.4.0 lands on the new project's detail screen, which has no row actions — go back to the list.
            openProjectsList();
            openIfClosed(projectName);
        }
    }

    // The React repository leaves a freshly created project CLOSED (the legacy UI opened it on create),
    // so the editor workspace stays empty. Open it into the workspace so callers can select its modules.
    private void openIfClosed(String projectName) {
        if (isProjectActionAvailable(projectName, "Open")) {
            openProject(projectName);
            waitUntilSpinnerLoaded();
        }
    }

    public void createProjectFromTemplateWithSelectRepo(String projectName, String templateName, String repositoryName) {
        createProjectLink.click();
        TemplateTabComponent templateComponent = createNewProjectComponent.selectTab(CreateNewProjectComponent.TabName.TEMPLATE);
        templateComponent.selectProjectTemplate(templateName);
        templateComponent.setProjectName(projectName);
        templateComponent.selectRepository(repositoryName);
        templateComponent.createProject();
        fillCommitInfo();
        waitUntilSpinnerLoaded();
        refreshBtn.click(DEFAULT_TIMEOUT_MS);
    }

    public void fillCommitInfo() {
        waitUntilSpinnerLoaded();
        if (configureCommitInfoComponentShade.isVisible(3000))
            configureCommitInfoComponent.fillCommitInfoWithRandomData();
    }

    public void refresh() {
        refreshBtn.click(DEFAULT_TIMEOUT_MS);
    }

    public void reloadPage() {
        page.reload();
        waitUntilSpinnerLoaded();
    }

    // --- React projects list actions (build 032c60a664ce+) ---

    public boolean isProjectPresent(String projectName) {
        return projectRowByName.format(projectName).isVisible(DEFAULT_TIMEOUT_MS);
    }

    /**
     * Clicks a row action by its label, wherever the row keeps it. Studio 6.4.0 shows only
     * Copy / Delete Branch / Open / Close as buttons and folds Save, Open Revision, Sync, Deploy,
     * Compare, Export and Delete into the row's overflow menu.
     */
    public void clickRowAction(String projectName, String actionLabel) {
        WebElement inlineAction = projectActionByName.format(projectName, actionLabel);
        if (inlineAction.isVisible(ROW_ACTION_PROBE_MS)) {
            inlineAction.click();
            return;
        }
        projectRowMoreBtn.format(projectName).click();
        overflowMenuItem.format(actionLabel).click();
    }

    // A closed project exposes the "Open" row action; an opened one exposes "Close".
    public boolean isProjectActionAvailable(String projectName, String actionLabel) {
        return getProjectActionLabels(projectName).contains(actionLabel);
    }

    // Clicks the row's Delete action and returns the (already React) confirm modal, ready to fill.
    public ProjectDeleteConfirmModalComponent deleteProject(String projectName) {
        clickRowAction(projectName, "Delete");
        return projectDeleteConfirmModalComponent.waitForVisible();
    }

    // Opens the React project-detail view (Overview/Files/History/...) by clicking the project row.
    public ProjectDetailPage openProjectDetail(String projectName) {
        projectNameInRow.format(projectName).click();
        return new ProjectDetailPage();
    }

    // When two rows share a name (e.g. the same project name copied across repositories), disambiguate by
    // state: the OPEN row exposes a "Close" action, the CLOSED row exposes an "Open" action. Opens that row's
    // detail. Replaces the legacy tree's selectOpened/ClosedItemInFolder.
    public ProjectDetailPage openProjectDetailByState(String projectName, boolean opened) {
        String action = opened ? "Close" : "Open";
        page.locator(String.format(
                "xpath=//tr[starts-with(@data-testid,'project-row')][.//span[normalize-space()='%s']][.//button[@aria-label='%s']]",
                projectName, action)).first().click();
        return new ProjectDetailPage();
    }

    /**
     * Opens a filter-rail group so its checkboxes exist. Studio 6.4.0 lets each group be collapsed (its rows
     * are then absent from the DOM, not merely hidden) and even hidden away behind a "show" button.
     */
    public void expandFilterGroup(String groupId) {
        WebElement showBtn = filterGroupShow.format(groupId);
        if (showBtn.isVisible(ROW_ACTION_PROBE_MS)) {
            showBtn.click();
        }
        WebElement toggle = filterGroupToggle.format(groupId);
        if (toggle.isVisible(ROW_ACTION_PROBE_MS) && "false".equals(toggle.getAttribute("aria-expanded"))) {
            toggle.click();
        }
    }

    // The React projects list is repo-filtered via checkboxes in the filter rail (filter-repo-<name>, lowercase).
    // Ensures the given repository's projects are shown (checks the box only if not already checked).
    public void ensureRepoFilterChecked(String repositoryNameLower) {
        expandFilterGroup(REPOSITORY_FILTER_GROUP);
        WebElement checkbox = filterRepoCheckbox.format(repositoryNameLower);
        if (checkbox.isVisible(ROW_ACTION_PROBE_MS) && !checkbox.isChecked()) {
            checkbox.click();
            waitUntilSpinnerLoaded();
        }
    }

    /**
     * Ticks (or unticks) a status facet — LOCAL / OPENED / EDITING / VIEWING_VERSION / CLOSED / DELETED.
     * A status no project is currently in is not offered at all, so this is a no-op for an absent facet.
     */
    public void setStatusFilter(String status, boolean checked) {
        expandFilterGroup(STATUS_FILTER_GROUP);
        WebElement checkbox = filterStatusCheckbox.format(status);
        if (checkbox.isVisible(ROW_ACTION_PROBE_MS) && checkbox.isChecked() != checked) {
            checkbox.click();
            waitUntilSpinnerLoaded();
        }
    }

    // React has no status column in the projects table — status lives in the project detail. This reads it
    // and returns to the list so callers can keep using row actions. (Legacy getProjectStatusFromTable.)
    public String getProjectStatusFromDetail(String projectName) {
        String status = openProjectDetail(projectName).getStatus();
        openProjectsList();
        return status;
    }

    // Return to the projects list from a project-detail view (the detail has no row actions), so callers
    // can chain list operations (open/close/delete) after inspecting a project's detail.
    public RepositoryPage openProjectsList() {
        java.net.URI current = java.net.URI.create(page.url());
        page.navigate(current.getScheme() + "://" + current.getAuthority() + "/projects");
        waitUntilSpinnerLoaded();
        return this;
    }

    // Opened projects expose "Close"; closed ones expose "Open".
    public void openProject(String projectName) {
        clickRowAction(projectName, "Open");
    }

    // Opens the React DeployModal via the project row's Deploy action (rocket icon, testid
    // project-action-deploy-<id>) — only present when a deployment/production repository is configured.
    // Replaces the legacy RepositoryContentButtonsPanelComponent.clickDeploy.
    public DeployModalComponent clickDeploy(String projectName) {
        clickRowAction(projectName, "Deploy");
        return deployModalComponent.waitForModal();
    }

    // Whether the project row exposes the Deploy action (ACL: needs >= Viewer on design + Edit on deploy repo).
    public boolean isDeployAvailable(String projectName) {
        return isProjectActionAvailable(projectName, "Deploy");
    }

    public void closeProject(String projectName) {
        clickRowAction(projectName, "Close");
        // Closing a project with uncommitted local changes prompts a "Discard unsaved changes?" confirm.
        if (discardCloseConfirmBtn.isVisible(3000)) {
            discardCloseConfirmBtn.click();
        }
        waitUntilSpinnerLoaded();
    }

    // Opens the React copy dialog via the row "Copy" action and returns it for full control
    // (repository / path / name), for multi-repo copy flows. Use when the name is unique in the list.
    public CopyProjectDialogComponent clickCopyAction(String projectName) {
        clickRowAction(projectName, "Copy");
        return copyProjectDialogComponent.waitForDialogToAppear();
    }

    /**
     * Clicks a row action on a same-name row picked out by its state (an opened row offers "Close", a
     * closed one "Open"), looking inline first and then in that row's overflow menu.
     */
    public void clickRowActionByState(String projectName, boolean opened, String actionLabel) {
        String stateAction = opened ? "Close" : "Open";
        WebElement inlineAction = projectActionByNameAndState.format(projectName, stateAction, actionLabel);
        if (inlineAction.isVisible(ROW_ACTION_PROBE_MS)) {
            inlineAction.click();
            return;
        }
        projectRowMoreBtnByState.format(projectName, stateAction).click();
        overflowMenuItem.format(actionLabel).click();
    }

    // Open a same-name project disambiguated by state (only a closed row can be opened).
    public void openProjectByState(String projectName, boolean opened) {
        clickRowActionByState(projectName, opened, "Open");
    }

    // Delete a same-name project disambiguated by state; returns the (React) confirm modal.
    public ProjectDeleteConfirmModalComponent deleteProjectByState(String projectName, boolean opened) {
        clickRowActionByState(projectName, opened, "Delete");
        return projectDeleteConfirmModalComponent.waitForVisible();
    }

    // Same, but disambiguates when two rows share a name: opened row exposes "Close", closed row "Open".
    public CopyProjectDialogComponent clickCopyActionByState(String projectName, boolean opened) {
        clickRowActionByState(projectName, opened, "Copy");
        return copyProjectDialogComponent.waitForDialogToAppear();
    }

    // Copy a project into a NEW project via the row "Copy" action. On a branching repository the dialog
    // opens in branch mode, so switch it to new-project mode before naming the copy.
    public void copyProject(String projectName, String newProjectName) {
        clickCopyAction(projectName).setAsNewProject().setNewProjectName(newProjectName);
        copyProjectSubmitBtn.click();
        fillCommitInfo();
        waitUntilSpinnerLoaded();
    }

    // Copy a project into a NEW BRANCH (the copy dialog's default mode on a git repository).
    public void copyProjectToBranch(String projectName, String branchName) {
        clickCopyAction(projectName).setBranchName(branchName);
        copyProjectSubmitBtn.click();
        fillCommitInfo();
        waitUntilSpinnerLoaded();
    }

    // Commits a project's uncommitted local changes via the row "Save" action → "Save project" dialog
    // (the Save action only appears while the project has local changes). Replaces the legacy
    // buttons-panel Save + SaveChangesComponent flow.
    public void saveProject(String projectName, String comment) {
        clickRowAction(projectName, "Save");
        saveProjectDialogComponent.waitForVisible().setComment(comment).submit();
        waitUntilSpinnerLoaded();
    }

    // Like saveProject, but also handles the "Configure Git Commit Info" modal raised on a user's FIRST commit
    // (the save dialog stays open behind it until the identity is filled).
    public void saveProjectWithCommitInfo(String projectName, String comment) {
        clickRowAction(projectName, "Save");
        saveProjectDialogComponent.waitForVisible().setComment(comment).clickSubmit();
        fillCommitInfo();
        saveProjectDialogComponent.waitForSubmitHidden();
        waitUntilSpinnerLoaded();
    }

    public void createProjectFromWorkSpace(String projectName, String repository, boolean selectAllProjects) {
        createProjectLink.click();
        WorkspaceComponent workspaceComponent = createNewProjectComponent.selectTab(CreateNewProjectComponent.TabName.WORKSPACE);
        if(!selectAllProjects && repository != null)
            workspaceComponent.selectProject(projectName).selectRepository(repository).save();
        else if(!selectAllProjects)
            workspaceComponent.selectProject(projectName).save();
        else if(repository == null)
            workspaceComponent.selectAllProjects().save();
        else
            workspaceComponent.selectAllProjects().selectRepository(repository).save();
    }

    public void unlockAllProjects() {
        WaitUtil.waitForCondition(() -> projectsTable.isVisible() && projectsTable.getRowsCount() > 0, 1000, 250, "Waiting for projects to load");

        int unlockCount = 0;
        while (true) {
            List<TableComponent.PlaywrightTableRowComponent> allRows = projectsTable.getRows();
            LOGGER.info("Checking table: {} rows", allRows.size());

            if (allRows.isEmpty()) {
                break;
            }

            boolean foundLockedProject = false;
            int rowNum = 0;
            for (TableComponent.PlaywrightTableRowComponent row : allRows) {
                rowNum++;
                List<WebElement> cells = row.getCells();

                if (cells.size() == 6) {
                    // Get project name from first cell for logging
                    String projectName = cells.get(0).getLocator().textContent();

                    WebElement lastCell = cells.get(5);
                    Locator openOrCloseBtn = lastCell.getLocator().locator("xpath=.//a/img[@class='actionImage' and contains(@src,'repository')]");

                    int buttonCount = openOrCloseBtn.count();
                    LOGGER.debug("Row {}: '{}' button count = {}", rowNum, projectName, buttonCount);

                    if (buttonCount > 0) {
                        String altText = openOrCloseBtn.getAttribute("alt");
                        LOGGER.debug("Row {}: '{}' alt = '{}'", rowNum, projectName, altText);

                        if (altText != null && altText.equalsIgnoreCase("Open")) {
                            LOGGER.info("Unlocking project '{}' at row {}", projectName, rowNum);
                            openOrCloseBtn.click();

                            if (WaitUtil.waitForCondition(() -> confirmOpeningDialogBtn.isVisible(), 500, 100, "Waiting for Confirmation Popup")) {
                                confirmOpeningDialogBtn.click();
                                confirmOpeningDialogShade.waitForHidden(3000);
                            }

                            unlockCount++;
                            foundLockedProject = true;
                            WaitUtil.sleep(500, "Wait for table to refresh after unlock");
                            break;
                        }
                    }
                }
            }

            if (!foundLockedProject) {
                LOGGER.info("No more locked projects found. Total unlocked: {}", unlockCount);
                break;
            }
        }
    }

    // React projects list (build 032c60a664ce+): rows are <tr data-testid=project-row-...> with the
    // project name in the row's first <span> — the legacy RichFaces filtered-table this used to scrape
    // no longer exists (it returned an empty list on React).
    public List<String> getAllVisibleProjectsInTable() {
        List<String> projectNames = new ArrayList<>();
        Locator rows = page.locator("xpath=//tr[starts-with(@data-testid,'project-row')]");
        WaitUtil.waitForCondition(() -> rows.count() > 0, 5000, 250, "Waiting for projects to load");
        int count = rows.count();
        for (int i = 0; i < count; i++) {
            // Only count rows that are actually visible — the React name filter hides non-matching rows.
            if (!rows.nth(i).isVisible()) {
                continue;
            }
            String name = rows.nth(i).locator("span").first().textContent();
            if (name != null && !name.trim().isEmpty()) {
                projectNames.add(name.trim());
            }
        }
        return projectNames;
    }

    public CopyProjectDialogComponent clickCopyProjectInTable(String projectName) {
        LOGGER.info("Clicking Copy button for project '{}' in table", projectName);
        int rowIndex = findProjectRowIndex(projectName);
        if (rowIndex == -1) {
            throw new RuntimeException("Project '" + projectName + "' not found in projects table");
        }

        TableComponent.PlaywrightTableRowComponent row = projectsTable.getRow(rowIndex);
        List<WebElement> cells = row.getCells();
        WebElement lastCell = cells.get(cells.size() - 1);
        lastCell.getLocator().locator("xpath=.//a/img[@alt='Copy']").click();

        copyProjectDialogComponent.waitForDialogToAppear();
        return copyProjectDialogComponent;
    }

    public Map<String, String> getProjectInfoFromTable(String projectName) {
        LOGGER.info("Getting project info for '{}' from table", projectName);
        Map<String, String> projectInfo = new java.util.HashMap<>();

        int rowIndex = findProjectRowIndex(projectName);
        if (rowIndex == -1) {
            throw new RuntimeException("Project '" + projectName + "' not found in projects table");
        }

        List<String> headers = projectsTable.getHeaders();
        List<String> rowValues = projectsTable.getRow(rowIndex).getValue();

        for (int i = 0; i < headers.size() && i < rowValues.size(); i++) {
            String headerText = headers.get(i).trim();
            String cellValue = rowValues.get(i);
            projectInfo.put(headerText, cellValue);
        }

        return projectInfo;
    }

    public void filterByName(String name) {
        // React projects-search is a plain <input> that filters the list on value change; a single fill()
        // (not per-char fillSequentially) reliably drives its onChange.
        filterByNameInput.click();
        filterByNameInput.fill(name);
        WaitUtil.sleep(800, "Waiting for the React client-side name filter to apply");
    }

    public void clearNameFilter() {
        // fill("") fires the React onChange (a bare clear() may not), restoring the full list.
        filterByNameInput.click();
        filterByNameInput.fill("");
        WaitUtil.sleep(800, "Waiting for the filter to clear and the full list to restore");
    }

    // Deleted projects are a status facet in the React rail, not a separate "show deleted" toggle.
    public void setShowDeletedProjects(boolean showDeleted) {
        setStatusFilter("DELETED", showDeleted);
    }

    public int countVisibleProjectsInTable() {
        return getAllVisibleProjectsInTable().size();
    }

    private int findProjectRowIndex(String projectName) {
        List<TableComponent.PlaywrightTableRowComponent> rows = projectsTable.getRows();
        for (int i = 0; i < rows.size(); i++) {
            List<String> rowValues = rows.get(i).getValue();
            if (!rowValues.isEmpty() && rowValues.getFirst().trim().equals(projectName)) {
                return i + 1;
            }
        }
        return -1;
    }

    public void createProjectFromOpenApi(String fileName, String projectName) {
        createProjectFromOpenApi(fileName, projectName, true);
    }

    public void createProjectFromOpenApi(String fileName, String projectName, boolean finalize) {
        createProjectLink.click();
        createNewProjectComponent.createProjectFromOpenApi(fileName, projectName, true);
        if (finalize) {
            fillCommitInfo();
            waitUntilSpinnerLoaded();
            openIfClosed(projectName);
        }
    }

    public String getMessagePopupText() {
        messagePopupText.waitForVisible(DEFAULT_TIMEOUT_MS);
        return messagePopupText.getText();
    }

    public void closeMessagePopup() {
        messagePopupOkBtn.click();
    }

    public String getInlineMessage() {
        inlineMessage.waitForVisible(DEFAULT_TIMEOUT_MS);
        return inlineMessage.getText().trim();
    }

    /**
     * Every action a project's row offers (Open/Close/Copy/Export/Delete/Deploy/...), taking both the
     * inline buttons and the overflow menu into account — 6.4.0 keeps most actions behind the menu, so
     * the inline buttons alone are not the project's permission set.
     */
    public List<String> getProjectActionLabels(String projectName) {
        List<String> labels = new ArrayList<>();
        Locator btns = page.locator(String.format(
                "xpath=//tr[starts-with(@data-testid,'project-row')][.//span[normalize-space()='%s']]//button[@aria-label]",
                projectName));
        WaitUtil.waitForCondition(() -> btns.count() > 0, DEFAULT_TIMEOUT_MS, 250, "Waiting for project row actions");
        int count = btns.count();
        for (int i = 0; i < count; i++) {
            String label = btns.nth(i).getAttribute("aria-label");
            if (label != null && !label.isEmpty() && !OVERFLOW_TRIGGER_LABEL.equals(label)) {
                labels.add(label);
            }
        }
        if (projectRowMoreBtn.format(projectName).isVisible(ROW_ACTION_PROBE_MS)) {
            projectRowMoreBtn.format(projectName).click();
            Locator items = page.locator(
                    "xpath=//div[contains(@class,'ant-dropdown')][not(contains(@class,'ant-dropdown-hidden'))]"
                            + "//li[contains(@class,'ant-dropdown-menu-item')]");
            WaitUtil.waitForCondition(() -> items.count() > 0, DEFAULT_TIMEOUT_MS, 250, "Waiting for the row overflow menu");
            labels.addAll(items.allInnerTexts().stream().map(String::trim).filter(text -> !text.isEmpty()).toList());
            page.keyboard().press("Escape");
        }
        return labels;
    }

    public List<String> getTableActionTitles(String projectName) {
        List<String> titles = new ArrayList<>();
        int rowIndex = findProjectRowIndex(projectName);
        if (rowIndex == -1) return titles;
        TableComponent.PlaywrightTableRowComponent row = projectsTable.getRow(rowIndex);
        List<WebElement> cells = row.getCells();
        WebElement actionsCell = cells.get(cells.size() - 1);
        com.microsoft.playwright.Locator links = actionsCell.getLocator().locator("xpath=.//a[@title]");
        for (int i = 0; i < links.count(); i++) {
            String title = links.nth(i).getAttribute("title");
            if (title != null && !title.isEmpty()) {
                titles.add(title);
            }
        }
        return titles;
    }

    public boolean isTableActionButtonPresent(String projectName, String actionTitle) {
        int rowIndex = findProjectRowIndex(projectName);
        if (rowIndex == -1) return false;
        TableComponent.PlaywrightTableRowComponent row = projectsTable.getRow(rowIndex);
        List<WebElement> cells = row.getCells();
        WebElement actionsCell = cells.get(cells.size() - 1);
        return actionsCell.getLocator().locator(String.format("xpath=.//a[@title='%s']", actionTitle)).count() > 0;
    }

    public void clickTableActionButton(String projectName, String actionTitle) {
        int rowIndex = findProjectRowIndex(projectName);
        if (rowIndex == -1) {
            throw new RuntimeException("Project '" + projectName + "' not found in projects table");
        }
        TableComponent.PlaywrightTableRowComponent row = projectsTable.getRow(rowIndex);
        List<WebElement> cells = row.getCells();
        WebElement actionsCell = cells.get(cells.size() - 1);
        actionsCell.getLocator().locator(String.format("xpath=.//a[@title='%s']", actionTitle)).click();
        WaitUtil.sleep(500, "Waiting after table action button click");
    }

    public String getProjectStatusFromTable(String projectName) {
        int rowIndex = findProjectRowIndex(projectName);
        if (rowIndex == -1) {
            throw new RuntimeException("Project '" + projectName + "' not found in projects table");
        }
        List<String> headers = projectsTable.getHeaders();
        int statusColIndex = headers.indexOf("Status");
        return projectsTable.getRow(rowIndex).getCells().get(statusColIndex).getText().trim();
    }
}