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
}