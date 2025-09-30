package domain.ui.webstudio.components.repositorytabcomponents;

import com.microsoft.playwright.Dialog;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.BaseComponent;

import java.util.List;
import java.util.stream.Collectors;

public class DeployConfigurationTabsComponent extends BaseComponent {

    private WebElement configurationTabs;
    private WebElement activeTab;
    private WebElement addConfigBtn;
    private WebElement saveBtn;
    private WebElement projectsToDeployTab;
    private WebElement addProjectButton;
    private WebElement projectsList;
    private WebElement revisionAddButtonTemplate;
    private WebElement addCurrentProjectToDeployButton;
    private WebElement projectNameInput;
    private WebElement deployProjectsTable;
    private WebElement removeProjectTemplate;

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
        addCurrentProjectToDeployButton = createScopedElement("xpath=.//input[@id='addCurrentProjectToDeploy']", "addCurrentProjectToDeployButton");
        projectNameInput = createScopedElement("xpath=.//select[@id='addDeployEntryForm:projectName']", "projectNameInput");
        deployProjectsTable = createScopedElement("xpath=.//table[@id='descriptorTable']", "deployProjectsTable");
        removeProjectTemplate = createScopedElement("xpath=.//td[preceding-sibling::td[text()='%s']]/a[@class='delete-icon']", "removeProjectTemplate");
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
        projectsList.selectByVisibleText(projectName);
        revisionAddButtonTemplate.format(revision.substring(0, 6)).click();
        return this;
    }

    public DeployConfigurationTabsComponent addProjectToDeploy(String projectName) {
        addProjectButton.click();
        projectNameInput.fillSequentially(projectName);
        addCurrentProjectToDeployButton.click();
        return this;
    }

    public List<String> getVisibleProjectsInDeployList() {
        return deployProjectsTable.getLocator()
            .locator("tbody tr td:nth-child(2)")
            .allTextContents()
            .stream()
            .filter(text -> !text.trim().isEmpty())
            .collect(Collectors.toList());
    }

    public DeployConfigurationTabsComponent removeProjectFromDeploy(String projectName) {
        page.onDialog(Dialog::accept);
        removeProjectTemplate.format(projectName).click();
        return this;
    }
}