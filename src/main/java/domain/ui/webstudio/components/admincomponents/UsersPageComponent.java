package domain.ui.webstudio.components.admincomponents;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.WaitUtil;

import java.util.ArrayList;
import java.util.List;

public class UsersPageComponent extends BaseComponent {

    private WebElement userTableBody;
    private WebElement userTableHeader;
    private WebElement addUserBtn;
    private WebElement usernameField;
    private WebElement emailField;
    private WebElement firstNameField;
    private WebElement lastNameField;
    private WebElement passwordField;
    private WebElement addRoleBtn;
    private WebElement selectOptionTemplate;
    private WebElement roleSelectorTemplate;
    private WebElement designRepoSelectorTemplate;
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
        addRoleBtn = new WebElement(page, "xpath=//button[./span[contains(text(),'Add Role')] and not(ancestor::div[@aria-hidden='true'])]", "addRoleBtn");
        designRepoSelectorTemplate = new WebElement(page, "xpath=//input[@id='designRepos_%s_id']", "designRepoSelectorTemplate");
        roleSelectorTemplate = new WebElement(page, "xpath=//input[@id='designRepos_%s_role']", "roleSelectorTemplate");
        selectOptionTemplate =  new WebElement(page,"xpath=//div[@class='rc-virtual-list-holder-inner' and not(ancestor::div[contains(@class,'dropdown-hidden')])]/div[@title='%s']", "selectOptionTemplate");
        saveBtn = new WebElement(page, "xpath=//button[./span[text()='Save'] or @type='submit']", "saveBtn");
        cancelBtn = new WebElement(page, "xpath=//button[./span[text()='Cancel']]", "cancelBtn");
    }

    public UsersPageComponent clickAddUser() {
        addUserBtn.click();
        return this;
    }

    public UsersPageComponent setUsername(String username) {
        usernameField.fill(username);
        return this;
    }

    public String getUsername() {
        return usernameField.getAttribute("value");
    }

    public UsersPageComponent setEmail(String email) {
        emailField.fill(email);
        return this;
    }

    public String getEmail() {
        return emailField.getAttribute("value");
    }

    public UsersPageComponent setFirstName(String firstName) {
        firstNameField.fill(firstName);
        return this;
    }

    public String getFirstName() {
        return firstNameField.getAttribute("value");
    }

    public UsersPageComponent setLastName(String lastName) {
        lastNameField.fill(lastName);
        return this;
    }

    public String getLastName() {
        return lastNameField.getAttribute("value");
    }

    public UsersPageComponent setPassword(String password) {
        passwordField.fill(password);
        return this;
    }

    public UsersPageComponent clickAddRoleBtn() {
        addRoleBtn.click();
        return this;
    }

    public UsersPageComponent setRoleRepository(int row, String repositoryName) {
        designRepoSelectorTemplate.format(row).click();
        selectOptionTemplate.format(repositoryName).waitForVisible().click();
        return this;
    }

    public UsersPageComponent setRole(int row, String role) {
        roleSelectorTemplate.format(row).click();
        selectOptionTemplate.format(role).waitForVisible().click();
        return this;
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

    public UsersPageComponent clickEditUser(String username) {
        WebElement editButton = createScopedElement(String.format("xpath=.//tbody[@class='ant-table-tbody']//tr[@data-row-key='%s']//button[contains(@class,'ant-btn') and .//span[contains(@aria-label,'edit')]]", username), "editUserButton");
        editButton.click();
        return this;
    }

    public void clickDeleteUser(String username) {
        WebElement deleteButton = createScopedElement(String.format("xpath=.//tbody[@class='ant-table-tbody']//tr[@data-row-key='%s']//button[contains(@class,'ant-btn') and .//span[contains(@aria-label,'delete')]]", username), "deleteUserButton");
        deleteButton.click();
        getModalOkBtn().click();
    }

    public boolean isUserInList(String username) {
        WebElement userRow = createScopedElement(String.format("xpath=.//tbody[@class='ant-table-tbody']//tr[@data-row-key='%s']", username), "userRow");
        return userRow.isVisible(2000);
    }

    public int getUsersCount() {
        WebElement tableBody = createScopedElement("xpath=.//tbody[@class='ant-table-tbody']", "tableBody");
        return tableBody.getLocator().locator("tr").count();
    }

    public String getErrorMessage() {
        WebElement errorNotification = new WebElement(page, "xpath=//div[contains(@class,'ant-notification-notice-error')]//div[contains(@class,'ant-notification-notice-message')]", "errorNotification");
        return errorNotification.waitForVisible().getText();
    }

    public boolean isErrorMessageDisplayed() {
        WebElement errorNotification = new WebElement(page, "xpath=//div[contains(@class,'ant-notification-notice-error')]", "errorNotification");
        return errorNotification.isVisible(3000);
    }

    public String getErrorDescription() {
        WebElement errorDescription = new WebElement(page, "xpath=//div[contains(@class,'ant-notification-notice-error')]//div[contains(@class,'ant-notification-notice-description')]", "errorDescription");
        return errorDescription.waitForVisible().getText();
    }

    public UsersPageComponent clearAllRoles() {
        WebElement removeRoleBtn = new WebElement(page, "xpath=//button[./span[contains(@aria-label,'delete')] and ancestor::div[contains(@class,'ant-form-item')]]", "removeRoleBtn");
        while (removeRoleBtn.isVisible(1000)) {
            removeRoleBtn.click();
        }
        return this;
    }

    public String getRoleRepository(int row) {
        WebElement designRepoField = designRepoSelectorTemplate.format(row);
        return designRepoField.getAttribute("value");
    }

    public String getRole(int row) {
        WebElement roleField = roleSelectorTemplate.format(row);
        return roleField.getAttribute("value");
    }

    public List<String> getAllUsernames() {
        WebElement tableBody = createScopedElement("xpath=.//tbody[@class='ant-table-tbody']", "tableBody");
        List<String> usernames = new ArrayList<>();
        int rowCount = tableBody.getLocator().locator("tr").count();
        for (int i = 0; i < rowCount; i++) {
            String username = tableBody.getLocator().locator("tr").nth(i).getAttribute("data-row-key");
            if (username != null && !username.isEmpty()) {
                usernames.add(username);
            }
        }
        return usernames;
    }

    public boolean areActionsAvailableForUser(String username) {
        WebElement editButton = createScopedElement(String.format("xpath=.//tbody[@class='ant-table-tbody']//tr[@data-row-key='%s']//button[.//span[contains(@aria-label,'edit')]]", username), "editButton");
        WebElement deleteButton = createScopedElement(String.format("xpath=.//tbody[@class='ant-table-tbody']//tr[@data-row-key='%s']//button[.//span[contains(@aria-label,'delete')]]", username), "deleteButton");
        return editButton.isVisible() && deleteButton.isVisible();
    }
}