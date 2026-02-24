package domain.ui.webstudio.components.common;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.createnewproject.ExcelFilesComponent;
import domain.ui.webstudio.components.createnewproject.OpenApiComponent;
import domain.ui.webstudio.components.createnewproject.TemplateTabComponent;
import domain.ui.webstudio.components.createnewproject.WorkspaceComponent;
import domain.ui.webstudio.components.createnewproject.ZipArchiveComponent;
import lombok.Getter;

public class CreateNewProjectComponent extends BaseComponent {

    private ExcelFilesComponent excelFilesComponent;
    private ZipArchiveComponent zipArchiveComponent;
    private TemplateTabComponent templateTabComponent;
    private WorkspaceComponent workspaceTabComponent;
    private OpenApiComponent openApiComponent;

    private WebElement tabTemplate;
    private WebElement closeDialogBtn;

    public CreateNewProjectComponent() {
        super(LocalDriverPool.getPage());
        initializeElements();
    }

    public CreateNewProjectComponent(WebElement rootLocator) {
        super(rootLocator);
        initializeElements();
    }

    private void initializeElements() {
        tabTemplate = createScopedElement("xpath=.//span[@class='rf-tab-lbl' and contains(text(), '%s')]", "projectTabLabel");
        excelFilesComponent = createScopedComponent(ExcelFilesComponent.class, "xpath=.//form[@name='createProjectFormFiles']", "excelFilesComponent");
        zipArchiveComponent = createScopedComponent(ZipArchiveComponent.class, "xpath=.//form[@name='uploadProjectForm']", "zipArchiveComponent");
        templateTabComponent = createScopedComponent(TemplateTabComponent.class, "xpath=.//form[@name='createProjectFormTempl']", "templateTabComponent");
        workspaceTabComponent = createScopedComponent(WorkspaceComponent.class, "xpath=.//form[@name='uploadWorkspaceProjectForm']", "workspaceTabComponent");
        openApiComponent = createScopedComponent(OpenApiComponent.class, "xpath=.//form[@name='openAPIProjectForm']", "openApiComponent");
        closeDialogBtn = createScopedElement("xpath=.//a[contains(@class,'rf-pp-hdr-cls')]", "closeDialogBtn");
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseComponent> T selectTab(TabName tabName) {
        tabTemplate.format(tabName.getValue()).click();

        return switch (tabName) {
            case TEMPLATE -> (T) templateTabComponent;
            case EXCEL_FILES -> (T) excelFilesComponent;
            case ZIP_ARCHIVE -> (T) zipArchiveComponent;
            case WORKSPACE -> (T) workspaceTabComponent;
            case OPEN_API -> (T) openApiComponent;
            default -> throw new IllegalArgumentException("Unsupported tab type: " + tabName);
        };
    }

    public void closeDialog() {
        closeDialogBtn.click();
    }

    @Getter
    public enum TabName {
        TEMPLATE("Template"),
        EXCEL_FILES("Excel Files"),
        ZIP_ARCHIVE("Zip Archive"),
        OPEN_API("OpenAPI"),
        WORKSPACE("Workspace");

        private final String value;

        TabName(String value) {
            this.value = value;
        }
    }
}