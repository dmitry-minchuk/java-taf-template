package domain.ui.webstudio.components.editortabcomponents;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.WaitUtil;
import lombok.Getter;

@Getter
public class CopyTableDialogComponent extends BaseComponent {

    private WebElement typeComboBox;
    private WebElement nameTextBox;
    private WebElement versionTextBox;
    private WebElement copyButton;
    private WebElement saveToLink;
    private WebElement moduleComboBox;
    private WebElement categoryComboBox;
    private WebElement categoryTextBox;
    private WebElement existingRadioBtn;
    private WebElement newRadioBtn;
    private WebElement propertyInputTemplate;
    private WebElement propertySelectTemplate;

    public CopyTableDialogComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public CopyTableDialogComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        // Main copy dialog elements based on MainTestMap.CopyTable selectors
        typeComboBox = createScopedElement("xpath=//select[@id='copy-table-form:copy-table-type']", "typeComboBox");
        nameTextBox = createScopedElement("xpath=//form[@id='copyTableForm']//input[@id='technicalName']", "nameTextBox");
        versionTextBox = createScopedElement("xpath=//span[@id='copyPropertiesTable']//tr[1]//input", "versionTextBox");
        copyButton = createScopedElement("xpath=//form[@id='copyTableForm']//input[@id='copyTableBtn']", "copyButton");
        saveToLink = createScopedElement("xpath=//form[@id='copyTableForm']//span[@id='save-panel']", "saveToLink");
        
        // Save To dialog elements
        moduleComboBox = createScopedElement("xpath=//table[@id='savePanel']//select[@id='workbooks']", "moduleComboBox");
        categoryComboBox = createScopedElement("xpath=//table[@id='savePanel']//select[@id='sheet']", "categoryComboBox");
        categoryTextBox = createScopedElement("xpath=//table[@id='savePanel']//input[@id='newSheetName']", "categoryTextBox");
        existingRadioBtn = createScopedElement("xpath=//table[@id='newSheet']//label[contains(text(), 'Existing')]//..//input", "existingRadioBtn");
        newRadioBtn = createScopedElement("xpath=//table[@id='newSheet']//label[contains(text(), 'New')]//..//input", "newRadioBtn");

        // BD version property templates — use format() with property label
        propertyInputTemplate = new WebElement(page,
                "xpath=//span[@id='copyPropertiesTable']//td[contains(text(), '%s')]/following-sibling::td//input[not(@type='hidden') and not(@readonly)]",
                "propertyInput");
        propertySelectTemplate = new WebElement(page,
                "xpath=//span[@id='copyPropertiesTable']//td[contains(text(), '%s')]/following-sibling::td//select",
                "propertySelect");
    }

    public CopyTableDialogComponent selectCopyAs(String value) {
        typeComboBox.selectByVisibleText(value);
        WaitUtil.sleep(250, "Waiting for copy type selection to apply and form fields to update");
        return this;
    }

    public CopyTableDialogComponent setName(String name) {
        nameTextBox.fill(name);
        return this;
    }

    public CopyTableDialogComponent setVersion(String version) {
        if (versionTextBox.isVisible()) {
            versionTextBox.fill(version);
        }
        return this;
    }

    public CopyTableDialogComponent setSaveTo(String categoryName) {
        if (categoryName != null && !categoryName.isEmpty()) {
            saveToLink.click();
            
            // Select "New" radio button and set category name
            newRadioBtn.click();
            categoryTextBox.fill(categoryName);
        }
        return this;
    }

    public CopyTableDialogComponent setTextProperty(String propertyLabel, String value) {
        WebElement input = propertyInputTemplate.format(propertyLabel);
        input.fill(value);
        input.getLocator().press("Tab");
        WaitUtil.sleep(500, "Waiting for property value to apply");
        return this;
    }

    public CopyTableDialogComponent setSelectProperty(String propertyLabel, String value) {
        WebElement select = propertySelectTemplate.format(propertyLabel);
        select.selectByVisibleText(value);
        WaitUtil.sleep(250, "Waiting for select property to apply");
        return this;
    }

    public boolean isCopyButtonEnabled() {
        return copyButton.isEnabled();
    }

    public void clickCopy() {
        copyButton.click();
    }

    public String getName() {
        return nameTextBox.getAttribute("value");
    }

    public String getVersion() {
        return versionTextBox.getAttribute("value");
    }

    public boolean isDialogVisible() {
        return copyButton.isVisible();
    }
}