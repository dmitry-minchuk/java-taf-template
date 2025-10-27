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
    private WebElement localRepositoryPathField;
    private WebElement remoteRepositoryCheckBox;
    private WebElement remoteRepositoryPathField;
    private WebElement remoteRepositoryLoginField;
    private WebElement remoteRepositoryPasswordField;
    private WebElement remoteRepositoryBranchField;
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
        deploymentRepositoriesTab = createScopedElement("xpath=.//div[contains(@class,'ant-tabs-tab') and contains(text(),'Deployment Repositories')]", "deploymentRepositoriesTab");
        addRepositoryBtn = createScopedElement("xpath=.//button[./span[contains(text(),'Add Design Repository')]]", "addRepositoryBtn");
        addDeploymentRepositoryBtn = createScopedElement("xpath=.//button[./span[contains(text(),'Add Deployment Repository')]]", "addDeploymentRepositoryBtn");
        designRepositoryList = createScopedElementList("xpath=.//div[@class='ant-tabs-content-holder']//div[@class='ant-tabs-nav-list']/div[@data-node-key]", "designRepositoryList");
        localRepositoryPathField = createScopedElement("xpath=.//input[@id='settings_localRepositoryPath']", "localRepositoryPathField");

        remoteRepositoryNameField = createScopedElement("xpath=.//input[@id='name']", "remoteRepositoryCheckBox");
        remoteRepositoryTypeSelector = createScopedElement("xpath=.//input[@id='type']", "remoteRepositoryCheckBox");
        remoteRepositoryCheckBox = createScopedElement("xpath=.//input[@id='settings_remoteRepository']", "remoteRepositoryCheckBox");
        remoteRepositoryPathField = createScopedElement("xpath=.//input[@id='settings_uri']", "loginField");
        remoteRepositoryLoginField = createScopedElement("xpath=.//input[@id='settings_login']", "loginField");
        remoteRepositoryPasswordField = createScopedElement("xpath=.//input[@id='settings_password']", "passwordField");
        remoteRepositoryBranchField = createScopedElement("xpath=.//input[@id='settings_branch']", "remoteRepositorBranchField");
        flatFolderStructureCheckBox = createScopedElement("xpath=.//input[@id='settings_flatFolderStructure']", "flatFolderStructureCheckBox");
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

    public RepositoriesPageComponent setLocalRepositoryPath(String path) {
        localRepositoryPathField.fillSequentially(path);
        return this;
    }

    public void addDesignRepository() {
        clickDesignRepositoriesTab();
        clickAddRepository();
    }

    public void createDesignRepository(String repositoryUrl, String login, String password, String branch, User user) {
        addDesignRepository();
        remoteRepositoryCheckBox.sleep(500).hover().check();
        remoteRepositoryPathField.fillSequentially(repositoryUrl);
        remoteRepositoryLoginField.fillSequentially(login);
        remoteRepositoryPasswordField.fillSequentially(password);
        remoteRepositoryBranchField.fillSequentially(branch);
        flatFolderStructureCheckBox.hover().uncheck();
        applyChangesAndRelogin(user);
    }

    public void applyChangesAndRelogin(User user) {
        applyChangesBtn.click();
        getModalOkBtn().click();
        new LoginPage().login(UserService.getUser(user), DEFAULT_TIMEOUT_MS * 100L);
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