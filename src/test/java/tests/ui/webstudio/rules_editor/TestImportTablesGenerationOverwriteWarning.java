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
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.TestDataUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestImportTablesGenerationOverwriteWarning extends BaseTest {

    private static final String OPENAPI_FILE = "openapi2.json";
    private static final String OPENAPI_FILE_1 = "openapi1.json";

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
