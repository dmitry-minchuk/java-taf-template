package domain.ui.webstudio.components.createnewproject;

import configuration.core.ui.SmartWebElement;
import configuration.core.ui.BasePageComponent;
import helpers.utils.TestDataUtil;
import org.openqa.selenium.support.FindBy;

public class ZipArchiveComponent extends BasePageComponent {

    @FindBy(xpath = ".//div[@id='uploadProjectForm:file']//input[@accept='zip']")
    private SmartWebElement fileInputField;

    @FindBy(xpath = ".//input[@id='uploadProjectForm:projectName']")
    private SmartWebElement projectNameField;

    @FindBy(id = "uploadProjectForm:sbtZipsBtn")
    private SmartWebElement createProjectBtn;

    @FindBy(xpath = ".//input[@value='Cancel']")
    private SmartWebElement cancelBtn;

    public ZipArchiveComponent() {
    }

    public void createProjectZipArchive(String fileName, String projectName) {
        String absoluteFilePath = TestDataUtil.getFilePathFromResources(fileName);
        fileInputField.sendKeys(absoluteFilePath);
        projectNameField.sendKeys(5, projectName);
        createProjectBtn.click();
    }
}
