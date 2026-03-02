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

public class TestCreateDataTablesFromOpenApiGetMethodNoRuntime extends BaseTest {

    private static final String FILE_NO_RUNTIME = "openapiDataNoRuntime.json";
    private static final String PROJECT_NO_RUNTIME = "openapiNoDataRuntime_";

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

        repositoryPage.createProjectFromOpenApi(fileName, projectName);

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Algorithms");

        editorPage.getEditorLeftRulesTreeComponent().setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE);
        editorPage.getEditorLeftRulesTreeComponent().expandFolderInTree("Spreadsheet");
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isItemExistsInFolder("Spreadsheet", "MyNotDataTable1"))
                .as("MyNotDataTable1 should be present in Spreadsheet folder")
                .isTrue();
        assertThat(editorPage.getEditorLeftRulesTreeComponent().isItemExistsInFolder("Spreadsheet", "getNotDatTable"))
                .as("getNotDatTable should be present in Spreadsheet folder")
                .isTrue();

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

        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Data", "MyDatatypeData");
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

        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Data", "MystrData");
        table = editorPage.getCenterTable();
        table.doubleClickCell(3, 1);
        editorPage.getEditorTableActionsPanelComponent().clickInsertRowAfter();
        table.editCell(4, 1, "test1");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        assertThat(table.getCellText(4, 1))
                .as("Single column of inserted row should contain 'test1'")
                .isEqualTo("test1");

        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Data", "SuperDatatypeData");
        table = editorPage.getCenterTable();
        table.doubleClickCell(2, 1);
        editorPage.getEditorTableActionsPanelComponent().clickInsertRowAfter();
        table.editCell(3, 1, ">MyDatatypeData");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        table.doubleClickCell(4, 1);
        editorPage.getEditorTableActionsPanelComponent().clickInsertRowAfter();
        table.editCell(5, 1, "test1");
        table.editCell(5, 2, "someValue");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        assertThat(table.getCellText(3, 1)).as("Reference row should contain '>MyDatatypeData'").isEqualTo(">MyDatatypeData");
        assertThat(table.getCellText(5, 1)).as("Data row first column should contain 'test1'").isEqualTo("test1");
        assertThat(table.getCellText(5, 2)).as("Data row second column should contain 'someValue'").isEqualTo("someValue");

        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Data", "NewDatatypeData");
        table = editorPage.getCenterTable();
        table.doubleClickCell(2, 1);
        editorPage.getEditorTableActionsPanelComponent().clickInsertRowAfter();
        table.editCell(3, 1, ">MyDatatypeData");
        table.editCell(3, 2, ">MyDatatypeData");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        table.doubleClickCell(4, 1);
        editorPage.getEditorTableActionsPanelComponent().clickInsertRowAfter();
        table.editCell(5, 1, "test1");
        table.editCell(5, 2, "test1");
        table.editCell(5, 3, "someValue");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        assertThat(table.getCellText(3, 1)).as("Reference row col 1 should contain '>MyDatatypeData'").isEqualTo(">MyDatatypeData");
        assertThat(table.getCellText(3, 2)).as("Reference row col 2 should contain '>MyDatatypeData'").isEqualTo(">MyDatatypeData");
        assertThat(table.getCellText(5, 1)).as("Data row col 1 should contain 'test1'").isEqualTo("test1");
        assertThat(table.getCellText(5, 2)).as("Data row col 2 should contain 'test1'").isEqualTo("test1");
        assertThat(table.getCellText(5, 3)).as("Data row col 3 should contain 'someValue'").isEqualTo("someValue");

        editorPage.getProblemsPanelComponent().checkNoProblems();
    }
}
