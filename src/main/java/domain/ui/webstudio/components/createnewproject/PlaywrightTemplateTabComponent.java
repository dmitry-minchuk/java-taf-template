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
        projectTemplate = createScopedElement("xpath=.//table[@id='projectTemplates']//tr[.//span[contains(text(), '%s')]]", "projectTemplate");
        projectNameField = createScopedElement("xpath=.//input[@id='createProjectFormTempl:projectName']", "projectNameField");
        createProjectBtn = createScopedElement("#createProjectFormTempl:sbtTemplatesBtn", "createProjectBtn");
        cancelBtn = createScopedElement("xpath=.//input[@value='Cancel']", "cancelBtn");
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