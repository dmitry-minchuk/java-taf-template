package domain.ui.webstudio.components.admincomponents;

import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import helpers.service.LoginService;
import helpers.service.UserService;

public class RepositoriesPageComponent extends BaseComponent {

    private WebElement designRepositoriesTab;
    private WebElement deployConfigRepositoryTab;
    private WebElement deploymentRepositoriesTab;
    private WebElement addRepositoryBtn;
    private WebElement repositoryNameField;
    private WebElement repositoryTypeDropdown;
    private WebElement repositoryUrlField;
    private WebElement saveBtn;
    private WebElement cancelBtn;

    // fields for Git repository configuration (from legacy TestLocalCentralProjects)
    private WebElement remoteRepositoryCheckBox;
    private WebElement loginField;
    private WebElement passwordField;
    private WebElement branchField;
    private WebElement flatFolderStructureCheckBox;
    private WebElement applyChangesBtn;

    public RepositoriesPageComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public RepositoriesPageComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        designRepositoriesTab = createScopedElement("xpath=.//div[contains(@class,'ant-tabs-tab') and contains(text(),'Design Repositories')]", "designRepositoriesTab");
        deployConfigRepositoryTab = createScopedElement("xpath=.//div[contains(@class,'ant-tabs-tab') and contains(text(),'Deploy Configuration Repository')]", "deployConfigRepositoryTab");
        deploymentRepositoriesTab = createScopedElement("xpath=.//div[contains(@class,'ant-tabs-tab') and contains(text(),'Deployment Repositories')]", "deploymentRepositoriesTab");
        addRepositoryBtn = createScopedElement("xpath=.//button[./span[text()='Add Repository'] or ./span[contains(text(),'Add')]]", "addRepositoryBtn");
        repositoryNameField = createScopedElement("xpath=.//input[@placeholder='Name' or @id='repositoryName']", "repositoryNameField");
        repositoryTypeDropdown = createScopedElement("xpath=.//select[contains(@id,'repositoryType')] | .//div[contains(@class,'ant-select') and ./preceding-sibling::*[contains(text(),'Type')]]", "repositoryTypeDropdown");
        repositoryUrlField = createScopedElement("xpath=.//input[@placeholder='URL' or @id='repositoryUrl']", "repositoryUrlField");
        saveBtn = createScopedElement("xpath=.//button[./span[text()='Save'] or @type='submit']", "saveBtn");
        cancelBtn = createScopedElement("xpath=.//button[./span[text()='Cancel']]", "cancelBtn");

        // Initialize Git repository configuration fields using legacy locator patterns
        String universalLocator = "xpath=.//input[contains(@id, '%s') and not(ancestor::div[@style='display: none;'])]";
        String credentialsLocator = "xpath=.//input[ancestor::td/preceding-sibling::td[text()='%s'] and not(ancestor::div[@style='display: none;'])]";

        remoteRepositoryCheckBox = createScopedElement(String.format(universalLocator, "designgitRemoteRepository"), "remoteRepositoryCheckBox");
        loginField = createScopedElement(String.format(credentialsLocator, "Login:"), "loginField");
        passwordField = createScopedElement(String.format(credentialsLocator, "Password:"), "passwordField");
        branchField = createScopedElement(String.format(credentialsLocator, "Branch:"), "branchField");
        flatFolderStructureCheckBox = createScopedElement(String.format(universalLocator, "designflatFolderStructure"), "flatFolderStructureCheckBox");
        applyChangesBtn = createScopedElement("xpath=.//button[./span[text()='Apply Changes'] or @value='Apply Changes']", "applyChangesBtn");
    }

    public void clickDesignRepositoriesTab() {
        designRepositoriesTab.click();
    }

    public void clickDeployConfigRepositoryTab() {
        deployConfigRepositoryTab.click();
    }

    public void clickDeploymentRepositoriesTab() {
        deploymentRepositoriesTab.click();
    }

    public void clickAddRepository() {
        addRepositoryBtn.click();
    }

    public void setRepositoryName(String name) {
        repositoryNameField.fill(name);
    }

    public String getRepositoryName() {
        return repositoryNameField.getAttribute("value");
    }

    public void selectRepositoryType(String type) {
        repositoryTypeDropdown.click();
        page.locator(".//div[contains(@class,'ant-select-item') and contains(text(),'" + type + "')]").click();
    }

    public void setRepositoryUrl(String url) {
        repositoryUrlField.fill(url);
    }

    public String getRepositoryUrl() {
        return repositoryUrlField.getAttribute("value");
    }

    public void saveRepository() {
        saveBtn.click();
    }

    public void cancelRepository() {
        cancelBtn.click();
    }

    public void addNewRepository(String name, String type, String url) {
        clickAddRepository();
        setRepositoryName(name);
        selectRepositoryType(type);
        setRepositoryUrl(url);
        saveRepository();
    }

    public boolean isAddRepositoryButtonVisible() {
        return addRepositoryBtn.isVisible();
    }

    // Method needed for TestLocalCentralProjects migration
    public void addDesignRepository() {
        clickDesignRepositoriesTab();
        clickAddRepository();
    }

    public void createDesignRepository(String repositoryUrl, String login, String password, String branch) {
        addDesignRepository();
        // Set remote repository checkbox
        if (!remoteRepositoryCheckBox.isSelected()) {
            remoteRepositoryCheckBox.click();
        }
        // Set repository URL (reuse existing field)
        setRepositoryUrl(repositoryUrl);
        // Set credentials
        loginField.fill(login);
        passwordField.fill(password);
        branchField.fill(branch);
        // Set flat folder structure to false
        if (flatFolderStructureCheckBox.isSelected()) {
            flatFolderStructureCheckBox.click();
        }
    }

    public void applyChangesAndRelogin(User user) {
        applyChangesBtn.click();
        new LoginService(LocalDriverPool.getPage()).login(UserService.getUser(user));
    }
}