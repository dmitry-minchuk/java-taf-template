package domain.ui.webstudio.components.admincomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.BaseComponent;
import domain.ui.webstudio.pages.mainpages.LoginPage;
import helpers.service.UserService;
import helpers.utils.StringUtil;

import java.util.List;

public class RepositoriesPageComponent extends BaseComponent {

    private WebElement designRepositoriesTab;
    private WebElement deploymentRepositoriesTab;
    private WebElement addRepositoryBtn;
    private WebElement addDeploymentRepositoryBtn;
    private List<WebElement> designRepositoryList;

    private WebElement remoteRepositoryNameField;
    private WebElement remoteRepositoryTypeSelector;
    private WebElement remoteRepositoryCheckBox;
    private WebElement remoteRepositoryPathField;
    private WebElement remoteRepositoryLoginField;
    private WebElement remoteRepositoryPasswordField;
    private WebElement remoteRepositoryBranchField;
    private WebElement flatFolderStructureCheckBox;
    private WebElement secureConnectionCheckbox;
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
        deploymentRepositoriesTab = createScopedElement("xpath=.//div[contains(@class,'ant-tabs-tab') and contains(text(),'Deployment Repositories')]", "deploymentRepositoriesTab");
        addRepositoryBtn = createScopedElement("xpath=.//button[./span[contains(text(),'Add Design Repository')]]", "addRepositoryBtn");
        addDeploymentRepositoryBtn = createScopedElement("xpath=.//button[./span[contains(text(),'Add Deployment Repository')]]", "addDeploymentRepositoryBtn");
        designRepositoryList = createScopedElementList("xpath=.//div[@class='ant-tabs-content-holder']//div[@class='ant-tabs-nav-list']/div[@data-node-key]", "designRepositoryList");

        remoteRepositoryNameField = createScopedElement("xpath=.//input[@id='name']", "remoteRepositoryCheckBox");
        remoteRepositoryTypeSelector = createScopedElement("xpath=.//input[@id='type']", "remoteRepositoryCheckBox");
        remoteRepositoryCheckBox = createScopedElement("xpath=.//input[@id='settings_remoteRepository']", "remoteRepositoryCheckBox");
        remoteRepositoryPathField = createScopedElement("xpath=.//input[@id='settings_uri']", "loginField");
        remoteRepositoryLoginField = createScopedElement("xpath=.//input[@id='settings_login']", "loginField");
        remoteRepositoryPasswordField = createScopedElement("xpath=.//input[@id='settings_password']", "passwordField");
        remoteRepositoryBranchField = createScopedElement("xpath=.//input[@id='settings_branch']", "remoteRepositorBranchField");
        flatFolderStructureCheckBox = createScopedElement("xpath=.//input[@id='settings_flatFolderStructure']", "flatFolderStructureCheckBox");
        secureConnectionCheckbox = createScopedElement("xpath=.//input[@id='settings_secureConnection']", "secureConnectionCheckbox");
        applyChangesBtn = createScopedElement("xpath=.//button[@type='submit']", "applyChangesBtn");
    }

    public RepositoriesPageComponent clickDesignRepositoriesTab() {
        designRepositoriesTab.click();
        return this;
    }

    public RepositoriesPageComponent clickAddRepository() {
        addRepositoryBtn.sleep(500).click();
        return this;
    }

    public RepositoriesPageComponent setRepositoryPath(String path) {
        remoteRepositoryPathField.fillSequentially(path);
        return this;
    }

    public void addDesignRepository() {
        clickDesignRepositoriesTab();
        clickAddRepository();
    }

    public RepositoriesPageComponent selectDesignRepositoryByName(String name) {
        designRepositoryList.stream()
                .filter(tab -> tab.getText().trim().equals(name))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Design repository tab '" + name + "' not found"))
                .click();
        return this;
    }

    public String getDesignRepositoryNameValue() {
        return remoteRepositoryNameField.getAttribute("value");
    }

    public String getDesignRepositoryType() {
        return remoteRepositoryTypeSelector.getAttribute("value");
    }

    public boolean isDesignRepositoryRemote() {
        return remoteRepositoryCheckBox.isChecked();
    }

    public String getDesignRepositoryLocalPath() {
        return remoteRepositoryPathField.getAttribute("value");
    }

    public String getDesignRepositoryUrl() {
        return remoteRepositoryPathField.getAttribute("value");
    }

    public RepositoriesPageComponent setDesignRepositoryType(String type) {
        remoteRepositoryTypeSelector.click();
        createScopedElement("xpath=.//div[contains(@class,'ant-select-item-option') and .//span[text()='" + type + "']]", "typeOption").click();
        return this;
    }

    public RepositoriesPageComponent setDesignRepositoryJdbcUrl(String url) {
        remoteRepositoryPathField.waitForVisible(3000).clear();
        remoteRepositoryPathField.fillSequentially(url);
        return this;
    }

    public RepositoriesPageComponent setDesignRepositoryLogin(String login) {
        remoteRepositoryLoginField.clear();
        remoteRepositoryLoginField.fillSequentially(login);
        return this;
    }

    public RepositoriesPageComponent setDesignRepositoryPassword(String password) {
        remoteRepositoryPasswordField.clear();
        remoteRepositoryPasswordField.fillSequentially(password);
        return this;
    }

    public RepositoriesPageComponent setSecureConnection(boolean enabled) {
        if (enabled != secureConnectionCheckbox.isChecked()) {
            secureConnectionCheckbox.click();
        }
        return this;
    }

    public RepositoriesPageComponent setFlatFolderStructure(boolean flat) {
        if (flat != flatFolderStructureCheckBox.isChecked()) {
            flatFolderStructureCheckBox.click();
        }
        return this;
    }

    public void createDesignRepository(String repositoryUrl, String login, String password, String branch, User user) {
        addDesignRepository();
        remoteRepositoryPathField.waitForVisible(1000).sleep(500).clear();
        remoteRepositoryPathField.fillSequentially(repositoryUrl);
        remoteRepositoryLoginField.fillSequentially(login);
        remoteRepositoryPasswordField.fillSequentially(password);
        remoteRepositoryBranchField.fillSequentially(branch);
        applyChangesAndRelogin(user);
    }

    public void applyChangesAndRelogin(User user) {
        applyChangesBtn.click();
        getModalOkBtn().click();
        new LoginPage().login(UserService.getUser(user), DEFAULT_TIMEOUT_MS * 1000L);
    }

    public void clickDeploymentRepositoriesTab() {
        deploymentRepositoriesTab.click();
    }

    public void clickAddDeploymentRepository() {
        addDeploymentRepositoryBtn.sleep(500).click();
    }

    public void addDeploymentRepository() {
        clickDeploymentRepositoriesTab();
        clickAddDeploymentRepository();
    }

    public void createH2DeploymentRepository(User user) {
        String repoUrl = String.format("jdbc:h2:mem:repo%s;DB_CLOSE_DELAY=-1", StringUtil.generateUniqueName(5));
        addDeploymentRepository();
        remoteRepositoryPathField.fillSequentially(repoUrl);
        applyChangesAndRelogin(user);
    }
}