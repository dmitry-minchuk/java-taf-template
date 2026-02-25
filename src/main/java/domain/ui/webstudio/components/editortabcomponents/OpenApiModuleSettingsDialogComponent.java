package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;
import helpers.utils.WaitUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OpenApiModuleSettingsDialogComponent extends BaseComponent {

    private WebElement contentDiv;
    private WebElement importAndOverrideBtn;
    private WebElement cancelBtn;
    private WebElement errorMsg;
    private List<WebElement> errorMsgs;
    private WebElement editRulesPathLink;
    private WebElement newRulesPathInput;
    private WebElement resetRulesPathLink;
    private WebElement rulesPathDisplay;
    private WebElement editDataPathLink;
    private WebElement newDataPathInput;
    private WebElement resetDataPathLink;
    private WebElement dataPathDisplay;

    public OpenApiModuleSettingsDialogComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public OpenApiModuleSettingsDialogComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        contentDiv = new WebElement(page,"xpath=//div[@id='openAPIModulesSettings_content']", "contentDiv");
        // These buttons are inside generateOpenAPIForm but may be outside the content div,
        // so they use page-level locators
        importAndOverrideBtn = new WebElement(page, "xpath=//input[@id='generateOpenAPIForm:generateOpenAPIBtn']", "importAndOverrideBtn");
        cancelBtn = new WebElement(page, "xpath=//div[@id='openAPIModulesSettings_content']//input[@value='Cancel']", "cancelBtn");
        errorMsg = new WebElement(page, "xpath=//form[@id='generateOpenAPIForm']//span[@class='error']", "errorMsg");
        errorMsgs = createElementList("xpath=//form[@id='generateOpenAPIForm']//span[@class='error']", "errorMsgs");
        editRulesPathLink = new WebElement(page, "xpath=//a[@id='editAlgoPath']", "editRulesPathLink");
        newRulesPathInput = new WebElement(page, "xpath=//input[@id='generateOpenAPIForm:newAlgoPath']", "newRulesPathInput");
        resetRulesPathLink = new WebElement(page, "xpath=//a[@id='resetAlgoPath']", "resetRulesPathLink");
        rulesPathDisplay = new WebElement(page, "xpath=//*[@id='algoPath']", "rulesPathDisplay");
        editDataPathLink = new WebElement(page, "xpath=//a[@id='editDataPath']", "editDataPathLink");
        newDataPathInput = new WebElement(page, "xpath=//input[@id='generateOpenAPIForm:newDataPath']", "newDataPathInput");
        resetDataPathLink = new WebElement(page, "xpath=//a[@id='resetDataPath']", "resetDataPathLink");
        dataPathDisplay = new WebElement(page, "xpath=//*[@id='dataPath']", "dataPathDisplay");
    }

    public String getContentText() {
        String raw = contentDiv.getInnerText(5000);
        return Arrays.stream(raw.split("\n"))
                .map(line -> line.replaceAll("\\s+", " ").trim())
                .filter(line -> !line.isEmpty())
                .collect(Collectors.joining("\n"));
    }

    public String getImportButtonText() {
        return importAndOverrideBtn.getAttribute("value");
    }

    public void clickImportAndOverride() {
        importAndOverrideBtn.click();
        WaitUtil.sleep(500, "Waiting for import action to process");
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

    public void clickEditRulesPath() {
        editRulesPathLink.click();
    }

    public void setNewRulesPath(String path) {
        newRulesPathInput.clear();
        newRulesPathInput.fillSequentially(path);
    }

    public void clickResetRulesPath() {
        resetRulesPathLink.click();
    }

    public String getRulesPathDisplayValue() {
        return rulesPathDisplay.getText();
    }

    public void clickEditDataPath() {
        editDataPathLink.click();
    }

    public void setNewDataPath(String path) {
        newDataPathInput.clear();
        newDataPathInput.fillSequentially(path);
    }

    public void clickResetDataPath() {
        resetDataPathLink.click();
    }

    public String getDataPathDisplayValue() {
        return dataPathDisplay.getText();
    }

    public boolean isVisible() {
        return importAndOverrideBtn.isVisible(3000);
    }

    public void waitForVisible() {
        WaitUtil.waitForCondition(this::isVisible, 10000, 250, "Waiting for OpenAPI Module Settings dialog to appear");
    }
}
