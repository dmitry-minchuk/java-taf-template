package domain.ui.webstudio.components.admincomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

public class PlaywrightRepositoriesPageComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement designRepositoriesTab;
    private PlaywrightWebElement deployConfigRepositoryTab;
    private PlaywrightWebElement deploymentRepositoriesTab;
    private PlaywrightWebElement addRepositoryBtn;
    private PlaywrightWebElement repositoryNameField;
    private PlaywrightWebElement repositoryTypeDropdown;
    private PlaywrightWebElement repositoryUrlField;
    private PlaywrightWebElement saveBtn;
    private PlaywrightWebElement cancelBtn;

    public PlaywrightRepositoriesPageComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightRepositoriesPageComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        designRepositoriesTab = createScopedElement(".//div[contains(@class,'ant-tabs-tab') and contains(text(),'Design Repositories')]", "designRepositoriesTab");
        deployConfigRepositoryTab = createScopedElement(".//div[contains(@class,'ant-tabs-tab') and contains(text(),'Deploy Configuration Repository')]", "deployConfigRepositoryTab");
        deploymentRepositoriesTab = createScopedElement(".//div[contains(@class,'ant-tabs-tab') and contains(text(),'Deployment Repositories')]", "deploymentRepositoriesTab");
        addRepositoryBtn = createScopedElement(".//button[./span[text()='Add Repository'] or ./span[contains(text(),'Add')]]", "addRepositoryBtn");
        repositoryNameField = createScopedElement(".//input[@placeholder='Name' or @id='repositoryName']", "repositoryNameField");
        repositoryTypeDropdown = createScopedElement(".//select[contains(@id,'repositoryType')] | .//div[contains(@class,'ant-select') and ./preceding-sibling::*[contains(text(),'Type')]]", "repositoryTypeDropdown");
        repositoryUrlField = createScopedElement(".//input[@placeholder='URL' or @id='repositoryUrl']", "repositoryUrlField");
        saveBtn = createScopedElement(".//button[./span[text()='Save'] or @type='submit']", "saveBtn");
        cancelBtn = createScopedElement(".//button[./span[text()='Cancel']]", "cancelBtn");
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

    public boolean isSaveButtonEnabled() {
        return saveBtn.isEnabled();
    }
}