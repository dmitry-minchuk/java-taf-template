package domain.ui.webstudio.components.createnewproject;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;

public class TemplateTabComponent extends BaseComponent {

    private WebElement projectTemplate;
    private WebElement projectNameField;
    private WebElement createProjectBtn;
    private WebElement cancelBtn;
    private WebElement repositorySelect;
    private WebElement pathInRepositoryField;

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
        repositorySelect = createScopedElement("xpath=.//select[@id='createProjectFormTempl:repositoryTemplate']", "repositorySelect");
        pathInRepositoryField = createScopedElement("xpath=.//input[@id='createProjectFormTempl:projectFolderTemplate']", "pathInRepositoryField");
    }

    public void selectProjectTemplate(String templateName) {
        projectTemplate.format(templateName).click();
    }

    public void setProjectName(String projectName) {
        projectNameField.clear();
        projectNameField.fillSequentially(projectName);
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

    public String getRepositorySelectValue() {
        return repositorySelect.getCurrentInputValue();
    }

    public TemplateTabComponent selectRepository(String repositoryName) {
        repositorySelect.selectByVisibleText(repositoryName);
        return this;
    }

    public boolean isPathInRepositoryVisible() {
        return pathInRepositoryField.isVisible(1000);
    }

    public String getPathInRepositoryValue() {
        return pathInRepositoryField.getCurrentInputValue();
    }

    public TemplateTabComponent setPathInRepository(String path) {
        pathInRepositoryField.clear();
        pathInRepositoryField.fillSequentially(path);
        return this;
    }

    public void clickCreate() {
        createProjectBtn.click();
    }
}