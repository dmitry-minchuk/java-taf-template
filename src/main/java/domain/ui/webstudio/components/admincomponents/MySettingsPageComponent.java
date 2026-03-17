package domain.ui.webstudio.components.admincomponents;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;

public class MySettingsPageComponent extends BaseComponent {

    private WebElement showHeaderCheckbox;
    private WebElement showFormulasCheckbox;
    private WebElement defaultOrderDropdown;
    private WebElement defaultOrderSelectedItem;
    private WebElement testsPerPageDropdown;
    private WebElement selectOptionTemplate;
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

        defaultOrderDropdown = createScopedElement("xpath=.//div[./label[@title='Default Order']]/following-sibling::div//div[contains(@class,'ant-select-content')]", "defaultOrderDropdown");
        defaultOrderSelectedItem = createScopedElement("xpath=.//input[@id='treeView']/ancestor::div[contains(@class,'ant-select')]//div[contains(@class,'ant-select-content')]", "defaultOrderSelectedItem");

        testsPerPageDropdown = createScopedElement("xpath=.//div[./label[@title='Tests Per Page']]/following-sibling::div//div[contains(@class,'ant-select-content')]", "testsPerPageDropdown");
        selectOptionTemplate =  new WebElement(page,"xpath=//div[@class='rc-virtual-list-holder-inner' and not(ancestor::div[contains(@class,'dropdown-hidden')])]/div[@title='%s']", "selectOptionTemplate");
        testsPerPageSelectedItem = createScopedElement("xpath=.//input[@id='testsPerPage']/..", "testsPerPageSelectedItem");

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
        selectOptionTemplate.format(orderOption).waitForVisible().click();
    }

    public String getDefaultOrder() {
        return defaultOrderSelectedItem.getAttribute("title");
    }

    public MySettingsPageComponent setTestsPerPage(int testsPerPage) {
        testsPerPageDropdown.click();
        selectOptionTemplate.format(String.valueOf(testsPerPage)).waitForVisible().click();
        return this;
    }

    public int getTestsPerPage() {
        return Integer.parseInt(testsPerPageSelectedItem.getAttribute("title"));
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