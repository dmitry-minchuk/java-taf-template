package domain.ui.webstudio.components.createnewproject;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import helpers.utils.TestDataUtil;

// Playwright version of ExcelFilesComponent for file upload functionality validation
public class PlaywrightExcelFilesComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement fileInputField;
    private PlaywrightWebElement projectNameField;
    private PlaywrightWebElement createProjectBtn;
    private PlaywrightWebElement cancelBtn;

    public PlaywrightExcelFilesComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightExcelFilesComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        // File input field: "xpath=.//div[@id='createProjectFormFiles:file']//input[@accept='xls, xlsx, xlsm']"
        fileInputField = createScopedElement("xpath=.//div[@id='createProjectFormFiles:file']//input[@accept='xls, xlsx, xlsm']", "fileInputField");
        
        // Project name field: "xpath=.//input[@id='createProjectFormFiles:projectName']"
        projectNameField = createScopedElement("xpath=.//input[@id='createProjectFormFiles:projectName']", "projectNameField");
        
        // Create project button: "#createProjectFormFiles\:sbtFilesBtn"
        createProjectBtn = createScopedElement("#createProjectFormFiles\\:sbtFilesBtn", "createProjectBtn");
        
        // Cancel button: "xpath=.//input[@value='Cancel']"
        cancelBtn = createScopedElement("xpath=.//input[@value='Cancel']", "cancelBtn");
    }

    // CRITICAL METHOD for file upload validation across LOCAL/DOCKER modes
    public void createProjectFromExcelFile(String fileName, String projectName) {
        String filePath = TestDataUtil.getFilePathFromResources(fileName);
        fileInputField.setInputFiles(filePath);
        projectNameField.fill(projectName);
        createProjectBtn.click();
    }
}