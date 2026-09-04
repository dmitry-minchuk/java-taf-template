package tests.ui.webstudio.rules_editor;

import configuration.annotations.KnownIssue;
import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.ChangesDialogComponent;
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

public class TestLocalChangesAfterTablesGenerationReImport extends BaseTest {

    private static final String OPENAPI_FILE_1 = "openapi1.json";
    private static final String OPENAPI_FILE_2 = "openapi2.json";
    private static final String OPENAPI_RECONCILIATION_WARNING = "OpenAPI Reconciliation: There are no suitable methods to check." +
            " Check the provided rules, annotation template class, and included/excluded methods in module settings.";

    @Test
    @TestCaseId("IPBQA-31512")
    @Description("Steps 1-1.3: Local Changes appear after Tables Generation re-import for OpenAPI project. Restore to older version gives warnings, restore to latest removes them.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    @KnownIssue("EPBDS-16528")
    public void testLocalChangesAfterTablesGenerationReImport() {
        String projectName = "TestLocalChanges1_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(DriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProjectFromOpenApi(OPENAPI_FILE_1, projectName);
        uploadFileToProject(repositoryPage, projectName, OPENAPI_FILE_2, OPENAPI_FILE_2);

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

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Algorithms");
        ChangesDialogComponent changesDialog = editorPage.getEditorToolbarPanelComponent().clickMore().clickChanges();

        assertThat(changesDialog.getChangesTitle())
                .as("Changes title should show 1 local change for Algorithms")
                .isEqualTo("Local Changes");
        assertThat(changesDialog.getChangesCount())
                .as("Changes title should show 1 local change for Algorithms")
                .isEqualTo(1);
        assertThat(changesDialog.getRowCount())
                .as("Should be 2 rows in history (current + previous)")
                .isEqualTo(2);

        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Models");
        changesDialog = editorPage.getEditorToolbarPanelComponent().clickMore().clickChanges();

        assertThat(changesDialog.getChangesTitle())
                .as("Changes title should show 1 local change for Models")
                .isEqualTo("Local Changes");
        assertThat(changesDialog.getChangesCount())
                .as("Changes title should show 1 local change for Models")
                .isEqualTo(1);
        assertThat(changesDialog.getRowCount())
                .as("Should be 2 rows in history (current + previous)")
                .isEqualTo(2);

        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Algorithms");
        changesDialog = editorPage.getEditorToolbarPanelComponent().clickMore().clickChanges();
        changesDialog.clickRestoreAtRow(2);
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Models");
        changesDialog = editorPage.getEditorToolbarPanelComponent().clickMore().clickChanges();
        assertThat(changesDialog.isRowCurrent(1))
                .as("Local Changes must show the history of the selected module: its latest import is still current")
                .isTrue();
        changesDialog.clickRestoreAtRow(2);
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Algorithms");
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete();

        assertThat(editorPage.getProblemsPanelComponent().hasErrors())
                .as("Algorithms should have no errors after restoring to original openapi1.json state")
                .isFalse();
        assertThat(editorPage.getProblemsPanelComponent().getAllWarnings())
                .as("Algorithms should have OpenAPI reconciliation warning after restoring to original state")
                .contains(OPENAPI_RECONCILIATION_WARNING);

        changesDialog = editorPage.getEditorToolbarPanelComponent().clickMore().clickChanges();
        changesDialog.clickRestoreAtRow(1);
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Models");
        changesDialog = editorPage.getEditorToolbarPanelComponent().clickMore().clickChanges();
        changesDialog.clickRestoreAtRow(1);
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Algorithms");
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete();
        editorPage.getProblemsPanelComponent().checkNoProblems();
    }

    private void uploadFileToProject(RepositoryPage repositoryPage, String projectName,
                                     String sourceFileName, String targetFileName) {
        repositoryPage.openProjectsList().openProjectDetail(projectName)
                .uploadFileAs(TestDataUtil.getFilePathFromResources(sourceFileName), targetFileName);
        repositoryPage.openProjectsList().saveProject(projectName, "Uploaded " + targetFileName);
    }
}
