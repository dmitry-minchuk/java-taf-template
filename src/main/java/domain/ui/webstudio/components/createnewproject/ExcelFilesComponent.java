package domain.ui.webstudio.components.createnewproject;

import configuration.core.SmartWebElement;
import domain.ui.BasePageComponent;
import helpers.utils.FileUtil;
import org.openqa.selenium.support.FindBy;

public class ExcelFilesComponent extends BasePageComponent {

    @FindBy(xpath = ".//div[@id='createProjectFormFiles:file']//input[@class='rf-fu-inp']")
    private SmartWebElement fileInputField;

    @FindBy(xpath = ".//input[@id='createProjectFormFiles:projectName']")
    private SmartWebElement projectNameField;

    @FindBy(id = "createProjectFormFiles:sbtFilesBtn")
    private SmartWebElement createProjectBtn;

    @FindBy(xpath = ".//input[@value='Cancel']")
    private SmartWebElement cancelBtn;

    public ExcelFilesComponent() {
    }

    public void createProjectFromExcelFile(String fileName, String projectName) {
        String absoluteFilePath = FileUtil.getFilePathFromResources(fileName);
        fileInputField.sendKeys(absoluteFilePath);
        projectNameField.sendKeys(projectName);
        createProjectBtn.click();
    }
}
