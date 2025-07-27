package domain.ui.webstudio.components.createnewproject;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

public class PlaywrightTemplateTabComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement projectTemplate;
    private PlaywrightWebElement projectNameField;
    private PlaywrightWebElement createProjectBtn;
    private PlaywrightWebElement cancelBtn;

    public PlaywrightTemplateTabComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightTemplateTabComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        projectTemplate = new PlaywrightWebElement(page, ".//table[@id='projectTemplates']//tr[.//span[contains(text(), '%s')]]", "Project Template");
        projectNameField = new PlaywrightWebElement(page, ".//input[@id='createProjectFormTempl:projectName']", "Project Name Field");
        createProjectBtn = new PlaywrightWebElement(page, "#createProjectFormTempl:sbtTemplatesBtn", "Create Project Button");
        cancelBtn = new PlaywrightWebElement(page, ".//input[@value='Cancel']", "Cancel Button");
    }

    public void selectProjectTemplate(String templateName) {
        projectTemplate.format(templateName).click();
    }

    public void setProjectName(String projectName) {
        projectNameField.fill(projectName);
    }

    public void createProject() {
        createProjectBtn.click();
    }

    public void cancel() {
        cancelBtn.click();
    }

    public void createProjectFromTemplate(String projectName, String templateName) {
        selectProjectTemplate(templateName);
        setProjectName(projectName);
        createProject();
    }

    public boolean isTemplateAvailable(String templateName) {
        return projectTemplate.format(templateName).isVisible();
    }

    public boolean isCreateButtonEnabled() {
        return createProjectBtn.isEnabled();
    }
}