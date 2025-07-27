package domain.ui.webstudio.components.createnewproject;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import helpers.utils.TestDataUtil;

/**
 * Playwright version of ZipArchiveComponent for ZIP file upload project creation
 * Supports file upload validation with LOCAL/DOCKER mode compatibility
 */
public class PlaywrightZipArchiveComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement fileInputField;
    private PlaywrightWebElement projectNameField;
    private PlaywrightWebElement createProjectBtn;
    private PlaywrightWebElement cancelBtn;

    public PlaywrightZipArchiveComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightZipArchiveComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        // ZIP file input field
        fileInputField = createScopedElement("xpath=.//input[@type='file']", "fileInputField");
        
        // Project name field
        projectNameField = createScopedElement("xpath=.//input[contains(@id,'projectName')]", "projectNameField");
        
        // Create project button
        createProjectBtn = createScopedElement("xpath=.//input[@value='Create']", "createProjectBtn");
        
        // Cancel button
        cancelBtn = createScopedElement("xpath=.//input[@value='Cancel']", "cancelBtn");
    }

    /**
     * Create project from ZIP archive file
     * @param fileName Name of the ZIP file in test resources
     * @param projectName Unique project name to use
     */
    public void createProjectZipArchive(String fileName, String projectName) {
        String absoluteFilePath = TestDataUtil.getFilePathFromResources(fileName);
        fileInputField.sendKeys(absoluteFilePath);
        projectNameField.fill(projectName);
        createProjectBtn.click();
    }
}