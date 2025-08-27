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
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
public class PlaywrightRepositoryPage extends PlaywrightProxyMainPage {

    private static final Logger LOGGER = LogManager.getLogger(PlaywrightRepositoryPage.class);
    
    private PlaywrightWebElement createProjectLink;
    private PlaywrightWebElement refreshBtn;
    private PlaywrightCreateNewProjectComponent createNewProjectComponent;
    private PlaywrightTabSwitcherComponent tabSwitcherComponent;
    private PlaywrightConfigureCommitInfoComponent configureCommitInfoComponent;
    private PlaywrightLeftRepositoryTreeComponent leftRepositoryTreeComponent;
    private PlaywrightRepositoryContentButtonsPanelComponent repositoryContentButtonsPanelComponent;

    public PlaywrightRepositoryPage() {
        super("/faces/pages/modules/repository/index.xhtml");
        initializeComponents();
    }

    private void initializeComponents() {
        createProjectLink = new PlaywrightWebElement(page, "xpath=//div[@id='top']//a[contains(text(), 'Create Project')]", "createProjectLink");
        refreshBtn = new PlaywrightWebElement(page, "xpath=//a[@id='designRepoRefresh']", "refreshBtn");
        createNewProjectComponent = createScopedComponent(PlaywrightCreateNewProjectComponent.class, "xpath=//div[@id='modalNewProject_container']", "createNewProjectComponent");
        tabSwitcherComponent = createScopedComponent(PlaywrightTabSwitcherComponent.class, "xpath=//ul[@role='menu' and contains(@class,'ant-menu-horizontal')]", "tabSwitcherComponent");
        configureCommitInfoComponent = createScopedComponent(PlaywrightConfigureCommitInfoComponent.class, "xpath=//div[@id='modalConfigureCommitInfo_container']", "configureCommitInfoComponent");
        leftRepositoryTreeComponent = createScopedComponent(PlaywrightLeftRepositoryTreeComponent.class, "xpath=//div[@id='repositoryTree']", "leftRepositoryTreeComponent");
        repositoryContentButtonsPanelComponent = createScopedComponent(PlaywrightRepositoryContentButtonsPanelComponent.class, "xpath=//div[@class='repository-buttons']", "repositoryContentButtonsPanelComponent");
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
        modalShade.waitForVisible();
        configureCommitInfoComponent.fillCommitInfoWithRandomData();
        refreshBtn.click();
    }

    public void refresh() {
        refreshBtn.click();
    }
}