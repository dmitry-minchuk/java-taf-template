package domain.ui.webstudio.components.createnewproject;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import helpers.utils.TestDataUtil;

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
        fileInputField = createScopedElement("xpath=.//div[@id='createProjectFormFiles:file']//input[@accept='xls, xlsx, xlsm']", "fileInputField");
        projectNameField = createScopedElement("xpath=.//input[@id='createProjectFormFiles:projectName']", "projectNameField");
        createProjectBtn = createScopedElement("#createProjectFormFiles\\:sbtFilesBtn", "createProjectBtn");
        cancelBtn = createScopedElement("xpath=.//input[@value='Cancel']", "cancelBtn");
    }

    public void createProjectFromExcelFile(String fileName, String projectName) {
        String filePath = TestDataUtil.getFilePathFromResources(fileName);
        fileInputField.setInputFiles(filePath);
        projectNameField.fill(projectName);
        createProjectBtn.click();
    }
}