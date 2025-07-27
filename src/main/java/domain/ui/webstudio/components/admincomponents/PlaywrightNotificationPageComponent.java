package domain.ui.webstudio.components.admincomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

public class PlaywrightNotificationPageComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement enableNotificationsCheckbox;
    private PlaywrightWebElement emailNotificationsCheckbox;
    private PlaywrightWebElement browserNotificationsCheckbox;
    private PlaywrightWebElement systemNotificationsCheckbox;
    private PlaywrightWebElement deploymentNotificationsCheckbox;
    private PlaywrightWebElement buildNotificationsCheckbox;
    private PlaywrightWebElement saveBtn;
    private PlaywrightWebElement resetBtn;
    private PlaywrightWebElement cancelBtn;

    public PlaywrightNotificationPageComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightNotificationPageComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        enableNotificationsCheckbox = new PlaywrightWebElement(page, ".//input[@type='checkbox' and (contains(@id,'enableNotifications') or ./following-sibling::*[contains(text(),'Enable Notifications')])]", "Enable Notifications Checkbox");
        emailNotificationsCheckbox = new PlaywrightWebElement(page, ".//input[@type='checkbox' and (contains(@id,'emailNotifications') or ./following-sibling::*[contains(text(),'Email Notifications')])]", "Email Notifications Checkbox");
        browserNotificationsCheckbox = new PlaywrightWebElement(page, ".//input[@type='checkbox' and (contains(@id,'browserNotifications') or ./following-sibling::*[contains(text(),'Browser Notifications')])]", "Browser Notifications Checkbox");
        systemNotificationsCheckbox = new PlaywrightWebElement(page, ".//input[@type='checkbox' and (contains(@id,'systemNotifications') or ./following-sibling::*[contains(text(),'System Notifications')])]", "System Notifications Checkbox");
        deploymentNotificationsCheckbox = new PlaywrightWebElement(page, ".//input[@type='checkbox' and (contains(@id,'deploymentNotifications') or ./following-sibling::*[contains(text(),'Deployment Notifications')])]", "Deployment Notifications Checkbox");
        buildNotificationsCheckbox = new PlaywrightWebElement(page, ".//input[@type='checkbox' and (contains(@id,'buildNotifications') or ./following-sibling::*[contains(text(),'Build Notifications')])]", "Build Notifications Checkbox");
        saveBtn = new PlaywrightWebElement(page, ".//button[./span[text()='Save'] or @type='submit']", "Save Button");
        resetBtn = new PlaywrightWebElement(page, ".//button[./span[text()='Reset'] or ./span[text()='Reset to Default']]", "Reset Button");
        cancelBtn = new PlaywrightWebElement(page, ".//button[./span[text()='Cancel']]", "Cancel Button");
    }

    public void setEnableNotifications(boolean enable) {
        if (enable != enableNotificationsCheckbox.isSelected()) {
            enableNotificationsCheckbox.click();
        }
    }

    public boolean isNotificationsEnabled() {
        return enableNotificationsCheckbox.isSelected();
    }

    public void setEmailNotifications(boolean enable) {
        if (enable != emailNotificationsCheckbox.isSelected()) {
            emailNotificationsCheckbox.click();
        }
    }

    public boolean isEmailNotificationsEnabled() {
        return emailNotificationsCheckbox.isSelected();
    }

    public void setBrowserNotifications(boolean enable) {
        if (enable != browserNotificationsCheckbox.isSelected()) {
            browserNotificationsCheckbox.click();
        }
    }

    public boolean isBrowserNotificationsEnabled() {
        return browserNotificationsCheckbox.isSelected();
    }

    public void setSystemNotifications(boolean enable) {
        if (enable != systemNotificationsCheckbox.isSelected()) {
            systemNotificationsCheckbox.click();
        }
    }

    public boolean isSystemNotificationsEnabled() {
        return systemNotificationsCheckbox.isSelected();
    }

    public void setDeploymentNotifications(boolean enable) {
        if (enable != deploymentNotificationsCheckbox.isSelected()) {
            deploymentNotificationsCheckbox.click();
        }
    }

    public boolean isDeploymentNotificationsEnabled() {
        return deploymentNotificationsCheckbox.isSelected();
    }

    public void setBuildNotifications(boolean enable) {
        if (enable != buildNotificationsCheckbox.isSelected()) {
            buildNotificationsCheckbox.click();
        }
    }

    public boolean isBuildNotificationsEnabled() {
        return buildNotificationsCheckbox.isSelected();
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