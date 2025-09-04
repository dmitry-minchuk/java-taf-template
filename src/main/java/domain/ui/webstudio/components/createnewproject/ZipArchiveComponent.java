package domain.ui.webstudio.components.createnewproject;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.TestDataUtil;
import helpers.utils.WaitUtil;

// Playwright version of ZipArchiveComponent for ZIP file upload project creation
public class ZipArchiveComponent extends BaseComponent {

    private WebElement fileInputField;
    private WebElement projectNameField;
    private WebElement createProjectBtn;
    private WebElement cancelBtn;

    public ZipArchiveComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public ZipArchiveComponent(WebElement rootLocator) {
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
        WaitUtil.sleep(1000); // For progress bar to finish
    }
    
    private void setProjectName(String projectName) {
        projectNameField.waitForVisible();
        projectNameField.clear();
        projectNameField.press("Tab");
        projectNameField.click();
        projectNameField.fillSequentially(projectName);
        projectNameField.press("Tab");
    }
}