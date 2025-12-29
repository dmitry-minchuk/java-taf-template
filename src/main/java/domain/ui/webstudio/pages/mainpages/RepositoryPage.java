package domain.ui.webstudio.pages.mainpages;

import com.microsoft.playwright.Locator;
import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.common.*;
import domain.ui.webstudio.components.createnewproject.ExcelFilesComponent;
import domain.ui.webstudio.components.createnewproject.TemplateTabComponent;
import domain.ui.webstudio.components.createnewproject.WorkspaceComponent;
import domain.ui.webstudio.components.createnewproject.ZipArchiveComponent;
import domain.ui.webstudio.components.repositorytabcomponents.*;
import domain.ui.webstudio.pages.BasePage;
import helpers.utils.WaitUtil;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class RepositoryPage extends BasePage {

    private static final Logger LOGGER = LogManager.getLogger(RepositoryPage.class);

    private TabSwitcherComponent tabSwitcherComponent;
    // top menu elements:
    private WebElement refreshBtn;
    private WebElement createProjectLink;
    private WebElement createDeployConfigBtn;
    // createDeployConfigBtn-related elements (I did not create a component for that):
    private WebElement configNameField;
    private WebElement createBtn;
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
    private CompareDialogComponent compareDialogComponent;
    private WebElement confirmOpeningDialogBtn;
    private SaveChangesComponent saveChangesComponent;
    private SyncChangesDialogComponent syncChangesDialogComponent;
    private ConfirmDeleteDialogComponent confirmDeleteDialogComponent;
    private TableComponent projectsTable;

    public RepositoryPage() {
        super();
        initializeComponents();
    }

    private void initializeComponents() {
        createProjectLink = new WebElement(page, "xpath=//div[@id='top']//a[contains(text(), 'Create Project')]", "createProjectLink");
        refreshBtn = new WebElement(page, "xpath=//a[@id='designRepoRefresh']", "refreshBtn");
        createDeployConfigBtn = new WebElement(page, "xpath=//a[contains(text(),'Create Deploy Configuration')]", "createDeployConfigBtnLocator");
        configNameField = new WebElement(page, "xpath=//input[@id='newDProjectForm:projectName']", "configNameFieldLocator");
        createBtn = new WebElement(page, "xpath=//input[@id='newDProjectForm:createBtn']", "createBtnLocator");

        createNewProjectComponent = createScopedComponent(CreateNewProjectComponent.class, "xpath=//div[@id='modalNewProject_container']", "createNewProjectComponent");
        tabSwitcherComponent = createScopedComponent(TabSwitcherComponent.class, "xpath=//ul[@role='menu' and contains(@class,'ant-menu-horizontal')]", "tabSwitcherComponent");
        configureCommitInfoComponentShade = new WebElement(page, "xpath=//div[@id='modalConfigureCommitInfo_shade']", "modalShade");
        configureCommitInfoComponent = createScopedComponent(ConfigureCommitInfoComponent.class, "xpath=//div[@id='modalConfigureCommitInfo_container']", "configureCommitInfoComponent");
        leftRepositoryTreeComponent = createScopedComponent(LeftRepositoryTreeComponent.class, "xpath=//div[@id='left']", "leftRepositoryTreeComponent");
        repositoryContentButtonsPanelComponent = createScopedComponent(RepositoryContentButtonsPanelComponent.class, "xpath=//div[@class='nav-panel']", "repositoryContentButtonsPanelComponent");
        repositoryContentTabSwitcherComponent = createScopedComponent(RepositoryContentTabSwitcherComponent.class, "xpath=//div[@id='nodeTabPanel']", "repositoryContentTabSwitcherComponent");
        deployModalComponent = new DeployModalComponent();
        tagsPopupComponent = createScopedComponent(TagsPopupComponent.class, "xpath=//div[@id='modalCreateProjectTags_container']", "tagsPopupComponent");
        missingTagsPopupComponent = createScopedComponent(MissingTagsPopupComponent.class, "xpath=//div[@id='modalConfirmIgnoreNonApplicableTags_container']", "tagsPopupComponent");
        projectsTable = createScopedComponent(TableComponent.class, "xpath=//table[contains(@class,'rf-dt table filtered-table')]", "projectsTable");
        copyProjectDialogComponent = createScopedComponent(CopyProjectDialogComponent.class, "xpath=//div[@id='modalCopyProject_container']", "copyProjectDialogComponent");
        uploadFileDialogComponent = createScopedComponent(UploadFileDialogComponent.class, "xpath=//div[@id='modalNewFile_container']", "uploadFileDialogComponent");
        saveChangesComponent = createScopedComponent(SaveChangesComponent.class, "xpath=//div[@id='modalSave_container']", "Save Changes Component");
        syncChangesDialogComponent = createScopedComponent(SyncChangesDialogComponent.class, "xpath=//div[@id='modalMergeBranches_container']", "syncChangesDialogComponent");
        confirmDeleteDialogComponent = createScopedComponent(ConfirmDeleteDialogComponent.class, "xpath=//div[@id='modalDeleteNode_container']", "confirmDeleteDialogComponent");

        confirmOpeningDialogBtn = new WebElement(page, "//div[@id='modalOpenProject_container' and not(ancestor::div[contains(@style, 'display: none;')])]//input[@value='Open Project']", "confirmOpeningDialogBtn");
    }

    public void createProject(CreateNewProjectComponent.TabName projectType, String projectName, String sourceName) {
        createProject(projectType, projectName, sourceName, true);
    }

    public void createProject(CreateNewProjectComponent.TabName projectType, String projectName, String sourceName, boolean finalize) {
        createProjectLink.click();
        switch (projectType) {
            case EXCEL_FILES:
                ExcelFilesComponent excelComponent = createNewProjectComponent.selectTab(projectType);
                excelComponent.createProjectFromExcelFile(sourceName, projectName);
                break;
            case ZIP_ARCHIVE:
                ZipArchiveComponent zipComponent = createNewProjectComponent.selectTab(projectType);
                zipComponent.createProjectZipArchive(sourceName, projectName);
                break;
            case TEMPLATE:
                TemplateTabComponent templateComponent = createNewProjectComponent.selectTab(projectType);
                templateComponent.createProjectFromTemplate(projectName, sourceName);
                break;
            default:
                throw new IllegalArgumentException("Unsupported project type: " + projectType);
        }

        if(finalize) {
            fillCommitInfo();
            waitUntilSpinnerLoaded();
            refreshBtn.click(10000);
        }
    }

    public void fillCommitInfo() {
        waitUntilSpinnerLoaded();
        if (configureCommitInfoComponentShade.isVisible(3000))
            configureCommitInfoComponent.fillCommitInfoWithRandomData();
    }

    public void refresh() {
        refreshBtn.click(DEFAULT_TIMEOUT_MS);
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

    public void createDeployConfiguration(String configName) {
        createDeployConfigBtn.click();
        configNameField.fillSequentially(configName);
        createBtn.click();
        refreshBtn.click(DEFAULT_TIMEOUT_MS);
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
                    WebElement lastCell = cells.get(5);
                    Locator openOrCloseBtn = lastCell.getLocator().locator("xpath=.//a/img[@class='actionImage' and contains(@src,'repository')]");

                    int buttonCount = openOrCloseBtn.count();
                    LOGGER.debug("Row {}: button count = {}", rowNum, buttonCount);

                    if (buttonCount > 0) {
                        String altText = openOrCloseBtn.getAttribute("alt");
                        LOGGER.debug("Row {}: alt = '{}'", rowNum, altText);

                        if (altText != null && altText.equalsIgnoreCase("Open")) {
                            LOGGER.info("Unlocking project at row {}", rowNum);
                            openOrCloseBtn.click();

                            if (WaitUtil.waitForCondition(() -> confirmOpeningDialogBtn.isVisible(), 500, 100, "Waiting for Confirmation Popup"))
                                confirmOpeningDialogBtn.click();

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

    public List<String> getAllVisibleProjectsInTable() {
        List<String> projectNames = new ArrayList<>();
        for (int i = 1; i <= projectsTable.getRows().size(); i++) {
            List<String> rowValues = projectsTable.getRow(i).getValue();
            if (!rowValues.isEmpty() && !rowValues.getFirst().isEmpty())
                projectNames.add(rowValues.getFirst().trim());
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
}