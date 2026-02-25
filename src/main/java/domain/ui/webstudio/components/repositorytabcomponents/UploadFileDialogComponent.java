package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UploadFileDialogComponent extends BaseComponent {

    private static final Logger LOGGER = LogManager.getLogger(UploadFileDialogComponent.class);

    private WebElement fileInput;
    private WebElement fileNameField;
    private WebElement uploadButton;
    private WebElement cancelButton;

    public UploadFileDialogComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public UploadFileDialogComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        fileInput = createScopedElement("xpath=.//input[@type='file']", "fileInput");
        fileNameField = createScopedElement("xpath=.//table[@id='newFileForm:newFilePanel']//input[@id='newFileForm:fileName']", "fileNameField");
        uploadButton = createScopedElement("xpath=.//footer/input[@value='Upload']", "uploadButton");
        cancelButton = createScopedElement("xpath=.//footer/input[@value='Cancel']", "cancelButton");
    }

    public UploadFileDialogComponent uploadFile(String filePath) {
        LOGGER.info("Uploading file: {}", filePath);
        WaitUtil.sleep(500, "Waiting for previous operations to finish");
        fileInput.setInputFiles(filePath);
        WaitUtil.sleep(500, "Waiting for file to be selected");
        return this;
    }

    public UploadFileDialogComponent setFileName(String fileName) {
        LOGGER.info("Setting file name: {}", fileName);
        fileNameField.fill(fileName);
        return this;
    }

    public String getFileName() {
        return fileNameField.getAttribute("value");
    }

    public void clickUploadButton() {
        LOGGER.info("Clicking Upload button");
        uploadButton.click();
        waitForDialogToClose();
        WaitUtil.sleep(500, "Waiting for Upload to finish processing");
    }

    public void clickCancelButton() {
        LOGGER.info("Clicking Cancel button");
        cancelButton.click();
    }

    public boolean isDialogVisible() {
        try {
            return fileInput.isVisible(1000);
        } catch (Exception e) {
            return false;
        }
    }

    public void waitForDialogToAppear() {
        WaitUtil.waitForCondition(this::isDialogVisible, 5000, 100, "Waiting for Upload File dialog to appear");
    }

    public void waitForDialogToClose() {
        WaitUtil.waitForCondition(() -> !isDialogVisible(), 5000, 100, "Waiting for Upload File dialog to close");
    }
}
