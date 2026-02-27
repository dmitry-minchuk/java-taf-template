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
import domain.ui.webstudio.components.editortabcomponents.OpenApiModuleSettingsDialogComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.TestDataUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestImportTablesGenerationForNonOpenApiProject extends BaseTest {

    private static final String OPENAPI_FILE = "openapi2.json";
    private static final String TEMPLATE_NAME = "Example 1 - Bank Rating";

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
