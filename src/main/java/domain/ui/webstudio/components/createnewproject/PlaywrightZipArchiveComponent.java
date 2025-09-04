package domain.ui.webstudio.components.createnewproject;

import configuration.core.ui.CoreComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.TestDataUtil;
import helpers.utils.WaitUtil;

// Playwright version of ZipArchiveComponent for ZIP file upload project creation
public class PlaywrightZipArchiveComponent extends CoreComponent {

    private PlaywrightWebElement fileInputField;
    private PlaywrightWebElement projectNameField;
    private PlaywrightWebElement createProjectBtn;
    private PlaywrightWebElement cancelBtn;

    public PlaywrightZipArchiveComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightZipArchiveComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        fileInputField = createScopedElement("xpath=.//input[@type='file']", "fileInputField");
        projectNameField = createScopedElement("xpath=.//input[contains(@id,'projectName')]", "projectNameField");
        createProjectBtn = createScopedElement("xpath=.//input[@value='Create']", "createProjectBtn");
        cancelBtn = createScopedElement("xpath=.//input[@value='Cancel']", "cancelBtn");
    }

    public void createProjectZipArchive(String fileName, String projectName) {
        uploadZipFile(fileName);
        setProjectName(projectName);
        createProjectBtn.click();
    }
    
    private void uploadZipFile(String fileName) {
        String absoluteFilePath = TestDataUtil.getFilePathFromResources(fileName);
        fileInputField.sendKeys(absoluteFilePath);
        WaitUtil.sleep(1000);
    }
    
    private void setProjectName(String projectName) {
        projectNameField.waitForVisible();
        projectNameField.clear();
        projectNameField.fillSequentially(projectName);
    }
}