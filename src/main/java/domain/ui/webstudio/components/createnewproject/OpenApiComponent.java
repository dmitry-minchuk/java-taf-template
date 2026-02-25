package domain.ui.webstudio.components.createnewproject;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.TestDataUtil;
import helpers.utils.WaitUtil;

public class OpenApiComponent extends BaseComponent {

    private WebElement fileInputField;
    private WebElement projectNameField;
    private WebElement dataModuleNameField;
    private WebElement dataModulePathDisplay;
    private WebElement rulesModuleNameField;
    private WebElement rulesModulePathDisplay;
    private WebElement editDataPathLink;
    private WebElement dataModulePathInput;
    private WebElement editRulesPathLink;
    private WebElement rulesModulePathInput;
    private WebElement resetRulesPathLink;
    private WebElement createProjectBtn;
    private WebElement clearFirstFileBtn;
    private WebElement clearAllBtn;
    private WebElement errorMessage;

    public OpenApiComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public OpenApiComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        fileInputField = createScopedElement("xpath=.//div[contains(@id,'openAPIProjectForm:file')]//input[@type='file']", "fileInputField");
        projectNameField = createScopedElement("xpath=.//input[@id='openAPIProjectForm:projectName']", "projectNameField");
        dataModuleNameField = createScopedElement("xpath=.//input[@id='openAPIProjectForm:modelsModuleName']", "dataModuleNameField");
        dataModulePathDisplay = createScopedElement("xpath=.//span[@id='openAPIProjectForm:modelsFileDisplayPath']", "dataModulePathDisplay");
        rulesModuleNameField = createScopedElement("xpath=.//input[@id='openAPIProjectForm:algorithmsModuleName']", "rulesModuleNameField");
        rulesModulePathDisplay = createScopedElement("xpath=.//span[@id='openAPIProjectForm:algorithmsFileDisplayPath']", "rulesModulePathDisplay");
        editDataPathLink = createScopedElement("xpath=.//a[@id='openAPIProjectForm:editDataPath']", "editDataPathLink");
        dataModulePathInput = createScopedElement("xpath=.//input[@id='openAPIProjectForm:modelsFilePath']", "dataModulePathInput");
        editRulesPathLink = createScopedElement("xpath=.//a[@id='openAPIProjectForm:editAlgoPath']", "editRulesPathLink");
        rulesModulePathInput = createScopedElement("xpath=.//input[@id='openAPIProjectForm:algorithmsFilePath']", "rulesModulePathInput");
        resetRulesPathLink = createScopedElement("xpath=.//a[@id='openAPIProjectForm:resetAlgoPath']", "resetRulesPathLink");
        createProjectBtn = createScopedElement("xpath=.//input[@id='openAPIProjectForm:submitOpenAPIFileBtn']", "createProjectBtn");
        clearFirstFileBtn = createScopedElement("xpath=.//div[contains(@id,'openAPIProjectForm:file')]//a[contains(@class,'rf-fu-itm-lnk')]", "clearFirstFileBtn");
        clearAllBtn = createScopedElement("xpath=.//div[contains(@id,'openAPIProjectForm:file')]//span[contains(@class,'rf-fu-btn-clr')]", "clearAllBtn");
        errorMessage = createScopedElement("xpath=.//span[contains(@class,'generalError')]", "errorMessage");
    }

    public void uploadOpenApiFile(String fileName) {
        String absoluteFilePath = TestDataUtil.getFilePathFromResources(fileName);
        fileInputField.setInputFiles(absoluteFilePath);
        WaitUtil.sleep(500, "Waiting for OpenAPI file upload to process");
    }

    public void setProjectName(String name) {
        projectNameField.clearByKeyCombination();
        projectNameField.fillSequentially(name);
    }

    public String getProjectName() {
        return projectNameField.getAttribute("value");
    }

    public void setDataModuleName(String name) {
        dataModuleNameField.clearByKeyCombination();
        dataModuleNameField.fillSequentially(name);
    }

    public String getDataModuleName() {
        return dataModuleNameField.getAttribute("value");
    }

    public String getDataModulePathDisplay() {
        return dataModulePathDisplay.getText();
    }

    public void setRulesModuleName(String name) {
        rulesModuleNameField.clearByKeyCombination();
        rulesModuleNameField.fillSequentially(name);
    }

    public String getRulesModuleName() {
        return rulesModuleNameField.getAttribute("value");
    }

    public String getRulesModulePathDisplay() {
        return rulesModulePathDisplay.getText();
    }

    public void clickEditDataPath() {
        editDataPathLink.click();
    }

    public void setDataModulePath(String path) {
        dataModulePathInput.clearByKeyCombination();
        dataModulePathInput.fillSequentially(path);
    }

    public String getDataModulePathInputValue() {
        return dataModulePathInput.getCurrentInputValue();
    }

    public void clickEditRulesPath() {
        editRulesPathLink.click();
    }

    public void setRulesModulePath(String path) {
        rulesModulePathInput.clearByKeyCombination();
        rulesModulePathInput.fillSequentially(path);
    }

    public void clickResetRulesPath() {
        resetRulesPathLink.click();
    }

    public void clickCreate() {
        createProjectBtn.click();
    }

    public boolean isCreateEnabled() {
        return createProjectBtn.isEnabled();
    }

    public void clearFirstFile() {
        clearFirstFileBtn.click();
    }

    public boolean isClearFirstFileVisible() {
        return clearFirstFileBtn.isVisible(1000);
    }

    public void clearAllFiles() {
        clearAllBtn.click();
    }

    public boolean isClearAllVisible() {
        return clearAllBtn.isVisible(1000);
    }

    public String getErrorMessage() {
        return errorMessage.getText();
    }

    public boolean isErrorMessageVisible() {
        return errorMessage.isVisible(2000);
    }
}
