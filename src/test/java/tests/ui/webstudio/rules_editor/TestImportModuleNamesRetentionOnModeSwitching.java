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
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestImportModuleNamesRetentionOnModeSwitching extends BaseTest {

    private static final String OPENAPI_FILE_1 = "openapi1.json";

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
}
