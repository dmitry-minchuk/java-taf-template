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
        designRepositoriesTab = new PlaywrightWebElement(page, ".//div[contains(@class,'ant-tabs-tab') and contains(text(),'Design Repositories')]", "Design Repositories Tab");
        deployConfigRepositoryTab = new PlaywrightWebElement(page, ".//div[contains(@class,'ant-tabs-tab') and contains(text(),'Deploy Configuration Repository')]", "Deploy Configuration Repository Tab");
        deploymentRepositoriesTab = new PlaywrightWebElement(page, ".//div[contains(@class,'ant-tabs-tab') and contains(text(),'Deployment Repositories')]", "Deployment Repositories Tab");
        addRepositoryBtn = new PlaywrightWebElement(page, ".//button[./span[text()='Add Repository'] or ./span[contains(text(),'Add')]]", "Add Repository Button");
        repositoryNameField = new PlaywrightWebElement(page, ".//input[@placeholder='Name' or @id='repositoryName']", "Repository Name Field");
        repositoryTypeDropdown = new PlaywrightWebElement(page, ".//select[contains(@id,'repositoryType')] | .//div[contains(@class,'ant-select') and ./preceding-sibling::*[contains(text(),'Type')]]", "Repository Type Dropdown");
        repositoryUrlField = new PlaywrightWebElement(page, ".//input[@placeholder='URL' or @id='repositoryUrl']", "Repository URL Field");
        saveBtn = new PlaywrightWebElement(page, ".//button[./span[text()='Save'] or @type='submit']", "Save Button");
        cancelBtn = new PlaywrightWebElement(page, ".//button[./span[text()='Cancel']]", "Cancel Button");
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