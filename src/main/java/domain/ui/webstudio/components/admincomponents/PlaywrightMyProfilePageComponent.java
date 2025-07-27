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
    private PlaywrightWebElement cancelBtn;
    private PlaywrightWebElement successNotification;
    private PlaywrightWebElement errorNotification;

    public PlaywrightMyProfilePageComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightMyProfilePageComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        usernameField = createScopedElement(".//input[@placeholder='Username' or @id='username']", "usernameField");
        emailField = createScopedElement(".//input[@placeholder='Email' or @id='email']", "emailField");
        resendVerificationEmailBtn = createScopedElement(".//button[./span[text()='Resend Verification Email'] or ./span[contains(text(),'Resend')]]", "resendVerificationEmailBtn");
        firstNameField = createScopedElement(".//input[@placeholder='First Name' or @id='firstName' or @id='givenName']", "firstNameField");
        lastNameField = createScopedElement(".//input[@placeholder='Last Name' or @id='lastName' or @id='familyName']", "lastNameField");
        displayNameField = createScopedElement(".//input[@placeholder='Display Name' or @id='displayName']", "displayNameField");
        currentPasswordField = createScopedElement(".//input[@placeholder='Current Password' or @type='password'][1]", "currentPasswordField");
        newPasswordField = createScopedElement(".//input[@placeholder='New Password' or @type='password'][2]", "newPasswordField");
        confirmPasswordField = createScopedElement(".//input[@placeholder='Confirm Password' or @placeholder='Confirm New Password' or @type='password'][3]", "confirmPasswordField");
        saveBtn = createScopedElement(".//button[./span[text()='Save'] or @type='submit']", "saveBtn");
        cancelBtn = createScopedElement(".//button[./span[text()='Cancel']]", "cancelBtn");
        successNotification = createScopedElement(".//div[contains(@class,'ant-notification') or contains(@class,'success-message')]", "successNotification");
        errorNotification = createScopedElement(".//div[contains(@class,'ant-notification') or contains(@class,'error-message')]", "errorNotification");
    }

    public String getUsername() {
        return usernameField.getAttribute("value");
    }

    public void setEmail(String email) {
        emailField.fill(email);
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

    public void setFirstName(String firstName) {
        firstNameField.fill(firstName);
    }

    public String getFirstName() {
        return firstNameField.getAttribute("value");
    }

    public void setLastName(String lastName) {
        lastNameField.fill(lastName);
    }

    public String getLastName() {
        return lastNameField.getAttribute("value");
    }

    public void setDisplayName(String displayName) {
        displayNameField.fill(displayName);
    }

    public String getDisplayName() {
        return displayNameField.getAttribute("value");
    }

    public void setFullName(String firstName, String lastName) {
        setFirstName(firstName);
        setLastName(lastName);
    }

    public void setCurrentPassword(String currentPassword) {
        currentPasswordField.fill(currentPassword);
    }

    public void setNewPassword(String newPassword) {
        newPasswordField.fill(newPassword);
    }

    public void setConfirmPassword(String confirmPassword) {
        confirmPasswordField.fill(confirmPassword);
    }

    public void changePassword(String currentPassword, String newPassword, String confirmPassword) {
        setCurrentPassword(currentPassword);
        setNewPassword(newPassword);
        setConfirmPassword(confirmPassword);
    }

    public void changePassword(String currentPassword, String newPassword) {
        changePassword(currentPassword, newPassword, newPassword);
    }

    public void saveProfile() {
        saveBtn.click();
    }

    public void cancelProfile() {
        cancelBtn.click();
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

    public String getProfileInfo() {
        return String.format("Profile: %s | Email: %s | Name: %s %s | Display: %s",
                getUsername(),
                getEmail(),
                getFirstName(),
                getLastName(),
                getDisplayName());
    }
}