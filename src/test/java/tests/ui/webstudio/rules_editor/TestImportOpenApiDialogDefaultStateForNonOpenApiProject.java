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
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestImportOpenApiDialogDefaultStateForNonOpenApiProject extends BaseTest {

    private static final String TEMPLATE_NAME = "Example 1 - Bank Rating";

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
}
