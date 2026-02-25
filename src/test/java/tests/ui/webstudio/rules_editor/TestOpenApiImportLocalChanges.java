package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.ChangesDialogComponent;
import domain.ui.webstudio.components.editortabcomponents.ImportOpenApiDialogComponent;
import domain.ui.webstudio.components.editortabcomponents.OpenApiModuleSettingsDialogComponent;
import domain.ui.webstudio.components.editortabcomponents.CompareLocalChangesDialogComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.TestDataUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Covered atomic tests (IPBQA-31512):
 *   1-1.3  - Local Changes after Tables Generation re-import for OpenAPI project (2 records in history)
 *   2-2.2  - Local Changes after re-import for template project + Compare window
 *   3      - Local Changes do NOT appear after Reconciliation mode import
 *   4      - No new records when regenerating from same file
 *   5      - New record appears when regenerating from previous file
 */
public class TestOpenApiImportLocalChanges extends BaseTest {

    private static final String OPENAPI_FILE_1 = "openapi1.json";
    private static final String OPENAPI_FILE_2 = "openapi2.json";
    private static final String TEMPLATE_AUTO_POLICY = "Example 3 - Auto Policy Calculation";
    private static final String OPENAPI_RECONCILIATION_WARNING = "OpenAPI Reconciliation: There are no suitable methods to check." +
            " Check the provided rules, annotation template class, and included/excluded methods in module settings.";

    @Test
    @TestCaseId("IPBQA-31512")
    @Description("Steps 1-1.3: Local Changes appear after Tables Generation re-import for OpenAPI project. Restore to older version gives warnings, restore to latest removes them.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testLocalChangesAfterTablesGenerationReImport() {
        String projectName = "TestLocalChanges1_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        // Create project from openapi1.json, upload openapi2.json
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProjectFromOpenApi(OPENAPI_FILE_1, projectName);
        uploadFileToProject(repositoryPage, projectName, OPENAPI_FILE_2, OPENAPI_FILE_2);

        // Tables Generation import openapi2.json with overwrite
        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

        ImportOpenApiDialogComponent importDialog = editorPage.openImportOpenApiDialog();
        importDialog.selectUploadInRepository();
        importDialog.setOpenApiFilePath(OPENAPI_FILE_2);
        importDialog.selectTablesGenerationMode();
        importDialog.clickImportTablesGeneration();

        OpenApiModuleSettingsDialogComponent settingsDialog = editorPage.getOpenApiModuleSettingsDialogComponent();
        settingsDialog.waitForVisible();
        settingsDialog.clickImportAndOverride();
        editorPage.waitUntilSpinnerLoaded();

        // Step 1: Verify Local Changes (1) with 2 rows for Algorithms module
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Algorithms");
        ChangesDialogComponent changesDialog = editorPage.getEditorToolbarPanelComponent().clickMore().clickChanges();

        assertThat(changesDialog.getChangesTitle())
                .as("Changes title should show 1 local change for Algorithms")
                .isEqualTo("Local Changes (1)");
        assertThat(changesDialog.getRowCount())
                .as("Should be 2 rows in history (current + previous)")
                .isEqualTo(2);

        // Verify Models module also has 1 local change with 2 rows
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Models");
        changesDialog = editorPage.getEditorToolbarPanelComponent().clickMore().clickChanges();

        assertThat(changesDialog.getChangesTitle())
                .as("Changes title should show 1 local change for Models")
                .isEqualTo("Local Changes (1)");
        assertThat(changesDialog.getRowCount())
                .as("Should be 2 rows in history (current + previous)")
                .isEqualTo(2);

        // Step 1.2: Restore Algorithms to older version (row 2 = second row = original state)
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Algorithms");
        changesDialog = editorPage.getEditorToolbarPanelComponent().clickMore().clickChanges();
        changesDialog.clickRestoreAtRow(2);
        editorPage.waitUntilSpinnerLoaded();

        // Restore Models to older version
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Models");
        changesDialog = editorPage.getEditorToolbarPanelComponent().clickMore().clickChanges();
        changesDialog.clickRestoreAtRow(2);
        editorPage.waitUntilSpinnerLoaded();

        // After restoring to original, Algorithms should have reconciliation warning but no errors
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Algorithms");
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete();

        assertThat(editorPage.getProblemsPanelComponent().hasErrors())
                .as("Algorithms should have no errors after restoring to original openapi1.json state")
                .isFalse();
        assertThat(editorPage.getProblemsPanelComponent().getAllWarnings())
                .as("Algorithms should have OpenAPI reconciliation warning after restoring to original state")
                .contains(OPENAPI_RECONCILIATION_WARNING);

        // Step 1.3: Restore Algorithms back to latest version (row 1 = first row = tables generation state)
        changesDialog = editorPage.getEditorToolbarPanelComponent().clickMore().clickChanges();
        changesDialog.clickRestoreAtRow(1);
        editorPage.waitUntilSpinnerLoaded();

        // Restore Models back to latest version
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Models");
        changesDialog = editorPage.getEditorToolbarPanelComponent().clickMore().clickChanges();
        changesDialog.clickRestoreAtRow(1);
        editorPage.waitUntilSpinnerLoaded();

        // After restoring to tables generation, no warnings
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Algorithms");
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete();
        editorPage.getProblemsPanelComponent().checkNoProblems();
    }

    @Test
    @TestCaseId("IPBQA-31512")
    @Description("Steps 2-2.2: Local Changes appear after Tables Generation re-import for template project. Restore shows reconciliation warning. Compare window shows changed items.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testLocalChangesAfterReImportForTemplateProject() {
        String projectName = "TestLocalChanges2_" + System.currentTimeMillis();
        String rulesModuleName = "AutoPolicyCalculation";
        String dataModuleName = "AutoPolicyTests";

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        // Create project from Example 3 template, upload openapi2.json
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(
                domain.ui.webstudio.components.common.CreateNewProjectComponent.TabName.TEMPLATE,
                projectName, TEMPLATE_AUTO_POLICY);
        uploadFileToProject(repositoryPage, projectName, OPENAPI_FILE_2, OPENAPI_FILE_2);

        // Tables Generation import openapi2.json with custom module names + overwrite
        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

        ImportOpenApiDialogComponent importDialog = editorPage.openImportOpenApiDialog();
        importDialog.selectUploadInRepository();
        importDialog.setOpenApiFilePath(OPENAPI_FILE_2);
        importDialog.selectTablesGenerationMode();
        importDialog.setRulesModuleName(rulesModuleName);
        importDialog.setDataModuleName(dataModuleName);
        importDialog.clickImportTablesGeneration();

        OpenApiModuleSettingsDialogComponent settingsDialog = editorPage.getOpenApiModuleSettingsDialogComponent();
        settingsDialog.waitForVisible();
        settingsDialog.clickImportAndOverride();
        editorPage.waitUntilSpinnerLoaded();

        // Step 2: Verify Local Changes (1) with 2 rows for AutoPolicyCalculation module
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, rulesModuleName);
        ChangesDialogComponent changesDialog = editorPage.getEditorToolbarPanelComponent().clickMore().clickChanges();

        assertThat(changesDialog.getChangesTitle())
                .as("Changes title should show 1 local change for AutoPolicyCalculation")
                .isEqualTo("Local Changes (1)");
        assertThat(changesDialog.getRowCount())
                .as("Should be 2 rows in history (current + previous)")
                .isEqualTo(2);

        // Verify AutoPolicyTests module also has 1 local change with 2 rows
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, dataModuleName);
        changesDialog = editorPage.getEditorToolbarPanelComponent().clickMore().clickChanges();

        assertThat(changesDialog.getChangesTitle())
                .as("Changes title should show 1 local change for AutoPolicyTests")
                .isEqualTo("Local Changes (1)");
        assertThat(changesDialog.getRowCount())
                .as("Should be 2 rows in history (current + previous)")
                .isEqualTo(2);

        // Step 2.1: Restore AutoPolicyCalculation to older version (row 2) → reconciliation warning
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, rulesModuleName);
        changesDialog = editorPage.getEditorToolbarPanelComponent().clickMore().clickChanges();
        changesDialog.clickRestoreAtRow(2);
        editorPage.waitUntilSpinnerLoaded();

        // Restore AutoPolicyTests to older version
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, dataModuleName);
        changesDialog = editorPage.getEditorToolbarPanelComponent().clickMore().clickChanges();
        changesDialog.clickRestoreAtRow(2);
        editorPage.waitUntilSpinnerLoaded();

        // After restoring to original template state, AutoPolicyCalculation should have reconciliation warning
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, rulesModuleName);
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete();

        assertThat(editorPage.getProblemsPanelComponent().hasErrors())
                .as("AutoPolicyCalculation should have no errors after restoring to original state")
                .isFalse();
        assertThat(editorPage.getProblemsPanelComponent().getAllWarnings())
                .as("AutoPolicyCalculation should have OpenAPI reconciliation warning after restore")
                .contains(OPENAPI_RECONCILIATION_WARNING);

        // Step 2.2: Open Changes → select both rows → click Compare → verify compare popup shows specific table items → close
        changesDialog = editorPage.getEditorToolbarPanelComponent().clickMore().clickChanges();
        changesDialog.setCompareCheckbox(1, true);
        changesDialog.setCompareCheckbox(2, true);
        CompareLocalChangesDialogComponent compareDialog = changesDialog.clickCompare();
        compareDialog.waitForDialogToAppear();

        assertThat(compareDialog.getLeftModulesList())
                .as("Compare window should list all changed tables from the AutoPolicyCalculation module")
                .contains("SpreadsheetResults", "Environment", "Data Table", "Calculation",
                        "Vehicle-Eligibility", "Vehicle-Scoring", "Driver-Eligibility", "Driver-Scoring",
                        "Client-Scoring", "Policy-Eligibility", "Vehicle-Premium", "Driver-Premium",
                        "Policy-Premium", "Domain", "Vocabulary", "Utilities");

        compareDialog.close();
    }

    @Test
    @TestCaseId("IPBQA-31512")
    @Description("Step 3: Local Changes do NOT appear after Reconciliation mode import – 'No changes in history' for both modules")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testNoLocalChangesAfterReconciliationImport() {
        String projectName = "TestLocalChanges3_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        // Create project from openapi1.json, upload openapi2.json
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProjectFromOpenApi(OPENAPI_FILE_1, projectName);
        uploadFileToProject(repositoryPage, projectName, OPENAPI_FILE_2, OPENAPI_FILE_2);

        // Reconciliation import of openapi2.json
        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

        ImportOpenApiDialogComponent importDialog = editorPage.openImportOpenApiDialog();
        importDialog.selectUploadInRepository();
        importDialog.setOpenApiFilePath(OPENAPI_FILE_2);
        importDialog.clickImportReconciliation();
        editorPage.waitUntilSpinnerLoaded();

        // Verify Algorithms has no local changes history
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Algorithms");
        ChangesDialogComponent changesDialog = editorPage.getEditorToolbarPanelComponent().clickMore().clickChanges();

        assertThat(changesDialog.getNoChangesMessage())
                .as("Reconciliation import should not create local changes history for Algorithms")
                .isEqualTo("No changes in history");

        // Verify Models has no local changes history
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Models");
        changesDialog = editorPage.getEditorToolbarPanelComponent().clickMore().clickChanges();

        assertThat(changesDialog.getNoChangesMessage())
                .as("Reconciliation import should not create local changes history for Models")
                .isEqualTo("No changes in history");
    }

    @Test
    @TestCaseId("IPBQA-31512")
    @Description("Step 4: No new local change record when regenerating from a file with the same content as the original")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testNoNewRecordWhenRegeneratingFromSameFile() {
        String projectName = "TestLocalChanges4_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        // Create project from openapi2.json, then upload a copy of openapi2.json with a different name
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProjectFromOpenApi(OPENAPI_FILE_2, projectName);
        // Upload openapi2.json as "openapi2-copy.json" (same content, different filename)
        uploadFileToProject(repositoryPage, projectName, OPENAPI_FILE_2, "openapi2-copy.json");

        // Tables Generation import using the copy (same content)
        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

        ImportOpenApiDialogComponent importDialog = editorPage.openImportOpenApiDialog();
        importDialog.selectUploadInRepository();
        importDialog.setOpenApiFilePath("openapi2-copy.json");
        importDialog.selectTablesGenerationMode();
        importDialog.clickImportTablesGeneration();

        OpenApiModuleSettingsDialogComponent settingsDialog = editorPage.getOpenApiModuleSettingsDialogComponent();
        settingsDialog.waitForVisible();
        settingsDialog.clickImportAndOverride();
        editorPage.waitUntilSpinnerLoaded();

        // Verify Algorithms has Local Changes (0) and no history
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Algorithms");
        ChangesDialogComponent changesDialog = editorPage.getEditorToolbarPanelComponent().clickMore().clickChanges();

        assertThat(changesDialog.getChangesTitle())
                .as("Reimporting same content should not create a new local change record")
                .isEqualTo("Local Changes (0)");
        assertThat(changesDialog.getNoChangesMessage())
                .as("No history entries should exist when content is unchanged")
                .isEqualTo("No changes in history");
    }

    @Test
    @TestCaseId("IPBQA-31512")
    @Description("Step 5: New local change record appears when regenerating from a different file (2 imports → Local Changes (2) with 3 rows)")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testNewRecordWhenRegeneratingFromDifferentFile() {
        String projectName = "TestLocalChanges5_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        // Create project from openapi1.json, upload openapi2.json
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProjectFromOpenApi(OPENAPI_FILE_1, projectName);
        uploadFileToProject(repositoryPage, projectName, OPENAPI_FILE_2, OPENAPI_FILE_2);

        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

        // First Tables Generation import: openapi2.json (creates 1st local change)
        ImportOpenApiDialogComponent importDialog = editorPage.openImportOpenApiDialog();
        importDialog.selectUploadInRepository();
        importDialog.setOpenApiFilePath(OPENAPI_FILE_2);
        importDialog.selectTablesGenerationMode();
        importDialog.clickImportTablesGeneration();

        OpenApiModuleSettingsDialogComponent settingsDialog = editorPage.getOpenApiModuleSettingsDialogComponent();
        settingsDialog.waitForVisible();
        settingsDialog.clickImportAndOverride();
        editorPage.waitUntilSpinnerLoaded();

        // Second Tables Generation import: openapi1.json (creates 2nd local change)
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        importDialog = editorPage.openImportOpenApiDialog();
        importDialog.selectUploadInRepository();
        importDialog.setOpenApiFilePath(OPENAPI_FILE_1);
        importDialog.selectTablesGenerationMode();
        importDialog.clickImportTablesGeneration();

        settingsDialog = editorPage.getOpenApiModuleSettingsDialogComponent();
        settingsDialog.waitForVisible();
        settingsDialog.clickImportAndOverride();
        editorPage.waitUntilSpinnerLoaded();

        // Verify Algorithms has Local Changes (2) with 3 rows in history
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Algorithms");
        ChangesDialogComponent changesDialog = editorPage.getEditorToolbarPanelComponent().clickMore().clickChanges();

        assertThat(changesDialog.getChangesTitle())
                .as("Two different file imports should create 2 local change records")
                .isEqualTo("Local Changes (2)");
        assertThat(changesDialog.getRowCount())
                .as("Should be 3 rows: current + 2 previous versions")
                .isEqualTo(3);
    }

    private void uploadFileToProject(RepositoryPage repositoryPage, String projectName,
                                     String sourceFileName, String targetFileName) {
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickUploadFileBtn();
        repositoryPage.getUploadFileDialogComponent().waitForDialogToAppear();
        repositoryPage.getUploadFileDialogComponent()
                .uploadFile(TestDataUtil.getFilePathFromResources(sourceFileName))
                .setFileName(targetFileName)
                .clickUploadButton();
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickSaveBtn();
        repositoryPage.getSaveChangesComponent().getSaveBtn().click();
        repositoryPage.waitUntilSpinnerLoaded();
    }
}
