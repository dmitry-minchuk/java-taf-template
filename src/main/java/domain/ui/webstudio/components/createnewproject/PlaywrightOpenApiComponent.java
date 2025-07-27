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
        openApiUrlField = createScopedElement(".//input[@placeholder='OpenAPI URL' or contains(@id,'openApiUrl')]", "openApiUrlField");
        projectNameField = createScopedElement(".//input[@id='openAPIProjectForm:projectName']", "projectNameField");
        createProjectBtn = createScopedElement("#openAPIProjectForm:sbtOpenAPIBtn", "createProjectBtn");
        cancelBtn = createScopedElement(".//input[@value='Cancel']", "cancelBtn");
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