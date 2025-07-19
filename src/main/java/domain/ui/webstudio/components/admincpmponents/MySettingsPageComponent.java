package domain.ui.webstudio.components.admincpmponents;

import configuration.core.ui.BasePageComponent;
import configuration.core.ui.SmartWebElement;
import helpers.utils.WaitUtil;
import org.openqa.selenium.support.FindBy;

import java.util.HashMap;
import java.util.Map;


public class MySettingsPageComponent extends BasePageComponent {

    // Table Settings Section Elements
    @FindBy(xpath = ".//input[@type='checkbox' and (contains(@id,'showHeader') or ./following-sibling::*[contains(text(),'Show Header')])]")
    private SmartWebElement showHeaderCheckbox;

    @FindBy(xpath = ".//input[@type='checkbox' and (contains(@id,'showFormulas') or ./following-sibling::*[contains(text(),'Show Formulas')])]")
    private SmartWebElement showFormulasCheckbox;

    @FindBy(xpath = ".//select[contains(@id,'defaultOrder') or ./preceding-sibling::*[contains(text(),'Default Order')]] | .//div[contains(@class,'ant-select') and ./preceding-sibling::*[contains(text(),'Default Order')]]")
    private SmartWebElement defaultOrderDropdown;

    @FindBy(xpath = ".//div[contains(@class,'ant-select-item') and contains(text(),'By Excel Sheet')]")
    private SmartWebElement byExcelSheetOption;

    // Testing Settings Section Elements
    @FindBy(xpath = ".//input[@type='number' and (contains(@id,'testsPerPage') or ./preceding-sibling::*[contains(text(),'Tests Per Page')])]")
    private SmartWebElement testsPerPageField;

    @FindBy(xpath = ".//input[@type='checkbox' and (contains(@id,'failuresOnly') or ./following-sibling::*[contains(text(),'Failures Only')])]")
    private SmartWebElement failuresOnlyCheckbox;

    @FindBy(xpath = ".//input[@type='checkbox' and (contains(@id,'compoundResult') or ./following-sibling::*[contains(text(),'Compound Result')])]")
    private SmartWebElement compoundResultCheckbox;

    // Trace Settings Section Elements
    @FindBy(xpath = ".//input[@type='checkbox' and (contains(@id,'showNumbers') or ./following-sibling::*[contains(text(),'Show Numbers Without Formatting')])]")
    private SmartWebElement showNumbersWithoutFormattingCheckbox;

    // Action Buttons
    @FindBy(xpath = ".//button[./span[text()='Save'] or @type='submit']")
    private SmartWebElement saveBtn;

    @FindBy(xpath = ".//button[./span[text()='Reset'] or ./span[text()='Reset to Default']]")
    private SmartWebElement resetBtn;

    @FindBy(xpath = ".//button[./span[text()='Cancel']]")
    private SmartWebElement cancelBtn;

    // Status and Notification Elements
    @FindBy(xpath = ".//div[contains(@class,'ant-notification') or contains(@class,'success-message')]")
    private SmartWebElement successNotification;

    @FindBy(xpath = ".//div[contains(@class,'ant-notification') or contains(@class,'error-message')]")
    private SmartWebElement errorNotification;

    // Optimized order option mappings
    private final Map<String, SmartWebElement> orderOptionMappings = new HashMap<>();

    // Constructor to initialize option mappings
    public MySettingsPageComponent() {
        super();
        initializeOrderOptionMappings();
    }

    
    private void initializeOrderOptionMappings() {
        orderOptionMappings.put("by excel sheet", byExcelSheetOption);
        // Additional order options can be added here
    }

    // Table Settings Methods
    
    public void setShowHeader(boolean show) {
        if (show != showHeaderCheckbox.isSelected()) {
            showHeaderCheckbox.click();
        }
    }

    
    public boolean isShowHeaderEnabled() {
        return showHeaderCheckbox.isSelected();
    }

    
    public void setShowFormulas(boolean show) {
        if (show != showFormulasCheckbox.isSelected()) {
            showFormulasCheckbox.click();
        }
    }

    
    public boolean isShowFormulasEnabled() {
        return showFormulasCheckbox.isSelected();
    }

    
    public void setDefaultOrder(String orderOption) {
        defaultOrderDropdown.click();
        
        // Use optimized mapping instead of switch statement
        SmartWebElement optionElement = getOrderOptionElement(orderOption);
        if (optionElement != null) {
            optionElement.click();
        }
    }

    
    private SmartWebElement getOrderOptionElement(String orderOption) {
        return orderOptionMappings.get(orderOption.toLowerCase());
    }

    
    public void setDefaultOrderByExcelSheet() {
        setDefaultOrder("By Excel Sheet");
    }

    
    public String getDefaultOrder() {
        return defaultOrderDropdown.getAttribute("title");
    }

    // Testing Settings Methods
    
    public void setTestsPerPage(int testsPerPage) {
        testsPerPageField.sendKeys(String.valueOf(testsPerPage));
    }

    
    public int getTestsPerPage() {
        String value = testsPerPageField.getAttribute("value");
        return value != null && !value.isEmpty() ? Integer.parseInt(value) : 5;
    }

    
    public void setFailuresOnly(boolean failuresOnly) {
        if (failuresOnly != failuresOnlyCheckbox.isSelected()) {
            failuresOnlyCheckbox.click();
        }
    }

    
    public boolean isFailuresOnlyEnabled() {
        return failuresOnlyCheckbox.isSelected();
    }

    
    public void setCompoundResult(boolean compoundResult) {
        if (compoundResult != compoundResultCheckbox.isSelected()) {
            compoundResultCheckbox.click();
        }
    }

    
    public boolean isCompoundResultEnabled() {
        return compoundResultCheckbox.isSelected();
    }

    // Trace Settings Methods
    
    public void setShowNumbersWithoutFormatting(boolean show) {
        if (show != showNumbersWithoutFormattingCheckbox.isSelected()) {
            showNumbersWithoutFormattingCheckbox.click();
        }
    }

    
    public boolean isShowNumbersWithoutFormattingEnabled() {
        return showNumbersWithoutFormattingCheckbox.isSelected();
    }

    // Action Methods
    
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
        return resetBtn.isDisplayed(2);
    }

    // Status and Notification Methods
    
    public boolean isSuccessNotificationDisplayed() {
        return successNotification.isDisplayed(3);
    }

    
    public boolean isErrorNotificationDisplayed() {
        return errorNotification.isDisplayed(3);
    }

    
    public String getSuccessNotificationMessage() {
        return successNotification.getText();
    }

    
    public String getErrorNotificationMessage() {
        return errorNotification.getText();
    }

    // Complex Settings Operations
    
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

    // Settings Validation Methods
    
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