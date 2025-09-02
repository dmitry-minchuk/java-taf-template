package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import org.testcontainers.containers.wait.strategy.Wait;

public class PlaywrightDeployConfigurationTabsComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement configurationTabs;
    private PlaywrightWebElement activeTab;
    private PlaywrightWebElement addConfigBtn;
    private PlaywrightWebElement saveBtn;
    private PlaywrightWebElement projectsToDeployTab;
    private PlaywrightWebElement addProjectButton;
    private PlaywrightWebElement projectsList;
    private PlaywrightWebElement revisionAddButtonTemplate;

    public PlaywrightDeployConfigurationTabsComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightDeployConfigurationTabsComponent(PlaywrightWebElement rootLocator) {
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
        projectsList = new PlaywrightWebElement(page, "xpath=//select[@id='addDeployEntryForm:projectName']", "projectsList");
        revisionAddButtonTemplate = new PlaywrightWebElement(page, "xpath=//table[@id='addDeployEntryForm:projectVersion']//tr//td//span[text()='%s']//parent::td//..//td/input", "revisionAddButtonTemplate");
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

    public PlaywrightDeployConfigurationTabsComponent openProjectsToDeployTab() {
        projectsToDeployTab.click();
        return this;
    }

    public PlaywrightDeployConfigurationTabsComponent addProject(String projectName, String revision) {
        addProjectButton.click();
        projectsList.selectOption(projectName);
        revisionAddButtonTemplate.format(revision.substring(0, 6)).click();
        return this;
    }
}