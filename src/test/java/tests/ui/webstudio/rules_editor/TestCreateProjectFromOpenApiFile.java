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
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

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

    @Test
    @TestCaseId("IPBQA-30678")
    @Description("Verify default values in the Create Project from OpenAPI form")
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
                .as("Data module name should default to 'Models'")
                .isEqualTo("Models");
        assertThat(openApiComponent.getDataModulePathDisplay())
                .as("Data module path should default to 'rules/Models.xlsx'")
                .isEqualTo("rules/Models.xlsx");
        assertThat(openApiComponent.getRulesModuleName())
                .as("Rules module name should default to 'Algorithms'")
                .isEqualTo("Algorithms");
        assertThat(openApiComponent.getRulesModulePathDisplay())
                .as("Rules module path should default to 'rules/Algorithms.xlsx'")
                .isEqualTo("rules/Algorithms.xlsx");

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
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Models");
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isFolderExistsInTree("Datatype"))
                .as("Models module should have Datatype folder")
                .isTrue();
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isFolderExistsInTree("Spreadsheet"))
                .as("Models module should NOT have Spreadsheet folder")
                .isFalse();
        editorPage.getProblemsPanelComponent().checkNoProblems();
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
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isFolderExistsInTree("Spreadsheet"))
                .as("Spreadsheets module should have Spreadsheet folder")
                .isTrue();
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isFolderExistsInTree("Datatype"))
                .as("Spreadsheets module should NOT have Datatype folder")
                .isFalse();
        editorPage.getProblemsPanelComponent().checkNoProblems();

        // Verify Data_Types module folder types
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Data_Types");
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isFolderExistsInTree("Datatype"))
                .as("Data_Types module should have Datatype folder")
                .isTrue();
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isFolderExistsInTree("Spreadsheet"))
                .as("Data_Types module should NOT have Spreadsheet folder")
                .isFalse();
        editorPage.getProblemsPanelComponent().checkNoProblems();
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

        // Validate: same module names error
        repositoryPage.getCreateProjectLink().click();
        OpenApiComponent openApiComponent = repositoryPage.getCreateNewProjectComponent()
                .selectTab(CreateNewProjectComponent.TabName.OPEN_API);
        openApiComponent.uploadOpenApiFile(JSON_FILE);
        openApiComponent.setProjectName("bla_" + System.currentTimeMillis());
        openApiComponent.setRulesModuleName("Models");
        openApiComponent.clickCreate();

        assertThat(openApiComponent.getErrorMessage())
                .as("Error should appear when module names are the same")
                .contains("Module names cannot be the same");

        // Validate: same module paths error
        repositoryPage.getCreateProjectLink().click();
        openApiComponent = repositoryPage.getCreateNewProjectComponent()
                .selectTab(CreateNewProjectComponent.TabName.OPEN_API);
        openApiComponent.uploadOpenApiFile(JSON_FILE);
        openApiComponent.setProjectName("bla2_" + System.currentTimeMillis());
        openApiComponent.clickEditDataPath();
        openApiComponent.setDataModulePath("rules/Models.xlsx");
        openApiComponent.clickCreate();

        assertThat(openApiComponent.getErrorMessage())
                .as("Error should appear when module paths are the same")
                .contains("Path for Modules cannot be the same");

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

        // Expand tree and delete openapi.yml
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .expandFolderInTree(projectName)
                .expandFolderInTree("rules")
                .selectItemInFolder("rules", "openapi.yml");

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
    }
}
