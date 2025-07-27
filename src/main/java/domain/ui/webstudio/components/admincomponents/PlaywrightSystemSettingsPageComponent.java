package domain.ui.webstudio.components.admincomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

public class PlaywrightSystemSettingsPageComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement dispatchingValidationCheckbox;
    private PlaywrightWebElement verifyOnEditCheckbox;
    private PlaywrightWebElement testThreadCountField;
    private PlaywrightWebElement projectHistoryCountField;
    private PlaywrightWebElement clearAllHistoryBtn;
    private PlaywrightWebElement updateSystemPropertiesCheckbox;
    private PlaywrightWebElement saveBtn;
    private PlaywrightWebElement cancelBtn;

    public PlaywrightSystemSettingsPageComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightSystemSettingsPageComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        dispatchingValidationCheckbox = new PlaywrightWebElement(page, "#dispatchingValidationEnabled", "Dispatching Validation Checkbox");
        verifyOnEditCheckbox = new PlaywrightWebElement(page, "#autoCompile", "Verify On Edit Checkbox");
        testThreadCountField = new PlaywrightWebElement(page, "#testRunThreadCount", "Test Thread Count Field");
        projectHistoryCountField = new PlaywrightWebElement(page, "#projectHistoryCount", "Project History Count Field");
        clearAllHistoryBtn = new PlaywrightWebElement(page, ".//button[./span[text()='Clear All History']]", "Clear All History Button");
        updateSystemPropertiesCheckbox = new PlaywrightWebElement(page, "#updateSystemProperties", "Update System Properties Checkbox");
        saveBtn = new PlaywrightWebElement(page, ".//button[./span[text()='Save'] or @type='submit']", "Save Button");
        cancelBtn = new PlaywrightWebElement(page, ".//button[./span[text()='Cancel']]", "Cancel Button");
    }

    public void setDispatchingValidation(boolean enable) {
        if (enable != dispatchingValidationCheckbox.isChecked()) {
            dispatchingValidationCheckbox.click();
        }
    }

    public boolean isDispatchingValidationEnabled() {
        return dispatchingValidationCheckbox.isChecked();
    }

    public void setVerifyOnEdit(boolean enable) {
        if (enable != verifyOnEditCheckbox.isChecked()) {
            verifyOnEditCheckbox.click();
        }
    }

    public boolean isVerifyOnEditEnabled() {
        return verifyOnEditCheckbox.isChecked();
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