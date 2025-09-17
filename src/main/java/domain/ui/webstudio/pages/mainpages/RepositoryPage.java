package domain.ui.webstudio.pages.mainpages;

import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.common.ConfigureCommitInfoComponent;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.common.TableComponent;
import domain.ui.webstudio.components.createnewproject.ExcelFilesComponent;
import domain.ui.webstudio.components.createnewproject.TemplateTabComponent;
import domain.ui.webstudio.components.createnewproject.ZipArchiveComponent;
import domain.ui.webstudio.components.repositorytabcomponents.LeftRepositoryTreeComponent;
import domain.ui.webstudio.components.repositorytabcomponents.RepositoryContentButtonsPanelComponent;
import domain.ui.webstudio.components.repositorytabcomponents.RepositoryContentTabPropertiesComponent;
import domain.ui.webstudio.components.repositorytabcomponents.DeployConfigurationTabsComponent;
import domain.ui.webstudio.pages.BasePage;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

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
    private RepositoryContentTabPropertiesComponent repositoryContentTabPropertiesComponent;
    private DeployConfigurationTabsComponent deployConfigurationTabsComponent;

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
        repositoryContentTabPropertiesComponent = createScopedComponent(RepositoryContentTabPropertiesComponent.class, "xpath=//span[@id='propertiesContent']", "repositoryContentTabPropertiesComponent");
        deployConfigurationTabsComponent = createScopedComponent(DeployConfigurationTabsComponent.class, "xpath=//div[@id='content']", "deployConfigurationTabsComponent");
        projectsTable = createScopedComponent(TableComponent.class, "xpath=//table[contains(@class,'rf-dt table')]", "projectsTable");
    }

    public void createProject(CreateNewProjectComponent.TabName projectType, String projectName, String sourceName) {
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

        if(configureCommitInfoComponentShade.isVisible(3000))
            configureCommitInfoComponent.fillCommitInfoWithRandomData();
        refreshBtn.click(10000);
    }

    public void refresh() {
        refreshBtn.click(DEFAULT_TIMEOUT_MS);
    }

    public void createProjectFromTemplate(String projectName, String templateName) {
        createProject(CreateNewProjectComponent.TabName.TEMPLATE, projectName, templateName);
    }

    public void createDeployConfiguration(String configName) {
        createDeployConfigBtn.click();
        configNameField.fillSequentially(configName);
        createBtn.click();
        refreshBtn.click(DEFAULT_TIMEOUT_MS);
    }

    public void unlockAllProjects() {
        for (int i = 1; i <= projectsTable.getRowsCount(); i++) {
            List<WebElement> cells = projectsTable.getRow(i).getCells();
            WebElement lastCell = cells.getLast();
            if (lastCell.getText().contains("Close")) {
                lastCell.click();
            }
        }
    }

    public List<String> getAllVisibleProjectsInTable() {
        List<String> projectNames = new ArrayList<>();
        for (int i = 1; i <= projectsTable.getRowsCount(); i++) {
            List<String> rowValues = projectsTable.getRow(i).getValue();
            if (!rowValues.isEmpty() && !rowValues.getFirst().isEmpty()) {
                projectNames.add(rowValues.getFirst().trim());
            }
        }
        return projectNames;
    }
}