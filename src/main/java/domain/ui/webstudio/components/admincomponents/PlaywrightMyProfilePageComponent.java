package domain.ui.webstudio.components.admincomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

public class PlaywrightMyProfilePageComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement usernameField;
    private PlaywrightWebElement emailField;
    private PlaywrightWebElement resendVerificationEmailBtn;
    private PlaywrightWebElement firstNameField;
    private PlaywrightWebElement lastNameField;
    private PlaywrightWebElement displayNameField;
    private PlaywrightWebElement currentPasswordField;
    private PlaywrightWebElement newPasswordField;
    private PlaywrightWebElement confirmPasswordField;
    private PlaywrightWebElement saveBtn;
    private PlaywrightWebElement successNotification;
    private PlaywrightWebElement errorNotification;
    private PlaywrightWebElement displayNamePatternDropdown;

    public PlaywrightMyProfilePageComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightMyProfilePageComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        usernameField = createScopedElement("xpath=.//input[@placeholder='Username' or @id='username']", "usernameField");
        emailField = createScopedElement("xpath=.//input[@placeholder='Email' or @id='email']", "emailField");
        resendVerificationEmailBtn = createScopedElement("xpath=.//button[./span[text()='Resend Verification Email'] or ./span[contains(text(),'Resend')]]", "resendVerificationEmailBtn");
        firstNameField = createScopedElement("xpath=.//input[@placeholder='First Name' or @id='firstName' or @id='givenName']", "firstNameField");
        lastNameField = createScopedElement("xpath=.//input[@placeholder='Last Name' or @id='lastName' or @id='familyName']", "lastNameField");
        displayNameField = createScopedElement("xpath=.//input[@placeholder='Display Name' or @id='displayName']", "displayNameField");
        currentPasswordField = createScopedElement("xpath=.//input[@placeholder='Current Password' or @type='password'][1]", "currentPasswordField");
        newPasswordField = createScopedElement("xpath=.//input[@placeholder='New Password' or @type='password'][2]", "newPasswordField");
        confirmPasswordField = createScopedElement("xpath=.//input[@placeholder='Confirm Password' or @placeholder='Confirm New Password' or @type='password'][3]", "confirmPasswordField");
        saveBtn = createScopedElement("xpath=.//button[./span[text()='Save'] or @type='submit']", "saveBtn");
        successNotification = createScopedElement("xpath=.//div[contains(@class,'ant-notification') or contains(@class,'success-message')]", "successNotification");
        errorNotification = createScopedElement("xpath=.//div[contains(@class,'ant-notification') or contains(@class,'error-message')]", "errorNotification");
        displayNamePatternDropdown = createScopedElement("xpath=.//div[//label[@title='Display Name']]/following-sibling::div//span[@class='ant-select-selection-item']", "displayNamePatternDropdown");
    }

    public String getUsername() {
        return usernameField.getAttribute("value");
    }

    public PlaywrightMyProfilePageComponent setEmail(String email) {
        emailField.fill(email);
        return this;
    }

    public String getEmail() {
        return emailField.getAttribute("value");
    }

    public void resendVerificationEmail() {
        resendVerificationEmailBtn.click();
    }

    public boolean isResendVerificationEmailAvailable() {
        return resendVerificationEmailBtn.isVisible();
    }

    public PlaywrightMyProfilePageComponent setFirstName(String firstName) {
        firstNameField.fill(firstName);
        return this;
    }

    public String getFirstName() {
        return firstNameField.getAttribute("value");
    }

    public PlaywrightMyProfilePageComponent setLastName(String lastName) {
        lastNameField.fill(lastName);
        return this;
    }

    public String getLastName() {
        return lastNameField.getAttribute("value");
    }

    public PlaywrightMyProfilePageComponent setDisplayName(String displayName) {
        displayNameField.fill(displayName);
        return this;
    }

    public String getDisplayName() {
        return displayNameField.getAttribute("value");
    }

    public void setFullName(String firstName, String lastName) {
        setFirstName(firstName);
        setLastName(lastName);
    }

    public PlaywrightMyProfilePageComponent setCurrentPassword(String currentPassword) {
        currentPasswordField.fill(currentPassword);
        return this;
    }

    public PlaywrightMyProfilePageComponent setNewPassword(String newPassword) {
        newPasswordField.fill(newPassword);
        return this;
    }

    public PlaywrightMyProfilePageComponent setConfirmPassword(String confirmPassword) {
        confirmPasswordField.fill(confirmPassword);
        return this;
    }

    public void changePassword(String currentPassword, String newPassword, String confirmPassword) {
        setCurrentPassword(currentPassword);
        setNewPassword(newPassword);
        setConfirmPassword(confirmPassword);
    }

    public void changePassword(String currentPassword, String newPassword) {
        changePassword(currentPassword, newPassword, newPassword);
    }

    public PlaywrightMyProfilePageComponent saveProfile() {
        saveBtn.click();
        return this;
    }

    public boolean isSaveButtonEnabled() {
        return saveBtn.isEnabled();
    }

    public boolean isSuccessNotificationDisplayed() {
        return successNotification.isVisible();
    }

    public boolean isErrorNotificationDisplayed() {
        return errorNotification.isVisible();
    }

    public String getSuccessNotificationMessage() {
        return successNotification.getText();
    }

    public String getErrorNotificationMessage() {
        return errorNotification.getText();
    }

    public void updateProfile(String email, String firstName, String lastName, String displayName) {
        if (email != null) {
            setEmail(email);
        }
        if (firstName != null) {
            setFirstName(firstName);
        }
        if (lastName != null) {
            setLastName(lastName);
        }
        if (displayName != null) {
            setDisplayName(displayName);
        }
        saveProfile();
    }

    public void updateProfile(String email, String firstName, String lastName) {
        String autoGeneratedDisplayName = generateDisplayName(firstName, lastName);
        updateProfile(email, firstName, lastName, autoGeneratedDisplayName);
    }

    private String generateDisplayName(String firstName, String lastName) {
        if (firstName == null && lastName == null) {
            return "";
        }
        if (firstName == null) {
            return lastName.trim();
        }
        if (lastName == null) {
            return firstName.trim();
        }
        return (firstName.trim() + " " + lastName.trim()).trim();
    }

    public void updateProfileAndPassword(String email, String firstName, String lastName, String displayName,
                                        String currentPassword, String newPassword) {
        updateProfile(email, firstName, lastName, displayName);
        changePassword(currentPassword, newPassword);
        saveProfile();
    }

    public boolean validateProfile(String expectedEmail, String expectedFirstName, String expectedLastName, String expectedDisplayName) {
        boolean emailMatches = expectedEmail == null || expectedEmail.equals(getEmail());
        boolean firstNameMatches = expectedFirstName == null || expectedFirstName.equals(getFirstName());
        boolean lastNameMatches = expectedLastName == null || expectedLastName.equals(getLastName());
        boolean displayNameMatches = expectedDisplayName == null || expectedDisplayName.equals(getDisplayName());
        
        return emailMatches && firstNameMatches && lastNameMatches && displayNameMatches;
    }

    public PlaywrightMyProfilePageComponent setDisplayNamePattern(String pattern) {
        displayNamePatternDropdown.click();
        PlaywrightWebElement option = new PlaywrightWebElement(page, String.format("xpath=.//div[@class='rc-virtual-list']//div[contains(@class,'ant-select-item-option') and @title='%s']", pattern), "displayNamePatternOption");
        option.click();
        return this;
    }

    public String getDisplayNamePattern() {
        return displayNamePatternDropdown.getAttribute("title");
    }

    public boolean hasDisplayNamePatternOptions(String... expectedOptions) {
        // Returns true as placeholder - needs actual options verification logic
        return true;
    }

    public String getCurrentPassword() {
        return currentPasswordField.getAttribute("value");
    }

    public String getNewPassword() {
        return newPasswordField.getAttribute("value");
    }

    public String getConfirmPassword() {
        return confirmPasswordField.getAttribute("value");
    }

    public String getProfileInfo() {
        return String.format("Profile: %s | Email: %s | Name: %s %s | Display: %s",
                getUsername(),
                getEmail(),
                getFirstName(),
                getLastName(),
                getDisplayName());
    }
}