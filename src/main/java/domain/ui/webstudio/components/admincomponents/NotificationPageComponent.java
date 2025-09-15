package domain.ui.webstudio.components.admincomponents;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;

public class NotificationPageComponent extends BaseComponent {

    private WebElement enableNotificationsCheckbox;
    private WebElement emailNotificationsCheckbox;
    private WebElement browserNotificationsCheckbox;
    private WebElement systemNotificationsCheckbox;
    private WebElement deploymentNotificationsCheckbox;
    private WebElement buildNotificationsCheckbox;
    private WebElement saveBtn;
    private WebElement resetBtn;
    private WebElement cancelBtn;

    public NotificationPageComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public NotificationPageComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        enableNotificationsCheckbox = createScopedElement("xpath=.//input[@type='checkbox' and (contains(@id,'enableNotifications') or ./following-sibling::*[contains(text(),'Enable Notifications')])]", "enableNotificationsCheckbox");
        emailNotificationsCheckbox = createScopedElement("xpath=.//input[@type='checkbox' and (contains(@id,'emailNotifications') or ./following-sibling::*[contains(text(),'Email Notifications')])]", "emailNotificationsCheckbox");
        browserNotificationsCheckbox = createScopedElement("xpath=.//input[@type='checkbox' and (contains(@id,'browserNotifications') or ./following-sibling::*[contains(text(),'Browser Notifications')])]", "browserNotificationsCheckbox");
        systemNotificationsCheckbox = createScopedElement("xpath=.//input[@type='checkbox' and (contains(@id,'systemNotifications') or ./following-sibling::*[contains(text(),'System Notifications')])]", "systemNotificationsCheckbox");
        deploymentNotificationsCheckbox = createScopedElement("xpath=.//input[@type='checkbox' and (contains(@id,'deploymentNotifications') or ./following-sibling::*[contains(text(),'Deployment Notifications')])]", "deploymentNotificationsCheckbox");
        buildNotificationsCheckbox = createScopedElement("xpath=.//input[@type='checkbox' and (contains(@id,'buildNotifications') or ./following-sibling::*[contains(text(),'Build Notifications')])]", "buildNotificationsCheckbox");
        saveBtn = createScopedElement("xpath=.//button[./span[text()='Save'] or @type='submit']", "saveBtn");
        resetBtn = createScopedElement("xpath=.//button[./span[text()='Reset'] or ./span[text()='Reset to Default']]", "resetBtn");
        cancelBtn = createScopedElement("xpath=.//button[./span[text()='Cancel']]", "cancelBtn");
    }

    public void setEnableNotifications(boolean enable) {
        if (enable != enableNotificationsCheckbox.isChecked()) {
            enableNotificationsCheckbox.click();
        }
    }

    public boolean isNotificationsEnabled() {
        return enableNotificationsCheckbox.isChecked();
    }

    public void setEmailNotifications(boolean enable) {
        if (enable != emailNotificationsCheckbox.isChecked()) {
            emailNotificationsCheckbox.click();
        }
    }

    public boolean isEmailNotificationsEnabled() {
        return emailNotificationsCheckbox.isChecked();
    }

    public void setBrowserNotifications(boolean enable) {
        if (enable != browserNotificationsCheckbox.isChecked()) {
            browserNotificationsCheckbox.click();
        }
    }

    public boolean isBrowserNotificationsEnabled() {
        return browserNotificationsCheckbox.isChecked();
    }

    public void setSystemNotifications(boolean enable) {
        if (enable != systemNotificationsCheckbox.isChecked()) {
            systemNotificationsCheckbox.click();
        }
    }

    public boolean isSystemNotificationsEnabled() {
        return systemNotificationsCheckbox.isChecked();
    }

    public void setDeploymentNotifications(boolean enable) {
        if (enable != deploymentNotificationsCheckbox.isChecked()) {
            deploymentNotificationsCheckbox.click();
        }
    }

    public boolean isDeploymentNotificationsEnabled() {
        return deploymentNotificationsCheckbox.isChecked();
    }

    public void setBuildNotifications(boolean enable) {
        if (enable != buildNotificationsCheckbox.isChecked()) {
            buildNotificationsCheckbox.click();
        }
    }

    public boolean isBuildNotificationsEnabled() {
        return buildNotificationsCheckbox.isChecked();
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

    public void configureAllNotifications(boolean enableNotifications, boolean emailNotifications, 
                                        boolean browserNotifications, boolean systemNotifications,
                                        boolean deploymentNotifications, boolean buildNotifications) {
        setEnableNotifications(enableNotifications);
        setEmailNotifications(emailNotifications);
        setBrowserNotifications(browserNotifications);
        setSystemNotifications(systemNotifications);
        setDeploymentNotifications(deploymentNotifications);
        setBuildNotifications(buildNotifications);
        saveSettings();
    }
}