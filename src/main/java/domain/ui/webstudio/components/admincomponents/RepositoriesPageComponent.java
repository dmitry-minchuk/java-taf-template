package domain.ui.webstudio.components.admincomponents;

import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.BaseComponent;
import domain.ui.webstudio.pages.mainpages.LoginPage;
import helpers.service.UserService;
import helpers.utils.WaitUtil;

import java.util.List;

public class RepositoriesPageComponent extends BaseComponent {

    private WebElement designRepositoriesTab;
    private WebElement addRepositoryBtn;
    private List<WebElement> designRepositoryList;

    private WebElement remoteRepositoryNameField;
    private WebElement remoteRepositoryTypeSelector;
    private WebElement remoteRepositoryCheckBox;
    private WebElement remoteRepositoryPathField;
    private WebElement remoteRepositoryLoginField;
    private WebElement remoteRepositoryPasswordField;
    private WebElement remoteRepositoryBranchField;
    private WebElement flatFolderStructureCheckBox;
    private WebElement applyChangesBtn;
    private WebElement modalOkBtn;

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
        addRepositoryBtn = createScopedElement("xpath=.//button[./span[contains(text(),'Add Design Repository')]]", "addRepositoryBtn");
        designRepositoryList = createScopedElementList("xpath=.//div[@class='ant-tabs-content-holder']//div[@class='ant-tabs-nav-list']/div[@data-node-key]", "designRepositoryList");

        remoteRepositoryNameField = createScopedElement("xpath=.//input[@id='name']", "remoteRepositoryCheckBox");
        remoteRepositoryTypeSelector = createScopedElement("xpath=.//input[@id='type']", "remoteRepositoryCheckBox");
        remoteRepositoryCheckBox = createScopedElement("xpath=.//input[@id='settings_remoteRepository']", "remoteRepositoryCheckBox");
        remoteRepositoryPathField = createScopedElement("xpath=.//input[@id='settings_uri']", "loginField");
        remoteRepositoryLoginField = createScopedElement("xpath=.//input[@id='settings_login']", "loginField");
        remoteRepositoryPasswordField = createScopedElement("xpath=.//input[@id='settings_password']", "passwordField");
        remoteRepositoryBranchField = createScopedElement("xpath=.//input[@id='settings_branch']", "remoteRepositorBranchField");
        flatFolderStructureCheckBox = createScopedElement("xpath=.//input[@id='settings_flatFolderStructure']", "flatFolderStructureCheckBox");
        applyChangesBtn = createScopedElement("xpath=.//button[@type='submit']", "applyChangesBtn");
        modalOkBtn = new WebElement(page, "xpath=//div[@class='ant-modal-content']//button[./span[contains(text(),'OK')]]", "applyChangesBtn");
    }

    public void clickDesignRepositoriesTab() {
        designRepositoriesTab.click();
    }

    public void clickAddRepository() {
        addRepositoryBtn.sleep(500).click();
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
        modalOkBtn.click();
        new LoginPage().login(UserService.getUser(user), DEFAULT_TIMEOUT_MS * 100L);
    }
}