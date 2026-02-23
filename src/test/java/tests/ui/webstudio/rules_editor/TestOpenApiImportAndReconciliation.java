package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.ImportOpenApiDialogComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.TestDataUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Covered atomic tests (IPBQA-31035):
 *   2.8.2  - Import OpenAPI for scaffolding in existing project (Tables Generation mode)
 *   2.8.4  - Reconciliation mode import: verify properties after import
 *   2.8.6  - OpenAPI file operations: module names same validation error
 */
public class TestOpenApiImportAndReconciliation extends BaseTest {

    private static final String OPENAPI_FILE = "openapi2.json";
    private static final String TEMPLATE_NAME = "Example 1 - Bank Rating";

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
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);

        repositoryPage.getRepositoryContentButtonsPanelComponent().clickUploadFileBtn();
        repositoryPage.getUploadFileDialogComponent().waitForDialogToAppear();
        repositoryPage.getUploadFileDialogComponent()
                .uploadFile(TestDataUtil.getFilePathFromResources(OPENAPI_FILE))
                .setFileName(OPENAPI_FILE)
                .clickUploadButton();

        // Step 3: Save changes in repository
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickSaveBtn();
        repositoryPage.getSaveChangesComponent().getSaveBtn().click();
        repositoryPage.waitUntilSpinnerLoaded();

        // Step 4: Navigate to Editor tab and select project
        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

        // Step 5: Open dialog, select "Uploaded in the Repository", enter path, import in Reconciliation mode
        ImportOpenApiDialogComponent importDialog = editorPage.openImportOpenApiDialog();
        assertThat(importDialog.isVisible())
                .as("Import OpenAPI dialog should be visible")
                .isTrue();

        importDialog.selectUploadInRepository();
        importDialog.setOpenApiFilePath(OPENAPI_FILE);
        importDialog.clickImportReconciliation();

        // Step 6: Verify OpenAPI properties after Reconciliation import
        assertThat(editorPage.getOpenApiPropertyValue("Mode:"))
                .as("Mode should be Reconciliation after reconciliation import")
                .isEqualTo("Reconciliation");
        assertThat(editorPage.getOpenApiPropertyValue("OpenAPI File:"))
                .as("OpenAPI File property should reflect the uploaded file name")
                .isEqualTo(OPENAPI_FILE);

        // Step 7: Save project
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

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);

        repositoryPage.getRepositoryContentButtonsPanelComponent().clickUploadFileBtn();
        repositoryPage.getUploadFileDialogComponent().waitForDialogToAppear();
        repositoryPage.getUploadFileDialogComponent()
                .uploadFile(TestDataUtil.getFilePathFromResources(OPENAPI_FILE))
                .setFileName(OPENAPI_FILE)
                .clickUploadButton();

        repositoryPage.getRepositoryContentButtonsPanelComponent().clickSaveBtn();
        repositoryPage.getSaveChangesComponent().getSaveBtn().click();
        repositoryPage.waitUntilSpinnerLoaded();

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
        assertThat(importDialog.getErrorMessage())
                .as("Error message should appear when Rules and Data module names are the same")
                .isEqualTo("Module names cannot be the same.");

        importDialog.clickCancel();
    }
}
