package domain.ui.webstudio.components.admincomponents;

import configuration.core.ui.BasePageComponent;
import configuration.core.ui.SmartWebElement;
import org.openqa.selenium.support.FindBy;


public class MyProfilePageComponent extends BasePageComponent {

    // Account Section Elements
    @FindBy(xpath = ".//input[@placeholder='Username' or @id='username']")
    private SmartWebElement usernameField;

    @FindBy(xpath = ".//input[@placeholder='Email' or @id='email']")
    private SmartWebElement emailField;

    @FindBy(xpath = ".//button[./span[text()='Resend Verification Email'] or ./span[contains(text(),'Resend')]]")
    private SmartWebElement resendVerificationEmailBtn;

    // Name Section Elements
    @FindBy(xpath = ".//input[@placeholder='First Name' or @id='firstName' or @id='givenName']")
    private SmartWebElement firstNameField;

    @FindBy(xpath = ".//input[@placeholder='Last Name' or @id='lastName' or @id='familyName']")
    private SmartWebElement lastNameField;

    @FindBy(xpath = ".//input[@placeholder='Display Name' or @id='displayName']")
    private SmartWebElement displayNameField;

    // Change Password Section Elements
    @FindBy(xpath = ".//input[@placeholder='Current Password' or @type='password'][1]")
    private SmartWebElement currentPasswordField;

    @FindBy(xpath = ".//input[@placeholder='New Password' or @type='password'][2]")
    private SmartWebElement newPasswordField;

    @FindBy(xpath = ".//input[@placeholder='Confirm Password' or @placeholder='Confirm New Password' or @type='password'][3]")
    private SmartWebElement confirmPasswordField;

    // Action Buttons
    @FindBy(xpath = ".//button[./span[text()='Save'] or @type='submit']")
    private SmartWebElement saveBtn;

    @FindBy(xpath = ".//button[./span[text()='Cancel']]")
    private SmartWebElement cancelBtn;

    // Status and Notification Elements
    @FindBy(xpath = ".//div[contains(@class,'ant-notification') or contains(@class,'success-message')]")
    private SmartWebElement successNotification;

    @FindBy(xpath = ".//div[contains(@class,'ant-notification') or contains(@class,'error-message')]")
    private SmartWebElement errorNotification;

    // Account Section Methods
    
    public String getUsername() {
        return usernameField.getAttribute("value");
    }

    
    public MyProfilePageComponent setEmail(String email) {
        emailField.sendKeys(email);
        return this;
    }

    
    public String getEmail() {
        return emailField.getAttribute("value");
    }

    
    public void resendVerificationEmail() {
        resendVerificationEmailBtn.click();
    }

    
    public boolean isResendVerificationEmailAvailable() {
        return resendVerificationEmailBtn.isDisplayed(2);
    }

    // Name Section Methods
    
    public MyProfilePageComponent setFirstName(String firstName) {
        firstNameField.sendKeys(firstName);
        return this;
    }

    
    public String getFirstName() {
        return firstNameField.getAttribute("value");
    }

    
    public MyProfilePageComponent setLastName(String lastName) {
        lastNameField.sendKeys(lastName);
        return this;
    }

    
    public String getLastName() {
        return lastNameField.getAttribute("value");
    }

    
    public MyProfilePageComponent setDisplayName(String displayName) {
        displayNameField.sendKeys(displayName);
        return this;
    }

    
    public String getDisplayName() {
        return displayNameField.getAttribute("value");
    }

    
    public void setFullName(String firstName, String lastName) {
        setFirstName(firstName);
        setLastName(lastName);
    }

    // Change Password Section Methods
    
    public MyProfilePageComponent setCurrentPassword(String currentPassword) {
        currentPasswordField.sendKeys(currentPassword);
        return this;
    }

    
    public MyProfilePageComponent setNewPassword(String newPassword) {
        newPasswordField.sendKeys(newPassword);
        return this;
    }

    
    public MyProfilePageComponent setConfirmPassword(String confirmPassword) {
        confirmPasswordField.sendKeys(confirmPassword);
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

    // Action Methods
    
    public void saveProfile() {
        saveBtn.click();
    }

    
    public void cancelProfile() {
        cancelBtn.click();
    }

    
    public boolean isSaveButtonEnabled() {
        return saveBtn.isEnabled();
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

    // Complex Profile Operations
    
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

    // Profile Validation Methods
    
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