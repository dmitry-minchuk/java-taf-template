package domain.ui.webstudio.pages.mainpages;

import configuration.core.SmartWebElement;
import domain.ui.webstudio.components.CreateNewProjectComponent;
import domain.ui.webstudio.components.createnewproject.ExcelFilesComponent;
import helpers.utils.StringUtil;
import org.openqa.selenium.support.FindBy;

public class RepositoryPage extends ProxyMainPage {

    @FindBy(xpath = "//div[@id='top']//a[contains(text(), 'Create Project')]")
    private SmartWebElement createProjectLink;

    @FindBy(xpath = "//a[@id='designRepoRefresh']")
    private SmartWebElement refreshBtn;

    @FindBy(xpath = "//div[@id='modalNewProject_container']")
    private CreateNewProjectComponent createNewProjectComponent;

    public RepositoryPage() {
        super("/faces/pages/modules/repository/index.xhtml");
    }

    public CreateNewProjectComponent clickCreateNewProjectLink() {
        createProjectLink.click();
        return createNewProjectComponent;
    }

    public void createProjectFromExcelFile(String projectName, String fileName) {
        createProjectLink.click();
        ExcelFilesComponent excelFilesComponent = createNewProjectComponent.selectTab(CreateNewProjectComponent.TabName.EXCEL_FILES);
        excelFilesComponent.createProjectFromExcelFile(fileName, projectName);
        refreshBtn.click();
    }

    public void refresh() {
        refreshBtn.click();
    }
}
