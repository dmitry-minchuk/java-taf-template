package domain.ui.webstudio.pages.mainpages;

import configuration.core.ui.PlaywrightWebElement;
import domain.ui.webstudio.components.CreateNewProjectComponent;
import domain.ui.webstudio.components.PlaywrightConfigureCommitInfoComponent;
import domain.ui.webstudio.components.PlaywrightCreateNewProjectComponent;
import domain.ui.webstudio.components.PlaywrightTabSwitcherComponent;
import domain.ui.webstudio.components.createnewproject.PlaywrightExcelFilesComponent;
import domain.ui.webstudio.components.createnewproject.PlaywrightZipArchiveComponent;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Playwright version of RepositoryPage - Main repository interface for project creation
 * URL: /faces/pages/modules/repository/index.xhtml
 * Contains TabSwitcherComponent and CreateNewProjectComponent for complete workflow
 */
@Getter
public class PlaywrightRepositoryPage extends PlaywrightProxyMainPage {

    private static final Logger LOGGER = LogManager.getLogger(PlaywrightRepositoryPage.class);
    
    private PlaywrightWebElement createProjectLink;
    private PlaywrightWebElement refreshBtn;
    private PlaywrightCreateNewProjectComponent createNewProjectComponent;
    private PlaywrightTabSwitcherComponent tabSwitcherComponent;
    private PlaywrightConfigureCommitInfoComponent configureCommitInfoComponent;

    public PlaywrightRepositoryPage() {
        super("/faces/pages/modules/repository/index.xhtml");
        initializeComponents();
    }

    private void initializeComponents() {
        // Create Project link: "//div[@id='top']//a[contains(text(), 'Create Project')]"
        createProjectLink = new PlaywrightWebElement(page, "xpath=//div[@id='top']//a[contains(text(), 'Create Project')]", "createProjectLink");
        
        // Refresh button: "//a[@id='designRepoRefresh']"
        refreshBtn = new PlaywrightWebElement(page, "xpath=//a[@id='designRepoRefresh']", "refreshBtn");
        
        // Create New Project modal component: "//div[@id='modalNewProject_container']"
        PlaywrightWebElement modalLocator = new PlaywrightWebElement(page, "xpath=//div[@id='modalNewProject_container']", "createNewProjectComponent");
        createNewProjectComponent = new PlaywrightCreateNewProjectComponent(modalLocator);
            
        // Tab switcher component for EDITOR/REPOSITORY navigation
        PlaywrightWebElement tabLocator = new PlaywrightWebElement(page, "xpath=//ul[contains(@class,'nav-tabs')]", "tabSwitcherComponent");
        tabSwitcherComponent = new PlaywrightTabSwitcherComponent(tabLocator);
        
        // Configure commit info modal component: "//div[@id='modalConfigureCommitInfo_container']"
        // Use exact same selector as original @FindBy(xpath = "//div[@id='modalConfigureCommitInfo_container']")
        PlaywrightWebElement commitInfoLocator = new PlaywrightWebElement(page, "xpath=//div[@id='modalConfigureCommitInfo_container']", "configureCommitInfoComponent");
        configureCommitInfoComponent = new PlaywrightConfigureCommitInfoComponent(commitInfoLocator);
    }

    // Main project creation method - validates file upload across LOCAL/DOCKER modes
    public void createProject(CreateNewProjectComponent.TabName projectType, String projectName, String sourceName) {
        // Step 1: Click Create Project link to open modal
        createProjectLink.click();
        
        // Step 2: Handle different project types
        switch (projectType) {
            case EXCEL_FILES:
                PlaywrightExcelFilesComponent excelComponent = createNewProjectComponent.selectTab(projectType);
                excelComponent.createProjectFromExcelFile(sourceName, projectName);
                break;
            case ZIP_ARCHIVE:
                PlaywrightZipArchiveComponent zipComponent = createNewProjectComponent.selectTab(projectType);
                zipComponent.createProjectZipArchive(sourceName, projectName);
                break;
            default:
                throw new IllegalArgumentException("Unsupported project type: " + projectType);
        }
        
        // Step 3: Handle commit configuration modal if present (like original RepositoryPage)
        PlaywrightWebElement modalShade = new PlaywrightWebElement(page, "xpath=//div[@id='modalConfigureCommitInfo_shade']", "modalShade");
        modalShade.waitForVisible();
        configureCommitInfoComponent.fillCommitInfoWithRandomData();
        refreshBtn.click();
    }

    public void createProjectFromExcelFile(String projectName, String fileName) {
        createProject(CreateNewProjectComponent.TabName.EXCEL_FILES, projectName, fileName);
    }

    public void createProjectFromZipArchive(String projectName, String fileName) {
        createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, projectName, fileName);
    }

    public void refresh() {
        refreshBtn.click();
    }
}