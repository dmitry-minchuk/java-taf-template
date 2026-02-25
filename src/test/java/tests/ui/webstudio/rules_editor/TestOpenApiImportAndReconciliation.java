package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.createnewproject.OpenApiComponent;
import domain.ui.webstudio.components.editortabcomponents.ImportOpenApiDialogComponent;
import domain.ui.webstudio.components.editortabcomponents.OpenApiModuleSettingsDialogComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.TestDataUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Covered atomic tests (IPBQA-31035):
 *   2.8.2  - Import OpenAPI for scaffolding in existing project (Tables Generation mode)
 *   2.8.4  - Reconciliation mode import: verify properties after import
 *   2.8.6  - OpenAPI file operations: module names same validation error
 *   1.1    - Module name retention when switching Reconciliation/Generation modes
 *   3-3.2  - Tables Generation with overwrite warning for existing modules
 *   8-8.2  - Dialog default state for non-OpenAPI project + file not found error
 *   10-10.1- Tables Generation overwrite for existing module (Bank Rating)
 *   12-13  - Duplicate path and same paths validation errors
 */
public class TestOpenApiImportAndReconciliation extends BaseTest {

    private static final String OPENAPI_FILE = "openapi2.json";
    private static final String OPENAPI_FILE_1 = "openapi1.json";
    private static final String OPENAPI_FILE_3 = "openapi3.json";
    private static final String TEMPLATE_NAME = "Example 1 - Bank Rating";
    private static final String TEMPLATE_CORPORATE = "Example 2 - Corporate Rating";

    @Test
    @TestCaseId("IPBQA-31035")
    @Description("Import OpenAPI in Reconciliation mode for project from template: verify OpenAPI properties after import")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testImportOpenApiReconciliationMode() {
        String projectName = "TestOpenApiReconciliation_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        // Step 1: Create project from template
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, projectName, TEMPLATE_NAME);

        // Step 2: Select project in tree and upload openapi2.json
        uploadFileToProject(repositoryPage, projectName, OPENAPI_FILE);

        // Step 3: Navigate to Editor tab and select project
        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

        // Step 4: Open dialog, select "Uploaded in the Repository", enter path, import in Reconciliation mode
        ImportOpenApiDialogComponent importDialog = editorPage.openImportOpenApiDialog();
        assertThat(importDialog.isVisible())
                .as("Import OpenAPI dialog should be visible")
                .isTrue();

        importDialog.selectUploadInRepository();
        importDialog.setOpenApiFilePath(OPENAPI_FILE);
        importDialog.clickImportReconciliation();

        // Step 5: Verify OpenAPI properties after Reconciliation import
        assertThat(editorPage.getOpenApiPropertyValue("Mode:"))
                .as("Mode should be Reconciliation after reconciliation import")
                .isEqualTo("Reconciliation");
        assertThat(editorPage.getOpenApiPropertyValue("OpenAPI File:"))
                .as("OpenAPI File property should reflect the uploaded file name")
                .isEqualTo(OPENAPI_FILE);

        // Step 6: Save project
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();
        editorPage.waitUntilSpinnerLoaded();
    }

    @Test
    @TestCaseId("IPBQA-31035")
    @Description("Import OpenAPI Tables Generation mode: verify same-module-names validation error")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testImportOpenApiModuleNamesValidation() {
        String projectName = "TestOpenApiValidation_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        // Step 1: Create project from template and upload OpenAPI file
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, projectName, TEMPLATE_NAME);
        uploadFileToProject(repositoryPage, projectName, OPENAPI_FILE);

        // Step 2: Navigate to Editor and open Import OpenAPI dialog
        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

        ImportOpenApiDialogComponent importDialog = editorPage.openImportOpenApiDialog();

        // Step 3: Select "Uploaded in Repository", set file, Tables generation, same module names
        importDialog.selectUploadInRepository();
        importDialog.setOpenApiFilePath(OPENAPI_FILE);
        importDialog.selectTablesGenerationMode();
        importDialog.setRulesModuleName("SameModule");
        importDialog.setDataModuleName("SameModule");
        importDialog.clickImportTablesGeneration();

        // Step 4: Verify validation error for same module names
        assertThat(importDialog.getErrorMessages())
                .as("Error message should appear when Rules and Data module names are the same")
                .contains("Module names cannot be the same.");

        importDialog.clickCancel();
    }

    @Test
    @TestCaseId("IPBQA-31035")
    @Description("Verify module names are retained when switching between Reconciliation and Tables Generation modes in Import OpenAPI dialog")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testImportModuleNamesRetentionOnModeSwitching() {
        String projectName = "TestModulesRetention_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        // Create project from openapi1.json with custom module names and custom paths
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getCreateProjectLink().click();
        OpenApiComponent openApiComponent = repositoryPage.getCreateNewProjectComponent()
                .selectTab(CreateNewProjectComponent.TabName.OPEN_API);
        openApiComponent.uploadOpenApiFile(OPENAPI_FILE_1);
        openApiComponent.setDataModuleName("Models_test");
        openApiComponent.clickEditDataPath();
        openApiComponent.setDataModulePath("rules2/Models_test2.xlsx");
        openApiComponent.setRulesModuleName("Algorithms_test");
        openApiComponent.clickEditRulesPath();
        openApiComponent.setRulesModulePath("rules1/Algorithms_test1.xlsx");
        openApiComponent.setProjectName(projectName);
        openApiComponent.clickCreate();
        repositoryPage.fillCommitInfo();
        repositoryPage.waitUntilSpinnerLoaded();
        repositoryPage.getRefreshBtn().click(10000);

        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

        // Step 1.1: Open dialog, switch to Tables Generation mode, verify pre-populated names
        ImportOpenApiDialogComponent importDialog = editorPage.openImportOpenApiDialog();
        importDialog.selectTablesGenerationMode();

        assertThat(importDialog.getRulesModuleName())
                .as("Rules module name should be pre-populated with 'Algorithms_test' from project creation")
                .isEqualTo("Algorithms_test");
        assertThat(importDialog.getDataModuleName())
                .as("Data module name should be pre-populated with 'Models_test' from project creation")
                .isEqualTo("Models_test");

        // Change module names, then switch modes and verify names are retained
        importDialog.setRulesModuleName("Algorithms_test_1");
        importDialog.setDataModuleName("Models_test_1");
        importDialog.selectReconciliationMode();
        importDialog.selectTablesGenerationMode();

        assertThat(importDialog.getRulesModuleName())
                .as("Rules module name should be retained as 'Algorithms_test_1' after Reconciliation/Generation mode switch")
                .isEqualTo("Algorithms_test_1");
        assertThat(importDialog.getDataModuleName())
                .as("Data module name should be retained as 'Models_test_1' after Reconciliation/Generation mode switch")
                .isEqualTo("Models_test_1");

        importDialog.clickCancel();
    }

    @Test
    @TestCaseId("IPBQA-31035")
    @Description("Tables Generation import shows overwrite warning for existing modules; verify warning text, button label, and properties after import")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testImportTablesGenerationOverwriteWarning() {
        String projectName = "TestOpenApiOverwrite_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        // Create project from openapi1.json with custom module names and paths
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getCreateProjectLink().click();
        OpenApiComponent openApiComponent = repositoryPage.getCreateNewProjectComponent()
                .selectTab(CreateNewProjectComponent.TabName.OPEN_API);
        openApiComponent.uploadOpenApiFile(OPENAPI_FILE_1);
        openApiComponent.setDataModuleName("Models_test");
        openApiComponent.clickEditDataPath();
        openApiComponent.setDataModulePath("rules2/Models_test2.xlsx");
        openApiComponent.setRulesModuleName("Algorithms_test");
        openApiComponent.clickEditRulesPath();
        openApiComponent.setRulesModulePath("rules1/Algorithms_test1.xlsx");
        openApiComponent.setProjectName(projectName);
        openApiComponent.clickCreate();
        repositoryPage.fillCommitInfo();
        repositoryPage.waitUntilSpinnerLoaded();
        repositoryPage.getRefreshBtn().click(10000);

        // Step 2: Upload openapi2.json and do Reconciliation import
        uploadFileToProject(repositoryPage, projectName, OPENAPI_FILE);

        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);
        ImportOpenApiDialogComponent importDialog = editorPage.openImportOpenApiDialog();
        importDialog.selectUploadInRepository();
        importDialog.setOpenApiFilePath(OPENAPI_FILE);
        importDialog.clickImportReconciliation();
        editorPage.waitUntilSpinnerLoaded();

        // Navigate to Algorithms_test module to allow compilation to complete
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Algorithms_test");
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete();

        // Step 3: Navigate back to project, open dialog in Tables Generation mode, trigger overwrite warning
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        importDialog = editorPage.openImportOpenApiDialog();
        importDialog.selectTablesGenerationMode();
        importDialog.clickImportTablesGeneration();

        OpenApiModuleSettingsDialogComponent settingsDialog = editorPage.getOpenApiModuleSettingsDialogComponent();
        settingsDialog.waitForVisible();

        assertThat(settingsDialog.getContentText())
                .as("Warning dialog should list both Algorithms_test and Models_test as modules to overwrite")
                .contains("Warning! The following module already exists and all of its content is going to be overwritten.\n" +
                        "Rules Module: Algorithms_test\n" +
                        "rules1/Algorithms_test1.xlsx\n" +
                        "Warning! The following module already exists and all of its content is going to be overwritten.\n" +
                        "Data Module: Models_test\n" +
                        "rules2/Models_test2.xlsx");
        assertThat(settingsDialog.getImportButtonText())
                .as("Import button should say 'Import and overwrite' when existing modules would be overwritten")
                .isEqualTo("Import and overwrite");
        assertThat(settingsDialog.isVisible())
                .as("Settings dialog with Cancel button should be visible")
                .isTrue();

        // Step 3.1: Cancel, re-open settings dialog, confirm import with overwrite
        settingsDialog.clickCancel();
        importDialog.selectTablesGenerationMode();
        importDialog.clickImportTablesGeneration();
        editorPage.getOpenApiModuleSettingsDialogComponent().waitForVisible();
        editorPage.getOpenApiModuleSettingsDialogComponent().clickImportAndOverride();
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Algorithms_test");
        editorPage.getProblemsPanelComponent().checkNoProblems();

        // Step 3.2: Verify OpenAPI properties after Tables Generation import
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        assertThat(editorPage.getOpenApiPropertyValue("Mode:"))
                .as("Mode should be 'Tables generation' after Tables Generation import with overwrite")
                .isEqualTo("Tables generation");
        assertThat(editorPage.getOpenApiPropertyValue("OpenAPI File:"))
                .as("OpenAPI File should be 'openapi2.json'")
                .isEqualTo(OPENAPI_FILE);
        assertThat(editorPage.getOpenApiPropertyValue("Rules Module:"))
                .as("Rules Module should remain 'Algorithms_test'")
                .isEqualTo("Algorithms_test");
        assertThat(editorPage.getOpenApiPropertyValue("Data Module:"))
                .as("Data Module should remain 'Models_test'")
                .isEqualTo("Models_test");
    }

    @Test
    @TestCaseId("IPBQA-31035")
    @Description("Import OpenAPI dialog default state for non-OpenAPI project: Reconciliation is default, file-not-found error shown on import")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testImportOpenApiDialogDefaultStateForNonOpenApiProject() {
        String projectName = "TestNonOpenApi_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, projectName, TEMPLATE_NAME);

        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

        // Step 8: Open dialog and immediately cancel
        ImportOpenApiDialogComponent importDialog = editorPage.openImportOpenApiDialog();
        importDialog.clickCancel();

        // Step 8.1: Reopen dialog, verify Reconciliation mode is selected by default
        importDialog = editorPage.openImportOpenApiDialog();
        assertThat(importDialog.isVisible())
                .as("Import OpenAPI dialog should be visible")
                .isTrue();
        assertThat(importDialog.isReconciliationModeSelected())
                .as("Reconciliation mode should be selected by default for a non-OpenAPI project")
                .isTrue();
        importDialog.selectUploadInRepository();
        importDialog.selectTablesGenerationMode();

        // Step 8.2: Enter non-existent file path and verify file-not-found error
        importDialog.setOpenApiFilePath("openapi_ex3_auto_r.json");
        importDialog.clickImportTablesGeneration();

        assertThat(importDialog.getAnyErrorMessage())
                .as("Error should indicate the OpenAPI file was not found in the repository")
                .isEqualTo("OpenAPI file with path: openapi_ex3_auto_r.json was not found.");

        importDialog.clickCancel();
    }

    @Test
    @TestCaseId("IPBQA-31035")
    @Description("Tables Generation import for non-OpenAPI project: overwrite existing module and create new Data module; verify module list and properties")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testImportTablesGenerationForNonOpenApiProject() {
        String projectName = "TestNonOpenApiGen_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        // Setup: Create Bank Rating template project and upload openapi2.json
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, projectName, TEMPLATE_NAME);
        uploadFileToProject(repositoryPage, projectName, OPENAPI_FILE);

        // Step 9: Reconciliation import of openapi2.json
        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);
        ImportOpenApiDialogComponent importDialog = editorPage.openImportOpenApiDialog();
        importDialog.selectUploadInRepository();
        importDialog.setOpenApiFilePath(OPENAPI_FILE);
        importDialog.clickImportReconciliation();
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();
        editorPage.waitUntilSpinnerLoaded();

        assertThat(editorPage.getOpenApiPropertyValue("Mode:"))
                .as("Mode should be 'Reconciliation' after reconciliation import")
                .isEqualTo("Reconciliation");
        assertThat(editorPage.getOpenApiPropertyValue("OpenAPI File:"))
                .as("OpenAPI File should be 'openapi2.json'")
                .isEqualTo(OPENAPI_FILE);

        // Step 9: Navigate to Bank Rating module — reconciliation errors must be present
        // openapi2.json doesn't match Bank Rating rules, so errors are expected
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Bank Rating");
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete();
        assertThat(editorPage.getProblemsPanelComponent().hasErrors())
                .as("Bank Rating module should have reconciliation errors after openapi2.json Reconciliation import")
                .isTrue();

        // Step 10: Navigate back to project, Tables Generation import with Bank Rating as Rules module
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        importDialog = editorPage.openImportOpenApiDialog();
        importDialog.selectTablesGenerationMode();
        importDialog.setRulesModuleName("Bank Rating");
        importDialog.clickImportTablesGeneration();

        OpenApiModuleSettingsDialogComponent settingsDialog = editorPage.getOpenApiModuleSettingsDialogComponent();
        settingsDialog.waitForVisible();
        assertThat(settingsDialog.getContentText())
                .as("Warning should show Bank Rating overwrite and Models creation")
                .contains("Warning! The following module already exists and all of its content is going to be overwritten.\n" +
                        "Rules Module: Bank Rating\n" +
                        "Bank Rating.xlsx")
                .contains("The following module doesn't exist and is going to be created:\n" +
                        "Data Module: Models\n" +
                        "rules/Models.xlsx");
        settingsDialog.clickImportAndOverride();

        // Verify module list: Models and Bank Rating present, Algorithms absent
        List<String> modules = editorPage.getEditorLeftProjectModuleSelectorComponent().getAllModuleNames(projectName);
        assertThat(modules)
                .as("Models module should be present after Tables Generation import")
                .contains("Models");
        assertThat(modules)
                .as("Bank Rating module should be present after overwrite import")
                .contains("Bank Rating");
        assertThat(modules)
                .as("Algorithms module should not be present (was never created in this project)")
                .doesNotContain("Algorithms");

        assertThat(editorPage.getOpenApiPropertyValue("Mode:"))
                .as("Mode should be 'Tables generation'")
                .isEqualTo("Tables generation");
        assertThat(editorPage.getOpenApiPropertyValue("OpenAPI File:"))
                .as("OpenAPI File should be 'openapi2.json'")
                .isEqualTo(OPENAPI_FILE);
        assertThat(editorPage.getOpenApiPropertyValue("Rules Module:"))
                .as("Rules Module should be 'Bank Rating'")
                .isEqualTo("Bank Rating");
        assertThat(editorPage.getOpenApiPropertyValue("Data Module:"))
                .as("Data Module should be 'Models'")
                .isEqualTo("Models");

        // Step 10.1: Save project and verify no problems in each new module
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Bank Rating");
        editorPage.getProblemsPanelComponent().checkNoProblems();

        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Models");
        editorPage.getProblemsPanelComponent().checkNoProblems();
    }

    @Test
    @TestCaseId("IPBQA-31035")
    @Description("Path validation in OpenAPI Module Settings dialog: duplicate path error and same-paths error")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testImportPathValidationErrors() {
        String projectName = "TestPathValidation_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        // Setup: Create Bank Rating project, upload openapi2.json, do Reconciliation import
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, projectName, TEMPLATE_NAME);
        uploadFileToProject(repositoryPage, projectName, OPENAPI_FILE);

        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);
        ImportOpenApiDialogComponent importDialog = editorPage.openImportOpenApiDialog();
        importDialog.selectUploadInRepository();
        importDialog.setOpenApiFilePath(OPENAPI_FILE);
        importDialog.clickImportReconciliation();
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();
        editorPage.waitUntilSpinnerLoaded();

        // Setup: Tables Generation import with Bank Rating rules module to create Models module (needed for path validation)
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Bank Rating");
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete();
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        importDialog = editorPage.openImportOpenApiDialog();
        importDialog.selectTablesGenerationMode();
        importDialog.setRulesModuleName("Bank Rating");
        importDialog.clickImportTablesGeneration();
        editorPage.getOpenApiModuleSettingsDialogComponent().waitForVisible();
        editorPage.getOpenApiModuleSettingsDialogComponent().clickImportAndOverride();
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();
        editorPage.waitUntilSpinnerLoaded();

        // Now: Bank Rating.xlsx and rules/Models.xlsx both exist in the project

        // Step 12: Try to import with paths pointing to already-existing files
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        importDialog = editorPage.openImportOpenApiDialog();
        importDialog.selectTablesGenerationMode();
        importDialog.setRulesModuleName("Alg");
        importDialog.setDataModuleName("Mod");
        importDialog.clickImportTablesGeneration();

        OpenApiModuleSettingsDialogComponent settingsDialog = editorPage.getOpenApiModuleSettingsDialogComponent();
        settingsDialog.waitForVisible();
        settingsDialog.clickEditRulesPath();
        settingsDialog.setNewRulesPath("Bank Rating.xlsx");
        settingsDialog.clickEditDataPath();
        settingsDialog.setNewDataPath("rules/Models.xlsx");
        settingsDialog.clickImportAndOverride();

        assertThat(settingsDialog.getErrorMessages())
                .as("Error should indicate a file with this name already exists in the project")
                .contains("File with such name already exists.");

        // Step 13: Change both paths to the same value and verify same-paths error
        settingsDialog.setNewRulesPath("aaa.xlsx");
        settingsDialog.setNewDataPath("aaa.xlsx");
        settingsDialog.clickImportAndOverride();

        assertThat(settingsDialog.getErrorMessages())
                .as("Error should indicate module paths cannot be the same")
                .contains("Module paths cannot be the same");

        settingsDialog.clickCancel();
        importDialog.clickCancel();
    }

    @Test
    @TestCaseId("IPBQA-31035")
    @Description("Steps 4-5.2: Import new modules with path editing/reset, module names retained after cancel, mixed new/existing modules scenario")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testImportNewModulesWithPathEditingAndMixedScenarios() {
        String projectName = "TestNewModulesPath_" + System.currentTimeMillis();
        String moduleName = "Mod-123";

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        // Setup: create project from openapi1.json with custom module names and paths
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getCreateProjectLink().click();
        OpenApiComponent openApiComponent = repositoryPage.getCreateNewProjectComponent()
                .selectTab(CreateNewProjectComponent.TabName.OPEN_API);
        openApiComponent.uploadOpenApiFile(OPENAPI_FILE_1);
        openApiComponent.setDataModuleName("Models_test");
        openApiComponent.clickEditDataPath();
        openApiComponent.setDataModulePath("rules2/Models_test2.xlsx");
        openApiComponent.setRulesModuleName("Algorithms_test");
        openApiComponent.clickEditRulesPath();
        openApiComponent.setRulesModulePath("rules1/Algorithms_test1.xlsx");
        openApiComponent.setProjectName(projectName);
        openApiComponent.clickCreate();
        repositoryPage.fillCommitInfo();
        repositoryPage.waitUntilSpinnerLoaded();
        repositoryPage.getRefreshBtn().click(10000);

        // Upload openapi2.json for later use in step 5
        uploadFileToProject(repositoryPage, projectName, OPENAPI_FILE);

        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

        // Reconciliation import of openapi2.json to establish module state
        ImportOpenApiDialogComponent importDialog = editorPage.openImportOpenApiDialog();
        importDialog.selectUploadInRepository();
        importDialog.setOpenApiFilePath(OPENAPI_FILE);
        importDialog.clickImportReconciliation();
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Algorithms_test");
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete();

        // Tables Generation overwrite to confirm paths (step 3.1 equivalent)
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        importDialog = editorPage.openImportOpenApiDialog();
        importDialog.selectTablesGenerationMode();
        importDialog.clickImportTablesGeneration();
        OpenApiModuleSettingsDialogComponent settingsDialog = editorPage.getOpenApiModuleSettingsDialogComponent();
        settingsDialog.waitForVisible();
        settingsDialog.clickImportAndOverride();
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();
        editorPage.waitUntilSpinnerLoaded();

        // === Step 4: Import with new modules Alg and Mod-123 from openapi1.json ===
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        importDialog = editorPage.openImportOpenApiDialog();
        importDialog.selectUploadInRepository();
        importDialog.setOpenApiFilePath(OPENAPI_FILE_1);
        importDialog.selectTablesGenerationMode();
        importDialog.setRulesModuleName("Alg");
        importDialog.setDataModuleName(moduleName);
        importDialog.clickImportTablesGeneration();

        settingsDialog = editorPage.getOpenApiModuleSettingsDialogComponent();
        settingsDialog.waitForVisible();

        assertThat(settingsDialog.getContentText())
                .as("Warning should show that both Alg and Mod-123 will be created as new modules")
                .contains("The following module doesn't exist and is going to be created:\n" +
                        "Rules Module: Alg\n" +
                        "rules/Alg.xlsx")
                .contains(String.format("The following module doesn't exist and is going to be created:\n" +
                        "Data Module: %s\n" +
                        "rules/%s.xlsx", moduleName, moduleName));
        assertThat(settingsDialog.getImportButtonText())
                .as("Import button should say 'Import' when only creating new modules")
                .isEqualTo("Import");

        // === Step 4.1: Edit rules path, then reset and verify default restored ===
        settingsDialog.clickEditRulesPath();
        settingsDialog.setNewRulesPath("rules/Alg12.xlsx");
        settingsDialog.clickResetRulesPath();

        assertThat(settingsDialog.isNewRulesPathInputVisible())
                .as("Rules path input should disappear after reset")
                .isFalse();
        assertThat(settingsDialog.getRulesPathDisplayValue())
                .as("Rules path display should revert to default 'rules/Alg.xlsx' after reset")
                .isEqualTo("rules/Alg.xlsx");

        settingsDialog.clickEditDataPath();
        settingsDialog.setNewDataPath("rules1/Mod1.xlsx");
        settingsDialog.clickResetDataPath();

        assertThat(settingsDialog.isNewDataPathInputVisible())
                .as("Data path input should disappear after reset")
                .isFalse();
        assertThat(settingsDialog.getDataPathDisplayValue())
                .as("Data path display should revert to default after reset")
                .isEqualTo(String.format("rules/%s.xlsx", moduleName));

        // === Step 4.2: Cancel, re-open, verify module names retained, set custom paths ===
        settingsDialog.clickCancel();
        importDialog.selectTablesGenerationMode();

        assertThat(importDialog.getRulesModuleName())
                .as("Rules module name 'Alg' should be retained after cancel")
                .isEqualTo("Alg");
        assertThat(importDialog.getDataModuleName())
                .as("Data module name 'Mod-123' should be retained after cancel")
                .isEqualTo(moduleName);

        importDialog.clickImportTablesGeneration();
        settingsDialog = editorPage.getOpenApiModuleSettingsDialogComponent();
        settingsDialog.waitForVisible();

        settingsDialog.clickEditRulesPath();
        settingsDialog.setNewRulesPath("rules/Alg12.xlsx");
        settingsDialog.clickEditDataPath();
        settingsDialog.setNewDataPath("rules1/Mod1.xlsx");

        // === Step 4.3: Import and verify module list and OpenAPI properties ===
        settingsDialog.clickImportAndOverride();
        editorPage.waitUntilSpinnerLoaded();

        List<String> modules = editorPage.getEditorLeftProjectModuleSelectorComponent().getAllModuleNames(projectName);
        assertThat(modules).as("Algorithms_test should still be present").contains("Algorithms_test");
        assertThat(modules).as("Models_test should still be present").contains("Models_test");
        assertThat(modules).as("Alg should be created").contains("Alg");
        assertThat(modules).as("Mod-123 should be created").contains(moduleName);

        assertThat(editorPage.getOpenApiPropertyValue("Mode:")).isEqualTo("Tables generation");
        assertThat(editorPage.getOpenApiPropertyValue("OpenAPI File:")).isEqualTo(OPENAPI_FILE_1);
        assertThat(editorPage.getOpenApiPropertyValue("Rules Module:")).isEqualTo("Alg");
        assertThat(editorPage.getOpenApiPropertyValue("Data Module:")).isEqualTo(moduleName);

        // === Step 5: Import with openapi2.json – new rules Alg1 + existing data Mod-123 ===
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        importDialog = editorPage.openImportOpenApiDialog();
        importDialog.selectUploadInRepository();
        importDialog.setOpenApiFilePath(OPENAPI_FILE);
        importDialog.selectTablesGenerationMode();
        importDialog.setRulesModuleName("Alg1");
        importDialog.clickImportTablesGeneration();

        settingsDialog = editorPage.getOpenApiModuleSettingsDialogComponent();
        settingsDialog.waitForVisible();

        assertThat(settingsDialog.getContentText())
                .as("Alg1 should be created (new), Mod-123 should be overwritten (existing at custom path)")
                .contains("The following module doesn't exist and is going to be created:\n" +
                        "Rules Module: Alg1\n" +
                        "rules/Alg1.xlsx")
                .contains(String.format("Warning! The following module already exists and all of its content is going to be overwritten.\n" +
                        "Data Module: %s\n" +
                        "rules1/Mod1.xlsx", moduleName));
        assertThat(settingsDialog.getImportButtonText())
                .as("Import button should say 'Import and overwrite' for mixed scenario")
                .isEqualTo("Import and overwrite");

        // === Step 5.1: Cancel, then import Alg (existing) + Mod1 (new) with custom data path ===
        settingsDialog.clickCancel();
        importDialog.selectTablesGenerationMode();
        importDialog.setRulesModuleName("Alg");
        importDialog.setDataModuleName("Mod1");
        importDialog.clickImportTablesGeneration();

        settingsDialog = editorPage.getOpenApiModuleSettingsDialogComponent();
        settingsDialog.waitForVisible();

        assertThat(settingsDialog.getContentText())
                .as("Alg should be overwritten (existing at rules/Alg12.xlsx), Mod1 should be created (new)")
                .contains("Warning! The following module already exists and all of its content is going to be overwritten.\n" +
                        "Rules Module: Alg\n" +
                        "rules/Alg12.xlsx")
                .contains("The following module doesn't exist and is going to be created:\n" +
                        "Data Module: Mod1\n" +
                        "rules/Mod1.xlsx");

        settingsDialog.clickEditDataPath();
        settingsDialog.setNewDataPath("rules/Mod5.xlsx");

        // === Step 5.2: Import and verify final module list and properties ===
        settingsDialog.clickImportAndOverride();
        editorPage.waitUntilSpinnerLoaded();

        modules = editorPage.getEditorLeftProjectModuleSelectorComponent().getAllModuleNames(projectName);
        assertThat(modules).as("Alg should remain in module list").contains("Alg");
        assertThat(modules).as("Mod-123 should remain in module list").contains(moduleName);
        assertThat(modules).as("Mod1 should be newly created").contains("Mod1");

        assertThat(editorPage.getOpenApiPropertyValue("Mode:")).isEqualTo("Tables generation");
        assertThat(editorPage.getOpenApiPropertyValue("OpenAPI File:")).isEqualTo(OPENAPI_FILE);
        assertThat(editorPage.getOpenApiPropertyValue("Rules Module:")).isEqualTo("Alg");
        assertThat(editorPage.getOpenApiPropertyValue("Data Module:")).isEqualTo("Mod1");
    }

    @Test
    @TestCaseId("IPBQA-31035")
    @Description("Steps 6-6.3: Project created from openapi3.json – cycle through Reconciliation and Tables Generation modes with different files")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testImportCycleThroughModesForOpenApiProject() {
        String projectName = "TestCycleModes_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProjectFromOpenApi(OPENAPI_FILE_3, projectName);

        // Step 6: Verify initial OpenAPI properties after creation from openapi3.json
        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

        assertThat(editorPage.getOpenApiPropertyValue("Mode:")).isEqualTo("Tables generation");
        assertThat(editorPage.getOpenApiPropertyValue("OpenAPI File:")).isEqualTo(OPENAPI_FILE_3);
        assertThat(editorPage.getOpenApiPropertyValue("Rules Module:")).isEqualTo("Algorithms");
        assertThat(editorPage.getOpenApiPropertyValue("Data Module:")).isEqualTo("Models");

        // Step 6.1: Switch to Reconciliation mode import (using Generate from Rules, no file path needed)
        ImportOpenApiDialogComponent importDialog = editorPage.openImportOpenApiDialog();
        importDialog.clickImportReconciliation();
        editorPage.waitUntilSpinnerLoaded();
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);

        assertThat(editorPage.getOpenApiPropertyValue("Mode:")).isEqualTo("Reconciliation");
        assertThat(editorPage.getOpenApiPropertyValue("OpenAPI File:")).isEqualTo(OPENAPI_FILE_3);

        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();
        editorPage.waitUntilSpinnerLoaded();

        // Upload openapi2.json to repository
        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        uploadFileToProject(repositoryPage, projectName, OPENAPI_FILE);

        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

        importDialog = editorPage.openImportOpenApiDialog();
        importDialog.selectUploadInRepository();
        importDialog.setOpenApiFilePath(OPENAPI_FILE);
        importDialog.selectTablesGenerationMode();
        importDialog.clickImportTablesGeneration();

        OpenApiModuleSettingsDialogComponent settingsDialog = editorPage.getOpenApiModuleSettingsDialogComponent();
        settingsDialog.waitForVisible();

        assertThat(settingsDialog.getContentText())
                .as("Both Algorithms and Models modules should be overwritten")
                .contains("Warning! The following module already exists and all of its content is going to be overwritten.\n" +
                        "Rules Module: Algorithms\n" +
                        "rules/Algorithms.xlsx")
                .contains("Warning! The following module already exists and all of its content is going to be overwritten.\n" +
                        "Data Module: Models\n" +
                        "rules/Models.xlsx");
        assertThat(settingsDialog.getImportButtonText()).isEqualTo("Import and overwrite");

        // Step 6.2: Import and override, save, verify properties and modules
        settingsDialog.clickImportAndOverride();
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();
        editorPage.waitUntilSpinnerLoaded();
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);

        assertThat(editorPage.getOpenApiPropertyValue("Mode:")).isEqualTo("Tables generation");
        assertThat(editorPage.getOpenApiPropertyValue("OpenAPI File:")).isEqualTo(OPENAPI_FILE);
        assertThat(editorPage.getOpenApiPropertyValue("Rules Module:")).isEqualTo("Algorithms");
        assertThat(editorPage.getOpenApiPropertyValue("Data Module:")).isEqualTo("Models");

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Algorithms");
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete();
        editorPage.getProblemsPanelComponent().checkNoProblems();

        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Models");
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete();
        editorPage.getProblemsPanelComponent().checkNoProblems();

        // Step 6.3: Import openapi3.json in Tables Generation mode, verify Algorithms has Spreadsheet+Configuration
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        importDialog = editorPage.openImportOpenApiDialog();
        importDialog.selectUploadInRepository();
        importDialog.setOpenApiFilePath(OPENAPI_FILE_3);
        importDialog.selectTablesGenerationMode();
        importDialog.clickImportTablesGeneration();

        settingsDialog = editorPage.getOpenApiModuleSettingsDialogComponent();
        settingsDialog.waitForVisible();
        settingsDialog.clickImportAndOverride();

        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "Algorithms");
        editorPage.getProblemsPanelComponent()
                .waitForCompilationToComplete();
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE);

        assertThat(editorPage.getEditorLeftRulesTreeComponent().isFolderExistsInTree("Spreadsheet"))
                .as("Algorithms should contain Spreadsheet tables after openapi3.json import")
                .isTrue();
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isFolderExistsInTree("Configuration"))
                .as("Algorithms should contain Configuration tables after openapi3.json import")
                .isTrue();
        editorPage.getProblemsPanelComponent().checkNoProblems();
    }

    @Test
    @TestCaseId("IPBQA-31035")
    @Description("Step 7: Project created from openapi2.json, import openapi1.json via Tables Generation – verify no problems in both modules")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testImportForProjectCreatedFromTwoOpenApiFiles() {
        String projectName = "TestTwoFiles_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProjectFromOpenApi(OPENAPI_FILE, projectName);

        uploadFileToProject(repositoryPage, projectName, OPENAPI_FILE_1);

        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

        ImportOpenApiDialogComponent importDialog = editorPage.openImportOpenApiDialog();
        importDialog.selectUploadInRepository();
        importDialog.setOpenApiFilePath(OPENAPI_FILE_1);
        importDialog.selectTablesGenerationMode();
        importDialog.clickImportTablesGeneration();

        OpenApiModuleSettingsDialogComponent settingsDialog = editorPage.getOpenApiModuleSettingsDialogComponent();
        settingsDialog.waitForVisible();
        settingsDialog.clickImportAndOverride();

        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Algorithms");
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete();
        editorPage.getProblemsPanelComponent().checkNoProblems();

        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Models");
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete();
        editorPage.getProblemsPanelComponent().checkNoProblems();
    }

    @Test
    @TestCaseId("IPBQA-31035")
    @Description("Step 14: Tables Generation import for Corporate Rating template project with openapi2.json – creates Algorithms and Models modules")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testImportTablesGenerationForCorporateRatingProject() {
        String projectName = "TestCorporate_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, projectName, TEMPLATE_CORPORATE);
        uploadFileToProject(repositoryPage, projectName, OPENAPI_FILE);

        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

        ImportOpenApiDialogComponent importDialog = editorPage.openImportOpenApiDialog();
        importDialog.selectUploadInRepository();
        importDialog.setOpenApiFilePath(OPENAPI_FILE);
        importDialog.selectTablesGenerationMode();
        importDialog.clickImportTablesGeneration();

        OpenApiModuleSettingsDialogComponent settingsDialog = editorPage.getOpenApiModuleSettingsDialogComponent();
        settingsDialog.waitForVisible();

        assertThat(settingsDialog.getContentText())
                .as("Algorithms and Models should be created as new modules (Corporate Rating does not have them)")
                .contains("The following module doesn't exist and is going to be created:\n" +
                        "Rules Module: Algorithms\n" +
                        "rules/Algorithms.xlsx")
                .contains("The following module doesn't exist and is going to be created:\n" +
                        "Data Module: Models\n" +
                        "rules/Models.xlsx");
        assertThat(settingsDialog.getImportButtonText()).isEqualTo("Import");

        settingsDialog.clickImportAndOverride();
        editorPage.waitUntilSpinnerLoaded();
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);

        // Step 14.1: verify module list
        List<String> modules = editorPage.getEditorLeftProjectModuleSelectorComponent().getAllModuleNames(projectName);
        assertThat(modules).as("Models module should be created").contains("Models");
        assertThat(modules).as("Corporate Rating should still be present").contains("Corporate Rating");
        assertThat(modules).as("Algorithms module should be created").contains("Algorithms");

        assertThat(editorPage.getOpenApiPropertyValue("Mode:")).isEqualTo("Tables generation");
        assertThat(editorPage.getOpenApiPropertyValue("OpenAPI File:")).isEqualTo(OPENAPI_FILE);
        assertThat(editorPage.getOpenApiPropertyValue("Rules Module:")).isEqualTo("Algorithms");
        assertThat(editorPage.getOpenApiPropertyValue("Data Module:")).isEqualTo("Models");

        // Verify Algorithms module content and compilation
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Algorithms");
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete();
        editorPage.getEditorLeftRulesTreeComponent().setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE);

        assertThat(editorPage.getEditorLeftRulesTreeComponent().isFolderExistsInTree("Spreadsheet"))
                .as("Algorithms should contain Spreadsheet tables").isTrue();
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isFolderExistsInTree("Configuration"))
                .as("Algorithms should contain Configuration tables").isTrue();
    }

    private void uploadFileToProject(RepositoryPage repositoryPage, String projectName, String fileName) {
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickUploadFileBtn();
        repositoryPage.getUploadFileDialogComponent().waitForDialogToAppear();
        repositoryPage.getUploadFileDialogComponent()
                .uploadFile(TestDataUtil.getFilePathFromResources(fileName))
                .setFileName(fileName)
                .clickUploadButton();
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickSaveBtn();
        repositoryPage.getSaveChangesComponent().getSaveBtn().click();
        repositoryPage.waitUntilSpinnerLoaded();
    }
}
