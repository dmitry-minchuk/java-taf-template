package domain.ui.webstudio.components.common;

import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import configuration.driver.DriverPool;
import domain.ui.webstudio.components.createnewproject.ExcelFilesComponent;
import domain.ui.webstudio.components.createnewproject.OpenApiComponent;
import domain.ui.webstudio.components.createnewproject.TemplateTabComponent;
import domain.ui.webstudio.components.createnewproject.ZipArchiveComponent;
import helpers.utils.TestDataUtil;
import helpers.utils.WaitUtil;
import lombok.Getter;

public class CreateNewProjectComponent extends BaseComponent {

    private static final int BRANCH_FIELD_PROBE_MS = DEFAULT_TIMEOUT_MS / 5;
    private static final int METHOD_STEP_PROBE_MS = 3000;
    private static final int METHOD_STEP_RETRY_TIMEOUT_MS = DEFAULT_TIMEOUT_MS * 2;

    private ExcelFilesComponent excelFilesComponent;
    private ZipArchiveComponent zipArchiveComponent;
    @Getter
    private TemplateTabComponent templateTabComponent;
    private OpenApiComponent openApiComponent;

    private WebElement tabTemplate;
    private WebElement closeDialogBtn;

    private WebElement methodTemplate;
    private WebElement methodExcel;
    private WebElement methodArchive;
    private WebElement cancelBtn;
    private WebElement submitBtn;
    private WebElement nameField;
    private WebElement templateGroups;
    private WebElement templateGroup;
    private WebElement templateItem;
    private WebElement excelUpload;
    private WebElement archiveUpload;
    private WebElement methodOpenApi;
    private WebElement openApiUpload;
    private WebElement openApiDataModuleField;
    private WebElement openApiDataPathField;
    private WebElement openApiRulesModuleField;
    private WebElement openApiRulesPathField;
    private WebElement openApiUploadedFileRemoveBtn;
    private java.util.List<WebElement> openApiUploadedFiles;
    private WebElement openApiError;
    private WebElement branchField;
    private WebElement repoSelect;
    private WebElement repoOption;
    private WebElement pathField;

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
        openApiComponent = createScopedComponent(OpenApiComponent.class, "xpath=.//form[@name='openAPIProjectForm']", "openApiComponent");
        closeDialogBtn = createScopedElement("xpath=.//img[@class='close']", "closeDialogBtn");

        methodTemplate = new WebElement(DriverPool.getPage(), "[data-testid=new-project-method-template]", "methodTemplate");
        methodExcel = new WebElement(DriverPool.getPage(), "[data-testid=new-project-method-excel]", "methodExcel");
        methodArchive = new WebElement(DriverPool.getPage(), "[data-testid=new-project-method-archive]", "methodArchive");
        cancelBtn = new WebElement(DriverPool.getPage(), "[data-testid=new-project-cancel]", "newProjectCancel");
        submitBtn = new WebElement(DriverPool.getPage(), "[data-testid=new-project-submit]", "newProjectSubmit");
        nameField = new WebElement(DriverPool.getPage(), "[data-testid=new-project-name]", "newProjectName");
        templateGroups = new WebElement(DriverPool.getPage(), "[data-testid=new-project-template-groups]", "templateGroups");
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
        openApiUploadedFileRemoveBtn = new WebElement(DriverPool.getPage(), "xpath=//span[contains(@class,'ant-upload-list-item-actions')]//button", "openApiRemoveFile");
        openApiUploadedFiles = createElementList("xpath=//div[contains(@class,'ant-upload-list-item')]", "openApiUploadedFiles");
        openApiError = new WebElement(DriverPool.getPage(), "[data-testid=new-project-error]", "newProjectError");
        repoSelect = new WebElement(DriverPool.getPage(), "[data-testid=new-project-repo]", "newProjectRepo");
        branchField = new WebElement(DriverPool.getPage(),
                "css=[data-testid=new-project-branch] input, input[data-testid=new-project-branch]", "newProjectBranch");
        repoOption = new WebElement(DriverPool.getPage(), "xpath=//div[contains(@class,'ant-select-item-option')][.//*[normalize-space(text())='%s'] or @title='%s']", "newProjectRepoOption");
        pathField = new WebElement(DriverPool.getPage(), "[data-testid=new-project-path]", "newProjectPath");
    }

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

    public void clickCreate() {
        clickCreate(true);
    }

    public void clickCreate(boolean expectWizardToClose) {
        waitForBranchToBeOffered();
        submitBtn.click();
        if (expectWizardToClose) {
            submitBtn.waitForHidden(DEFAULT_TIMEOUT_MS);
        } else {
            openApiError.isVisible(DEFAULT_TIMEOUT_MS);
        }
    }

    private void waitForBranchToBeOffered() {
        if (!branchField.isVisible(BRANCH_FIELD_PROBE_MS)) {
            return;
        }
        WaitUtil.waitForCondition(() -> !branchField.getCurrentInputValue().isBlank(),
                DEFAULT_TIMEOUT_MS, 250, "Waiting for the wizard to fill in the branch");
    }

    public void createProjectFromTemplate(String templateName, String projectName) {
        createProjectFromTemplate(templateName, projectName, true);
    }

    public void createProjectFromTemplate(String templateName, String projectName, boolean submit) {
        chooseMethod(methodTemplate, templateGroups, "From template");
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

    private void chooseMethod(WebElement methodCard, WebElement methodStepElement, String methodName) {
        boolean opened = WaitUtil.retryAction(() -> {
            methodCard.click();
            if (!WaitUtil.waitForCondition(methodStepElement::exists, METHOD_STEP_PROBE_MS, 250,
                    "Waiting for the '" + methodName + "' step to render")) {
                throw new IllegalStateException("The '" + methodName + "' step is not rendered yet");
            }
        }, METHOD_STEP_RETRY_TIMEOUT_MS, 250, "Opening the '" + methodName + "' step of the Create project wizard");
        if (!opened) {
            throw new IllegalStateException("The '" + methodName + "' step of the Create project wizard did not open within "
                    + METHOD_STEP_RETRY_TIMEOUT_MS + " ms after clicking its card");
        }
    }

    private static String groupOf(String templateName) {
        if (templateName.startsWith("Example")) return "examples";
        if (templateName.startsWith("Tutorial")) return "tutorials";
        return "templates";
    }

    public void createProjectFromExcel(String excelFileName, String projectName) {
        chooseMethod(methodExcel, excelUpload, "From Excel files");
        excelUpload.setInputFiles(TestDataUtil.getFilePathFromResources(excelFileName));
        if (projectName != null && !projectName.isEmpty()) {
            typeProjectName(projectName);
        }
        submitBtn.click();
    }

    public void createProjectFromZip(String zipFileName, String projectName) {
        chooseMethod(methodArchive, archiveUpload, "From archive");
        archiveUpload.setInputFiles(TestDataUtil.getFilePathFromResources(zipFileName));
        if (projectName != null && !projectName.isEmpty()) {
            typeProjectName(projectName);
        }
        submitBtn.click();
    }

    public CreateNewProjectComponent selectMethod(TabName method) {
        switch (method) {
            case TEMPLATE -> chooseMethod(methodTemplate, templateGroups, "From template");
            case ZIP_ARCHIVE -> chooseMethod(methodArchive, archiveUpload, "From archive");
            case EXCEL_FILES -> chooseMethod(methodExcel, excelUpload, "From Excel files");
            case OPEN_API -> chooseMethod(methodOpenApi, openApiUpload, "From OpenAPI");
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

    public String getError() {
        return openApiError.waitForVisible(DEFAULT_TIMEOUT_MS).getText().trim();
    }

    public boolean hasError() {
        return openApiError.isVisible(DEFAULT_TIMEOUT_MS / 2);
    }

    public void createProjectFromOpenApi(String fileName, String projectName, boolean submit) {
        chooseMethod(methodOpenApi, openApiUpload, "From OpenAPI");
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
        OPEN_API("OpenAPI");

        private final String value;

        TabName(String value) {
            this.value = value;
        }
    }
}
