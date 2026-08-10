package domain.ui.webstudio.components.common;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.DriverPool;
import domain.ui.webstudio.components.createnewproject.ExcelFilesComponent;
import domain.ui.webstudio.components.createnewproject.OpenApiComponent;
import domain.ui.webstudio.components.createnewproject.TemplateTabComponent;
import domain.ui.webstudio.components.createnewproject.WorkspaceComponent;
import domain.ui.webstudio.components.createnewproject.ZipArchiveComponent;
import helpers.utils.TestDataUtil;
import helpers.utils.WaitUtil;
import lombok.Getter;

public class CreateNewProjectComponent extends BaseComponent {

    // The branch field renders with the wizard step, so a short probe decides present-vs-absent.
    private static final int BRANCH_FIELD_PROBE_MS = DEFAULT_TIMEOUT_MS / 5;

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
    private WebElement cancelBtn;
    private WebElement submitBtn;
    private WebElement nameField;
    private WebElement templateGroup; // format(groupKey): templates/examples/tutorials
    private WebElement templateItem;  // format(templateName): visible label
    private WebElement excelUpload;   // file input on the "From Excel files" step
    private WebElement archiveUpload; // file input on the "From archive" (.zip) step
    private WebElement methodOpenApi;
    private WebElement openApiUpload; // file input on the "From OpenAPI" step (data/rules modules auto-fill)
    private WebElement openApiDataModuleField;
    private WebElement openApiDataPathField;
    private WebElement openApiRulesModuleField;
    private WebElement openApiRulesPathField;
    private WebElement openApiUploadedFileRemoveBtn;
    private java.util.List<WebElement> openApiUploadedFiles;
    private WebElement openApiError;
    // 6.4.0 added a required Branch field; the repository config fills it in asynchronously and the wizard
    // refuses to submit while it is empty, leaving the modal open over the rest of the page.
    private WebElement branchField;
    private WebElement repoSelect;    // new-project-repo (ant-select) — target design repository (shown with >1 repo)
    private WebElement repoOption;    // body-level ant-select option, format(repoName)
    private WebElement pathField;     // new-project-path (path-in-repository for non-flat repos)

    public CreateNewProjectComponent() {
        super(DriverPool.getPage());
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

        methodTemplate = new WebElement(DriverPool.getPage(), "[data-testid=new-project-method-template]", "methodTemplate");
        methodExcel = new WebElement(DriverPool.getPage(), "[data-testid=new-project-method-excel]", "methodExcel");
        methodArchive = new WebElement(DriverPool.getPage(), "[data-testid=new-project-method-archive]", "methodArchive");
        cancelBtn = new WebElement(DriverPool.getPage(), "[data-testid=new-project-cancel]", "newProjectCancel");
        submitBtn = new WebElement(DriverPool.getPage(), "[data-testid=new-project-submit]", "newProjectSubmit");
        nameField = new WebElement(DriverPool.getPage(), "[data-testid=new-project-name]", "newProjectName");
        templateGroup = new WebElement(DriverPool.getPage(), "[data-testid=template-group-%s]", "templateGroup");
        templateItem = new WebElement(DriverPool.getPage(), "xpath=//div[@data-testid='new-project-template']//button[.//span[normalize-space()='%s']]", "templateItem");
        excelUpload = new WebElement(DriverPool.getPage(), "[data-testid=new-project-excel-upload]", "excelUpload");
        archiveUpload = new WebElement(DriverPool.getPage(), "[data-testid=new-project-upload]", "archiveUpload");
        methodOpenApi = new WebElement(DriverPool.getPage(), "[data-testid=new-project-method-openapi]", "methodOpenApi");
        openApiUpload = new WebElement(DriverPool.getPage(), "[data-testid=new-project-openapi-upload]", "openApiUpload");
        openApiDataModuleField = new WebElement(DriverPool.getPage(), "[data-testid=new-project-openapi-data-module]", "openApiDataModule");
        openApiDataPathField = new WebElement(DriverPool.getPage(), "[data-testid=new-project-openapi-data-path]", "openApiDataPath");
        openApiRulesModuleField = new WebElement(DriverPool.getPage(), "[data-testid=new-project-openapi-rules-module]", "openApiRulesModule");
        openApiRulesPathField = new WebElement(DriverPool.getPage(), "[data-testid=new-project-openapi-rules-path]", "openApiRulesPath");
        // The remove button sits inside the upload list item's actions container.
        openApiUploadedFileRemoveBtn = new WebElement(DriverPool.getPage(), "xpath=//span[contains(@class,'ant-upload-list-item-actions')]//button", "openApiRemoveFile");
        openApiUploadedFiles = createElementList("xpath=//div[contains(@class,'ant-upload-list-item')]", "openApiUploadedFiles");
        openApiError = new WebElement(DriverPool.getPage(), "[data-testid=new-project-error]", "newProjectError");
        repoSelect = new WebElement(DriverPool.getPage(), "[data-testid=new-project-repo]", "newProjectRepo");
        // antd wraps the field, so the testid can sit on the wrapper - read the inner input either way.
        branchField = new WebElement(DriverPool.getPage(),
                "css=[data-testid=new-project-branch] input, input[data-testid=new-project-branch]", "newProjectBranch");
        repoOption = new WebElement(DriverPool.getPage(), "xpath=//div[contains(@class,'ant-select-item-option')][.//*[normalize-space(text())='%s'] or @title='%s']", "newProjectRepoOption");
        pathField = new WebElement(DriverPool.getPage(), "[data-testid=new-project-path]", "newProjectPath");
    }

    // --- React create-wizard target-repository + path controls (visible on the form step once >1 design repo
    // exists). Used by multi-repo tests to create/copy into a specific repository at a given path. ---
    public CreateNewProjectComponent selectRepository(String repositoryName) {
        repoSelect.waitForVisible(5000);
        repoSelect.click();
        repoOption.format(repositoryName, repositoryName).click();
        return this;
    }

    public String getRepositorySelectValue() {
        return repoSelect.getText().trim();
    }

    public boolean isPathInRepositoryVisible() {
        return pathField.isVisible(3000);
    }

    public String getPathInRepositoryValue() {
        return pathField.getCurrentInputValue();
    }

    public CreateNewProjectComponent setPathInRepository(String path) {
        pathField.clear();
        pathField.fill(path);
        return this;
    }

    /**
     * Puts the name into the wizard's Name field and makes sure that is all it holds.
     *
     * <p>The wizard suggests a name from the picked template or archive, and that suggestion arrives after the
     * field is already on screen: typing while it lands leaves both values in the field (e.g. "Sample Project"
     * plus the typed name), and the project is then created under that joined name. Retyping until the field
     * reads back exactly what was asked for is what makes this reliable.
     */
    private void typeProjectName(String projectName) {
        boolean accepted = WaitUtil.waitForCondition(() -> {
            nameField.fill(projectName);
            return projectName.equals(nameField.getCurrentInputValue());
        }, DEFAULT_TIMEOUT_MS, 300, "Waiting for the name field to hold only the requested project name");
        if (!accepted) {
            throw new IllegalStateException("The wizard kept its suggested name next to '" + projectName
                    + "': field reads '" + nameField.getCurrentInputValue() + "'");
        }
    }

    /** Presses Create for a project that must be created: the wizard is expected to close. */
    public void clickCreate() {
        clickCreate(true);
    }

    /**
     * Presses Create.
     *
     * @param expectWizardToClose {@code true} when the create must succeed — the wizard closes, and leaving
     *        it open would let the modal swallow every later click, so its disappearance is waited for.
     *        {@code false} for the negative flows (a malformed spec, a rejected name): the wizard stays open
     *        on purpose and reports the problem in its own error area, so waiting for it to close would
     *        time out before the caller ever gets to read the message.
     */
    public void clickCreate(boolean expectWizardToClose) {
        waitForBranchToBeOffered();
        submitBtn.click();
        if (expectWizardToClose) {
            submitBtn.waitForHidden(DEFAULT_TIMEOUT_MS);
        } else {
            // Give the wizard a moment to answer, so a following getError() reads the message rather than
            // racing the request. Whether it appears is the caller's assertion, so nothing is enforced here.
            openApiError.isVisible(DEFAULT_TIMEOUT_MS);
        }
    }

    /**
     * Waits until the wizard's Branch field carries the value its repository config supplies. Submitting
     * earlier is rejected client-side, with no request sent and the modal left open. Only the visible field
     * is waited for: the OpenAPI path keeps a branch input in the DOM that it never fills, and waiting on
     * that one burned the full timeout on every Create.
     */
    private void waitForBranchToBeOffered() {
        if (!branchField.isVisible(BRANCH_FIELD_PROBE_MS)) {
            return;
        }
        WaitUtil.waitForCondition(() -> !branchField.getCurrentInputValue().isBlank(),
                DEFAULT_TIMEOUT_MS, 250, "Waiting for the wizard to fill in the branch");
    }

    // Full create-from-template path in the React wizard (method -> group -> item -> name -> Create).
    public void createProjectFromTemplate(String templateName, String projectName) {
        createProjectFromTemplate(templateName, projectName, true);
    }

    // submit=false opens the wizard and fills the form without pressing Create (partial flows).
    public void createProjectFromTemplate(String templateName, String projectName, boolean submit) {
        methodTemplate.click();
        if (templateName != null && !templateName.isEmpty()) {
            templateGroup.format(groupOf(templateName)).click();
            templateItem.format(templateName).click();
        }
        if (projectName != null && !projectName.isEmpty()) {
            typeProjectName(projectName);
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

    // Create-from-Excel path in the React wizard (method -> upload .xlsx -> name -> Create).
    public void createProjectFromExcel(String excelFileName, String projectName) {
        methodExcel.click();
        excelUpload.setInputFiles(TestDataUtil.getFilePathFromResources(excelFileName));
        if (projectName != null && !projectName.isEmpty()) {
            typeProjectName(projectName);
        }
        submitBtn.click();
    }

    // Create-from-archive path in the React wizard (method -> upload .zip -> name -> Create).
    public void createProjectFromZip(String zipFileName, String projectName) {
        methodArchive.click();
        archiveUpload.setInputFiles(TestDataUtil.getFilePathFromResources(zipFileName));
        if (projectName != null && !projectName.isEmpty()) {
            typeProjectName(projectName);
        }
        submitBtn.click();
    }

    /** Picks how the project is created (the wizard opens the matching form right away). */
    public CreateNewProjectComponent selectMethod(TabName method) {
        switch (method) {
            case TEMPLATE -> methodTemplate.click();
            case ZIP_ARCHIVE -> methodArchive.click();
            case EXCEL_FILES -> methodExcel.click();
            case OPEN_API -> methodOpenApi.click();
            default -> throw new IllegalArgumentException("Unsupported create method: " + method);
        }
        return this;
    }

    public CreateNewProjectComponent uploadOpenApiSpec(String fileName) {
        openApiUpload.setInputFiles(TestDataUtil.getFilePathFromResources(fileName));
        return this;
    }

    public CreateNewProjectComponent setProjectName(String projectName) {
        typeProjectName(projectName);
        return this;
    }

    public String getProjectName() {
        return nameField.getCurrentInputValue();
    }

    // --- "From OpenAPI" step fields. The wizard shows the module names and paths as plain inputs, filled in
    // from the uploaded specification; paths are edited directly, so the legacy "Edit path" step is gone. ---

    public CreateNewProjectComponent setDataModuleName(String moduleName) {
        openApiDataModuleField.fill(moduleName);
        return this;
    }

    public String getDataModuleName() {
        return openApiDataModuleField.getCurrentInputValue();
    }

    public CreateNewProjectComponent setDataModulePath(String path) {
        openApiDataPathField.fill(path);
        return this;
    }

    public String getDataModulePathInputValue() {
        return getDataModulePath();
    }

    public String getDataModulePath() {
        return openApiDataPathField.getCurrentInputValue();
    }

    public CreateNewProjectComponent setRulesModuleName(String moduleName) {
        openApiRulesModuleField.fill(moduleName);
        return this;
    }

    public String getRulesModuleName() {
        return openApiRulesModuleField.getCurrentInputValue();
    }

    public CreateNewProjectComponent setRulesModulePath(String path) {
        openApiRulesPathField.fill(path);
        return this;
    }

    public String getRulesModulePath() {
        return openApiRulesPathField.getCurrentInputValue();
    }

    /**
     * Removes the uploaded specification from the step (the upload takes a single file). The remove button
     * only shows on hover, so presence is judged by the upload list itself, not by the button.
     */
    public CreateNewProjectComponent clearOpenApiFile() {
        if (!openApiUploadedFiles.isEmpty()) {
            openApiUploadedFileRemoveBtn.click();
            WaitUtil.waitForCondition(openApiUploadedFiles::isEmpty, DEFAULT_TIMEOUT_MS, 200,
                    "Waiting for the uploaded specification to be removed");
        }
        return this;
    }

    public boolean isOpenApiFileUploaded() {
        return !openApiUploadedFiles.isEmpty();
    }

    public boolean isCreateEnabled() {
        return submitBtn.isEnabled();
    }

    /** The wizard's error message, e.g. when the specification or a module name is missing. */
    public String getError() {
        return openApiError.waitForVisible(DEFAULT_TIMEOUT_MS).getText().trim();
    }

    public boolean hasError() {
        return openApiError.isVisible(DEFAULT_TIMEOUT_MS / 2);
    }

    // Create-from-OpenAPI path in the React wizard (method -> upload spec -> name -> Create).
    // The data/rules module name and path fields auto-populate from the uploaded spec.
    public void createProjectFromOpenApi(String fileName, String projectName, boolean submit) {
        methodOpenApi.click();
        openApiUpload.setInputFiles(TestDataUtil.getFilePathFromResources(fileName));
        if (projectName != null && !projectName.isEmpty()) {
            typeProjectName(projectName);
        }
        if (submit) {
            submitBtn.click();
        }
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
        cancelBtn.click();
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