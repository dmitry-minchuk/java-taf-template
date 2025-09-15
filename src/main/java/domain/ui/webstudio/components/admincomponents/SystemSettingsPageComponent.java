package domain.ui.webstudio.components.admincomponents;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;

public class SystemSettingsPageComponent extends BaseComponent {

    private WebElement dispatchingValidationCheckbox;
    private WebElement verifyOnEditCheckbox;
    private WebElement testThreadCountField;
    private WebElement projectHistoryCountField;
    private WebElement clearAllHistoryBtn;
    private WebElement updateSystemPropertiesCheckbox;
    private WebElement saveBtn;
    private WebElement cancelBtn;

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
        updateSystemPropertiesCheckbox = createScopedElement("#updateSystemProperties", "updateSystemPropertiesCheckbox");
        saveBtn = createScopedElement("xpath=.//button[./span[text()='Save'] or @type='submit']", "saveBtn");
        cancelBtn = createScopedElement("xpath=.//button[./span[text()='Cancel']]", "cancelBtn");
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

    public void setTestThreadCount(int count) {
        testThreadCountField.fill(String.valueOf(count));
    }

    public int getTestThreadCount() {
        String value = testThreadCountField.getAttribute("value");
        return value != null && !value.isEmpty() ? Integer.parseInt(value) : 1;
    }

    public void setProjectHistoryCount(int count) {
        projectHistoryCountField.fill(String.valueOf(count));
    }

    public int getProjectHistoryCount() {
        String value = projectHistoryCountField.getAttribute("value");
        return value != null && !value.isEmpty() ? Integer.parseInt(value) : 10;
    }

    public void clearAllHistory() {
        clearAllHistoryBtn.click();
    }

    public void setUpdateSystemProperties(boolean enable) {
        if (enable != updateSystemPropertiesCheckbox.isChecked()) {
            updateSystemPropertiesCheckbox.click();
        }
    }

    public boolean isUpdateSystemPropertiesEnabled() {
        return updateSystemPropertiesCheckbox.isChecked();
    }

    public void saveSettings() {
        saveBtn.click();
    }

    public void cancelSettings() {
        cancelBtn.click();
    }

    public void configureSystemSettings(boolean dispatchingValidation, boolean verifyOnEdit, 
                                      int testThreadCount, int projectHistoryCount, 
                                      boolean updateSystemProperties) {
        setDispatchingValidation(dispatchingValidation);
        setVerifyOnEdit(verifyOnEdit);
        setTestThreadCount(testThreadCount);
        setProjectHistoryCount(projectHistoryCount);
        setUpdateSystemProperties(updateSystemProperties);
        saveSettings();
    }
}