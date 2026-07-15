package domain.ui.webstudio.components.common;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.createnewproject.ExcelFilesComponent;
import domain.ui.webstudio.components.createnewproject.OpenApiComponent;
import domain.ui.webstudio.components.createnewproject.TemplateTabComponent;
import domain.ui.webstudio.components.createnewproject.WorkspaceComponent;
import domain.ui.webstudio.components.createnewproject.ZipArchiveComponent;
import helpers.utils.TestDataUtil;
import lombok.Getter;

public class CreateNewProjectComponent extends BaseComponent {

    private ExcelFilesComponent excelFilesComponent;
    private ZipArchiveComponent zipArchiveComponent;
    @Getter
    private TemplateTabComponent templateTabComponent;
    private WorkspaceComponent workspaceTabComponent;
    private OpenApiComponent openApiComponent;

    private WebElement tabTemplate;
    private WebElement closeDialogBtn;

    // React "Create project" wizard (build 032c60a664ce+): page-level data-testid controls.
    private WebElement methodTemplate;
    private WebElement methodExcel;
    private WebElement methodArchive;
    private WebElement nextBtn;
    private WebElement cancelBtn;
    private WebElement submitBtn;
    private WebElement nameField;
    private WebElement templateGroup; // format(groupKey): templates/examples/tutorials
    private WebElement templateItem;  // format(templateName): visible label
    private WebElement excelUpload;   // file input on the "From Excel files" step
    private WebElement archiveUpload; // file input on the "From archive" (.zip) step

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
        closeDialogBtn = createScopedElement("xpath=.//img[@class='close']", "closeDialogBtn");

        methodTemplate = new WebElement(LocalDriverPool.getPage(), "[data-testid=new-project-method-template]", "methodTemplate");
        methodExcel = new WebElement(LocalDriverPool.getPage(), "[data-testid=new-project-method-excel]", "methodExcel");
        methodArchive = new WebElement(LocalDriverPool.getPage(), "[data-testid=new-project-method-archive]", "methodArchive");
        nextBtn = new WebElement(LocalDriverPool.getPage(), "[data-testid=new-project-next]", "newProjectNext");
        cancelBtn = new WebElement(LocalDriverPool.getPage(), "[data-testid=new-project-cancel]", "newProjectCancel");
        submitBtn = new WebElement(LocalDriverPool.getPage(), "[data-testid=new-project-submit]", "newProjectSubmit");
        nameField = new WebElement(LocalDriverPool.getPage(), "[data-testid=new-project-name]", "newProjectName");
        templateGroup = new WebElement(LocalDriverPool.getPage(), "[data-testid=template-group-%s]", "templateGroup");
        templateItem = new WebElement(LocalDriverPool.getPage(), "xpath=//div[@data-testid='new-project-template']//button[.//span[normalize-space()='%s']]", "templateItem");
        excelUpload = new WebElement(LocalDriverPool.getPage(), "[data-testid=new-project-excel-upload]", "excelUpload");
        archiveUpload = new WebElement(LocalDriverPool.getPage(), "[data-testid=new-project-upload]", "archiveUpload");
    }

    // Full create-from-template path in the React wizard (method -> Next -> group -> item -> name -> Create).
    public void createProjectFromTemplate(String templateName, String projectName) {
        createProjectFromTemplate(templateName, projectName, true);
    }

    // submit=false opens the wizard and fills the form without pressing Create (partial flows).
    public void createProjectFromTemplate(String templateName, String projectName, boolean submit) {
        methodTemplate.click();
        nextBtn.click();
        if (templateName != null && !templateName.isEmpty()) {
            templateGroup.format(groupOf(templateName)).click();
            templateItem.format(templateName).click();
        }
        if (projectName != null && !projectName.isEmpty()) {
            nameField.fill(projectName);
        }
        if (submit) {
            submitBtn.click();
        }
    }

    // Predefined templates are grouped Templates / Examples / Tutorials in the React wizard.
    private static String groupOf(String templateName) {
        if (templateName.startsWith("Example")) return "examples";
        if (templateName.startsWith("Tutorial")) return "tutorials";
        return "templates";
    }

    // Create-from-Excel path in the React wizard (method -> Next -> upload .xlsx -> name -> Create).
    public void createProjectFromExcel(String excelFileName, String projectName) {
        methodExcel.click();
        nextBtn.click();
        excelUpload.setInputFiles(TestDataUtil.getFilePathFromResources(excelFileName));
        if (projectName != null && !projectName.isEmpty()) {
            nameField.fill(projectName);
        }
        submitBtn.click();
    }

    // Create-from-archive path in the React wizard (method -> Next -> upload .zip -> name -> Create).
    public void createProjectFromZip(String zipFileName, String projectName) {
        methodArchive.click();
        nextBtn.click();
        archiveUpload.setInputFiles(TestDataUtil.getFilePathFromResources(zipFileName));
        if (projectName != null && !projectName.isEmpty()) {
            nameField.fill(projectName);
        }
        submitBtn.click();
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

    public void cancelCreation() {
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