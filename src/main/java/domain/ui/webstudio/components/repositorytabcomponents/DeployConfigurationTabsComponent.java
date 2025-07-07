package domain.ui.webstudio.components.repositorytabcomponents;

import configuration.core.ui.BasePageComponent;
import configuration.core.ui.SmartWebElement;
import org.openqa.selenium.support.FindBy;

public class DeployConfigurationTabsComponent extends BasePageComponent {

    @FindBy(xpath = ".//span[text()='Projects to Deploy']")
    private SmartWebElement projectsToDeployTab;

    @FindBy(xpath = ".//input[@id='addProjectsId']")
    private SmartWebElement addProjectButton;

    @FindBy(xpath = "//select[@id='addDeployEntryForm:projectName']")
    private SmartWebElement projectsList;

    @FindBy(xpath = "//table[@id='addDeployEntryForm:projectVersion']//tr//td//span[text()='%s']//parent::td//..//td/input")
    private SmartWebElement addButtonInDialog;

    public DeployConfigurationTabsComponent openProjectsToDeployTab() {
        projectsToDeployTab.click();
        return this;
    }

    public DeployConfigurationTabsComponent addProject(String projectName, String revision) {
        addProjectButton.click();
        projectsList.selectByVisibleText(projectName);
        addButtonInDialog.format(revision).click();
        return this;
    }
} 