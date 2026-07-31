package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.ProjectDetailPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.ZipUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCreateProjectFromOpenApiYamlWithCustomModuleNames extends BaseTest {

    private static final String YAML_FILE = "new_openapi_1.yaml";

    @Test
    @TestCaseId("IPBQA-30678")
    @Description("Create project from YAML file with custom module names and paths, verify structure. "
            + "KNOWN-FAILING: saving the project after a module is removed fails server-side with "
            + "ProjectException \"Object ... is not a tree\", so the Save dialog never closes."
            + " Known bug: EPBDS-16361.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCreateProjectFromOpenApiYamlWithCustomModuleNames() {
        String projectName = "YamlOpenApiProject_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        repositoryPage.getCreateProjectLink().click();
        CreateNewProjectComponent openApiComponent = repositoryPage.getCreateNewProjectComponent();
        openApiComponent.selectMethod(CreateNewProjectComponent.TabName.OPEN_API);

        openApiComponent.uploadOpenApiSpec(YAML_FILE);
        openApiComponent.setProjectName(projectName);

        assertThat(openApiComponent.isCreateEnabled())
                .as("Create button should be enabled after uploading file and setting project name").isTrue();

        // In the React wizard the module name and its path are independent inputs: renaming the module no
        // longer rewrites the path, so both are set explicitly.
        openApiComponent.setDataModuleName("Data_Types");
        openApiComponent.setDataModulePath("rules/Data_Types.xlsx");
        assertThat(openApiComponent.getDataModulePath())
                .as("Data module path should hold what was typed").isEqualTo("rules/Data_Types.xlsx");

        openApiComponent.setRulesModuleName("Spreadsheets");
        openApiComponent.setRulesModulePath("rules/Spreadsheets.xlsx");
        assertThat(openApiComponent.getRulesModulePath())
                .as("Rules module path should hold what was typed").isEqualTo("rules/Spreadsheets.xlsx");
        openApiComponent.setDataModulePath("rules1/Data_Types_file.xlsx");
        assertThat(openApiComponent.getDataModulePathInputValue())
                .as("Data module path input should reflect custom path").isEqualTo("rules1/Data_Types_file.xlsx");
        // The React wizard has no "reset path" button — the path is a plain input, so it is typed back.
        openApiComponent.setRulesModulePath("rules/Spreadsheets_file.xlsx");
        openApiComponent.setRulesModulePath("rules/Spreadsheets.xlsx");
        assertThat(openApiComponent.getRulesModulePath())
                .as("Rules module path should read back what was typed").isEqualTo("rules/Spreadsheets.xlsx");

        openApiComponent.clickCreate();
        repositoryPage.fillCommitInfo();
        repositoryPage.waitUntilSpinnerLoaded();

        ProjectDetailPage projectFiles = repositoryPage.openProjectsList().openProjectDetail(projectName);
        assertThat(projectFiles.isFilePresent("Data_Types_file.xlsx"))
                .as("Data_Types_file.xlsx should be present in the project files").isTrue();
        assertThat(projectFiles.isFilePresent("Spreadsheets.xlsx"))
                .as("Spreadsheets.xlsx should be present in the project files").isTrue();
        assertThat(projectFiles.isFilePresent("new_openapi_1.yaml"))
                .as("new_openapi_1.yaml should be present in the project files").isTrue();

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

        assertThat(editorPage.getOpenApiPropertyValue("OpenAPI File:")).isEqualTo("new_openapi_1.yaml");
        assertThat(editorPage.getOpenApiPropertyValue("Mode:")).isEqualTo("Tables generation");
        assertThat(editorPage.getOpenApiPropertyValue("Rules Module:")).isEqualTo("Spreadsheets");
        assertThat(editorPage.getOpenApiPropertyValue("Data Module:")).isEqualTo("Data_Types");

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Spreadsheets");
        editorPage.getEditorLeftRulesTreeComponent().setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE);
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isFolderExistsInTree("Spreadsheet")).isTrue();
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isFolderExistsInTree("Datatype")).isFalse();
        editorPage.getProblemsPanelComponent().checkNoProblems();

        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Data_Types");
        editorPage.getEditorLeftRulesTreeComponent().setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE);
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isFolderExistsInTree("Datatype")).isTrue();
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isFolderExistsInTree("Spreadsheet")).isFalse();
        editorPage.getProblemsPanelComponent().checkNoProblems();

        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getProjectDetailsComponent().openEditModuleDialog("Spreadsheets");
        editorPage.getAddModulePopupComponent().setModuleName("Spreadsheets_test");
        editorPage.getAddModulePopupComponent().saveModule();
        editorPage.getProjectDetailsComponent().openEditModuleDialog("Data_Types");
        editorPage.getAddModulePopupComponent().setModuleName("Data_Type_test");
        editorPage.getAddModulePopupComponent().saveModule();
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().clickSave();
        editorPage.waitUntilSpinnerLoaded();

        assertThat(editorPage.getOpenApiPropertyValue("Rules Module:")).isEqualTo("Spreadsheets_test");
        assertThat(editorPage.getOpenApiPropertyValue("Data Module:")).isEqualTo("Data_Type_test");

        editorPage.getEditorToolbarPanelComponent().clickExport();
        File exportedZipAfterRename = editorPage.getExportProjectDialogComponent().clickExportAndDownload();
        String rulesXmlAfterRename = ZipUtil.readFileFromZip(exportedZipAfterRename, "rules.xml");
        assertThat(rulesXmlAfterRename)
                .contains("<name>Spreadsheets_test</name>").contains("<rules-root path=\"rules/Spreadsheets.xlsx\"/>")
                .contains("<name>Data_Type_test</name>").contains("<rules-root path=\"rules1/Data_Types_file.xlsx\"/>")
                .contains("<model-module-name>Data_Type_test</model-module-name>")
                .contains("<algorithm-module-name>Spreadsheets_test</algorithm-module-name>");

        editorPage.getProjectDetailsComponent().openRemoveModuleDialog("Spreadsheets_test");
        editorPage.getRemoveModulePopupComponent().setLeaveFile(false);
        editorPage.getRemoveModulePopupComponent().clickRemove();
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().clickSave();
        editorPage.waitUntilSpinnerLoaded();

        List<String> modulesAfterDelete = editorPage.getEditorLeftProjectModuleSelectorComponent().getAllModuleNames(projectName);
        assertThat(modulesAfterDelete).doesNotContain("Spreadsheets_test");

        editorPage.getEditorToolbarPanelComponent().clickExport();
        File exportedZipAfterDelete = editorPage.getExportProjectDialogComponent().clickExportAndDownload();
        String rulesXmlAfterDelete = ZipUtil.readFileFromZip(exportedZipAfterDelete, "rules.xml");
        assertThat(rulesXmlAfterDelete)
                .contains("<name>Data_Type_test</name>")
                .contains("<rules-root path=\"rules1/Data_Types_file.xlsx\"/>")
                .doesNotContain("<name>Spreadsheets_test</name>")
                .doesNotContain("<algorithm-module-name>Spreadsheets_test</algorithm-module-name>");
    }
}
