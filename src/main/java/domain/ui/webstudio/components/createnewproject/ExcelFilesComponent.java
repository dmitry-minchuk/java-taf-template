package domain.ui.webstudio.components.createnewproject;

import configuration.core.ui.SmartWebElement;
import configuration.core.ui.BasePageComponent;
import helpers.utils.TestDataUtil;
import org.openqa.selenium.support.FindBy;

public class ExcelFilesComponent extends BasePageComponent {

    @FindBy(xpath = ".//div[@id='createProjectFormFiles:file']//input[@accept='xls, xlsx, xlsm']")
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
        String absoluteFilePath = TestDataUtil.getFilePathFromResources(fileName);
        fileInputField.sendKeys(absoluteFilePath);
        projectNameField.sendKeys(5, projectName);
        createProjectBtn.click();
    }
}
