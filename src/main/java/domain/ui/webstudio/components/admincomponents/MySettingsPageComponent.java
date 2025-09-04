package domain.ui.webstudio.components.admincomponents;

import configuration.core.ui.CoreComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;

import java.util.HashMap;
import java.util.Map;

public class MySettingsPageComponent extends CoreComponent {

    private WebElement showHeaderCheckbox;
    private WebElement showFormulasCheckbox;
    private WebElement defaultOrderDropdown;
    private WebElement byExcelSheetOption;
    private WebElement testsPerPageField;
    private WebElement failuresOnlyCheckbox;
    private WebElement compoundResultCheckbox;
    private WebElement showNumbersWithoutFormattingCheckbox;
    private WebElement saveBtn;
    private WebElement resetBtn;
    private WebElement cancelBtn;
    private WebElement successNotification;
    private WebElement errorNotification;

    private final Map<String, WebElement> orderOptionMappings = new HashMap<>();

    public MySettingsPageComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
        initializeOrderOptionMappings();
    }

    public MySettingsPageComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
        initializeOrderOptionMappings();
    }

    private void initializeElements() {
        showHeaderCheckbox = createScopedElement("xpath=.//input[@type='checkbox' and (contains(@id,'showHeader') or ./following-sibling::*[contains(text(),'Show Header')])]", "showHeaderCheckbox");
        showFormulasCheckbox = createScopedElement("xpath=.//input[@type='checkbox' and (contains(@id,'showFormulas') or ./following-sibling::*[contains(text(),'Show Formulas')])]", "showFormulasCheckbox");
        defaultOrderDropdown = createScopedElement("xpath=.//select[contains(@id,'defaultOrder') or ./preceding-sibling::*[contains(text(),'Default Order')]] | .//div[contains(@class,'ant-select') and ./preceding-sibling::*[contains(text(),'Default Order')]]", "defaultOrderDropdown");
        byExcelSheetOption = createScopedElement("xpath=.//div[contains(@class,'ant-select-item') and contains(text(),'By Excel Sheet')]", "byExcelSheetOption");
        testsPerPageField = createScopedElement("xpath=.//input[@type='number' and (contains(@id,'testsPerPage') or ./preceding-sibling::*[contains(text(),'Tests Per Page')])]", "testsPerPageField");
        failuresOnlyCheckbox = createScopedElement("xpath=.//input[@type='checkbox' and (contains(@id,'failuresOnly') or ./following-sibling::*[contains(text(),'Failures Only')])]", "failuresOnlyCheckbox");
        compoundResultCheckbox = createScopedElement("xpath=.//input[@type='checkbox' and (contains(@id,'compoundResult') or ./following-sibling::*[contains(text(),'Compound Result')])]", "compoundResultCheckbox");
        showNumbersWithoutFormattingCheckbox = createScopedElement("xpath=.//input[@type='checkbox' and (contains(@id,'showNumbers') or ./following-sibling::*[contains(text(),'Show Numbers Without Formatting')])]", "showNumbersWithoutFormattingCheckbox");
        saveBtn = createScopedElement("xpath=.//button[./span[text()='Save'] or @type='submit']", "saveBtn");
        resetBtn = createScopedElement("xpath=.//button[./span[text()='Reset'] or ./span[text()='Reset to Default']]", "resetBtn");
        cancelBtn = createScopedElement("xpath=.//button[./span[text()='Cancel']]", "cancelBtn");
        successNotification = createScopedElement("xpath=.//div[contains(@class,'ant-notification') or contains(@class,'success-message')]", "successNotification");
        errorNotification = createScopedElement("xpath=.//div[contains(@class,'ant-notification') or contains(@class,'error-message')]", "errorNotification");
    }

    private void initializeOrderOptionMappings() {
        orderOptionMappings.put("by excel sheet", byExcelSheetOption);
    }

    public MySettingsPageComponent setShowHeader(boolean show) {
        if (show != showHeaderCheckbox.isSelected()) {
            showHeaderCheckbox.click();
        }
        return this;
    }

    public boolean isShowHeaderEnabled() {
        return showHeaderCheckbox.isSelected();
    }

    public MySettingsPageComponent setShowFormulas(boolean show) {
        if (show != showFormulasCheckbox.isSelected()) {
            showFormulasCheckbox.click();
        }
        return this;
    }

    public boolean isShowFormulasEnabled() {
        return showFormulasCheckbox.isSelected();
    }

    public void setDefaultOrder(String orderOption) {
        defaultOrderDropdown.click();
        
        WebElement optionElement = getOrderOptionElement(orderOption);
        if (optionElement != null) {
            optionElement.click();
        }
    }

    private WebElement getOrderOptionElement(String orderOption) {
        return orderOptionMappings.get(orderOption.toLowerCase());
    }

    public void setDefaultOrderByExcelSheet() {
        setDefaultOrder("By Excel Sheet");
    }

    public String getDefaultOrder() {
        return defaultOrderDropdown.getAttribute("title");
    }

    public MySettingsPageComponent setTestsPerPage(int testsPerPage) {
        testsPerPageField.fill(String.valueOf(testsPerPage));
        return this;
    }

    public int getTestsPerPage() {
        String value = testsPerPageField.getAttribute("value");
        return value != null && !value.isEmpty() ? Integer.parseInt(value) : 5;
    }

    public MySettingsPageComponent setFailuresOnly(boolean failuresOnly) {
        if (failuresOnly != failuresOnlyCheckbox.isSelected()) {
            failuresOnlyCheckbox.click();
        }
        return this;
    }

    public boolean isFailuresOnlyEnabled() {
        return failuresOnlyCheckbox.isSelected();
    }

    public MySettingsPageComponent setCompoundResult(boolean compoundResult) {
        if (compoundResult != compoundResultCheckbox.isSelected()) {
            compoundResultCheckbox.click();
        }
        return this;
    }

    public boolean isCompoundResultEnabled() {
        return compoundResultCheckbox.isSelected();
    }

    public MySettingsPageComponent setShowNumbersWithoutFormatting(boolean show) {
        if (show != showNumbersWithoutFormattingCheckbox.isSelected()) {
            showNumbersWithoutFormattingCheckbox.click();
        }
        return this;
    }

    public boolean isShowNumbersWithoutFormattingEnabled() {
        return showNumbersWithoutFormattingCheckbox.isSelected();
    }

    public MySettingsPageComponent saveSettings() {
        saveBtn.click();
        return this;
    }

    public void resetSettings() {
        resetBtn.click();
    }

    public void cancelSettings() {
        cancelBtn.click();
    }

    public void configureTableSettings(boolean showHeader, boolean showFormulas, String defaultOrder) {
        setShowHeader(showHeader);
        setShowFormulas(showFormulas);
        if (defaultOrder != null) {
            setDefaultOrder(defaultOrder);
        }
    }

    public void configureTestingSettings(int testsPerPage, boolean failuresOnly, boolean compoundResult) {
        setTestsPerPage(testsPerPage);
        setFailuresOnly(failuresOnly);
        setCompoundResult(compoundResult);
    }

    public void configureTraceSettings(boolean showNumbersWithoutFormatting) {
        setShowNumbersWithoutFormatting(showNumbersWithoutFormatting);
    }

    public void configureAllSettings(boolean showHeader, boolean showFormulas, String defaultOrder,
                                   int testsPerPage, boolean failuresOnly, boolean compoundResult,
                                   boolean showNumbersWithoutFormatting) {
        configureTableSettings(showHeader, showFormulas, defaultOrder);
        configureTestingSettings(testsPerPage, failuresOnly, compoundResult);
        configureTraceSettings(showNumbersWithoutFormatting);
        saveSettings();
    }

    public void configureDefaultSettings(int testsPerPage) {
        configureAllSettings(true, true, "By Excel Sheet", testsPerPage, false, false, false);
    }

    public boolean validateSettings(Boolean expectedShowHeader, Boolean expectedShowFormulas,
                                  Integer expectedTestsPerPage, Boolean expectedFailuresOnly,
                                  Boolean expectedCompoundResult, Boolean expectedShowNumbersWithoutFormatting) {
        boolean headerMatches = expectedShowHeader == null || expectedShowHeader.equals(isShowHeaderEnabled());
        boolean formulasMatches = expectedShowFormulas == null || expectedShowFormulas.equals(isShowFormulasEnabled());
        boolean testsPerPageMatches = expectedTestsPerPage == null || expectedTestsPerPage.equals(getTestsPerPage());
        boolean failuresOnlyMatches = expectedFailuresOnly == null || expectedFailuresOnly.equals(isFailuresOnlyEnabled());
        boolean compoundResultMatches = expectedCompoundResult == null || expectedCompoundResult.equals(isCompoundResultEnabled());
        boolean numbersFormattingMatches = expectedShowNumbersWithoutFormatting == null || 
                                         expectedShowNumbersWithoutFormatting.equals(isShowNumbersWithoutFormattingEnabled());
        
        return headerMatches && formulasMatches && testsPerPageMatches && 
               failuresOnlyMatches && compoundResultMatches && numbersFormattingMatches;
    }

    public String getSettingsInfo() {
        return String.format("Settings - Header: %s | Formulas: %s | Order: %s | Tests/Page: %d | Failures Only: %s | Compound: %s | Numbers Without Formatting: %s",
                isShowHeaderEnabled(),
                isShowFormulasEnabled(),
                getDefaultOrder(),
                getTestsPerPage(),
                isFailuresOnlyEnabled(),
                isCompoundResultEnabled(),
                isShowNumbersWithoutFormattingEnabled());
    }
}