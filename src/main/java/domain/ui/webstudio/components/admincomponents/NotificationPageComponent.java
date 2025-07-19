package domain.ui.webstudio.components.admincomponents;

import configuration.core.ui.BasePageComponent;
import configuration.core.ui.SmartWebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;

public class NotificationPageComponent extends BasePageComponent {

    // Notification Settings Section Elements
    @FindBy(xpath = ".//input[@type='checkbox' and (contains(@id,'enableNotifications') or ./following-sibling::*[contains(text(),'Enable Notifications')])]")
    private SmartWebElement enableNotificationsCheckbox;

    @FindBy(xpath = ".//input[@type='checkbox' and (contains(@id,'emailNotifications') or ./following-sibling::*[contains(text(),'Email Notifications')])]")
    private SmartWebElement emailNotificationsCheckbox;

    @FindBy(xpath = ".//input[@type='checkbox' and (contains(@id,'browserNotifications') or ./following-sibling::*[contains(text(),'Browser Notifications')])]")
    private SmartWebElement browserNotificationsCheckbox;

    @FindBy(xpath = ".//input[@type='checkbox' and (contains(@id,'systemNotifications') or ./following-sibling::*[contains(text(),'System Notifications')])]")
    private SmartWebElement systemNotificationsCheckbox;

    // Notification Types Configuration
    @FindBy(xpath = ".//input[@type='checkbox' and (contains(@id,'deploymentNotifications') or ./following-sibling::*[contains(text(),'Deployment Notifications')])]")
    private SmartWebElement deploymentNotificationsCheckbox;

    @FindBy(xpath = ".//input[@type='checkbox' and (contains(@id,'buildNotifications') or ./following-sibling::*[contains(text(),'Build Notifications')])]")
    private SmartWebElement buildNotificationsCheckbox;

    @FindBy(xpath = ".//input[@type='checkbox' and (contains(@id,'errorNotifications') or ./following-sibling::*[contains(text(),'Error Notifications')])]")
    private SmartWebElement errorNotificationsCheckbox;

    @FindBy(xpath = ".//input[@type='checkbox' and (contains(@id,'warningNotifications') or ./following-sibling::*[contains(text(),'Warning Notifications')])]")
    private SmartWebElement warningNotificationsCheckbox;

    @FindBy(xpath = ".//input[@type='checkbox' and (contains(@id,'infoNotifications') or ./following-sibling::*[contains(text(),'Info Notifications')])]")
    private SmartWebElement infoNotificationsCheckbox;

    // Notification Frequency Settings
    @FindBy(xpath = ".//select[contains(@id,'notificationFrequency')] | .//div[contains(@class,'ant-select') and ./preceding-sibling::*[contains(text(),'Notification Frequency')]]")
    private SmartWebElement notificationFrequencyDropdown;

    @FindBy(xpath = ".//div[contains(@class,'ant-select-item') and contains(text(),'Immediate')]")
    private SmartWebElement immediateFrequencyOption;

    @FindBy(xpath = ".//div[contains(@class,'ant-select-item') and contains(text(),'Hourly')]")
    private SmartWebElement hourlyFrequencyOption;

    @FindBy(xpath = ".//div[contains(@class,'ant-select-item') and contains(text(),'Daily')]")
    private SmartWebElement dailyFrequencyOption;

    @FindBy(xpath = ".//div[contains(@class,'ant-select-item') and contains(text(),'Weekly')]")
    private SmartWebElement weeklyFrequencyOption;

    // Email Configuration Section
    @FindBy(xpath = ".//input[@placeholder='Notification Email Address' or contains(@id,'notificationEmail')]")
    private SmartWebElement notificationEmailField;

    @FindBy(xpath = ".//input[@placeholder='Email Subject Prefix' or contains(@id,'emailSubjectPrefix')]")
    private SmartWebElement emailSubjectPrefixField;

    @FindBy(xpath = ".//textarea[@placeholder='Email Template' or contains(@id,'emailTemplate')]")
    private SmartWebElement emailTemplateField;

    // Browser Notification Settings
    @FindBy(xpath = ".//input[@type='number' and (contains(@id,'notificationTimeout') or ./preceding-sibling::*[contains(text(),'Notification Timeout')])]")
    private SmartWebElement notificationTimeoutField;

    @FindBy(xpath = ".//input[@type='checkbox' and (contains(@id,'soundEnabled') or ./following-sibling::*[contains(text(),'Enable Sound')])]")
    private SmartWebElement soundEnabledCheckbox;

    @FindBy(xpath = ".//select[contains(@id,'notificationPosition')] | .//div[contains(@class,'ant-select') and ./preceding-sibling::*[contains(text(),'Notification Position')]]")
    private SmartWebElement notificationPositionDropdown;

    @FindBy(xpath = ".//div[contains(@class,'ant-select-item') and contains(text(),'Top Right')]")
    private SmartWebElement topRightPositionOption;

    @FindBy(xpath = ".//div[contains(@class,'ant-select-item') and contains(text(),'Top Left')]")
    private SmartWebElement topLeftPositionOption;

    @FindBy(xpath = ".//div[contains(@class,'ant-select-item') and contains(text(),'Bottom Right')]")
    private SmartWebElement bottomRightPositionOption;

    @FindBy(xpath = ".//div[contains(@class,'ant-select-item') and contains(text(),'Bottom Left')]")
    private SmartWebElement bottomLeftPositionOption;

    // Notification History Section
    @FindBy(xpath = ".//table//tbody[@class='ant-table-tbody']")
    private SmartWebElement notificationHistoryTable;

    @FindBy(xpath = ".//table//tbody[@class='ant-table-tbody']//tr[@class='ant-table-row ant-table-row-level-0']")
    private List<SmartWebElement> notificationHistoryRows;

    @FindBy(xpath = ".//button[./span[text()='Clear History'] or ./span[contains(text(),'Clear')]]")
    private SmartWebElement clearHistoryBtn;

    @FindBy(xpath = ".//input[@type='checkbox' and (contains(@id,'enableHistory') or ./following-sibling::*[contains(text(),'Enable Notification History')])]")
    private SmartWebElement enableHistoryCheckbox;

    @FindBy(xpath = ".//input[@type='number' and (contains(@id,'maxHistorySize') or ./preceding-sibling::*[contains(text(),'Max History Size')])]")
    private SmartWebElement maxHistorySizeField;

    // Test Notification Section
    @FindBy(xpath = ".//button[./span[text()='Send Test Notification'] or ./span[contains(text(),'Test')]]")
    private SmartWebElement sendTestNotificationBtn;

    @FindBy(xpath = ".//input[@placeholder='Test Message' or contains(@id,'testMessage')]")
    private SmartWebElement testMessageField;

    @FindBy(xpath = ".//select[contains(@id,'testNotificationType')] | .//div[contains(@class,'ant-select') and ./preceding-sibling::*[contains(text(),'Test Type')]]")
    private SmartWebElement testNotificationTypeDropdown;

    // Action Buttons
    @FindBy(xpath = ".//button[./span[text()='Save Settings'] or ./span[text()='Save']]")
    private SmartWebElement saveSettingsBtn;

    @FindBy(xpath = ".//button[./span[text()='Reset to Default'] or ./span[text()='Reset']]")
    private SmartWebElement resetToDefaultBtn;

    @FindBy(xpath = ".//button[./span[text()='Test All Notifications']]")
    private SmartWebElement testAllNotificationsBtn;

    // Status and Notification Elements
    @FindBy(xpath = ".//div[contains(@class,'ant-notification') or contains(@class,'success-message')]")
    private SmartWebElement successNotification;

    @FindBy(xpath = ".//div[contains(@class,'ant-notification') or contains(@class,'error-message')]")
    private SmartWebElement errorNotification;


    // General Notification Settings Methods
    public void setNotificationsEnabled(boolean enable) {
        if (enable != enableNotificationsCheckbox.isSelected()) {
            enableNotificationsCheckbox.click();
        }
    }

    public boolean isNotificationsEnabled() {
        return enableNotificationsCheckbox.isSelected();
    }

    public void setEmailNotificationsEnabled(boolean enable) {
        if (enable != emailNotificationsCheckbox.isSelected()) {
            emailNotificationsCheckbox.click();
        }
    }

    public boolean isEmailNotificationsEnabled() {
        return emailNotificationsCheckbox.isSelected();
    }

    public void setBrowserNotificationsEnabled(boolean enable) {
        if (enable != browserNotificationsCheckbox.isSelected()) {
            browserNotificationsCheckbox.click();
        }
    }

    public boolean isBrowserNotificationsEnabled() {
        return browserNotificationsCheckbox.isSelected();
    }

    public void setSystemNotificationsEnabled(boolean enable) {
        if (enable != systemNotificationsCheckbox.isSelected()) {
            systemNotificationsCheckbox.click();
        }
    }

    public boolean isSystemNotificationsEnabled() {
        return systemNotificationsCheckbox.isSelected();
    }

    // Notification Types Methods
    public void setDeploymentNotificationsEnabled(boolean enable) {
        if (enable != deploymentNotificationsCheckbox.isSelected()) {
            deploymentNotificationsCheckbox.click();
        }
    }

    public void setBuildNotificationsEnabled(boolean enable) {
        if (enable != buildNotificationsCheckbox.isSelected()) {
            buildNotificationsCheckbox.click();
        }
    }

    public void setErrorNotificationsEnabled(boolean enable) {
        if (enable != errorNotificationsCheckbox.isSelected()) {
            errorNotificationsCheckbox.click();
        }
    }

    public void setWarningNotificationsEnabled(boolean enable) {
        if (enable != warningNotificationsCheckbox.isSelected()) {
            warningNotificationsCheckbox.click();
        }
    }

    public void setInfoNotificationsEnabled(boolean enable) {
        if (enable != infoNotificationsCheckbox.isSelected()) {
            infoNotificationsCheckbox.click();
        }
    }

    // Notification Frequency Methods
    public void setNotificationFrequency(String frequency) {
        notificationFrequencyDropdown.click();
        
        switch (frequency.toLowerCase()) {
            case "immediate":
                immediateFrequencyOption.click();
                break;
            case "hourly":
                hourlyFrequencyOption.click();
                break;
            case "daily":
                dailyFrequencyOption.click();
                break;
            case "weekly":
                weeklyFrequencyOption.click();
                break;
        }
    }

    public String getNotificationFrequency() {
        return notificationFrequencyDropdown.getAttribute("title");
    }

    // Email Configuration Methods
    public void setNotificationEmail(String email) {
        notificationEmailField.sendKeys(email);
    }

    public String getNotificationEmail() {
        return notificationEmailField.getAttribute("value");
    }

    public void setEmailSubjectPrefix(String prefix) {
        emailSubjectPrefixField.sendKeys(prefix);
    }

    public String getEmailSubjectPrefix() {
        return emailSubjectPrefixField.getAttribute("value");
    }

    public void setEmailTemplate(String template) {
        emailTemplateField.sendKeys(template);
    }

    public String getEmailTemplate() {
        return emailTemplateField.getAttribute("value");
    }

    // Browser Notification Settings Methods
    public void setNotificationTimeout(int timeout) {
        notificationTimeoutField.sendKeys(String.valueOf(timeout));
    }

    public int getNotificationTimeout() {
        String value = notificationTimeoutField.getAttribute("value");
        return value != null && !value.isEmpty() ? Integer.parseInt(value) : 5;
    }

    public void setSoundEnabled(boolean enable) {
        if (enable != soundEnabledCheckbox.isSelected()) {
            soundEnabledCheckbox.click();
        }
    }

    public boolean isSoundEnabled() {
        return soundEnabledCheckbox.isSelected();
    }

    public void setNotificationPosition(String position) {
        notificationPositionDropdown.click();
        
        switch (position.toLowerCase()) {
            case "top right":
                topRightPositionOption.click();
                break;
            case "top left":
                topLeftPositionOption.click();
                break;
            case "bottom right":
                bottomRightPositionOption.click();
                break;
            case "bottom left":
                bottomLeftPositionOption.click();
                break;
        }
    }

    // Notification History Methods
    public int getNotificationHistoryCount() {
        return notificationHistoryRows.size();
    }

    public void clearNotificationHistory() {
        clearHistoryBtn.click();
        getConfirmationPopup().confirm();
    }

    public void setHistoryEnabled(boolean enable) {
        if (enable != enableHistoryCheckbox.isSelected()) {
            enableHistoryCheckbox.click();
        }
    }

    public void setMaxHistorySize(int size) {
        maxHistorySizeField.sendKeys(String.valueOf(size));
    }

    // Test Notification Methods
    public void sendTestNotification(String message) {
        testMessageField.sendKeys(message);
        sendTestNotificationBtn.click();
    }

    public void sendTestNotification() {
        sendTestNotification("Test notification from Admin UI");
    }

    public void testAllNotifications() {
        testAllNotificationsBtn.click();
    }

    // Action Methods
    public void saveSettings() {
        saveSettingsBtn.click();
    }

    public void resetToDefault() {
        resetToDefaultBtn.click();
        getConfirmationPopup().confirm();
    }

    // Status and Notification Methods
    public boolean isSuccessNotificationDisplayed() {
        return successNotification.isDisplayed(3);
    }

    public boolean isErrorNotificationDisplayed() {
        return errorNotification.isDisplayed(3);
    }

    // Complex Configuration Methods
    public void configureNotificationTypes(boolean deployment, boolean build, boolean error, boolean warning, boolean info) {
        setDeploymentNotificationsEnabled(deployment);
        setBuildNotificationsEnabled(build);
        setErrorNotificationsEnabled(error);
        setWarningNotificationsEnabled(warning);
        setInfoNotificationsEnabled(info);
    }

    public void configureEmailNotifications(String email, String subjectPrefix, String template) {
        setEmailNotificationsEnabled(true);
        setNotificationEmail(email);
        if (subjectPrefix != null) {
            setEmailSubjectPrefix(subjectPrefix);
        }
        if (template != null) {
            setEmailTemplate(template);
        }
    }

    public void configureBrowserNotifications(int timeout, boolean sound, String position) {
        setBrowserNotificationsEnabled(true);
        setNotificationTimeout(timeout);
        setSoundEnabled(sound);
        setNotificationPosition(position);
    }

    public void configureAllSettings(NotificationConfig config) {
        setNotificationsEnabled(config.enabled);
        if (config.enabled) {
            setNotificationFrequency(config.frequency);
            if (config.enableEmail && config.email != null) {
                configureEmailNotifications(config.email, config.emailSubjectPrefix, config.emailTemplate);
            }
            setBrowserNotificationsEnabled(config.enableBrowser);
            if (config.enableBrowser) {
                configureBrowserNotifications(config.browserTimeout, config.soundEnabled, config.position);
            }
            configureNotificationTypes(config.deployment, config.build, config.error, config.warning, config.info);
        }
        saveSettings();
    }


    public String getNotificationSettingsInfo() {
        return String.format("Notifications - Enabled: %s | Email: %s | Browser: %s | Frequency: %s | History Count: %d",
                isNotificationsEnabled(),
                isEmailNotificationsEnabled(),
                isBrowserNotificationsEnabled(),
                getNotificationFrequency(),
                getNotificationHistoryCount());
    }

    public static class NotificationConfig {
        public final boolean enabled;
        public final String frequency;
        public final String email;
        public final boolean enableBrowser;
        public final boolean enableEmail;
        public final String emailSubjectPrefix;
        public final String emailTemplate;
        public final int browserTimeout;
        public final boolean soundEnabled;
        public final String position;
        public final boolean deployment;
        public final boolean build;
        public final boolean error;
        public final boolean warning;
        public final boolean info;

        private NotificationConfig(Builder builder) {
            this.enabled = builder.enabled;
            this.frequency = builder.frequency;
            this.email = builder.email;
            this.enableBrowser = builder.enableBrowser;
            this.enableEmail = builder.enableEmail;
            this.emailSubjectPrefix = builder.emailSubjectPrefix;
            this.emailTemplate = builder.emailTemplate;
            this.browserTimeout = builder.browserTimeout;
            this.soundEnabled = builder.soundEnabled;
            this.position = builder.position;
            this.deployment = builder.deployment;
            this.build = builder.build;
            this.error = builder.error;
            this.warning = builder.warning;
            this.info = builder.info;
        }

        public static class Builder {
            private final boolean enabled;
            private final String frequency;
            private String email;
            private boolean enableBrowser = false;
            private boolean enableEmail = false;
            private String emailSubjectPrefix = "[OpenL]";
            private String emailTemplate;
            private int browserTimeout = 5;
            private boolean soundEnabled = true;
            private String position = "Top Right";
            private boolean deployment = true;
            private boolean build = true;
            private boolean error = true;
            private boolean warning = true;
            private boolean info = false;

            public Builder(boolean enabled, String frequency) {
                this.enabled = enabled;
                this.frequency = frequency;
            }

            public Builder email(String email) {
                this.email = email;
                return this;
            }

            public Builder enableBrowser(boolean enableBrowser) {
                this.enableBrowser = enableBrowser;
                return this;
            }

            public Builder enableEmail(boolean enableEmail) {
                this.enableEmail = enableEmail;
                return this;
            }

            public Builder emailSubjectPrefix(String emailSubjectPrefix) {
                this.emailSubjectPrefix = emailSubjectPrefix;
                return this;
            }

            public Builder emailTemplate(String emailTemplate) {
                this.emailTemplate = emailTemplate;
                return this;
            }

            public Builder browserTimeout(int browserTimeout) {
                this.browserTimeout = browserTimeout;
                return this;
            }

            public Builder soundEnabled(boolean soundEnabled) {
                this.soundEnabled = soundEnabled;
                return this;
            }

            public Builder position(String position) {
                this.position = position;
                return this;
            }

            public Builder notificationTypes(boolean deployment, boolean build, boolean error, boolean warning, boolean info) {
                this.deployment = deployment;
                this.build = build;
                this.error = error;
                this.warning = warning;
                this.info = info;
                return this;
            }

            public NotificationConfig build() {
                return new NotificationConfig(this);
            }
        }
    }
}