package domain.ui.webstudio.pages.mainpages;

import configuration.core.ui.SmartWebElement;
import domain.ui.webstudio.components.ConfigureCommitInfoComponent;
import domain.ui.webstudio.components.CreateNewProjectComponent;
import domain.ui.webstudio.components.createnewproject.ExcelFilesComponent;
import domain.ui.webstudio.components.createnewproject.TemplateTabComponent;
import domain.ui.webstudio.components.createnewproject.ZipArchiveComponent;
import org.openqa.selenium.support.FindBy;

public class RepositoryPage extends ProxyMainPage {

    @FindBy(xpath = "//div[@id='top']//a[contains(text(), 'Create Project')]")
    private SmartWebElement createProjectLink;

    @FindBy(xpath = "//a[@id='designRepoRefresh']")
    private SmartWebElement refreshBtn;

    @FindBy(xpath = "//div[@id='modalNewProject_container']")
    private CreateNewProjectComponent createNewProjectComponent;

    @FindBy(xpath = "//div[@id='modalConfigureCommitInfo_container']")
    private ConfigureCommitInfoComponent configureCommitInfoComponent;

    public RepositoryPage() {
        super("/faces/pages/modules/repository/index.xhtml");
    }

    public CreateNewProjectComponent clickCreateNewProjectLink() {
        createProjectLink.click();
        return createNewProjectComponent;
    }

    public void createProject(CreateNewProjectComponent.TabName projectType, String projectName, String sourceName) {
        createProjectLink.click();
        switch (projectType) {
            case EXCEL_FILES:
                ExcelFilesComponent excelFilesComponent = createNewProjectComponent.selectTab(projectType);
                excelFilesComponent.createProjectFromExcelFile(sourceName, projectName);
                break;
            case ZIP_ARCHIVE:
                ZipArchiveComponent zipArchiveComponent = createNewProjectComponent.selectTab(projectType);
                zipArchiveComponent.createProjectZipArchive(sourceName, projectName);
                break;
            case TEMPLATE:
                TemplateTabComponent templateTabComponent = createNewProjectComponent.selectTab(projectType);
                templateTabComponent.createProjectFromTemplate(projectName, sourceName);
                break;
            default:
                throw new IllegalArgumentException("Unsupported project type: " + projectType);
        }
        if (configureCommitInfoComponent.isPresent()) {
            configureCommitInfoComponent.fillCommitInfoWithRandomData();
        }
        refreshBtn.click();
    }

    public void createProjectFromExcelFile(String projectName, String fileName) {
        createProjectLink.click();
        ExcelFilesComponent excelFilesComponent = createNewProjectComponent.selectTab(CreateNewProjectComponent.TabName.EXCEL_FILES);
        excelFilesComponent.createProjectFromExcelFile(fileName, projectName);
        if(configureCommitInfoComponent.isPresent())
            configureCommitInfoComponent.fillCommitInfoWithRandomData();
        refreshBtn.click(6);
    }

    public void createProjectFromZipArchive(String projectName, String fileName) {
        createProjectLink.click();
        ZipArchiveComponent zipArchiveComponent = createNewProjectComponent.selectTab(CreateNewProjectComponent.TabName.ZIP_ARCHIVE);
        zipArchiveComponent.createProjectZipArchive(fileName, projectName);
        if(configureCommitInfoComponent.isPresent())
            configureCommitInfoComponent.fillCommitInfoWithRandomData();
        refreshBtn.click();
    }

    public void createProjectFromTemplate(String projectName, String templateName) {
        createProjectLink.click();
        TemplateTabComponent templateTabComponent = createNewProjectComponent.selectTab(CreateNewProjectComponent.TabName.TEMPLATE);
        templateTabComponent.createProjectFromTemplate(projectName, templateName);
        if(configureCommitInfoComponent.isPresent())
            configureCommitInfoComponent.fillCommitInfoWithRandomData();
        refreshBtn.click();
    }

    public void refresh() {
        refreshBtn.click();
    }
}
