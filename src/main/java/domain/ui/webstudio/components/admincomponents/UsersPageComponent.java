package domain.ui.webstudio.components.admincomponents;

import domain.ui.webstudio.components.BaseComponent;
import domain.ui.webstudio.components.common.TableComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import helpers.utils.WaitUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class UsersPageComponent extends BaseComponent {

    // Column indices for users table (1-based)
    private static final int COL_USERNAME = 1;
    private static final int COL_FULLNAME = 2;
    private static final int COL_EMAIL = 3;
    private static final int COL_ACTIONS = 4;

    // ==== Table Section ====
    private TableComponent usersTable;
    private WebElement addUserBtn;

    // ==== Drawer (Edit Form) Section ====
    private WebElement drawer;
    private WebElement drawerCloseBtn;
    private WebElement usernameField;
    private WebElement emailField;
    private WebElement passwordField;
    private WebElement firstNameField;
    private WebElement lastNameField;
    private WebElement displayNamePatternDropdown;
    private WebElement displayNameField;
    private WebElement saveBtn;
    private WebElement inviteBtn;
    private WebElement cancelBtn;

    // ==== Role Management Section ====
    private WebElement addRoleBtn;
    private WebElement roleRepositoryTemplate;
    private WebElement roleNameTemplate;
    private WebElement removeRoleBtn;
    private WebElement selectOptionTemplate;

    // ==== Error Handling Section ====
    private WebElement errorNotification;
    private WebElement errorDescription;

    public UsersPageComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public UsersPageComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        // Initialize table
        usersTable = createScopedComponent(TableComponent.class, "xpath=.//table", "usersTable");
        addUserBtn = createScopedElement("xpath=.//button[./span[text()='Add User']]", "addUserBtn");

        // Drawer elements (not scoped to rootLocator as drawer is outside content div)
        drawer = new WebElement(page, "xpath=//div[contains(@class,'ant-drawer-open')]", "drawer");
        drawerCloseBtn = new WebElement(page, "xpath=//div[contains(@class,'ant-drawer-open')]//button[contains(@class,'ant-drawer-close')]", "drawerCloseBtn");

        // Form fields
        usernameField = new WebElement(page, "xpath=//input[@id='username']", "usernameField");
        emailField = new WebElement(page, "xpath=//input[@id='email']", "emailField");
        passwordField = new WebElement(page, "xpath=//input[@id='password']", "passwordField");
        firstNameField = new WebElement(page, "xpath=//input[@id='firstName']", "firstNameField");
        lastNameField = new WebElement(page, "xpath=//input[@id='lastName']", "lastNameField");
        displayNamePatternDropdown = new WebElement(page, "xpath=//input[@id='displayNameSelect']", "displayNamePatternDropdown");
        displayNameField = new WebElement(page, "xpath=//input[@id='displayName']", "displayNameField");

        // Form buttons
        saveBtn = new WebElement(page, "xpath=//div[contains(@class,'ant-drawer-open')]//button[./span[text()='Save']]", "saveBtn");
        inviteBtn = new WebElement(page, "xpath=//div[contains(@class,'ant-drawer-open')]//button[./span[text()='Invite']]", "inviteBtn");
        cancelBtn = new WebElement(page, "xpath=//div[contains(@class,'ant-drawer-open')]//button[./span[text()='Cancel']]", "cancelBtn");

        // Role management
        addRoleBtn = new WebElement(page, "xpath=//div[@role='tabpanel' and @aria-hidden='false']//button[./span[contains(text(),'Add Role')]]", "addRoleBtn");
        roleRepositoryTemplate = new WebElement(page, "xpath=//input[@id='designRepos_%s_id']", "designRepoSelectorTemplate");
        roleNameTemplate = new WebElement(page, "xpath=//input[@id='designRepos_%s_role']", "roleSelectorTemplate");
        removeRoleBtn = new WebElement(page, "xpath=//button[./span[contains(@aria-label,'delete')] and ancestor::div[contains(@class,'ant-form-item')]]", "removeRoleBtn");
        selectOptionTemplate = new WebElement(page, "xpath=//div[@class='rc-virtual-list-holder-inner' and not(ancestor::div[contains(@class,'dropdown-hidden')])]/div[@title='%s']", "selectOptionTemplate");

        // Error handling
        errorNotification = new WebElement(page, "xpath=//div[contains(@class,'ant-notification-notice-error')]", "errorNotification");
        errorDescription = new WebElement(page, "xpath=//div[contains(@class,'ant-notification-notice-error')]//div[contains(@class,'ant-notification-notice-description')]", "errorDescription");
    }

    public int getUserRow(String username) {
        WaitUtil.waitForCondition(() -> getAllUsernames().contains(username), DEFAULT_TIMEOUT_MS, 100, "Waiting for user '" + username + "' to appear in users table");
        int rowCount = usersTable.getRowsCount();
        for (int i = 1; i <= rowCount; i++) {
            String cellText = usersTable.getCellText(i, COL_USERNAME);
            if (cellText.contains(username)) {
                return i;
            }
        }
        throw new NoSuchElementException("User not found in table: " + username);
    }

    public String getUsernameFromRow(int rowIndex) {
        return usersTable.getCellText(rowIndex, COL_USERNAME).replaceAll("\\s+", " ").trim();
    }

    public String getEmailFromRow(int rowIndex) {
        return usersTable.getCellText(rowIndex, COL_EMAIL);
    }

    public String getFullNameFromRow(int rowIndex) {
        return usersTable.getCellText(rowIndex, COL_FULLNAME);
    }

    public boolean isUserInList(String username) {
        try {
            getUserRow(username);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public List<String> getAllUsernames() {
        List<String> usernames = new ArrayList<>();
        int rowCount = usersTable.getRowsCount();
        for (int i = 1; i <= rowCount; i++) {
            usernames.add(getUsernameFromRow(i));
        }
        return usernames;
    }

    public int getUsersCount() {
        return usersTable.getRowsCount();
    }

    public boolean areActionsAvailableForUser(String username) {
        int row = getUserRow(username);
        WebElement actionsCell = usersTable.getCell(row, COL_ACTIONS);
        return actionsCell.getLocator().locator("button >> svg[data-icon='edit']").isVisible() &&
               actionsCell.getLocator().locator("button >> svg[data-icon='delete']").isVisible();
    }

    public UsersPageComponent clickEditUser(String username) {
        int row = getUserRow(username);
        usersTable.getCell(row, COL_ACTIONS).getLocator().locator("button >> svg[data-icon='edit']").first().click();
        drawer.waitForVisible(3000);
        WaitUtil.sleep(150, "Waiting for user edit drawer to fully open");
        return this;
    }

    public void clickDeleteUser(String username) {
        int row = getUserRow(username);
        usersTable.getCell(row, COL_ACTIONS).getLocator().locator("button >> svg[data-icon='delete']").first().click();
        getModalOkBtn().click();
        WaitUtil.sleep(150, "Waiting for user deletion to complete");
    }

    // ========================================
    // Form Methods - Add/Edit User
    // ========================================

    public UsersPageComponent clickAddUser() {
        addUserBtn.click();
        drawer.waitForVisible(3000);
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
        roleRepositoryTemplate.format(row).getLocator().locator("xpath=/ancestor::span[@class='ant-select-selection-wrap']").click();
        selectOptionTemplate.format(repositoryName).waitForVisible().click();
        return this;
    }

    public UsersPageComponent setRole(int row, String role) {
        roleNameTemplate.format(row).getLocator().locator("xpath=/ancestor::span[@class='ant-select-selection-wrap']").click();
        selectOptionTemplate.format(role).waitForVisible().click();
        return this;
    }

    public String getRoleRepository(int row) {
        return roleRepositoryTemplate.format(row).getLocator().locator("xpath=/ancestor::span/following-sibling::span").getAttribute("title");
    }

    public String getRole(int row) {
        return roleNameTemplate.format(row).getLocator().locator("xpath=/ancestor::span/following-sibling::span").getAttribute("title");
    }

    public UsersPageComponent clearAllRoles() {
        while (removeRoleBtn.isVisible(1000)) {
            removeRoleBtn.click();
        }
        return this;
    }

    public void saveUser() {
        saveBtn.click();
        drawer.waitForHidden(3000);
    }

    public void inviteUser(boolean waitForDrawerToGetHidden) {
        inviteBtn.click();
        if(waitForDrawerToGetHidden)
            drawer.waitForHidden(3000);
    }

    public void inviteUser() {
        inviteUser(true);
    }

    public void cancelUser() {
        cancelBtn.click();
        drawer.waitForHidden(3000);
    }

    // ========================================
    // Legacy Methods (for backward compatibility)
    // ========================================

    @Deprecated
    public String getSpecificUserEmail(String username) {
        return getEmailFromRow(getUserRow(username));
    }

    @Deprecated
    public String getSpecificUserElement(String username, String elementType) {
        int row = getUserRow(username);
        switch (elementType) {
            case "users-firstname":
                String fullName = getFullNameFromRow(row);
                return fullName.split(" ")[0];
            case "users-lastname":
                String fullNameLast = getFullNameFromRow(row);
                String[] nameParts = fullNameLast.split(" ");
                return nameParts.length > 1 ? nameParts[nameParts.length - 1] : "";
            case "users-displayname":
                return getFullNameFromRow(row);
            default:
                return "";
        }
    }
}
