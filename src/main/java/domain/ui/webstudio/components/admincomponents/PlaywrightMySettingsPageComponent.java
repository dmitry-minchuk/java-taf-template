package domain.ui.webstudio.components.admincomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

import java.util.HashMap;
import java.util.Map;

public class PlaywrightMySettingsPageComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement showHeaderCheckbox;
    private PlaywrightWebElement showFormulasCheckbox;
    private PlaywrightWebElement defaultOrderDropdown;
    private PlaywrightWebElement byExcelSheetOption;
    private PlaywrightWebElement testsPerPageField;
    private PlaywrightWebElement failuresOnlyCheckbox;
    private PlaywrightWebElement compoundResultCheckbox;
    private PlaywrightWebElement showNumbersWithoutFormattingCheckbox;
    private PlaywrightWebElement saveBtn;
    private PlaywrightWebElement resetBtn;
    private PlaywrightWebElement cancelBtn;
    private PlaywrightWebElement successNotification;
    private PlaywrightWebElement errorNotification;

    private final Map<String, PlaywrightWebElement> orderOptionMappings = new HashMap<>();

    public PlaywrightMySettingsPageComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
        initializeOrderOptionMappings();
    }

    public PlaywrightMySettingsPageComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
        initializeOrderOptionMappings();
    }

    private void initializeElements() {
        showHeaderCheckbox = new PlaywrightWebElement(page, ".//input[@type='checkbox' and (contains(@id,'showHeader') or ./following-sibling::*[contains(text(),'Show Header')])]", "Show Header Checkbox");
        showFormulasCheckbox = new PlaywrightWebElement(page, ".//input[@type='checkbox' and (contains(@id,'showFormulas') or ./following-sibling::*[contains(text(),'Show Formulas')])]", "Show Formulas Checkbox");
        defaultOrderDropdown = new PlaywrightWebElement(page, ".//select[contains(@id,'defaultOrder') or ./preceding-sibling::*[contains(text(),'Default Order')]] | .//div[contains(@class,'ant-select') and ./preceding-sibling::*[contains(text(),'Default Order')]]", "Default Order Dropdown");
        byExcelSheetOption = new PlaywrightWebElement(page, ".//div[contains(@class,'ant-select-item') and contains(text(),'By Excel Sheet')]", "By Excel Sheet Option");
        testsPerPageField = new PlaywrightWebElement(page, ".//input[@type='number' and (contains(@id,'testsPerPage') or ./preceding-sibling::*[contains(text(),'Tests Per Page')])]", "Tests Per Page Field");
        failuresOnlyCheckbox = new PlaywrightWebElement(page, ".//input[@type='checkbox' and (contains(@id,'failuresOnly') or ./following-sibling::*[contains(text(),'Failures Only')])]", "Failures Only Checkbox");
        compoundResultCheckbox = new PlaywrightWebElement(page, ".//input[@type='checkbox' and (contains(@id,'compoundResult') or ./following-sibling::*[contains(text(),'Compound Result')])]", "Compound Result Checkbox");
        showNumbersWithoutFormattingCheckbox = new PlaywrightWebElement(page, ".//input[@type='checkbox' and (contains(@id,'showNumbers') or ./following-sibling::*[contains(text(),'Show Numbers Without Formatting')])]", "Show Numbers Without Formatting Checkbox");
        saveBtn = new PlaywrightWebElement(page, ".//button[./span[text()='Save'] or @type='submit']", "Save Button");
        resetBtn = new PlaywrightWebElement(page, ".//button[./span[text()='Reset'] or ./span[text()='Reset to Default']]", "Reset Button");
        cancelBtn = new PlaywrightWebElement(page, ".//button[./span[text()='Cancel']]", "Cancel Button");
        successNotification = new PlaywrightWebElement(page, ".//div[contains(@class,'ant-notification') or contains(@class,'success-message')]", "Success Notification");
        errorNotification = new PlaywrightWebElement(page, ".//div[contains(@class,'ant-notification') or contains(@class,'error-message')]", "Error Notification");
    }

    private void initializeOrderOptionMappings() {
        orderOptionMappings.put("by excel sheet", byExcelSheetOption);
    }

    public void setShowHeader(boolean show) {
        if (show != showHeaderCheckbox.isChecked()) {
            showHeaderCheckbox.click();
        }
    }

    public boolean isShowHeaderEnabled() {
        return showHeaderCheckbox.isChecked();
    }

    public void setShowFormulas(boolean show) {
        if (show != showFormulasCheckbox.isChecked()) {
            showFormulasCheckbox.click();
        }
    }

    public boolean isShowFormulasEnabled() {
        return showFormulasCheckbox.isChecked();
    }

    public void setDefaultOrder(String orderOption) {
        defaultOrderDropdown.click();
        
        PlaywrightWebElement optionElement = getOrderOptionElement(orderOption);
        if (optionElement != null) {
            optionElement.click();
        }
    }

    private PlaywrightWebElement getOrderOptionElement(String orderOption) {
        return orderOptionMappings.get(orderOption.toLowerCase());
    }

    public void setDefaultOrderByExcelSheet() {
        setDefaultOrder("By Excel Sheet");
    }

    public String getDefaultOrder() {
        return defaultOrderDropdown.getAttribute("title");
    }

    public void setTestsPerPage(int testsPerPage) {
        testsPerPageField.fill(String.valueOf(testsPerPage));
    }

    public int getTestsPerPage() {
        String value = testsPerPageField.getAttribute("value");
        return value != null && !value.isEmpty() ? Integer.parseInt(value) : 5;
    }

    public void setFailuresOnly(boolean failuresOnly) {
        if (failuresOnly != failuresOnlyCheckbox.isChecked()) {
            failuresOnlyCheckbox.click();
        }
    }

    public boolean isFailuresOnlyEnabled() {
        return failuresOnlyCheckbox.isChecked();
    }

    public void setCompoundResult(boolean compoundResult) {
        if (compoundResult != compoundResultCheckbox.isChecked()) {
            compoundResultCheckbox.click();
        }
    }

    public boolean isCompoundResultEnabled() {
        return compoundResultCheckbox.isChecked();
    }

    public void setShowNumbersWithoutFormatting(boolean show) {
        if (show != showNumbersWithoutFormattingCheckbox.isChecked()) {
            showNumbersWithoutFormattingCheckbox.click();
        }
    }

    public boolean isShowNumbersWithoutFormattingEnabled() {
        return showNumbersWithoutFormattingCheckbox.isChecked();
    }

    public void saveSettings() {
        saveBtn.click();
    }

    public void resetSettings() {
        resetBtn.click();
    }

    public void cancelSettings() {
        cancelBtn.click();
    }

    public boolean isSaveButtonEnabled() {
        return saveBtn.isEnabled();
    }

    public boolean isResetButtonAvailable() {
        return resetBtn.isVisible();
    }

    public boolean isSuccessNotificationDisplayed() {
        return successNotification.isVisible();
    }

    public boolean isErrorNotificationDisplayed() {
        return errorNotification.isVisible();
    }

    public String getSuccessNotificationMessage() {
        return successNotification.textContent();
    }

    public String getErrorNotificationMessage() {
        return errorNotification.textContent();
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