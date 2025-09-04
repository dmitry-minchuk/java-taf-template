package domain.ui.webstudio.components.createnewproject;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;

public class TemplateTabComponent extends BaseComponent {

    private WebElement projectTemplate;
    private WebElement projectNameField;
    private WebElement createProjectBtn;
    private WebElement cancelBtn;

    public TemplateTabComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public TemplateTabComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        projectTemplate = createScopedElement("xpath=.//table[@id='projectTemplates']//tr[.//span[contains(text(), '%s')]]", "projectTemplate");
        projectNameField = createScopedElement("xpath=.//input[@id='createProjectFormTempl:projectName']", "projectNameField");
        createProjectBtn = createScopedElement("xpath=.//input[@id='createProjectFormTempl:sbtTemplatesBtn']", "createProjectBtn");
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
}