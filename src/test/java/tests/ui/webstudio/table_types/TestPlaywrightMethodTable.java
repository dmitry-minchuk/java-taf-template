package tests.ui.webstudio.table_types;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import com.microsoft.playwright.Dialog;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.core.ui.PlaywrightTableComponent;
import configuration.driver.PlaywrightDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.PlaywrightLeftRulesTreeComponent;
import domain.ui.webstudio.components.editortabcomponents.PlaywrightCopyTableDialogComponent;
import domain.ui.webstudio.components.editortabcomponents.PlaywrightTableToolbarPanelComponent;
import domain.ui.webstudio.pages.mainpages.PlaywrightEditorPage;
import helpers.service.PlaywrightWorkflowService;
import helpers.utils.StringUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestPlaywrightMethodTable extends BaseTest {

    @Test
    @TestCaseId("IPBQA-31319")
    @Description("Test Method table operations: creation, editing, run, table management")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testPlaywrightMethodTable() {
        String projectName = PlaywrightWorkflowService.loginCreateProjectFromExcelFile(User.ADMIN, "TestMethodTable.xlsx");
        PlaywrightEditorPage editorPage = new PlaywrightEditorPage();

        editorPage.getLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "TestMethodTable");
        editorPage.getLeftRulesTreeComponent()
                .setViewFilter(PlaywrightLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Method")
                .selectItemInFolder("Method", "getGreetings");

        editorPage.getProblemsPanelComponent().checkNoProblems();

        // Get the center table and verify initial content
        PlaywrightTableComponent table = editorPage.getCenterTable();
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

    private void runMethodTest(PlaywrightEditorPage editorPage, String inputParam, List<String> expectedResult) {
        var runMenu = editorPage.getTableToolbarPanelComponent().clickRun();
        runMenu.setInputTextField("1", inputParam)
                .clickRunInsideMenu();
        List<String> actualResult = getTestResult(1);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    private void testTableEditingOperations(PlaywrightEditorPage editorPage, PlaywrightTableComponent table) {
        editorPage.getLeftRulesTreeComponent()
                .selectItemInFolder("Method", "getGreetings");

        table.doubleClickCell(2, 1);
        editorPage.getEditTablePanelComponent()
                .getInsertRowAfterBtn()
                .click();
        table.editCell(3, 1, "return \"Happy Birthday, \"+name;");
        
        // Verify table now has 3 rows
        assertThat(table.getRowsCount()).isEqualTo(3);
        assertThat(table.getRow(1).getValue()).containsExactly("Method String getGreetings (String name)");
        assertThat(table.getRow(2).getValue()).anyMatch(cell -> cell.contains("return \"Hi,\"+name"));
        assertThat(table.getRow(3).getValue()).containsExactly("return \"Happy Birthday, \"+name;");

        // Remove the added row
        table.doubleClickCell(3, 1);
        editorPage.getEditTablePanelComponent().getRemoveRowBtn().click();
        editorPage.getEditTablePanelComponent().getSaveChangesBtn().click();
        editorPage.getProblemsPanelComponent().checkNoProblems();
        
        // Verify table is back to original state
        assertThat(table.getRowsCount()).isEqualTo(2);
        assertThat(table.getRow(1).getValue()).containsExactly("Method String getGreetings (String name)");
        assertThat(table.getRow(2).getValue()).anyMatch(cell -> cell.contains("return \"Hi,\"+name"));
    }

    private void testTableCopyAndManagement(PlaywrightEditorPage editorPage) {
        copyTableAsNew("getGreetings2", "");
        
        // Verify both tables exist in rules tree
        editorPage.getLeftRulesTreeComponent().checkRulesTablePresent("Method", "getGreetings");
        editorPage.getLeftRulesTreeComponent().checkRulesTablePresent("Method", "getGreetings2");

        // Select and remove the copied table
        editorPage.getLeftRulesTreeComponent().selectItemInFolder("Method", "getGreetings2");
        removeCurrentTable();
        
        // Verify tree state after removal
        editorPage.getLeftRulesTreeComponent().expandFolderInTree("Method");
        editorPage.getLeftRulesTreeComponent().checkRulesTableAbsent("Method", "getGreetings2");
        editorPage.getLeftRulesTreeComponent().checkRulesTablePresent("Method", "getGreetings");
    }

    private List<String> getTestResult(int rowIndex) {
        // Use existing PlaywrightTestResultValidationComponent to get test result data
        PlaywrightEditorPage editorPage = new PlaywrightEditorPage();
        return editorPage.getTestResultValidationComponent().getTestResultData(rowIndex);
    }

    private void copyTableAsNew(String newName, String description) {
        // Use existing PlaywrightTableToolbarPanelComponent to trigger copy dialog
        PlaywrightEditorPage editorPage = new PlaywrightEditorPage();
        
        // 1. Click Copy button in toolbar
        editorPage.getTableToolbarPanelComponent().clickCopy();
        
        // 2. Work with copy dialog
        PlaywrightCopyTableDialogComponent copyDialog = new PlaywrightCopyTableDialogComponent();
        copyDialog.selectCopyAs("New Table")
                  .setName(newName);
        
        // Set save location if description is provided
        if (description != null && !description.isEmpty()) {
            copyDialog.setSaveTo(description);
        }
        
        // 3. Execute copy
        copyDialog.clickCopy();
    }

    private void removeCurrentTable() {
        // Use existing PlaywrightTableToolbarPanelComponent to trigger table removal
        PlaywrightEditorPage editorPage = new PlaywrightEditorPage();
        
        // 1. Click Remove button in toolbar
        editorPage.getTableToolbarPanelComponent().clickRemove();
        
        // 2. Handle confirmation dialog - Playwright automatically handles alerts
        PlaywrightDriverPool.getPage().onDialog(Dialog::accept);
    }
}