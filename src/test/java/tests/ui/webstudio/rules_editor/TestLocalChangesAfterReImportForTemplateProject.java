package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.ChangesDialogComponent;
import domain.ui.webstudio.components.editortabcomponents.CompareLocalChangesDialogComponent;
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

public class TestLocalChangesAfterReImportForTemplateProject extends BaseTest {

    private static final String OPENAPI_FILE_2 = "openapi2.json";
    private static final String TEMPLATE_AUTO_POLICY = "Example 3 - Auto Policy Calculation";
    private static final String OPENAPI_RECONCILIATION_WARNING = "OpenAPI Reconciliation: There are no suitable methods to check." +
            " Check the provided rules, annotation template class, and included/excluded methods in module settings.";

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

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(
                CreateNewProjectComponent.TabName.TEMPLATE,
                projectName, TEMPLATE_AUTO_POLICY);
        uploadFileToProject(repositoryPage, projectName, OPENAPI_FILE_2, OPENAPI_FILE_2);

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

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, rulesModuleName);
        ChangesDialogComponent changesDialog = editorPage.getEditorToolbarPanelComponent().clickMore().clickChanges();

        assertThat(changesDialog.getChangesTitle())
                .as("Changes title should show 1 local change for AutoPolicyCalculation")
                .isEqualTo("Local Changes (1)");
        assertThat(changesDialog.getRowCount())
                .as("Should be 2 rows in history (current + previous)")
                .isEqualTo(2);

        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, dataModuleName);
        changesDialog = editorPage.getEditorToolbarPanelComponent().clickMore().clickChanges();

        assertThat(changesDialog.getChangesTitle())
                .as("Changes title should show 1 local change for AutoPolicyTests")
                .isEqualTo("Local Changes (1)");
        assertThat(changesDialog.getRowCount())
                .as("Should be 2 rows in history (current + previous)")
                .isEqualTo(2);

        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, rulesModuleName);
        changesDialog = editorPage.getEditorToolbarPanelComponent().clickMore().clickChanges();
        changesDialog.clickRestoreAtRow(2);
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, dataModuleName);
        changesDialog = editorPage.getEditorToolbarPanelComponent().clickMore().clickChanges();
        changesDialog.clickRestoreAtRow(2);
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, rulesModuleName);
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete();

        assertThat(editorPage.getProblemsPanelComponent().hasErrors())
                .as("AutoPolicyCalculation should have no errors after restoring to original state")
                .isFalse();
        assertThat(editorPage.getProblemsPanelComponent().getAllWarnings())
                .as("AutoPolicyCalculation should have OpenAPI reconciliation warning after restore")
                .contains(OPENAPI_RECONCILIATION_WARNING);

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
