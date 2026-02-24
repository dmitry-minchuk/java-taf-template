package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TableComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Covered atomic tests (IPBQA-31073):
 *   2.8.3 - Create Data tables from OpenAPI GET methods
 *   - Verify Data tables are generated from OpenAPI GET methods (runtime and no-runtime variants)
 *   - Verify tables are editable and changes are saved correctly
 */
public class TestCreateDataTablesFromOpenApiGetMethod extends BaseTest {

    private static final String FILE_WITH_RUNTIME = "openapiDataRuntime.json";
    private static final String PROJECT_WITH_RUNTIME = "openapiDataRuntime_";

    private static final String FILE_NO_RUNTIME = "openapiDataNoRuntime.json";
    private static final String PROJECT_NO_RUNTIME = "openapiNoDataRuntime_";

    @Test
    @TestCaseId("IPBQA-31073")
    @Description("Create Data tables from OpenAPI GET methods with runtime context: verify tables exist and are editable")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCreateDataTablesFromOpenApiGetMethodWithRuntime() {
        String projectName = PROJECT_WITH_RUNTIME + System.currentTimeMillis();
        scenario(FILE_WITH_RUNTIME, projectName);
    }

    @Test
    @TestCaseId("IPBQA-31073")
    @Description("Create Data tables from OpenAPI GET methods without runtime context: verify tables exist and are editable")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCreateDataTablesFromOpenApiGetMethodNoRuntime() {
        String projectName = PROJECT_NO_RUNTIME + System.currentTimeMillis();
        scenario(FILE_NO_RUNTIME, projectName);
    }

    private void scenario(String fileName, String projectName) {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        // Create project from OpenAPI file
        repositoryPage.createProjectFromOpenApi(fileName, projectName);

        // Navigate to Editor and select Algorithms module
        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Algorithms");

        // Verify Spreadsheet items: non-data tables generated from API methods
        editorPage.getEditorLeftRulesTreeComponent().setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE);
        editorPage.getEditorLeftRulesTreeComponent().expandFolderInTree("Spreadsheet");
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isItemExistsInFolder("Spreadsheet", "MyNotDataTable1"))
                .as("MyNotDataTable1 should be present in Spreadsheet folder")
                .isTrue();
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isItemExistsInFolder("Spreadsheet", "getNotDatTable"))
                .as("getNotDatTable should be present in Spreadsheet folder")
                .isTrue();

        // Verify Data tables generated from GET methods exist
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isFolderExistsInTree("Data"))
                .as("Data folder should be present for tables generated from GET methods")
                .isTrue();
        editorPage.getEditorLeftRulesTreeComponent().expandFolderInTree("Data");
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isItemExistsInFolder("Data", "MyDatatypeData"))
                .as("MyDatatypeData table should be present in Data folder")
                .isTrue();
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isItemExistsInFolder("Data", "MystrData"))
                .as("MystrData table should be present in Data folder")
                .isTrue();
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isItemExistsInFolder("Data", "SuperDatatypeData"))
                .as("SuperDatatypeData table should be present in Data folder")
                .isTrue();
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isItemExistsInFolder("Data", "NewDatatypeData"))
                .as("NewDatatypeData table should be present in Data folder")
                .isTrue();

        // Verify MyDatatypeData table is editable
        editorPage.getEditorLeftRulesTreeComponent()
                .selectItemInFolder("Data", "MyDatatypeData");

        TableComponent table = editorPage.getCenterTable();
        table.doubleClickCell(3, 1);
        editorPage.getEditorTableActionsPanelComponent().clickInsertRowAfter();
        table.editCell(4, 1, "test1");
        table.editCell(4, 2, "100");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        assertThat(table.getCellText(4, 1))
                .as("First column of inserted row should contain 'test1'")
                .isEqualTo("test1");
        assertThat(table.getCellText(4, 2))
                .as("Second column of inserted row should contain '100'")
                .isEqualTo("100");

        editorPage.getProblemsPanelComponent().checkNoProblems();
    }
}
