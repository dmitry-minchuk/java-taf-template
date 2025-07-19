package domain.ui.webstudio.components.admincpmponents;

import configuration.core.ui.BasePageComponent;
import configuration.core.ui.SmartWebElement;
import helpers.utils.WaitUtil;
import org.openqa.selenium.support.FindBy;

import java.util.List;


public class UsersPageComponent extends BasePageComponent {

    // User Table Elements
    @FindBy(xpath = ".//table//tbody[@class='ant-table-tbody']")
    private SmartWebElement userTableBody;

    @FindBy(xpath = ".//table//thead[@class='ant-table-thead']")
    private SmartWebElement userTableHeader;

    @FindBy(xpath = ".//table//tbody[@class='ant-table-tbody']//tr[@class='ant-table-row ant-table-row-level-0']")
    private List<SmartWebElement> userRows;

    // Add User Button
    @FindBy(xpath = ".//button[./span[text()='Add User']]")
    private SmartWebElement addUserBtn;

    // User Row Template Elements (for dynamic user interaction)
    @FindBy(xpath = ".//tr[@data-row-key='%s']")
    private SmartWebElement userRowTemplate;

    @FindBy(xpath = ".//tr[@data-row-key='%s']//td[1]//strong")
    private SmartWebElement usernameTemplate;

    @FindBy(xpath = ".//tr[@data-row-key='%s']//td[2]")
    private SmartWebElement fullNameTemplate;

    @FindBy(xpath = ".//tr[@data-row-key='%s']//td[3]")
    private SmartWebElement emailTemplate;

    @FindBy(xpath = ".//tr[@data-row-key='%s']//td[4]//button[1]")
    private SmartWebElement editUserBtnTemplate;

    @FindBy(xpath = ".//tr[@data-row-key='%s']//td[4]//button[2]")
    private SmartWebElement deleteUserBtnTemplate;

    // User Status Elements
    @FindBy(xpath = ".//tr[@data-row-key='%s']//span[contains(@class,'ant-badge-dot')]")
    private SmartWebElement userStatusDotTemplate;

    @FindBy(xpath = ".//tr[@data-row-key='%s']//span[contains(@class,'anticon-exclamation-circle')]")
    private SmartWebElement emailVerificationIconTemplate;

    // User Creation/Edit Form Elements (Modal)
    @FindBy(xpath = ".//div[contains(@class,'ant-modal')]//input[@placeholder='Username']")
    private SmartWebElement usernameField;

    @FindBy(xpath = ".//div[contains(@class,'ant-modal')]//input[@placeholder='Full Name']")
    private SmartWebElement fullNameField;

    @FindBy(xpath = ".//div[contains(@class,'ant-modal')]//input[@placeholder='Email']")
    private SmartWebElement emailField;

    @FindBy(xpath = ".//div[contains(@class,'ant-modal')]//input[@placeholder='Password']")
    private SmartWebElement passwordField;

    @FindBy(xpath = ".//div[contains(@class,'ant-modal')]//input[@placeholder='Confirm Password']")
    private SmartWebElement confirmPasswordField;

    @FindBy(xpath = ".//div[contains(@class,'ant-modal')]//button[./span[text()='Save']]")
    private SmartWebElement saveUserBtn;

    @FindBy(xpath = ".//div[contains(@class,'ant-modal')]//button[./span[text()='Cancel']]")
    private SmartWebElement cancelUserBtn;


    // User Table Operations
    
    public int getUserCount() {
        return userRows.size();
    }

    
    public boolean isUserExists(String username) {
        return userRowTemplate.format(username).isDisplayed(2);
    }

    
    public String getUserFullName(String username) {
        return fullNameTemplate.format(username).getText();
    }

    
    public String getUserEmail(String username) {
        return emailTemplate.format(username).getText();
    }

    
    public boolean isUserActive(String username) {
        return userStatusDotTemplate.format(username).isDisplayed(2);
    }

    
    public boolean hasEmailVerificationIssues(String username) {
        return emailVerificationIconTemplate.format(username).isDisplayed(2);
    }

    // User Actions
    
    public void clickAddUser() {
        addUserBtn.click();
    }

    
    public void clickEditUser(String username) {
        editUserBtnTemplate.format(username).click();
    }

    
    public void clickDeleteUser(String username) {
        deleteUserBtnTemplate.format(username).click();
    }

    // User Form Operations
    
    public void fillUserForm(String username, String fullName, String email, String password, String confirmPassword) {
        if (username != null) {
            usernameField.sendKeys(username);
        }
        
        if (fullName != null) {
            fullNameField.sendKeys(fullName);
        }
        
        if (email != null) {
            emailField.sendKeys(email);
        }
        
        if (password != null) {
            passwordField.sendKeys(password);
        }
        
        if (confirmPassword != null) {
            confirmPasswordField.sendKeys(confirmPassword);
        }
    }

    
    public void saveUser() {
        saveUserBtn.click();
    }

    
    public void cancelUser() {
        cancelUserBtn.click();
    }

    // Confirmation Dialog Operations
    
    public void confirmAction() {
        getConfirmationPopup().confirm();
    }

    
    public void cancelAction() {
        getConfirmationPopup().cancel();
    }

    // Complex User Operations
    
    public void createUser(String username, String fullName, String email, String password, String confirmPassword) {
        clickAddUser();
        fillUserForm(username, fullName, email, password, confirmPassword != null ? confirmPassword : password);
        saveUser();
    }

    
    public void createUser(String username, String fullName, String email, String password) {
        createUser(username, fullName, email, password, password);
    }

    
    public void editUser(String username, String newFullName, String newEmail) {
        clickEditUser(username);
        fillUserForm(null, newFullName, newEmail, null, null);
        saveUser();
    }

    
    public void deleteUser(String username) {
        clickDeleteUser(username);
        confirmAction();
    }

    
    public void cancelDeleteUser(String username) {
        clickDeleteUser(username);
        cancelAction();
    }

    // User Validation Operations
    
    public boolean validateUser(String username, String expectedFullName, String expectedEmail) {
        if (!isUserExists(username)) {
            return false;
        }
        
        String actualFullName = getUserFullName(username);
        String actualEmail = getUserEmail(username);
        
        boolean fullNameMatches = expectedFullName == null || expectedFullName.equals(actualFullName);
        boolean emailMatches = expectedEmail == null || expectedEmail.equals(actualEmail);
        
        return fullNameMatches && emailMatches;
    }

    
    public boolean validateUserStatus(String username) {
        return isUserActive(username) && !hasEmailVerificationIssues(username);
    }

    
    public String getUserInfo(String username) {
        if (!isUserExists(username)) {
            return "User not found: " + username;
        }
        
        return String.format("User: %s | Full Name: %s | Email: %s | Active: %s | Email Issues: %s",
                username,
                getUserFullName(username),
                getUserEmail(username),
                isUserActive(username),
                hasEmailVerificationIssues(username));
    }
}