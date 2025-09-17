package domain.ui.webstudio.components.createnewproject;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.TestDataUtil;
import helpers.utils.WaitUtil;
import lombok.Getter;

@Getter
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
        projectNameField = createScopedElement("xpath=.//input[@id='uploadProjectForm:projectName']", "projectNameField");
        createProjectBtn = createScopedElement("xpath=.//input[@id='uploadProjectForm:sbtZipsBtn']", "createProjectBtn");
        cancelBtn = createScopedElement("xpath=.//input[@value='Cancel']", "cancelBtn");
    }

    public void createProjectZipArchive(String fileName, String projectName) {
        uploadZipFile(fileName);
        setProjectName(projectName);
        createProjectBtn.click();
    }

    public void uploadZipFile(String fileName) {
        String absoluteFilePath = TestDataUtil.getFilePathFromResources(fileName);
        fileInputField.sendKeys(absoluteFilePath);
        WaitUtil.sleep(1000); // For progress bar to finish
    }

    public void setProjectName(String projectName) {
        projectNameField.clearByKeyCombination();
        projectNameField.fillSequentially(projectName);
    }
}