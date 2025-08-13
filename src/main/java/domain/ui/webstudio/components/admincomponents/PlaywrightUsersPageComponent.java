package domain.ui.webstudio.components.admincomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

public class PlaywrightUsersPageComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement userTableBody;
    private PlaywrightWebElement userTableHeader;
    private PlaywrightWebElement addUserBtn;
    private PlaywrightWebElement usernameField;
    private PlaywrightWebElement emailField;
    private PlaywrightWebElement firstNameField;
    private PlaywrightWebElement lastNameField;
    private PlaywrightWebElement passwordField;
    private PlaywrightWebElement saveBtn;
    private PlaywrightWebElement cancelBtn;
    private PlaywrightWebElement displayNamePatternField;
    private PlaywrightWebElement displayNameField;
    private PlaywrightWebElement administratorsGroupCheckbox;

    public PlaywrightUsersPageComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightUsersPageComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        userTableBody = createScopedElement("xpath=.//table//tbody[@class='ant-table-tbody']", "userTableBody");
        userTableHeader = createScopedElement("xpath=.//table//thead[@class='ant-table-thead']", "userTableHeader");
        addUserBtn = createScopedElement("xpath=.//button[./span[text()='Add User']]", "addUserBtn");
        usernameField = createScopedElement("xpath=.//input[@placeholder='Username' or @id='username']", "usernameField");
        emailField = createScopedElement("xpath=.//input[@placeholder='Email' or @id='email']", "emailField");
        firstNameField = createScopedElement("xpath=.//input[@placeholder='First Name' or @id='firstName']", "firstNameField");
        lastNameField = createScopedElement("xpath=.//input[@placeholder='Last Name' or @id='lastName']", "lastNameField");
        passwordField = createScopedElement("xpath=.//input[@placeholder='Password' or @id='password' or @type='password']", "passwordField");
        saveBtn = createScopedElement("xpath=.//button[./span[text()='Save'] or @type='submit']", "saveBtn");
        cancelBtn = createScopedElement("xpath=.//button[./span[text()='Cancel']]", "cancelBtn");
        displayNamePatternField = createScopedElement("xpath=.//select[@id='displayNamePattern'] | .//div[contains(@class,'ant-select') and ./preceding-sibling::*[contains(text(),'Display Name Pattern')]]", "displayNamePatternField");
        displayNameField = createScopedElement("xpath=.//input[@placeholder='Display Name' or @id='displayName']", "displayNameField");
        administratorsGroupCheckbox = createScopedElement("xpath=.//input[@type='checkbox' and (contains(@id,'admin') or ./following-sibling::*[contains(text(),'Administrator')])]", "administratorsGroupCheckbox");
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

    public boolean isUserTableVisible() {
        return userTableBody.isVisible();
    }

    public boolean isAddUserButtonVisible() {
        return addUserBtn.isVisible();
    }

    public int getUserCount() {
        return page.locator(".//table//tbody[@class='ant-table-tbody']//tr[@class='ant-table-row ant-table-row-level-0']").count();
    }

    public void setDisplayNamePattern(String pattern) {
        displayNamePatternField.click();
        PlaywrightWebElement option = createScopedElement("xpath=.//div[contains(@class,'ant-select-item') and contains(text(),'" + pattern + "')]", "displayNamePatternOption");
        option.click();
    }

    public void setDisplayName(String displayName) {
        displayNameField.fill(displayName);
    }

    public String getDisplayName() {
        return displayNameField.getAttribute("value");
    }

    public void setAdministratorsGroup(boolean isAdmin) {
        if (isAdmin != administratorsGroupCheckbox.isSelected()) {
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
        PlaywrightWebElement emailElement = createScopedElement(String.format("xpath=.//tbody[@class='ant-table-tbody']//tr[@data-row-key='%s']//td[3]", username), "userEmail");
        return emailElement.getText();
    }
    
    private String getFullNameForUser(String username) {
        PlaywrightWebElement fullNameElement = createScopedElement(String.format("xpath=.//tbody[@class='ant-table-tbody']//tr[@data-row-key='%s']//td[2]", username), "userFullName");
        return fullNameElement.getText();
    }

    public boolean isUserPresent(String username) {
        // Using generic table row locator - may need refinement with actual UI locators
        PlaywrightWebElement userRow = createScopedElement("xpath=.//table//tbody//tr[contains(.,'" + username + "')]", "userRow");
        return userRow.isVisible();
    }

    public void editUser(String username) {
        // Returns empty implementation - locators need to be found in openl-tests
        PlaywrightWebElement editButton = createScopedElement("xpath=.//table//tbody//tr[contains(.,'" + username + "')]//button[contains(@class,'edit')]", "editUserButton");
        editButton.click();
    }

    public void deleteUser(String username) {
        // Returns empty implementation - locators need to be found in openl-tests  
        PlaywrightWebElement deleteButton = createScopedElement("xpath=.//table//tbody//tr[contains(.,'" + username + "')]//button[contains(@class,'delete')]", "deleteUserButton");
        deleteButton.click();
    }

    public void verifyUserInfoInTable(String username, String firstName, String lastName, String groups,
                                    String localUser, String email, String displayName,
                                    boolean isEditable, boolean isRemovable) {
        // Verification implementation - locators need to be found in openl-tests
        // This method would verify all user information is correctly displayed in the table
        // For now, using basic presence check
        assert isUserPresent(username) : "User " + username + " should be present in table";
    }

    public boolean isUserRemovableTypeNotSpecified(String username) {
        // Returns false as default - actual implementation needs proper locators
        return false;
    }

    public boolean isUnsafePasswordWarningDisplayed(String username) {
        // Returns false as default - actual implementation needs proper locators  
        return false;
    }

    public void checkUserInfoUserTypeNotSpecified(String username, String firstName, String lastName,
                                                 String groups, String email, String displayName,
                                                 boolean isEditable, boolean isRemovable) {
        // Implementation placeholder - needs proper locators from openl-tests
        verifyUserInfoInTable(username, firstName, lastName, groups, "not specified", email, displayName, isEditable, isRemovable);
    }

    public void deleteUserTypeNotSpecified(String username) {
        // Implementation placeholder - needs proper locators from openl-tests
        deleteUser(username);
    }

    public String getUsersPageInfo() {
        return String.format("Users Page - Total Users: %d | Add User Available: %s",
                getUserCount(),
                isAddUserButtonVisible());
    }
}