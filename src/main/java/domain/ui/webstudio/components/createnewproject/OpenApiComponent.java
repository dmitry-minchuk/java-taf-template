package domain.ui.webstudio.components.createnewproject;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;

public class OpenApiComponent extends BaseComponent {

    private WebElement openApiUrlField;
    private WebElement projectNameField;
    private WebElement createProjectBtn;
    private WebElement cancelBtn;

    public OpenApiComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public OpenApiComponent(WebElement rootLocator) {
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