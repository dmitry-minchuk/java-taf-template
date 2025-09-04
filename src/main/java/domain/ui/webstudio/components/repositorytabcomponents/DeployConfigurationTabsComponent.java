package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.CoreComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;

public class DeployConfigurationTabsComponent extends CoreComponent {

    private WebElement configurationTabs;
    private WebElement activeTab;
    private WebElement addConfigBtn;
    private WebElement saveBtn;
    private WebElement projectsToDeployTab;
    private WebElement addProjectButton;
    private WebElement projectsList;
    private WebElement revisionAddButtonTemplate;

    public DeployConfigurationTabsComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public DeployConfigurationTabsComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        configurationTabs = createScopedElement("xpath=.//div[contains(@class,'deploy-configuration-tabs')]", "configurationTabs");
        activeTab = createScopedElement("xpath=.//div[contains(@class,'tab-active')]", "activeTab");
        addConfigBtn = createScopedElement("xpath=.//button[./span[text()='Add Configuration']]", "addConfigBtn");
        saveBtn = createScopedElement("xpath=.//button[./span[text()='Save']]", "saveBtn");
        projectsToDeployTab = createScopedElement("xpath=.//span[text()='Projects to Deploy']", "projectsToDeployTab");
        addProjectButton = createScopedElement("xpath=.//input[@id='addProjectsId']", "addProjectButton");
        projectsList = new WebElement(page, "xpath=//select[@id='addDeployEntryForm:projectName']", "projectsList");
        revisionAddButtonTemplate = new WebElement(page, "xpath=//table[@id='addDeployEntryForm:projectVersion']//tr//td//span[text()='%s']//parent::td//..//td/input", "revisionAddButtonTemplate");
    }

    public void clickAddConfiguration() {
        addConfigBtn.click();
    }

    public void save() {
        saveBtn.click();
    }

    public boolean isConfigurationTabsVisible() {
        return configurationTabs.isVisible();
    }

    public DeployConfigurationTabsComponent openProjectsToDeployTab() {
        projectsToDeployTab.click();
        return this;
    }

    public DeployConfigurationTabsComponent addProject(String projectName, String revision) {
        addProjectButton.click();
        projectsList.selectOption(projectName);
        revisionAddButtonTemplate.format(revision.substring(0, 6)).click();
        return this;
    }
}