package domain.ui.webstudio.components.createnewproject;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.TestDataUtil;

public class ExcelFilesComponent extends BaseComponent {

    private WebElement fileInputField;
    private WebElement projectNameField;
    private WebElement createProjectBtn;
    private WebElement cancelBtn;

    public ExcelFilesComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public ExcelFilesComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        fileInputField = createScopedElement("xpath=.//div[@id='createProjectFormFiles:file']//input[@accept='xls, xlsx, xlsm']", "fileInputField");
        projectNameField = createScopedElement("xpath=.//input[@id='createProjectFormFiles:projectName']", "projectNameField");
        createProjectBtn = createScopedElement("#createProjectFormFiles\\:sbtFilesBtn", "createProjectBtn");
        cancelBtn = createScopedElement("xpath=.//input[@value='Cancel']", "cancelBtn");
    }

    public void createProjectFromExcelFile(String fileName, String projectName) {
        String filePath = TestDataUtil.getFilePathFromResources(fileName);
        fileInputField.setInputFiles(filePath);
        projectNameField.clear();
        projectNameField.fillSequentially(projectName);
        createProjectBtn.click();
    }
}