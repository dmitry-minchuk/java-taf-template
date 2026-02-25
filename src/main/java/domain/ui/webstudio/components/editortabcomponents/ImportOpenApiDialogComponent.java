package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

import java.util.List;

public class ImportOpenApiDialogComponent extends BaseComponent {

    private WebElement openApiFilePathInput;
    private WebElement generateFromRulesRadio;
    private WebElement uploadInRepositoryRadio;
    private WebElement reconciliationRadio;
    private WebElement generationRadio;
    private WebElement rulesModuleInput;
    private WebElement dataModuleInput;
    private WebElement importReconciliationBtn;
    private WebElement importTablesGenerationBtn;
    private WebElement createOrUpdateSchemaBtn;
    private WebElement cancelBtn;
    private WebElement errorMsg;
    private WebElement anyErrorMsg;
    private List<WebElement> errorMsgs;

    public ImportOpenApiDialogComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public ImportOpenApiDialogComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        openApiFilePathInput = createScopedElement("xpath=.//input[@id='importOpenAPIForm:openAPIPath']", "openApiFilePathInput");
        generateFromRulesRadio = createScopedElement("xpath=.//input[@id='openApiImportType1']", "generateFromRulesRadio");
        uploadInRepositoryRadio = createScopedElement("xpath=.//input[@id='openApiImportType2']", "uploadInRepositoryRadio");
        reconciliationRadio = createScopedElement("xpath=.//input[@id='reconciliation']", "reconciliationRadio");
        generationRadio = createScopedElement("xpath=.//input[@id='generation']", "generationRadio");
        rulesModuleInput = createScopedElement("xpath=.//input[@id='importOpenAPIForm:algorithmModuleName']", "rulesModuleInput");
        dataModuleInput = createScopedElement("xpath=.//input[@id='importOpenAPIForm:modelModuleName']", "dataModuleInput");
        importReconciliationBtn = createScopedElement("xpath=.//input[@id='importOpenAPIForm:reconciliationOpenAPIBtn']", "importReconciliationBtn");
        importTablesGenerationBtn = createScopedElement("xpath=.//input[@id='importOpenAPIForm:importOpenAPIBtn']", "importTablesGenerationBtn");
        createOrUpdateSchemaBtn = createScopedElement("xpath=.//input[@id='importOpenAPIForm:createOrUpdateOpenAPISchemaBtn']", "createOrUpdateSchemaBtn");
        cancelBtn = createScopedElement("xpath=.//input[@value='Cancel']", "cancelBtn");
        errorMsg = createScopedElement("xpath=.//input[@id='importOpenAPIForm:algorithmModuleName']/following-sibling::span//span[@class='error']", "errorMsg");
        anyErrorMsg = createScopedElement("xpath=.//span[@class='error']", "anyErrorMsg");
        errorMsgs = createScopedElementList("xpath=.//span[@class='error']", "errorMsgs");
    }

    public void selectUploadInRepository() {
        uploadInRepositoryRadio.click();
    }

    public void selectGenerateFromRules() {
        generateFromRulesRadio.click();
    }

    public void setOpenApiFilePath(String path) {
        openApiFilePathInput.clear();
        openApiFilePathInput.fillSequentially(path);
        openApiFilePathInput.press("Tab");
    }

    public void selectReconciliationMode() {
        reconciliationRadio.click();
    }

    public void selectTablesGenerationMode() {
        generationRadio.click();
    }

    public void setRulesModuleName(String name) {
        rulesModuleInput.clear();
        rulesModuleInput.fillSequentially(name);
    }

    public String getRulesModuleName() {
        return rulesModuleInput.getCurrentInputValue();
    }

    public void setDataModuleName(String name) {
        dataModuleInput.clear();
        dataModuleInput.fillSequentially(name);
    }

    public String getDataModuleName() {
        return dataModuleInput.getCurrentInputValue();
    }

    public void clickImportReconciliation() {
        importReconciliationBtn.press("Tab");
        importReconciliationBtn.click();
        WaitUtil.sleep(500, "Waiting for reconciliation import to process");
    }

    public void clickImportTablesGeneration() {
        importTablesGenerationBtn.click();
        WaitUtil.sleep(500, "Waiting for tables generation import to process");
    }

    public void clickCreateOrUpdateSchema() {
        createOrUpdateSchemaBtn.click();
        WaitUtil.sleep(500, "Waiting for schema creation to process");
    }

    public void clickCancel() {
        cancelBtn.click();
    }

    public String getErrorMessage() {
        return errorMsg.getText(3000);
    }

    public List<String> getErrorMessages() {
        WaitUtil.waitForListNotEmpty(() -> errorMsgs, 5000, 250, "Waiting for error messages to appear");
        return errorMsgs.stream()
                .map(e -> e.getText().trim())
                .toList();
    }

    public String getAnyErrorMessage() {
        return anyErrorMsg.getText(3000);
    }

    public boolean isReconciliationModeSelected() {
        return reconciliationRadio.isChecked();
    }

    public boolean isVisible() {
        return cancelBtn.isVisible(3000);
    }

    public void waitForVisible() {
        WaitUtil.waitForCondition(this::isVisible, 10000, 250, "Waiting for Import OpenAPI dialog to appear");
    }
}
