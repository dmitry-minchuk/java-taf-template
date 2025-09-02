package domain.ui.webstudio.pages.mainpages;

import configuration.core.ui.PlaywrightWebElement;
import domain.ui.webstudio.components.PlaywrightConfigureCommitInfoComponent;
import domain.ui.webstudio.components.PlaywrightCreateNewProjectComponent;
import domain.ui.webstudio.components.PlaywrightTabSwitcherComponent;
import domain.ui.webstudio.components.createnewproject.PlaywrightExcelFilesComponent;
import domain.ui.webstudio.components.createnewproject.PlaywrightTemplateTabComponent;
import domain.ui.webstudio.components.createnewproject.PlaywrightZipArchiveComponent;
import domain.ui.webstudio.components.repositorytabcomponents.PlaywrightLeftRepositoryTreeComponent;
import domain.ui.webstudio.components.repositorytabcomponents.PlaywrightRepositoryContentButtonsPanelComponent;
import domain.ui.webstudio.components.repositorytabcomponents.PlaywrightRepositoryContentTabPropertiesComponent;
import domain.ui.webstudio.components.repositorytabcomponents.PlaywrightDeployConfigurationTabsComponent;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
public class PlaywrightRepositoryPage extends PlaywrightProxyMainPage {

    private static final Logger LOGGER = LogManager.getLogger(PlaywrightRepositoryPage.class);

    private PlaywrightTabSwitcherComponent tabSwitcherComponent;
    // top menu elements:
    private PlaywrightWebElement refreshBtn;
    private PlaywrightWebElement createProjectLink;
    private PlaywrightWebElement createDeployConfigBtn;
    // createDeployConfigBtn-related elements (I did not create a component for that):
    private PlaywrightWebElement configNameField;
    private PlaywrightWebElement createBtn;
    // other components:
    private PlaywrightCreateNewProjectComponent createNewProjectComponent;
    private PlaywrightConfigureCommitInfoComponent configureCommitInfoComponent;
    private PlaywrightLeftRepositoryTreeComponent leftRepositoryTreeComponent;
    private PlaywrightRepositoryContentButtonsPanelComponent repositoryContentButtonsPanelComponent;
    private PlaywrightRepositoryContentTabPropertiesComponent repositoryContentTabPropertiesComponent;
    private PlaywrightDeployConfigurationTabsComponent deployConfigurationTabsComponent;

    public PlaywrightRepositoryPage() {
        super("/faces/pages/modules/repository/index.xhtml");
        initializeComponents();
    }

    private void initializeComponents() {
        createProjectLink = new PlaywrightWebElement(page, "xpath=//div[@id='top']//a[contains(text(), 'Create Project')]", "createProjectLink");
        refreshBtn = new PlaywrightWebElement(page, "xpath=//a[@id='designRepoRefresh']", "refreshBtn");
        createDeployConfigBtn = new PlaywrightWebElement(page, "xpath=//a[contains(text(),'Create Deploy Configuration')]", "createDeployConfigBtnLocator");
        configNameField = new PlaywrightWebElement(page, "xpath=//input[@id='newDProjectForm:projectName']", "configNameFieldLocator");
        createBtn = new PlaywrightWebElement(page, "xpath=//input[@id='newDProjectForm:createBtn']", "createBtnLocator");

        createNewProjectComponent = createScopedComponent(PlaywrightCreateNewProjectComponent.class, "xpath=//div[@id='modalNewProject_container']", "createNewProjectComponent");
        tabSwitcherComponent = createScopedComponent(PlaywrightTabSwitcherComponent.class, "xpath=//ul[@role='menu' and contains(@class,'ant-menu-horizontal')]", "tabSwitcherComponent");
        configureCommitInfoComponent = createScopedComponent(PlaywrightConfigureCommitInfoComponent.class, "xpath=//div[@id='modalConfigureCommitInfo_container']", "configureCommitInfoComponent");
        leftRepositoryTreeComponent = createScopedComponent(PlaywrightLeftRepositoryTreeComponent.class, "xpath=//div[@id='left']", "leftRepositoryTreeComponent");
        repositoryContentButtonsPanelComponent = createScopedComponent(PlaywrightRepositoryContentButtonsPanelComponent.class, "xpath=//div[@class='nav-panel']", "repositoryContentButtonsPanelComponent");
        repositoryContentTabPropertiesComponent = createScopedComponent(PlaywrightRepositoryContentTabPropertiesComponent.class, "xpath=//span[@id='propertiesContent']", "repositoryContentTabPropertiesComponent");
        deployConfigurationTabsComponent = createScopedComponent(PlaywrightDeployConfigurationTabsComponent.class, "xpath=//div[@id='content']", "deployConfigurationTabsComponent");
    }

    public void createProject(PlaywrightCreateNewProjectComponent.TabName projectType, String projectName, String sourceName) {
        createProjectLink.click();
        switch (projectType) {
            case EXCEL_FILES:
                PlaywrightExcelFilesComponent excelComponent = createNewProjectComponent.selectTab(projectType);
                excelComponent.createProjectFromExcelFile(sourceName, projectName);
                break;
            case ZIP_ARCHIVE:
                PlaywrightZipArchiveComponent zipComponent = createNewProjectComponent.selectTab(projectType);
                zipComponent.createProjectZipArchive(sourceName, projectName);
                break;
            case TEMPLATE:
                PlaywrightTemplateTabComponent templateComponent = createNewProjectComponent.selectTab(projectType);
                templateComponent.createProjectFromTemplate(projectName, sourceName);
                break;
            default:
                throw new IllegalArgumentException("Unsupported project type: " + projectType);
        }

        PlaywrightWebElement modalShade = new PlaywrightWebElement(page, "xpath=//div[@id='modalConfigureCommitInfo_shade']", "modalShade");
        if(modalShade.isVisible(3000))
            configureCommitInfoComponent.fillCommitInfoWithRandomData();
        refreshBtn.click();
    }

    public void refresh() {
        refreshBtn.click();
    }

    public void createProjectFromTemplate(String projectName, String templateName) {
        createProject(PlaywrightCreateNewProjectComponent.TabName.TEMPLATE, projectName, templateName);
    }

    public void createDeployConfiguration(String configName) {
        createDeployConfigBtn.click();
        configNameField.fillSequentially(configName);
        createBtn.click();
        refreshBtn.click();
    }
}