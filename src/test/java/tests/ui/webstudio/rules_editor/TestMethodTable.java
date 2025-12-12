package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.ui.webstudio.components.common.TableComponent;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestMethodTable extends BaseTest {

    @Test
    @TestCaseId("IPBQA-31319")
    @Description("Test Method table operations: creation, editing, run, table management")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testMethodTable() {
        String projectName = WorkflowService.loginCreateProjectFromExcelFile(User.ADMIN, "TestMethodTable.xlsx");
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "TestMethodTable");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Method")
                .selectItemInFolder("Method", "getGreetings");

        editorPage.getProblemsPanelComponent().checkNoProblems();

        // Get the center table and verify initial content
        TableComponent table = editorPage.getCenterTable();
        assertThat(table.getRowsCount()).isEqualTo(2);
        
        // Verify row 1 content: Method signature
        List<String> row1Content = table.getRow(1).getValue();
        assertThat(row1Content).containsExactly("Method String getGreetings (String name)");
        
        // Verify row 2 content: Method body
        List<String> row2Content = table.getRow(2).getValue();
        assertThat(row2Content).anyMatch(cell -> cell.contains("return \"Hi,\"+name"));

        // Run the method with test parameter
        runMethodTest(editorPage, "Tom", Arrays.asList("1", "Tom", "Hi,Tom"));

        // Test table editing operations
        testTableEditingOperations(editorPage, table);

        // Test table copy and management operations  
        testTableCopyAndManagement(editorPage);
    }

    private void runMethodTest(EditorPage editorPage, String inputParam, List<String> expectedResult) {
        var runMenu = editorPage.getEditorToolbarPanelComponent().clickRun();
        runMenu.setInputTextField("1", inputParam)
                .clickRunInsideMenu();
        List<String> actualResult = editorPage.getTestResultValidationComponent().getTestResult(1);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    private void testTableEditingOperations(EditorPage editorPage, TableComponent table) {
        editorPage.getEditorLeftRulesTreeComponent()
                .selectItemInFolder("Method", "getGreetings");

        table.doubleClickCell(2, 1);
        editorPage.getEditorTableActionsPanelComponent()
                .clickInsertRowAfter();
        table.editCell(3, 1, "return \"Happy Birthday, \"+name;");
        
        // Verify table now has 3 rows
        assertThat(table.getRowsCount()).isEqualTo(3);
        assertThat(table.getRow(1).getValue()).containsExactly("Method String getGreetings (String name)");
        assertThat(table.getRow(2).getValue()).anyMatch(cell -> cell.contains("return \"Hi,\"+name"));
        assertThat(table.getRow(3).getValue()).containsExactly("return \"Happy Birthday, \"+name;");

        // Remove the added row
        table.doubleClickCell(3, 1);
        editorPage.getEditorTableActionsPanelComponent().clickRemoveRow();
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();
        editorPage.getProblemsPanelComponent().checkNoProblems();
        
        // Verify table is back to original state
        assertThat(table.getRowsCount()).isEqualTo(2);
        assertThat(table.getRow(1).getValue()).containsExactly("Method String getGreetings (String name)");
        assertThat(table.getRow(2).getValue()).anyMatch(cell -> cell.contains("return \"Hi,\"+name"));
    }

    private void testTableCopyAndManagement(EditorPage editorPage) {
        editorPage.getEditorToolbarPanelComponent().copyTableAsNew("getGreetings2", "");
        
        // Verify both tables exist in rules tree
        editorPage.getEditorLeftRulesTreeComponent().checkRulesTablePresent("Method", "getGreetings");
        editorPage.getEditorLeftRulesTreeComponent().checkRulesTablePresent("Method", "getGreetings2");

        // Select and remove the copied table
        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Method", "getGreetings2");
        editorPage.getEditorToolbarPanelComponent().removeCurrentTable();
        
        // Verify tree state after removal
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Method");
        editorPage.getEditorLeftRulesTreeComponent().checkRulesTableAbsent("Method", "getGreetings2");
        editorPage.getEditorLeftRulesTreeComponent().checkRulesTablePresent("Method", "getGreetings");
    }
}