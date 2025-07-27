package domain.ui.webstudio.components.createnewproject;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;

public class PlaywrightOpenApiComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement openApiUrlField;
    private PlaywrightWebElement projectNameField;
    private PlaywrightWebElement createProjectBtn;
    private PlaywrightWebElement cancelBtn;

    public PlaywrightOpenApiComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeElements();
    }

    public PlaywrightOpenApiComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        openApiUrlField = new PlaywrightWebElement(page, ".//input[@placeholder='OpenAPI URL' or contains(@id,'openApiUrl')]", "OpenAPI URL Field");
        projectNameField = new PlaywrightWebElement(page, ".//input[@id='openAPIProjectForm:projectName']", "Project Name Field");
        createProjectBtn = new PlaywrightWebElement(page, "#openAPIProjectForm:sbtOpenAPIBtn", "Create Project Button");
        cancelBtn = new PlaywrightWebElement(page, ".//input[@value='Cancel']", "Cancel Button");
    }

    public void setOpenApiUrl(String url) {
        openApiUrlField.fill(url);
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

    public void createProjectFromOpenApi(String url, String projectName) {
        setOpenApiUrl(url);
        setProjectName(projectName);
        createProject();
    }
}