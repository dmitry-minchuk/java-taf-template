package domain.ui.webstudio.components.admincpmponents;

import configuration.core.ui.BasePageComponent;
import configuration.core.ui.SmartWebElement;
import helpers.utils.WaitUtil;
import org.openqa.selenium.support.FindBy;

public class SystemSettingsPageComponent extends BasePageComponent {

    // Core Settings Section
    @FindBy(id = "dispatchingValidationEnabled")
    private SmartWebElement dispatchingValidationCheckbox;

    @FindBy(id = "autoCompile")
    private SmartWebElement verifyOnEditCheckbox;

    // Testing Section
    @FindBy(id = "testRunThreadCount")
    private SmartWebElement testThreadCountField;

    // History Section
    @FindBy(id = "projectHistoryCount")
    private SmartWebElement projectHistoryCountField;

    @FindBy(xpath = ".//button[./span[text()='Clear All History']]")
    private SmartWebElement clearAllHistoryBtn;

    // Other Settings Section
    @FindBy(id = "updateSystemProperties")
    private SmartWebElement updateSystemPropertiesCheckbox;

    @FindBy(id = "datePattern")
    private SmartWebElement datePatternField;

    @FindBy(id = "timeFormat")
    private SmartWebElement timeFormatField;

    // Database Configuration Section
    @FindBy(id = "db_url")
    private SmartWebElement databaseUrlField;

    @FindBy(id = "db_username")
    private SmartWebElement databaseUsernameField;

    @FindBy(id = "db_password")
    private SmartWebElement databasePasswordField;

    @FindBy(id = "db_driver")
    private SmartWebElement databaseDriverField;

    // Action buttons
    @FindBy(xpath = ".//button[./span[text()='Apply']]")
    private SmartWebElement applyBtn;

    @FindBy(xpath = ".//button[./span[text()='Save']]")
    private SmartWebElement saveBtn;

    @FindBy(xpath = ".//button[./span[text()='Reset']]")
    private SmartWebElement resetBtn;

    // Core Settings Methods
    public void enableDispatchingValidation() {
        if (!isDispatchingValidationEnabled()) {
            dispatchingValidationCheckbox.click();
        }
    }

    public void disableDispatchingValidation() {
        if (isDispatchingValidationEnabled()) {
            dispatchingValidationCheckbox.click();
        }
    }

    public boolean isDispatchingValidationEnabled() {
        return dispatchingValidationCheckbox.isSelected();
    }

    public void enableVerifyOnEdit() {
        if (!isVerifyOnEditEnabled()) {
            verifyOnEditCheckbox.click();
        }
    }

    public void disableVerifyOnEdit() {
        if (isVerifyOnEditEnabled()) {
            verifyOnEditCheckbox.click();
        }
    }

    public boolean isVerifyOnEditEnabled() {
        return verifyOnEditCheckbox.isSelected();
    }

    // Testing Configuration Methods
    public void setTestThreadCount(int threadCount) {
        testThreadCountField.sendKeys(String.valueOf(threadCount));
    }

    public int getTestThreadCount() {
        return Integer.parseInt(testThreadCountField.getAttribute("value"));
    }

    // History Configuration Methods
    public void setProjectHistoryCount(int historyCount) {
        projectHistoryCountField.sendKeys(String.valueOf(historyCount));
    }

    public int getProjectHistoryCount() {
        return Integer.parseInt(projectHistoryCountField.getAttribute("value"));
    }

    public void clearAllHistory() {
        clearAllHistoryBtn.click();
        getConfirmationPopup().confirm();
    }

    // Other Settings Methods
    public void enableUpdateSystemProperties() {
        if (!isUpdateSystemPropertiesEnabled()) {
            updateSystemPropertiesCheckbox.click();
        }
    }

    public void disableUpdateSystemProperties() {
        if (isUpdateSystemPropertiesEnabled()) {
            updateSystemPropertiesCheckbox.click();
        }
    }

    public boolean isUpdateSystemPropertiesEnabled() {
        return updateSystemPropertiesCheckbox.isSelected();
    }

    public void setDatePattern(String pattern) {
        datePatternField.sendKeys(pattern);
    }

    public String getDatePattern() {
        return datePatternField.getAttribute("value");
    }

    public void setTimeFormat(String format) {
        timeFormatField.sendKeys(format);
    }

    public String getTimeFormat() {
        return timeFormatField.getAttribute("value");
    }

    // Database Configuration Methods
    public void setDatabaseUrl(String url) {
        databaseUrlField.sendKeys(url);
    }

    public String getDatabaseUrl() {
        return databaseUrlField.getAttribute("value");
    }

    public void setDatabaseUsername(String username) {
        databaseUsernameField.sendKeys(username);
    }

    public String getDatabaseUsername() {
        return databaseUsernameField.getAttribute("value");
    }

    public void setDatabasePassword(String password) {
        databasePasswordField.sendKeys(password);
    }

    public String getDatabasePassword() {
        return databasePasswordField.getAttribute("value");
    }

    public void setDatabaseDriver(String driver) {
        databaseDriverField.sendKeys(driver);
    }

    public String getDatabaseDriver() {
        return databaseDriverField.getAttribute("value");
    }

    public void configureDatabaseConnection(String url, String username, String password, String driver) {
        setDatabaseUrl(url);
        setDatabaseUsername(username);
        setDatabasePassword(password);
        setDatabaseDriver(driver);
    }

    // Action Methods
    public void applySettings() {
        applyBtn.click();
        getConfirmationPopup().confirm();
    }

    public void saveSettings() {
        saveBtn.click();
        getConfirmationPopup().confirm();
    }

    public void resetSettings() {
        resetBtn.click();
        getConfirmationPopup().confirm();
    }

    public void configureCoreSettings(boolean dispatchingValidation, boolean verifyOnEdit, 
                                    int testThreadCount, int projectHistoryCount) {
        if (dispatchingValidation) {
            enableDispatchingValidation();
        } else {
            disableDispatchingValidation();
        }
        
        if (verifyOnEdit) {
            enableVerifyOnEdit();
        } else {
            disableVerifyOnEdit();
        }
        
        setTestThreadCount(testThreadCount);
        setProjectHistoryCount(projectHistoryCount);
    }

    public void configureFormatSettings(String datePattern, String timeFormat, boolean updateSystemProperties) {
        setDatePattern(datePattern);
        setTimeFormat(timeFormat);
        
        if (updateSystemProperties) {
            enableUpdateSystemProperties();
        } else {
            disableUpdateSystemProperties();
        }
    }
}