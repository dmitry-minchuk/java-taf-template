package domain.ui.webstudio.components.admincomponents;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;

public class MySettingsPageComponent extends BaseComponent {

    private WebElement showHeaderCheckbox;
    private WebElement showFormulasCheckbox;
    private WebElement defaultOrderDropdown;
    private WebElement defaultOrderDropdownList;
    private WebElement defaultOrderSelectedItem;
    private WebElement testsPerPageDropdown;
    private WebElement testsPerPageDropdownList;
    private WebElement testsPerPageSelectedItem;
    private WebElement failuresOnlyCheckbox;
    private WebElement compoundResultCheckbox;
    private WebElement showNumbersWithoutFormattingCheckbox;
    private WebElement saveBtn;

    public MySettingsPageComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public MySettingsPageComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        showHeaderCheckbox = createScopedElement("xpath=.//input[@id='showHeader']", "showHeaderCheckbox");
        showFormulasCheckbox = createScopedElement("xpath=.//input[@id='showFormulas']", "showFormulasCheckbox");

        defaultOrderDropdown = createScopedElement("xpath=.//input[@id='treeView']", "defaultOrderDropdown");
        defaultOrderDropdownList = new WebElement(page, "xpath=//div[contains(@class,'ant-select-dropdown')]//div[contains(@class,'ant-select-item-option') and @title='%s']", "defaultOrderDropdownList");
        defaultOrderSelectedItem = createScopedElement("xpath=.//input[@id='treeView']/ancestor::div[contains(@class,'ant-select')]//span[@class='ant-select-selection-item']", "defaultOrderSelectedItem");

        testsPerPageDropdown = createScopedElement("xpath=.//input[@id='testsPerPage']", "testsPerPageDropdown");
        testsPerPageDropdownList = new WebElement(page, "xpath=//div[contains(@class,'ant-select-dropdown')]//div[contains(@class,'ant-select-item-option') and @title='%s']", "testsPerPageDropdownList");
        testsPerPageSelectedItem = createScopedElement("xpath=.//input[@id='testsPerPage']/ancestor::div[contains(@class,'ant-select')]//span[@class='ant-select-selection-item']", "testsPerPageSelectedItem");

        failuresOnlyCheckbox = createScopedElement("xpath=.//input[@id='testsFailuresOnly']", "failuresOnlyCheckbox");
        compoundResultCheckbox = createScopedElement("xpath=.//input[@id='showComplexResult']", "compoundResultCheckbox");
        showNumbersWithoutFormattingCheckbox = createScopedElement("xpath=.//input[@id='showRealNumbers']", "showNumbersWithoutFormattingCheckbox");
        saveBtn = createScopedElement("xpath=.//button[./span[text()='Save'] or @type='submit']", "saveBtn");
    }

    public MySettingsPageComponent setShowHeader(boolean show) {
        if (show != showHeaderCheckbox.isChecked()) {
            showHeaderCheckbox.click();
        }
        return this;
    }

    public boolean isShowHeaderEnabled() {
        return showHeaderCheckbox.isChecked();
    }

    public MySettingsPageComponent setShowFormulas(boolean show) {
        if (show != showFormulasCheckbox.isChecked()) {
            showFormulasCheckbox.click();
        }
        return this;
    }

    public boolean isShowFormulasEnabled() {
        return showFormulasCheckbox.isChecked();
    }

    public void setDefaultOrder(String orderOption) {
        defaultOrderDropdown.click();
        defaultOrderDropdownList.format(orderOption).waitForVisible(500).click();
    }

    public String getDefaultOrder() {
        return defaultOrderSelectedItem.getAttribute("title");
    }

    public MySettingsPageComponent setTestsPerPage(int testsPerPage) {
        testsPerPageDropdown.click();
        testsPerPageDropdownList.format(String.valueOf(testsPerPage)).waitForVisible(500).click();
        return this;
    }

    public int getTestsPerPage() {
        String value = testsPerPageSelectedItem.getAttribute("title");
        return value != null && !value.isEmpty() ? Integer.parseInt(value) : 5;
    }

    public MySettingsPageComponent setFailuresOnly(boolean failuresOnly) {
        if (failuresOnly != failuresOnlyCheckbox.isChecked()) {
            failuresOnlyCheckbox.click();
        }
        return this;
    }

    public boolean isFailuresOnlyEnabled() {
        return failuresOnlyCheckbox.isChecked();
    }

    public MySettingsPageComponent setCompoundResult(boolean compoundResult) {
        if (compoundResult != compoundResultCheckbox.isChecked()) {
            compoundResultCheckbox.click();
        }
        return this;
    }

    public boolean isCompoundResultEnabled() {
        return compoundResultCheckbox.isChecked();
    }

    public MySettingsPageComponent setShowNumbersWithoutFormatting(boolean show) {
        if (show != showNumbersWithoutFormattingCheckbox.isChecked()) {
            showNumbersWithoutFormattingCheckbox.click();
        }
        return this;
    }

    public boolean isShowNumbersWithoutFormattingEnabled() {
        return showNumbersWithoutFormattingCheckbox.isChecked();
    }

    public MySettingsPageComponent saveSettings() {
        saveBtn.click();
        return this;
    }
}