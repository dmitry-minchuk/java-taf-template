package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.common.TableComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.components.createnewproject.OpenApiComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.TestDataUtil;
import helpers.utils.ZipUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Covered atomic tests (IPBQA-30678):
 *   2.8.1 - Create Project from OpenAPI file (JSON and YAML formats)
 *   - Verify default form values on OpenAPI tab
 *   - Create project from JSON, verify repository tree structure and Editor properties
 *   - Create project from YAML with custom module names and paths
 *   - Validate same-module-names and same-paths errors
 *   - Verify OpenAPI properties cleared after deleting the OpenAPI file
 */
public class TestCreateProjectFromOpenApiFile extends BaseTest {

    private static final String JSON_FILE = "openapi.json";
    private static final String YAML_FILE = "new_openapi_1.yaml";
    private static final String YML_FILE = "openapi.yml";
    private static final String INVALID_FILENAME_YAML = "dg!@#$%^&()_+{}.yaml";
    private static final String NON_OPENAPI_FILE = "non_openapi_file.xlsx";
    private static final String INVALID_JSON_FILE_1 = "openapi_inv1.json";
    private static final String INVALID_JSON_FILE_2 = "openapi_inv2.json";

    @Test
    @TestCaseId("IPBQA-30678")
    @Description("Verify default values in the Create Project from OpenAPI form: empty name fields, pre-filled path defaults")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCreateProjectFromOpenApiDefaultFormValues() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        repositoryPage.getCreateProjectLink().click();
        OpenApiComponent openApiComponent = repositoryPage.getCreateNewProjectComponent()
                .selectTab(CreateNewProjectComponent.TabName.OPEN_API);

        assertThat(openApiComponent.getProjectName())
                .as("Project name should be empty by default")
                .isEmpty();
        assertThat(openApiComponent.getDataModuleName())
                .as("Data module name should be empty by default")
                .isEmpty();
        assertThat(openApiComponent.getDataModulePathDisplay())
                .as("Data module path should default to 'rules/Models.xlsx'")
                .isEqualTo("rules/Models.xlsx");
        assertThat(openApiComponent.getRulesModuleName())
                .as("Rules module name should be empty by default")
                .isEmpty();
        assertThat(openApiComponent.getRulesModulePathDisplay())
                .as("Rules module path should default to 'rules/Algorithms.xlsx'")
                .isEqualTo("rules/Algorithms.xlsx");

        repositoryPage.getCreateNewProjectComponent().closeDialog();

        // Step 27: Verify Clear and ClearAll button behavior
        repositoryPage.getCreateProjectLink().click();
        openApiComponent = repositoryPage.getCreateNewProjectComponent()
                .selectTab(CreateNewProjectComponent.TabName.OPEN_API);
        openApiComponent.uploadOpenApiFile(JSON_FILE);
        openApiComponent.setProjectName("bla");

        assertThat(openApiComponent.isClearFirstFileVisible())
                .as("Clear button should be visible after file upload")
                .isTrue();
        assertThat(openApiComponent.isClearAllVisible())
                .as("ClearAll button should be visible after file upload")
                .isTrue();

        openApiComponent.clearFirstFile();
        assertThat(openApiComponent.isCreateEnabled())
                .as("Create button should be disabled after clearing the file")
                .isFalse();
        assertThat(openApiComponent.isClearFirstFileVisible())
                .as("Clear button should be absent after file is cleared")
                .isFalse();
        assertThat(openApiComponent.isClearAllVisible())
                .as("ClearAll button should be absent after file is cleared")
                .isFalse();

        openApiComponent.uploadOpenApiFile(JSON_FILE);
        openApiComponent.clearAllFiles();
        assertThat(openApiComponent.isCreateEnabled())
                .as("Create button should be disabled after clearing all files")
                .isFalse();
        assertThat(openApiComponent.isClearFirstFileVisible())
                .as("Clear button should be absent after ClearAll")
                .isFalse();
        assertThat(openApiComponent.isClearAllVisible())
                .as("ClearAll button should be absent after ClearAll")
                .isFalse();

        repositoryPage.getCreateNewProjectComponent().closeDialog();
    }

    @Test
    @TestCaseId("IPBQA-30678")
    @Description("Create project from OpenAPI JSON file and verify repository tree structure and Editor module properties")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCreateProjectFromOpenApiJsonFile() {
        String projectName = "JsonOpenApiProject_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        repositoryPage.createProjectFromOpenApi(JSON_FILE, projectName);

        // Verify repository tree: expand project and rules folder to check files
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .expandFolderInTree(projectName)
                .expandFolderInTree("rules");

        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("Algorithms.xlsx"))
                .as("Algorithms.xlsx should be present in repository tree")
                .isTrue();
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("Models.xlsx"))
                .as("Models.xlsx should be present in repository tree")
                .isTrue();
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("openapi.json"))
                .as("openapi.json should be present in repository tree")
                .isTrue();
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("rules.xml"))
                .as("rules.xml should be present in repository tree")
                .isTrue();
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("rules-deploy.xml"))
                .as("rules-deploy.xml should be present in repository tree")
                .isTrue();

        // Navigate to Editor and select project
        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

        // Verify OpenAPI properties
        assertThat(editorPage.getOpenApiPropertyValue("OpenAPI File:"))
                .as("OpenAPI File property should reflect uploaded file name")
                .isEqualTo("openapi.json");
        assertThat(editorPage.getOpenApiPropertyValue("Mode:"))
                .as("Mode should be 'Tables generation' for file-based project creation")
                .isEqualTo("Tables generation");
        assertThat(editorPage.getOpenApiPropertyValue("Rules Module:"))
                .as("Rules Module property should be 'Algorithms'")
                .isEqualTo("Algorithms");
        assertThat(editorPage.getOpenApiPropertyValue("Data Module:"))
                .as("Data Module property should be 'Models'")
                .isEqualTo("Models");

        // Verify Algorithms module folder types
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Algorithms");
        editorPage.getEditorLeftRulesTreeComponent().setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE);
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isFolderExistsInTree("Spreadsheet"))
                .as("Algorithms module should have Spreadsheet folder")
                .isTrue();
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isFolderExistsInTree("Configuration"))
                .as("Algorithms module should have Configuration folder")
                .isTrue();
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isFolderExistsInTree("Datatype"))
                .as("Algorithms module should NOT have Datatype folder")
                .isFalse();
        editorPage.getProblemsPanelComponent().checkNoProblems();

        // Verify Models module folder types
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Models");
        editorPage.getEditorLeftRulesTreeComponent().setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE);
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isFolderExistsInTree("Datatype"))
                .as("Models module should have Datatype folder")
                .isTrue();
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isFolderExistsInTree("Spreadsheet"))
                .as("Models module should NOT have Spreadsheet folder")
                .isFalse();
        editorPage.getProblemsPanelComponent().checkNoProblems();

        // Step 4.2: Edit a Datatype cell in Models module and save
        editorPage.getEditorLeftRulesTreeComponent().expandFolderInTree("Datatype");
        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Datatype", "JAXRSErrorResponse");
        TableComponent datatypeTable = editorPage.getCenterTable();
        datatypeTable.editCell(3, 1, "String[]");
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.waitUntilSpinnerLoaded();
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getProblemsPanelComponent().checkNoProblems();

        // Step 5: Export project and download ZIP
        editorPage.getEditorToolbarPanelComponent().clickExport();
        File exportedZip = editorPage.getExportProjectDialogComponent().clickExportAndDownload();

        // Step 6: Verify rules-deploy.xml content
        String deployXml = ZipUtil.readFileFromZip(exportedZip, "rules-deploy.xml");
        assertThat(deployXml)
                .as("rules-deploy.xml should contain isProvideRuntimeContext=true")
                .contains("<isProvideRuntimeContext>true</isProvideRuntimeContext>");
        assertThat(deployXml)
                .as("rules-deploy.xml should contain RESTFUL publisher")
                .contains("<publisher>RESTFUL</publisher>");
        assertThat(deployXml)
                .as("rules-deploy.xml should contain annotationTemplateClassName")
                .contains("<annotationTemplateClassName>org.openl.generated.services.Service</annotationTemplateClassName>");

        // Step 7: Verify rules.xml content
        String rulesXml = ZipUtil.readFileFromZip(exportedZip, "rules.xml");
        assertThat(rulesXml)
                .as("rules.xml should contain Algorithms module")
                .contains("<name>Algorithms</name>")
                .contains("<rules-root path=\"rules/Algorithms.xlsx\"/>");
        assertThat(rulesXml)
                .as("rules.xml should contain Models module")
                .contains("<name>Models</name>")
                .contains("<rules-root path=\"rules/Models.xlsx\"/>");
        assertThat(rulesXml)
                .as("rules.xml should contain OpenAPI configuration")
                .contains("<path>openapi.json</path>")
                .contains("<model-module-name>Models</model-module-name>")
                .contains("<algorithm-module-name>Algorithms</algorithm-module-name>")
                .contains("<mode>GENERATION</mode>");

        // Step 8: Rename modules Algorithms → Algorithms_test, Models → Models_test
        editorPage.getProjectDetailsComponent().openEditModuleDialog("Algorithms");
        editorPage.getAddModulePopupComponent().setModuleName("Algorithms_test");
        editorPage.getAddModulePopupComponent().saveModule();
        editorPage.getProjectDetailsComponent().openEditModuleDialog("Models");
        editorPage.getAddModulePopupComponent().setModuleName("Models_test");
        editorPage.getAddModulePopupComponent().saveModule();
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.waitUntilSpinnerLoaded();

        // Step 9: Verify rules.xml reflects renamed modules
        editorPage.getEditorToolbarPanelComponent().clickExport();
        File exportedZipAfterRename = editorPage.getExportProjectDialogComponent().clickExportAndDownload();
        String rulesXmlAfterRename = ZipUtil.readFileFromZip(exportedZipAfterRename, "rules.xml");
        assertThat(rulesXmlAfterRename)
                .as("rules.xml should reflect renamed modules Algorithms_test and Models_test")
                .contains("<name>Algorithms_test</name>")
                .contains("<name>Models_test</name>")
                .contains("<model-module-name>Models_test</model-module-name>")
                .contains("<algorithm-module-name>Algorithms_test</algorithm-module-name>");
        assertThat(rulesXmlAfterRename)
                .as("rules.xml should not contain old module names after rename")
                .doesNotContain("<model-module-name>Models</model-module-name>")
                .doesNotContain("<algorithm-module-name>Algorithms</algorithm-module-name>");

        // Step 10: Copy module Algorithms_test → Algorithms2
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Algorithms_test");
        editorPage.openCopyModuleDialog();
        editorPage.getCopyModuleDialogComponent().setModuleName("Algorithms2");
        editorPage.getCopyModuleDialogComponent().clickCopy();
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.waitUntilSpinnerLoaded();
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        List<String> modulesAfterCopy = editorPage.getEditorLeftProjectModuleSelectorComponent().getAllModuleNames(projectName);
        assertThat(modulesAfterCopy)
                .as("Algorithms2 module should be present after copy")
                .contains("Algorithms2");

        // Step 11: Upload rules.xlsx to repository at project root level
        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickUploadFileBtn();
        repositoryPage.getUploadFileDialogComponent()
                .uploadFile(TestDataUtil.getFilePathFromResources("rules.xlsx"))
                .clickUploadButton();
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickSaveBtn();
        repositoryPage.getSaveChangesComponent().getSaveBtn().click();
        repositoryPage.waitUntilSpinnerLoaded();
        repositoryPage.getRefreshBtn().click(10000);

        // Verify uploaded file visible in Editor module list
        EditorPage editorPageAfterUpload = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPageAfterUpload.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);
        List<String> modulesAfterUpload = editorPageAfterUpload.getEditorLeftProjectModuleSelectorComponent().getAllModuleNames(projectName);
        assertThat(modulesAfterUpload)
                .as("Module 'rules' should appear after uploading rules.xlsx")
                .contains("rules");

        // Step 12: Verify rules.xml after copy and upload
        editorPageAfterUpload.getEditorToolbarPanelComponent().clickExport();
        File exportedZipAfterUpload = editorPageAfterUpload.getExportProjectDialogComponent().clickExportAndDownload();
        String rulesXmlAfterUpload = ZipUtil.readFileFromZip(exportedZipAfterUpload, "rules.xml");
        assertThat(rulesXmlAfterUpload)
                .as("rules.xml should contain Algorithms2 module after copy")
                .contains("<name>Algorithms2</name>");
        assertThat(rulesXmlAfterUpload)
                .as("rules.xml should contain rules module after upload")
                .contains("<name>rules</name>");

        // Step 13: Copy project to a new branch via repository Copy button
        repositoryPage = editorPageAfterUpload.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickCopyBtn();
        repositoryPage.getCopyProjectDialogComponent().clickCopyButton();
        repositoryPage.waitUntilSpinnerLoaded();

        // Step 14: Copy project to a separate independent project
        repositoryPage.getLeftRepositoryTreeComponent()
                .selectItemInFolder("Projects", projectName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickCopyBtn();
        repositoryPage.getCopyProjectDialogComponent()
                .setSeparateProject(true)
                .setNewProjectName(projectName + "-Copy")
                .clickCopyButton();
        repositoryPage.waitUntilSpinnerLoaded();
        repositoryPage.getRefreshBtn().click(10000);

        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree(projectName + "-Copy"))
                .as("Copied project '" + projectName + "-Copy' should be present in repository tree")
                .isTrue();
    }

    @Test
    @TestCaseId("IPBQA-30678")
    @Description("Create project from YAML file with custom module names and paths, verify structure")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCreateProjectFromOpenApiYamlWithCustomModuleNames() {
        String projectName = "YamlOpenApiProject_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        repositoryPage.getCreateProjectLink().click();
        OpenApiComponent openApiComponent = repositoryPage.getCreateNewProjectComponent()
                .selectTab(CreateNewProjectComponent.TabName.OPEN_API);

        openApiComponent.uploadOpenApiFile(YAML_FILE);
        openApiComponent.setProjectName(projectName);

        assertThat(openApiComponent.isCreateEnabled())
                .as("Create button should be enabled after uploading file and setting project name")
                .isTrue();

        // Set custom module names and verify auto-generated paths
        openApiComponent.setDataModuleName("Data_Types");
        assertThat(openApiComponent.getDataModulePathDisplay())
                .as("Data module path should auto-update to 'rules/Data_Types.xlsx'")
                .isEqualTo("rules/Data_Types.xlsx");

        openApiComponent.setRulesModuleName("Spreadsheets");
        assertThat(openApiComponent.getRulesModulePathDisplay())
                .as("Rules module path should auto-update to 'rules/Spreadsheets.xlsx'")
                .isEqualTo("rules/Spreadsheets.xlsx");

        // Edit data module path to a custom value
        openApiComponent.clickEditDataPath();
        openApiComponent.setDataModulePath("rules1/Data_Types_file.xlsx");
        assertThat(openApiComponent.getDataModulePathInputValue())
                .as("Data module path input should reflect custom path")
                .isEqualTo("rules1/Data_Types_file.xlsx");

        // Edit rules path, then reset to verify reset functionality
        openApiComponent.clickEditRulesPath();
        openApiComponent.setRulesModulePath("rules/Spreadsheets_file.xlsx");
        openApiComponent.clickResetRulesPath();
        assertThat(openApiComponent.getRulesModulePathDisplay())
                .as("Rules module path should reset to default 'rules/Spreadsheets.xlsx' after reset")
                .isEqualTo("rules/Spreadsheets.xlsx");

        openApiComponent.clickCreate();
        repositoryPage.fillCommitInfo();
        repositoryPage.waitUntilSpinnerLoaded();
        repositoryPage.getRefreshBtn().click(10000);

        // Verify repository tree structure
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .expandFolderInTree(projectName)
                .expandFolderInTree("rules")
                .expandFolderInTree("rules1");

        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("Data_Types_file.xlsx"))
                .as("Data_Types_file.xlsx should be present in rules1 folder")
                .isTrue();
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("Spreadsheets.xlsx"))
                .as("Spreadsheets.xlsx should be present in rules folder")
                .isTrue();
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("new_openapi_1.yaml"))
                .as("new_openapi_1.yaml should be present in repository tree")
                .isTrue();

        // Verify Editor module properties
        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

        assertThat(editorPage.getOpenApiPropertyValue("OpenAPI File:"))
                .as("OpenAPI File should be 'new_openapi_1.yaml'")
                .isEqualTo("new_openapi_1.yaml");
        assertThat(editorPage.getOpenApiPropertyValue("Mode:"))
                .as("Mode should be 'Tables generation'")
                .isEqualTo("Tables generation");
        assertThat(editorPage.getOpenApiPropertyValue("Rules Module:"))
                .as("Rules Module should be 'Spreadsheets'")
                .isEqualTo("Spreadsheets");
        assertThat(editorPage.getOpenApiPropertyValue("Data Module:"))
                .as("Data Module should be 'Data_Types'")
                .isEqualTo("Data_Types");

        // Verify Spreadsheets module folder types
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Spreadsheets");
        editorPage.getEditorLeftRulesTreeComponent().setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE);
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isFolderExistsInTree("Spreadsheet"))
                .as("Spreadsheets module should have Spreadsheet folder")
                .isTrue();
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isFolderExistsInTree("Datatype"))
                .as("Spreadsheets module should NOT have Datatype folder")
                .isFalse();
        editorPage.getProblemsPanelComponent().checkNoProblems();

        // Verify Data_Types module folder types
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Data_Types");
        editorPage.getEditorLeftRulesTreeComponent().setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE);
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isFolderExistsInTree("Datatype"))
                .as("Data_Types module should have Datatype folder")
                .isTrue();
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isFolderExistsInTree("Spreadsheet"))
                .as("Data_Types module should NOT have Spreadsheet folder")
                .isFalse();
        editorPage.getProblemsPanelComponent().checkNoProblems();

        // Step 19: Rename modules Spreadsheets → Spreadsheets_test, Data_Types → Data_Type_test
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getProjectDetailsComponent().openEditModuleDialog("Spreadsheets");
        editorPage.getAddModulePopupComponent().setModuleName("Spreadsheets_test");
        editorPage.getAddModulePopupComponent().saveModule();
        editorPage.getProjectDetailsComponent().openEditModuleDialog("Data_Types");
        editorPage.getAddModulePopupComponent().setModuleName("Data_Type_test");
        editorPage.getAddModulePopupComponent().saveModule();
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.waitUntilSpinnerLoaded();

        // Verify OpenAPI properties updated with new module names
        assertThat(editorPage.getOpenApiPropertyValue("Rules Module:"))
                .as("Rules Module property should update to 'Spreadsheets_test' after rename")
                .isEqualTo("Spreadsheets_test");
        assertThat(editorPage.getOpenApiPropertyValue("Data Module:"))
                .as("Data Module property should update to 'Data_Type_test' after rename")
                .isEqualTo("Data_Type_test");

        // Step 20: Verify rules.xml reflects renamed modules
        editorPage.getEditorToolbarPanelComponent().clickExport();
        File exportedZipAfterRename = editorPage.getExportProjectDialogComponent().clickExportAndDownload();
        String rulesXmlAfterRename = ZipUtil.readFileFromZip(exportedZipAfterRename, "rules.xml");
        assertThat(rulesXmlAfterRename)
                .as("rules.xml should contain Spreadsheets_test module after rename")
                .contains("<name>Spreadsheets_test</name>")
                .contains("<rules-root path=\"rules/Spreadsheets.xlsx\"/>");
        assertThat(rulesXmlAfterRename)
                .as("rules.xml should contain Data_Type_test module after rename")
                .contains("<name>Data_Type_test</name>")
                .contains("<rules-root path=\"rules1/Data_Types_file.xlsx\"/>");
        assertThat(rulesXmlAfterRename)
                .as("rules.xml should contain updated OpenAPI references after rename")
                .contains("<model-module-name>Data_Type_test</model-module-name>")
                .contains("<algorithm-module-name>Spreadsheets_test</algorithm-module-name>");

        // Step 21: Delete module Spreadsheets_test (without leaving Excel file)
        editorPage.getProjectDetailsComponent().openRemoveModuleDialog("Spreadsheets_test");
        editorPage.getRemoveModulePopupComponent().setLeaveFile(false);
        editorPage.getRemoveModulePopupComponent().clickRemove();
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.waitUntilSpinnerLoaded();

        List<String> modulesAfterDelete = editorPage.getEditorLeftProjectModuleSelectorComponent().getAllModuleNames(projectName);
        assertThat(modulesAfterDelete)
                .as("Spreadsheets_test module should be absent after deletion")
                .doesNotContain("Spreadsheets_test");

        // Step 22: Verify rules.xml after module deletion
        editorPage.getEditorToolbarPanelComponent().clickExport();
        File exportedZipAfterDelete = editorPage.getExportProjectDialogComponent().clickExportAndDownload();
        String rulesXmlAfterDelete = ZipUtil.readFileFromZip(exportedZipAfterDelete, "rules.xml");
        assertThat(rulesXmlAfterDelete)
                .as("rules.xml should contain only Data_Type_test module after deleting Spreadsheets_test")
                .contains("<name>Data_Type_test</name>")
                .contains("<rules-root path=\"rules1/Data_Types_file.xlsx\"/>");
        assertThat(rulesXmlAfterDelete)
                .as("rules.xml should not contain deleted Spreadsheets_test module")
                .doesNotContain("<name>Spreadsheets_test</name>");
        assertThat(rulesXmlAfterDelete)
                .as("rules.xml OpenAPI section should not contain algorithm-module-name after deleting rules module")
                .doesNotContain("<algorithm-module-name>Spreadsheets_test</algorithm-module-name>");
    }

    @Test
    @TestCaseId("IPBQA-30678")
    @Description("Validate same module names and same module paths errors in Create Project from OpenAPI form")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCreateProjectFromOpenApiFormValidation() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        // Step 26.1: OpenAPI file name contains forbidden characters
        repositoryPage.getCreateProjectLink().click();
        OpenApiComponent openApiComponent = repositoryPage.getCreateNewProjectComponent()
                .selectTab(CreateNewProjectComponent.TabName.OPEN_API);
        openApiComponent.uploadOpenApiFile(INVALID_FILENAME_YAML);
        openApiComponent.setProjectName("bla");
        openApiComponent.clickCreate();
        assertThat(repositoryPage.getInlineMessage())
                .as("Error should appear when OpenAPI filename contains forbidden characters")
                .contains("OpenAPI File Name cannot contain forbidden");

        // Step 26.2: Non-OpenAPI file type (xlsx) - popup appears immediately after upload
        repositoryPage.getCreateProjectLink().click();
        openApiComponent = repositoryPage.getCreateNewProjectComponent()
                .selectTab(CreateNewProjectComponent.TabName.OPEN_API);
        openApiComponent.uploadOpenApiFile(NON_OPENAPI_FILE);
        assertThat(repositoryPage.getMessagePopupText())
                .as("Popup should appear when non-OpenAPI file is uploaded")
                .contains("Only JSON and YML/YAML files are accepted");
        repositoryPage.closeMessagePopup();
        repositoryPage.getCreateNewProjectComponent().closeDialog();

        // Step 26.3: Project name contains forbidden characters
        repositoryPage.getCreateProjectLink().click();
        openApiComponent = repositoryPage.getCreateNewProjectComponent()
                .selectTab(CreateNewProjectComponent.TabName.OPEN_API);
        openApiComponent.uploadOpenApiFile(JSON_FILE);
        openApiComponent.setProjectName("test%?>");
        openApiComponent.clickCreate();
        assertThat(repositoryPage.getInlineMessage())
                .as("Error should appear when project name contains forbidden characters")
                .contains("is not a valid project name");

        // Step 26.4: Module name contains forbidden characters
        repositoryPage.getCreateProjectLink().click();
        openApiComponent = repositoryPage.getCreateNewProjectComponent()
                .selectTab(CreateNewProjectComponent.TabName.OPEN_API);
        openApiComponent.uploadOpenApiFile(JSON_FILE);
        openApiComponent.setProjectName("bla");
        openApiComponent.setDataModuleName("Models?*/test");
        openApiComponent.clickEditDataPath();
        openApiComponent.setDataModulePath("rules/Models.xlsx");
        openApiComponent.clickCreate();
        assertThat(repositoryPage.getInlineMessage())
                .as("Error should appear when module name contains forbidden characters")
                .contains("Module Name cannot contain forbidden");

        // Step 26.5: Module path contains forbidden characters
        repositoryPage.getCreateProjectLink().click();
        openApiComponent = repositoryPage.getCreateNewProjectComponent()
                .selectTab(CreateNewProjectComponent.TabName.OPEN_API);
        openApiComponent.uploadOpenApiFile(JSON_FILE);
        openApiComponent.setProjectName("bla");
        openApiComponent.clickEditDataPath();
        openApiComponent.setDataModulePath("rules/Models?*test.xlsx");
        openApiComponent.clickCreate();
        assertThat(repositoryPage.getInlineMessage())
                .as("Error should appear when module path contains forbidden characters")
                .contains("Project creating is failed");

        // Step 26.6: Validate: same module names error
        repositoryPage.getCreateProjectLink().click();
        openApiComponent = repositoryPage.getCreateNewProjectComponent()
                .selectTab(CreateNewProjectComponent.TabName.OPEN_API);
        openApiComponent.uploadOpenApiFile(JSON_FILE);
        openApiComponent.setProjectName("bla_" + System.currentTimeMillis());
        openApiComponent.setRulesModuleName("Models");
        openApiComponent.clickCreate();
        repositoryPage.fillCommitInfo();

        assertThat(repositoryPage.getInlineMessage())
                .as("Error should appear when module names are the same")
                .contains("Module names cannot be the same");

        // Step 26.7: Validate: same module paths error
        repositoryPage.getCreateProjectLink().click();
        openApiComponent = repositoryPage.getCreateNewProjectComponent()
                .selectTab(CreateNewProjectComponent.TabName.OPEN_API);
        openApiComponent.uploadOpenApiFile(JSON_FILE);
        openApiComponent.setProjectName("bla2_" + System.currentTimeMillis());
        openApiComponent.clickEditDataPath();
        openApiComponent.setDataModulePath("rules/Algorithms.xlsx");
        openApiComponent.clickCreate();

        assertThat(repositoryPage.getInlineMessage())
                .as("Error should appear when module paths are the same")
                .contains("Path for Modules cannot be the same");

        // Step 28: Invalid JSON structure (file 1) - project must not be created
        String invalidProjectName = "InvalidJsonStructure_" + System.currentTimeMillis();
        repositoryPage.getCreateProjectLink().click();
        openApiComponent = repositoryPage.getCreateNewProjectComponent()
                .selectTab(CreateNewProjectComponent.TabName.OPEN_API);
        openApiComponent.uploadOpenApiFile(INVALID_JSON_FILE_1);
        openApiComponent.setProjectName(invalidProjectName);
        openApiComponent.clickCreate();
        assertThat(repositoryPage.getInlineMessage())
                .as("Error should appear for invalid JSON structure (file 1)")
                .contains("Project creating is failed");

        // Step 28.1: Invalid JSON structure (file 2) - project must not be created
        repositoryPage.getCreateProjectLink().click();
        openApiComponent = repositoryPage.getCreateNewProjectComponent()
                .selectTab(CreateNewProjectComponent.TabName.OPEN_API);
        openApiComponent.uploadOpenApiFile(INVALID_JSON_FILE_2);
        openApiComponent.setProjectName(invalidProjectName);
        openApiComponent.clickCreate();
        assertThat(repositoryPage.getInlineMessage())
                .as("Error should appear for invalid JSON structure (file 2)")
                .contains("Project creating is failed");

        // Step 29: After project creation, form resets to default module names and paths
        String step29ProjectName = "bla29_" + System.currentTimeMillis();
        repositoryPage.getCreateProjectLink().click();
        openApiComponent = repositoryPage.getCreateNewProjectComponent()
                .selectTab(CreateNewProjectComponent.TabName.OPEN_API);
        openApiComponent.uploadOpenApiFile(JSON_FILE);
        openApiComponent.setProjectName(step29ProjectName);
        openApiComponent.setDataModuleName("bla1");
        openApiComponent.setRulesModuleName("kek2");
        openApiComponent.clickCreate();
        repositoryPage.fillCommitInfo();
        repositoryPage.waitUntilSpinnerLoaded();
        repositoryPage.getRefreshBtn().click(10000);

        repositoryPage.getCreateProjectLink().click();
        openApiComponent = repositoryPage.getCreateNewProjectComponent()
                .selectTab(CreateNewProjectComponent.TabName.OPEN_API);
        assertThat(openApiComponent.getDataModuleName())
                .as("Data module name should not retain custom value 'bla1' after project creation")
                .isNotEqualTo("bla1");
        assertThat(openApiComponent.getDataModulePathDisplay())
                .as("Data module path should reset to default 'rules/Models.xlsx' after project creation")
                .isEqualTo("rules/Models.xlsx");
        assertThat(openApiComponent.getRulesModuleName())
                .as("Rules module name should not retain custom value 'kek2' after project creation")
                .isNotEqualTo("kek2");
        assertThat(openApiComponent.getRulesModulePathDisplay())
                .as("Rules module path should reset to default 'rules/Algorithms.xlsx' after project creation")
                .isEqualTo("rules/Algorithms.xlsx");
        repositoryPage.getCreateNewProjectComponent().closeDialog();
    }

    @Test
    @TestCaseId("IPBQA-30678")
    @Description("Delete OpenAPI file from repository and verify OpenAPI properties section becomes empty in Editor")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testDeleteOpenApiFileRemovesProperties() {
        String projectName = "YmlOpenApiProject_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        // Create project from .yml file
        repositoryPage.createProjectFromOpenApi(YML_FILE, projectName);

        // Expand tree and delete openapi.yml (file lives at project root, not inside rules/)
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .expandFolderInTree(projectName)
                .selectItemInFolder(projectName, "openapi.yml");

        repositoryPage.getRepositoryContentButtonsPanelComponent().clickDeleteBtn();
        repositoryPage.getConfirmDeleteDialogComponent().clickDelete();

        repositoryPage.getRepositoryContentButtonsPanelComponent().clickSaveBtn();
        repositoryPage.getSaveChangesComponent().getSaveBtn().click();
        repositoryPage.waitUntilSpinnerLoaded();

        // Navigate to Editor and verify OpenAPI properties section is empty
        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

        assertThat(editorPage.isOpenApiPropertiesSectionEmpty())
                .as("OpenAPI properties section should be empty after deleting the OpenAPI file")
                .isTrue();

        // Verify modules still present without problems
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Algorithms");
        editorPage.getProblemsPanelComponent().checkNoProblems();

        // Step 25: Verify rules.xml after deleting openapi.yml no longer has <openapi> section
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorToolbarPanelComponent().clickExport();
        File exportedZip = editorPage.getExportProjectDialogComponent().clickExportAndDownload();
        String rulesXml = ZipUtil.readFileFromZip(exportedZip, "rules.xml");
        assertThat(rulesXml)
                .as("rules.xml should still contain Algorithms and Models modules after deleting openapi.yml")
                .contains("<name>Algorithms</name>")
                .contains("<name>Models</name>");
        assertThat(rulesXml)
                .as("rules.xml should not contain <openapi> section after deleting the OpenAPI file")
                .doesNotContain("<openapi>");
    }
}
