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
        userTableBody = new PlaywrightWebElement(page, ".//table//tbody[@class='ant-table-tbody']", "User Table Body");
        userTableHeader = new PlaywrightWebElement(page, ".//table//thead[@class='ant-table-thead']", "User Table Header");
        addUserBtn = new PlaywrightWebElement(page, ".//button[./span[text()='Add User']]", "Add User Button");
        usernameField = new PlaywrightWebElement(page, ".//input[@placeholder='Username' or @id='username']", "Username Field");
        emailField = new PlaywrightWebElement(page, ".//input[@placeholder='Email' or @id='email']", "Email Field");
        firstNameField = new PlaywrightWebElement(page, ".//input[@placeholder='First Name' or @id='firstName']", "First Name Field");
        lastNameField = new PlaywrightWebElement(page, ".//input[@placeholder='Last Name' or @id='lastName']", "Last Name Field");
        passwordField = new PlaywrightWebElement(page, ".//input[@placeholder='Password' or @id='password' or @type='password']", "Password Field");
        saveBtn = new PlaywrightWebElement(page, ".//button[./span[text()='Save'] or @type='submit']", "Save Button");
        cancelBtn = new PlaywrightWebElement(page, ".//button[./span[text()='Cancel']]", "Cancel Button");
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