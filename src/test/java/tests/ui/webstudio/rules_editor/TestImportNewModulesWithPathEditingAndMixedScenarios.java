package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
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

public class TestImportNewModulesWithPathEditingAndMixedScenarios extends BaseTest {

    private static final String OPENAPI_FILE = "openapi2.json";
    private static final String OPENAPI_FILE_1 = "openapi1.json";

    @Test
    @TestCaseId("IPBQA-31035")
    @Description("Steps 4-5.2: Import new modules with path editing/reset, module names retained after cancel, mixed new/existing modules scenario.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testImportNewModulesWithPathEditingAndMixedScenarios() {
        String projectName = "TestNewModulesPath_" + System.currentTimeMillis();
        String moduleName = "Mod-123";

        LoginService loginService = new LoginService(DriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getCreateProjectLink().click();
        CreateNewProjectComponent openApiComponent = repositoryPage.getCreateNewProjectComponent();
        openApiComponent.selectMethod(CreateNewProjectComponent.TabName.OPEN_API);
        openApiComponent.uploadOpenApiSpec(OPENAPI_FILE_1);
        openApiComponent.setDataModuleName("Models_test");
        openApiComponent.setDataModulePath("rules2/Models_test2.xlsx");
        openApiComponent.setRulesModuleName("Algorithms_test");
        openApiComponent.setRulesModulePath("rules1/Algorithms_test1.xlsx");
        openApiComponent.setProjectName(projectName);
        openApiComponent.clickCreate();
        repositoryPage.fillCommitInfo();
        repositoryPage.waitUntilSpinnerLoaded();

        uploadFileToProject(repositoryPage, projectName, OPENAPI_FILE);

        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

        ImportOpenApiDialogComponent importDialog = editorPage.openImportOpenApiDialog();
        importDialog.selectUploadInRepository();
        importDialog.setOpenApiFilePath(OPENAPI_FILE);
        importDialog.clickImportReconciliation();
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Algorithms_test");
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete();

        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        importDialog = editorPage.openImportOpenApiDialog();
        importDialog.selectTablesGenerationMode();
        importDialog.clickImportTablesGeneration();
        OpenApiModuleSettingsDialogComponent settingsDialog = editorPage.getOpenApiModuleSettingsDialogComponent();
        settingsDialog.waitForVisible();
        settingsDialog.clickImportAndOverride();
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().clickSave();
        editorPage.waitUntilSpinnerLoaded();

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

    private void uploadFileToProject(RepositoryPage repositoryPage, String projectName, String fileName) {
        repositoryPage.openProjectsList().openProjectDetail(projectName)
                .uploadFileAs(TestDataUtil.getFilePathFromResources(fileName), fileName);
        repositoryPage.openProjectsList().saveProject(projectName, "Uploaded " + fileName);
    }
}
