package domain.ui.webstudio.components.editortabcomponents;

import configuration.core.ui.CoreComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.WaitUtil;
import lombok.Getter;

@Getter
public class PlaywrightCopyTableDialogComponent extends CoreComponent {

    private PlaywrightWebElement typeComboBox;
    private PlaywrightWebElement nameTextBox;
    private PlaywrightWebElement versionTextBox;
    private PlaywrightWebElement copyButton;
    private PlaywrightWebElement saveToLink;
    private PlaywrightWebElement moduleComboBox;
    private PlaywrightWebElement categoryComboBox;
    private PlaywrightWebElement categoryTextBox;
    private PlaywrightWebElement existingRadioBtn;
    private PlaywrightWebElement newRadioBtn;

    public PlaywrightCopyTableDialogComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightCopyTableDialogComponent(PlaywrightWebElement rootLocator) {
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
    }

    public PlaywrightCopyTableDialogComponent selectCopyAs(String value) {
        typeComboBox.selectByVisibleText(value);
        WaitUtil.sleep(250);
        return this;
    }

    public PlaywrightCopyTableDialogComponent setName(String name) {
        nameTextBox.fill(name);
        return this;
    }

    public PlaywrightCopyTableDialogComponent setVersion(String version) {
        if (versionTextBox.isVisible()) {
            versionTextBox.fill(version);
        }
        return this;
    }

    public PlaywrightCopyTableDialogComponent setSaveTo(String categoryName) {
        if (categoryName != null && !categoryName.isEmpty()) {
            saveToLink.click();
            
            // Select "New" radio button and set category name
            newRadioBtn.click();
            categoryTextBox.fill(categoryName);
        }
        return this;
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