package domain.ui.webstudio.components.admincomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.BaseComponent;
import domain.ui.webstudio.pages.mainpages.LoginPage;
import helpers.service.UserService;

public class SystemSettingsPageComponent extends BaseComponent {

    private WebElement dispatchingValidationCheckbox;
    private WebElement verifyOnEditCheckbox;
    private WebElement testThreadCountField;
    private WebElement projectHistoryCountField;
    private WebElement clearAllHistoryBtn;
    private WebElement applyButton;
    private WebElement errorMessage;

    // Date/Time Format fields
    private WebElement dateFormatField;
    private WebElement timeFormatField;

    // Database Configuration fields
    private WebElement databaseUrlField;
    private WebElement databaseUserField;
    private WebElement databasePasswordField;
    private WebElement databaseMaxPoolSizeField;

    public SystemSettingsPageComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public SystemSettingsPageComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        dispatchingValidationCheckbox = createScopedElement("#dispatchingValidationEnabled", "dispatchingValidationCheckbox");
        verifyOnEditCheckbox = createScopedElement("#autoCompile", "verifyOnEditCheckbox");
        testThreadCountField = createScopedElement("#testRunThreadCount", "testThreadCountField");
        projectHistoryCountField = createScopedElement("#projectHistoryCount", "projectHistoryCountField");
        clearAllHistoryBtn = createScopedElement("xpath=.//button[./span[text()='Clear All History']]", "clearAllHistoryBtn");
        applyButton = createScopedElement("xpath=.//button[./span[text()='Apply Changes'] or ./span[text()='Apply']]", "applyButton");
        errorMessage = createScopedElement("xpath=.//div[contains(@class, 'ant-form-item-explain-error')]", "errorMessage");

        // Date/Time Format fields
        dateFormatField = createScopedElement("xpath=.//input[@id='datePattern']", "dateFormatField");
        timeFormatField = createScopedElement("xpath=.//input[@id='timeFormat']", "timeFormatField");

        // Database Configuration fields
        databaseUrlField = createScopedElement("xpath=.//input[@id='db_url']", "databaseUrlField");
        databaseUserField = createScopedElement("xpath=.//input[@id='db_user']", "databaseUserField");
        databasePasswordField = createScopedElement("xpath=.//input[@id='db_password']", "databasePasswordField");
        databaseMaxPoolSizeField = createScopedElement("xpath=.//input[@id='db_maximumPoolSize']", "databaseMaxPoolSizeField");
    }

    public void setDispatchingValidation(boolean enable) {
        if (enable != dispatchingValidationCheckbox.isChecked()) {
            dispatchingValidationCheckbox.click();
        }
    }

    public void setVerifyOnEdit(boolean enable) {
        if (enable != verifyOnEditCheckbox.isChecked()) {
            verifyOnEditCheckbox.click();
        }
    }

    public void setTestThreadCount(String value) {
        testThreadCountField.fill(value);
    }

    public void clickApplyButton() {
        applyButton.click();
        getModalOkBtn().waitForVisible().click();
    }

    public void applySettingsAndRelogin(User user) {
        clickApplyButton();
        new LoginPage().login(UserService.getUser(user));
    }

    // Date/Time Format methods
    public void setDateFormat(String format) {
        dateFormatField.fill(format);
    }

    public String getDateFormat() {
        return dateFormatField.getAttribute("value");
    }

    public void setTimeFormat(String format) {
        timeFormatField.fill(format);
    }

    public String getTimeFormat() {
        return timeFormatField.getAttribute("value");
    }

    // Database Configuration methods
    public String getDatabaseUrl() {
        return databaseUrlField.getAttribute("value");
    }

    public void setDatabaseUrl(String url) {
        databaseUrlField.fill(url);
    }

    public String getDatabaseUser() {
        return databaseUserField.getAttribute("value");
    }

    public void setDatabaseUser(String user) {
        databaseUserField.fill(user);
    }

    public void setDatabasePassword(String password) {
        databasePasswordField.fill(password);
    }

    public int getDatabaseMaxPoolSize() {
        String value = databaseMaxPoolSizeField.getAttribute("value");
        return value != null && !value.isEmpty() ? Integer.parseInt(value) : 50;
    }

    public void setDatabaseMaxPoolSize(int size) {
        databaseMaxPoolSizeField.fill(String.valueOf(size));
    }

    public void setProjectHistoryCount(String value) {
        projectHistoryCountField.clear();
        projectHistoryCountField.fill(value);
    }

    public String getProjectHistoryCount() {
        return projectHistoryCountField.getAttribute("value");
    }

    public void clearAllHistory() {
        clearAllHistoryBtn.click();
        getModalOkBtn().waitForVisible().click();
    }
}