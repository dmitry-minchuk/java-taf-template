package domain.ui.webstudio.pages.mainpages;

import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.common.*;
import domain.ui.webstudio.components.createnewproject.OpenApiComponent;
import domain.ui.webstudio.components.repositorytabcomponents.*;
import domain.ui.webstudio.pages.BasePage;
import helpers.utils.WaitUtil;
import lombok.Getter;

import java.util.List;

@Getter
public class RepositoryPage extends BasePage {

    private TabSwitcherComponent tabSwitcherComponent;
    private WebElement refreshBtn;
    private WebElement createProjectLink;
    private ProjectsTableComponent projectsListTable;
    private ProjectsFilterRailComponent filterRail;
    private WebElement discardCloseConfirmBtn;
    private WebElement copyProjectNameField;
    private WebElement copyProjectSubmitBtn;
    private WebElement filterByNameInput;
    private CreateNewProjectComponent createNewProjectComponent;
    private ConfigureCommitInfoComponent configureCommitInfoComponent;
    private WebElement configureCommitInfoComponentShade;
    private DeployModalComponent deployModalComponent;
    private CopyProjectDialogComponent copyProjectDialogComponent;
    private WebElement errorNotification;
    private SyncChangesDialogComponent syncChangesDialogComponent;
    private BypassConfirmDialogComponent bypassConfirmDialogComponent;
    private ResolveConflictsDialogComponent resolveConflictsDialogComponent;
    private ProjectDeleteConfirmModalComponent projectDeleteConfirmModalComponent;
    private SaveProjectDialogComponent saveProjectDialogComponent;

    public RepositoryPage() {
        super();
        initializeComponents();
    }

    private void initializeComponents() {
        createProjectLink = new WebElement(page, "[data-testid=projects-new]", "createProjectLink");
        refreshBtn = new WebElement(page, "xpath=//a[@id='designRepoRefresh']", "refreshBtn");
        projectsListTable = new ProjectsTableComponent(page);
        filterRail = new ProjectsFilterRailComponent(page);
        discardCloseConfirmBtn = new WebElement(page, "[data-testid=discard-close-confirm]", "discardCloseConfirmBtn");
        copyProjectNameField = new WebElement(page, "[data-testid=copy-project-name]", "copyProjectNameField");
        copyProjectSubmitBtn = new WebElement(page, "[data-testid=copy-project-submit]", "copyProjectSubmitBtn");

        filterByNameInput = new WebElement(page, "[data-testid=projects-search]", "filterByNameInput");

        createNewProjectComponent = createScopedComponent(CreateNewProjectComponent.class, "xpath=//div[@id='modalNewProject_container']", "createNewProjectComponent");
        tabSwitcherComponent = createScopedComponent(TabSwitcherComponent.class, "xpath=//ul[@role='menu' and contains(@class,'ant-menu-horizontal')]", "tabSwitcherComponent");
        configureCommitInfoComponentShade = new WebElement(page, "xpath=//div[@role='dialog'][.//div[contains(@class,'ant-modal-title') and normalize-space()='Configure Git Commit Info']]", "modalShade");
        configureCommitInfoComponent = createScopedComponent(ConfigureCommitInfoComponent.class, "xpath=//div[@role='dialog'][.//div[contains(@class,'ant-modal-title') and normalize-space()='Configure Git Commit Info']]", "configureCommitInfoComponent");
        deployModalComponent = new DeployModalComponent();
        copyProjectDialogComponent = createScopedComponent(CopyProjectDialogComponent.class, "xpath=//div[@id='modalCopyProject_container']", "copyProjectDialogComponent");
        syncChangesDialogComponent = createScopedComponent(SyncChangesDialogComponent.class, "xpath=//div[@role='dialog' and .//form[@id='merge_branches_form']]", "syncChangesDialogComponent");
        bypassConfirmDialogComponent = new BypassConfirmDialogComponent();
        resolveConflictsDialogComponent = createScopedComponent(ResolveConflictsDialogComponent.class, "xpath=//div[@role='dialog' and .//span[text()='Resolve Conflicts']]", "resolveConflictsDialogComponent");
        projectDeleteConfirmModalComponent = new ProjectDeleteConfirmModalComponent();
        saveProjectDialogComponent = new SaveProjectDialogComponent();

        errorNotification = new WebElement(page, "xpath=(//div[contains(@class,'ant-notification-notice')])[1]", "errorNotification");
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
            openProjectsList();
            openIfClosed(projectName);
        }
    }

    private void openIfClosed(String projectName) {
        if (isProjectActionAvailable(projectName, "Open")) {
            openProject(projectName);
            waitUntilSpinnerLoaded();
        }
    }

    public void createProjectFromTemplateWithSelectRepo(String projectName, String templateName, String repositoryName) {
        createProject(CreateNewProjectComponent.TabName.TEMPLATE, projectName, templateName, false);
        createNewProjectComponent.selectRepository(repositoryName).clickCreate();
        fillCommitInfo();
        waitUntilSpinnerLoaded();
        openProjectsList();
        openIfClosed(projectName);
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

    public boolean isProjectPresent(String projectName) {
        return projectsListTable.isProjectPresent(projectName);
    }

    public void clickRowAction(String projectName, String actionLabel) {
        projectsListTable.clickRowAction(projectName, actionLabel);
    }

    public boolean isProjectActionAvailable(String projectName, String actionLabel) {
        return getProjectActionLabels(projectName).contains(actionLabel);
    }

    public ProjectDeleteConfirmModalComponent deleteProject(String projectName) {
        clickRowAction(projectName, "Delete");
        return projectDeleteConfirmModalComponent.waitForVisible();
    }

    public ProjectDetailPage openProjectDetail(String projectName) {
        projectsListTable.clickProjectName(projectName);
        return new ProjectDetailPage();
    }

    public ProjectDetailPage openProjectDetailByState(String projectName, boolean opened) {
        projectsListTable.clickRowByState(projectName, opened);
        return new ProjectDetailPage();
    }

    public void expandFilterGroup(String groupId) {
        filterRail.expandFilterGroup(groupId);
    }

    public void ensureRepoFilterChecked(String repositoryNameLower) {
        filterRail.ensureRepoFilterChecked(repositoryNameLower);
    }

    public void setStatusFilter(String status, boolean checked) {
        filterRail.setStatusFilter(status, checked);
    }

    public String getProjectStatusFromDetail(String projectName) {
        String status = openProjectDetail(projectName).getStatus();
        openProjectsList();
        return status;
    }

    public String getProjectBranchFromTable(String projectName) {
        return projectsListTable.getProjectBranch(projectName);
    }

    public RepositoryPage openProjectsList() {
        java.net.URI current = java.net.URI.create(page.url());
        page.navigate(current.getScheme() + "://" + current.getAuthority() + "/projects");
        waitUntilSpinnerLoaded();
        return this;
    }

    public void openProject(String projectName) {
        clickRowAction(projectName, "Open");
        projectsListTable.waitUntilOpened(projectName);
        waitUntilSpinnerLoaded();
    }

    public DeployModalComponent clickDeploy(String projectName) {
        clickRowAction(projectName, "Deploy");
        return deployModalComponent.waitForModal();
    }

    public boolean isDeployAvailable(String projectName) {
        return isProjectActionAvailable(projectName, "Deploy");
    }

    public void closeProject(String projectName) {
        clickRowAction(projectName, "Close");
        if (discardCloseConfirmBtn.isVisible(3000)) {
            discardCloseConfirmBtn.click();
        }
        waitUntilSpinnerLoaded();
    }

    public CopyProjectDialogComponent clickCopyAction(String projectName) {
        clickRowAction(projectName, "Copy");
        return copyProjectDialogComponent.waitForDialogToAppear();
    }

    public void clickRowActionByState(String projectName, boolean opened, String actionLabel) {
        projectsListTable.clickRowActionByState(projectName, opened, actionLabel);
    }

    public void openProjectByState(String projectName, boolean opened) {
        clickRowActionByState(projectName, opened, "Open");
    }

    public ProjectDeleteConfirmModalComponent deleteProjectByState(String projectName, boolean opened) {
        clickRowActionByState(projectName, opened, "Delete");
        return projectDeleteConfirmModalComponent.waitForVisible();
    }

    public CopyProjectDialogComponent clickCopyActionByState(String projectName, boolean opened) {
        clickRowActionByState(projectName, opened, "Copy");
        return copyProjectDialogComponent.waitForDialogToAppear();
    }

    public void copyProject(String projectName, String newProjectName) {
        clickCopyAction(projectName).setAsNewProject().setNewProjectName(newProjectName);
        copyProjectSubmitBtn.click();
        fillCommitInfo();
        waitUntilSpinnerLoaded();
    }

    public void copyProjectToBranch(String projectName, String branchName) {
        clickCopyAction(projectName).setBranchName(branchName);
        copyProjectSubmitBtn.click();
        fillCommitInfo();
        waitUntilSpinnerLoaded();
    }

    public void saveProject(String projectName, String comment) {
        clickRowAction(projectName, "Save");
        saveProjectDialogComponent.waitForVisible().setComment(comment).submit();
        waitUntilSpinnerLoaded();
    }

    public void saveProjectWithCommitInfo(String projectName, String comment) {
        clickRowAction(projectName, "Save");
        saveProjectDialogComponent.waitForVisible().setComment(comment).clickSubmit();
        fillCommitInfo();
        saveProjectDialogComponent.waitForSubmitHidden();
        waitUntilSpinnerLoaded();
    }

    public List<String> getAllVisibleProjectsInTable() {
        return projectsListTable.getAllVisibleProjectNames();
    }

    public void filterByName(String name) {
        filterByNameInput.click();
        filterByNameInput.fill(name);
        WaitUtil.sleep(800, "Waiting for the React client-side name filter to apply");
    }

    public void clearNameFilter() {
        filterByNameInput.click();
        filterByNameInput.fill("");
        WaitUtil.sleep(800, "Waiting for the filter to clear and the full list to restore");
    }

    public int countVisibleProjectsInTable() {
        return getAllVisibleProjectsInTable().size();
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
            openProjectsList();
            openIfClosed(projectName);
        }
    }

    public String getErrorNotification() {
        errorNotification.waitForVisible(DEFAULT_TIMEOUT_MS);
        return errorNotification.getText().trim();
    }

    public List<String> getProjectActionLabels(String projectName) {
        return projectsListTable.getProjectActionLabels(projectName);
    }
}
