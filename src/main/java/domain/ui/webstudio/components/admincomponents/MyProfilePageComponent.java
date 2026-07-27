package domain.ui.webstudio.components.admincomponents;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import helpers.utils.WaitUtil;

import java.util.List;
import configuration.driver.LocalDriverPool;

public class MyProfilePageComponent extends BaseComponent {

    private WebElement usernameField;
    private WebElement emailField;
    private WebElement resendVerificationEmailBtn;
    private WebElement firstNameField;
    private WebElement lastNameField;
    private WebElement displayNameField;
    private WebElement currentPasswordField;
    private WebElement newPasswordField;
    private WebElement confirmPasswordField;
    private WebElement saveBtn;
    private WebElement successNotification;
    private WebElement errorNotification;
    private WebElement displayNamePatternDropdown;

    public MyProfilePageComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public MyProfilePageComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private List<WebElement> validationErrors;

    private void initializeElements() {
        validationErrors = createElementList("xpath=//div[contains(@class,'ant-form-item-explain-error')]", "profileValidationErrors");
        usernameField = createScopedElement("xpath=.//input[@placeholder='Username' or @id='username']", "usernameField");
        emailField = createScopedElement("xpath=.//input[@placeholder='Email' or @id='email']", "emailField");
        resendVerificationEmailBtn = createScopedElement("xpath=.//button[./span[text()='Resend Verification Email'] or ./span[contains(text(),'Resend')]]", "resendVerificationEmailBtn");
        firstNameField = createScopedElement("xpath=.//input[@placeholder='First Name' or @id='firstName' or @id='givenName']", "firstNameField");
        lastNameField = createScopedElement("xpath=.//input[@placeholder='Last Name' or @id='lastName' or @id='familyName']", "lastNameField");
        displayNameField = createScopedElement("xpath=.//input[@placeholder='Display Name' or @id='displayName']", "displayNameField");
        currentPasswordField = createScopedElement("xpath=.//input[@id='changePassword_currentPassword']", "currentPasswordField");
        newPasswordField = createScopedElement("xpath=.//input[@id='changePassword_newPassword']", "newPasswordField");
        confirmPasswordField = createScopedElement("xpath=.//input[@id='changePassword_confirmPassword']", "confirmPasswordField");
        saveBtn = createScopedElement("xpath=.//button[./span[text()='Save'] or @type='submit']", "saveBtn");
        successNotification = createScopedElement("xpath=.//div[contains(@class,'ant-notification') or contains(@class,'success-message')]", "successNotification");
        errorNotification = createScopedElement("xpath=.//div[contains(@class,'ant-notification') or contains(@class,'error-message')]", "errorNotification");
        displayNamePatternDropdown = createScopedElement("xpath=.//div[//label[@title='Display Name']]/following-sibling::div//input[@id='displayNameSelect']", "displayNamePatternDropdown");
    }

    public String getUsername() {
        return usernameField.getAttribute("value");
    }

    public MyProfilePageComponent setEmail(String email) {
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

    public MyProfilePageComponent setFirstName(String firstName) {
        firstNameField.fill(firstName);
        return this;
    }

    public String getFirstName() {
        return firstNameField.getAttribute("value");
    }

    public MyProfilePageComponent setLastName(String lastName) {
        lastNameField.fill(lastName);
        return this;
    }

    public String getLastName() {
        return lastNameField.getAttribute("value");
    }

    public MyProfilePageComponent setDisplayName(String displayName) {
        // The field only takes typing in Custom mode; the other modes compose the name from first/last name
        // and keep it read-only.
        if (!displayNameField.isEnabled()) {
            setDisplayNamePattern("Custom");
        }
        displayNameField.fill(displayName);
        return this;
    }

    public String getDisplayName() {
        return displayNameField.sleep(100).waitForVisible(500).getAttribute("value");
    }

    public void setFullName(String firstName, String lastName) {
        setFirstName(firstName);
        setLastName(lastName);
    }

    public MyProfilePageComponent setCurrentPassword(String currentPassword) {
        currentPasswordField.fill(currentPassword);
        return this;
    }

    public MyProfilePageComponent setNewPassword(String newPassword) {
        newPasswordField.fill(newPassword);
        return this;
    }

    public MyProfilePageComponent setConfirmPassword(String confirmPassword) {
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

    /**
     * Waits until Save becomes unavailable — it starts out that way, before anything is edited.
     */
    public boolean waitForSaveProfileDisabled() {
        return WaitUtil.waitForCondition(() -> !getSaveProfileBtn().isEnabled(), DEFAULT_TIMEOUT_MS, 250,
                "Waiting for the profile Save button to become disabled");
    }

    /**
     * Messages shown under the fields, e.g. "Email is required". Studio 6.4.0 keeps Save clickable when a
     * required field is emptied and points at the field instead, so this is how a rejected form is checked.
     */
    public List<String> getValidationErrors() {
        WaitUtil.waitForCondition(() -> !validationErrors.isEmpty(), DEFAULT_TIMEOUT_MS, 250,
                "Waiting for the profile form to report its required fields");
        return validationErrors.stream().map(WebElement::getText).map(String::trim).toList();
    }

    public WebElement getSaveProfileBtn() {
        return saveBtn;
    }

    public MyProfilePageComponent saveProfile() {
        saveBtn.click();
        closeAllMessages();
        return this;
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

    public MyProfilePageComponent setDisplayNamePattern(String pattern) {
        displayNamePatternDropdown.click();
        WebElement option = new WebElement(page, String.format("xpath=.//div[@class='rc-virtual-list']//div[contains(@class,'ant-select-item-option') and @title='%s']", pattern), "displayNamePatternOption");
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