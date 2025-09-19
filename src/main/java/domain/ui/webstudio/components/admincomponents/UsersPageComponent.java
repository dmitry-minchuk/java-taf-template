package domain.ui.webstudio.components.admincomponents;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;

public class UsersPageComponent extends BaseComponent {

    private WebElement userTableBody;
    private WebElement userTableHeader;
    private WebElement addUserBtn;
    private WebElement usernameField;
    private WebElement emailField;
    private WebElement firstNameField;
    private WebElement lastNameField;
    private WebElement passwordField;
    private WebElement saveBtn;
    private WebElement cancelBtn;
    private WebElement displayNamePatternField;
    private WebElement displayNameField;
    private WebElement administratorsGroupCheckbox;

    public UsersPageComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public UsersPageComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        userTableBody = createScopedElement("xpath=.//table//tbody[@class='ant-table-tbody']", "userTableBody");
        userTableHeader = createScopedElement("xpath=.//table//thead[@class='ant-table-thead']", "userTableHeader");
        addUserBtn = createScopedElement("xpath=.//button[./span[text()='Add User']]", "addUserBtn");
        displayNamePatternField = createScopedElement("xpath=.//select[@id='displayNamePattern'] | .//div[contains(@class,'ant-select') and ./preceding-sibling::*[contains(text(),'Display Name Pattern')]]", "displayNamePatternField");
        displayNameField = createScopedElement("xpath=.//input[@placeholder='Display Name' or @id='displayName']", "displayNameField");
        administratorsGroupCheckbox = createScopedElement("xpath=.//input[@type='checkbox' and (contains(@id,'admin') or ./following-sibling::*[contains(text(),'Administrator')])]", "administratorsGroupCheckbox");

        // Right panel not belonging to users page html hierarchy
        usernameField = new WebElement(page, "xpath=//input[@id='username']", "usernameField");
        emailField = new WebElement(page, "xpath=//input[@id='email']", "emailField");
        firstNameField = new WebElement(page, "xpath=//input[@id='firstName']", "firstNameField");
        lastNameField = new WebElement(page, "xpath=//input[@id='lastName']", "lastNameField");
        passwordField = new WebElement(page, "xpath=//input[@id='password' or @type='password']", "passwordField");
        saveBtn = new WebElement(page, "xpath=//button[./span[text()='Save'] or @type='submit']", "saveBtn");
        cancelBtn = new WebElement(page, "xpath=//button[./span[text()='Cancel']]", "cancelBtn");
    }

    public void clickAddUser() {
        addUserBtn.click();
    }

    public void setUsername(String username) {
        usernameField.fill(username);
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

    public void setPassword(String password) {
        passwordField.fill(password);
    }

    public void saveUser() {
        saveBtn.click();
    }

    public void cancelUser() {
        cancelBtn.click();
    }

    public void addNewUser(String username, String email, String firstName, String lastName, String password) {
        clickAddUser();
        setUsername(username);
        setEmail(email);
        setFirstName(firstName);
        setLastName(lastName);
        setPassword(password);
        saveUser();
    }

    public void setDisplayNamePattern(String pattern) {
        displayNamePatternField.click();
        WebElement option = createScopedElement("xpath=.//div[contains(@class,'ant-select-item') and contains(text(),'" + pattern + "')]", "displayNamePatternOption");
        option.click();
    }

    public void setDisplayName(String displayName) {
        displayNameField.fill(displayName);
    }

    public void setAdministratorsGroup(boolean isAdmin) {
        if (isAdmin != administratorsGroupCheckbox.isChecked()) {
            administratorsGroupCheckbox.click();
        }
    }

    public void addNewUser(String username,
                           String email,
                           String firstName,
                           String lastName,
                           String password,
                           String displayNamePattern,
                           String displayName,
                           boolean isAdmin) {
        clickAddUser();
        setUsername(username);
        setEmail(email);
        setFirstName(firstName);
        setLastName(lastName);
        setPassword(password);
        if (displayNamePattern != null) {
            setDisplayNamePattern(displayNamePattern);
        }
        if (displayName != null) {
            setDisplayName(displayName);
        }
        setAdministratorsGroup(isAdmin);
        saveUser();
    }

    // User table verification methods
    public String getSpecificUserElement(String username, String elementType) {
        switch (elementType) {
            case "users-firstname":
                // Extract first name from Full Name column (column 2)
                String fullName = getFullNameForUser(username);
                return fullName.split(" ")[0]; // First word as first name
                
            case "users-lastname":
                // Extract last name from Full Name column (column 2)
                String fullNameLast = getFullNameForUser(username);
                String[] nameParts = fullNameLast.split(" ");
                return nameParts.length > 1 ? nameParts[nameParts.length - 1] : ""; // Last word as last name
                
            case "users-displayname":
                // Full Name is the display name in new UI
                return getFullNameForUser(username);
                
            default:
                return "";
        }
    }

    public String getSpecificUserEmail(String username) {
        WebElement emailElement = createScopedElement(String.format("xpath=.//tbody[@class='ant-table-tbody']//tr[@data-row-key='%s']//td[3]", username), "userEmail");
        return emailElement.getText();
    }
    
    private String getFullNameForUser(String username) {
        WebElement fullNameElement = createScopedElement(String.format("xpath=.//tbody[@class='ant-table-tbody']//tr[@data-row-key='%s']//td[2]", username), "userFullName");
        return fullNameElement.sleep(350).getText();
    }
}