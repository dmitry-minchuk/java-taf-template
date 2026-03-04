package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.CompareExcelFilesDialogComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import helpers.utils.TestDataUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCompareExcelFiles extends BaseTest {

    private static final String FILE_1 = "All_tables_type.xlsx";
    private static final String FILE_2 = "All_tables_type2.xlsx";

    @Test
    @TestCaseId("IPBQA-28380")
    @Description("Compare Excel files functionality: upload two Excel files, verify tree structure, cell differences and highlighting")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCompareExcelFiles() {
        WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        EditorPage editorPage = new EditorPage();

        CompareExcelFilesDialogComponent compareDialog = editorPage
                .getEditorToolbarPanelComponent()
                .clickMore()
                .clickCompareExcelFiles();

        compareDialog.uploadFile(TestDataUtil.getFilePathFromResources(FILE_1));
        compareDialog.uploadFile(TestDataUtil.getFilePathFromResources(FILE_2));

        // Verification of EPBDS-3227: Add button should be absent when 2 files are uploaded
        assertThat(compareDialog.isAddButtonPresent())
                .as("Add button should be absent when 2 files are already uploaded (EPBDS-3227)")
                .isFalse();

        // Verification of EPBDS-9988: second element in upload list has no bottom border
        assertThat(compareDialog.getSecondElementBorderBottomStyle())
                .as("Second element in upload list should have no bottom border (EPBDS-9988)")
                .isEqualTo("none");

        // Verification of EPBDS-3446: Clear all → re-upload → Compare button present and enabled
        compareDialog.clickClearAll();
        compareDialog.uploadFile(TestDataUtil.getFilePathFromResources(FILE_1));
        compareDialog.uploadFile(TestDataUtil.getFilePathFromResources(FILE_2));

        assertThat(compareDialog.isCompareExcelBtnPresent())
                .as("Compare button should be present after uploading files (EPBDS-3446)")
                .isTrue();
        assertThat(compareDialog.isCompareExcelBtnEnabled())
                .as("Compare button should be enabled after uploading files (EPBDS-3446)")
                .isTrue();

        compareDialog.setShowEqualRowsExcel(true);
        compareDialog.clickCompareExcel();

        // Tree should show changed sheets (Rules, Old Sheet, New test tab) but not Const (equal)
        assertThat(compareDialog.isTreeItemPresent("Rules")).as("Rules node present").isTrue();
        assertThat(compareDialog.isTreeItemPresent("Old Sheet")).as("Old Sheet node present").isTrue();
        assertThat(compareDialog.isTreeItemPresent("New test tab")).as("New test tab node present").isTrue();
        assertThat(compareDialog.isTreeItemPresent("Const")).as("Const should be absent when showEqualElements=false").isFalse();

        // Verify differences in each changed table node under Rules
        compareDialog.openTreeNode("Rules");
        assertThat(compareDialog.isTreeItemPresent("Spreadsheet SpreadsheetResult SpreadsheetTable (ByteValue a_byte)")).isTrue();
        assertThat(compareDialog.isTreeItemPresent("SimpleRules int SimpleRulesTable(Boolean CondValue)")).isTrue();
        assertThat(compareDialog.isTreeItemPresent("SimpleRules BigDecimal SimpleRuleTable(integer e_var)")).isTrue();
        assertThat(compareDialog.isTreeItemPresent("Datatype Vocabulary1 <String[]>")).isTrue();
        assertThat(compareDialog.isTreeItemPresent("SmartLookup DoubleValue SmartLookup1(int intValue, String arg2)")).isTrue();
        assertThat(compareDialog.isTreeItemPresent("Conditions  testConditions")).isTrue();
        assertThat(compareDialog.isTreeItemPresent("Returns")).isTrue();

        verifyDifferenceInCells(compareDialog, "Spreadsheet SpreadsheetResult SpreadsheetTable (ByteValue a_byte)", 3, 3, "200", "201");
        verifyDifferenceInCells(compareDialog, "SimpleRules int SimpleRulesTable(Boolean CondValue)", 2, 1, "Val", "Val1");
        verifyDifferenceInCells(compareDialog, "SimpleRules BigDecimal SimpleRuleTable(integer e_var)", 4, 1, "2", "3");
        verifyDifferenceInCells(compareDialog, "Datatype Vocabulary1 <String[]>", 3, 1, "Bla3, Bla4", "Bla3, Bla4. Bla1");
        verifyDifferenceInCells(compareDialog, "SmartLookup DoubleValue SmartLookup1(int intValue, String arg2)", 7, 2, "0.05", "0.005");
        verifyDifferenceInCells(compareDialog, "Conditions  testConditions", 3, 2, "a==d.b", "a==c.b");
        verifyDifferenceInCells(compareDialog, "Conditions  testConditions", 4, 2, "Integer a", "Integer c");
        verifyDifferenceInCells(compareDialog, "Returns", 3, 2, "new Double[] {a, b, c, d}", "new Double[] {a, b, c}");

        // ColumnMatch subtree: verify single-side tables (only in one file)
        compareDialog.openTreeNode("ColumnMatch <MATCH> String ColumnMatchTable(Long RandNumber)");
        assertThat(compareDialog.isTreeItemPresent("SmartRules DoubleValue SmartRules1(String stringValue, int integerValue)")).isTrue();
        assertThat(compareDialog.isTreeItemPresent("Actions")).isTrue();

        // SmartRules1: only in first file (firstFragment present, secondFragment absent)
        compareDialog.clickTreeNode("SmartRules DoubleValue SmartRules1(String stringValue, int integerValue)");
        assertThat(compareDialog.isFirstFragmentPresent()).as("SmartRules1 should have first fragment").isTrue();
        assertThat(compareDialog.isSecondFragmentPresent()).as("SmartRules1 should have no second fragment").isFalse();

        // Actions: only in second file (firstFragment absent, secondFragment present)
        compareDialog.clickTreeNode("Actions");
        assertThat(compareDialog.isFirstFragmentPresent()).as("Actions should have no first fragment").isFalse();
        assertThat(compareDialog.isSecondFragmentPresent()).as("Actions should have second fragment").isTrue();

        // Old Sheet: SimpleRuleTable2 only in first file
        compareDialog.openTreeNode("Old Sheet");
        assertThat(compareDialog.isTreeItemPresent("SimpleRules BigDecimal SimpleRuleTable2(integer e_var)")).isTrue();

        compareDialog.clickTreeNode("SimpleRules BigDecimal SimpleRuleTable2(integer e_var)");
        assertThat(compareDialog.isFirstFragmentPresent()).isTrue();
        assertThat(compareDialog.isSecondFragmentPresent()).isFalse();

        // New test tab: someRule only in second file
        compareDialog.openTreeNode("New test tab");
        assertThat(compareDialog.isTreeItemPresent("SmartRules String someRule(Integer a)")).isTrue();

        compareDialog.clickTreeNode("SmartRules String someRule(Integer a)");
        assertThat(compareDialog.isFirstFragmentPresent()).isFalse();
        assertThat(compareDialog.isSecondFragmentPresent()).isTrue();

        // Re-upload files and enable showEqualElements to see the Const sheet
        compareDialog.uploadFile(TestDataUtil.getFilePathFromResources(FILE_1));
        compareDialog.uploadFile(TestDataUtil.getFilePathFromResources(FILE_2));
        compareDialog.setShowEqualElements(true);
        compareDialog.clickCompareExcel();

        assertThat(compareDialog.isTreeItemPresent("Rules")).isTrue();
        assertThat(compareDialog.isTreeItemPresent("Old Sheet")).isTrue();
        assertThat(compareDialog.isTreeItemPresent("New test tab")).isTrue();
        assertThat(compareDialog.isTreeItemPresent("Const")).isTrue();

        compareDialog.openTreeNode("Const");
        assertThat(compareDialog.isTreeItemPresent("Constants")).isTrue();
        assertThat(compareDialog.isTreeItemPresent("SimpleRules int SimpleRulesTable(Boolean CondValue)")).isTrue();

        // Constants: equal in both files → both fragments present
        compareDialog.clickTreeNode("Constants");
        assertThat(compareDialog.isFirstFragmentPresent()).isTrue();
        assertThat(compareDialog.isSecondFragmentPresent()).isTrue();

        // SimpleRulesTable: equal in both files → both fragments present
        compareDialog.clickTreeNode("SimpleRules int SimpleRulesTable(Boolean CondValue)");
        assertThat(compareDialog.isFirstFragmentPresent()).isTrue();
        assertThat(compareDialog.isSecondFragmentPresent()).isTrue();

        compareDialog.close();
    }

    private void verifyDifferenceInCells(CompareExcelFilesDialogComponent dialog,
                                         String nodeName, int row, int col,
                                         String expectedValue1, String expectedValue2) {
        dialog.clickTreeNode(nodeName);
        assertThat(dialog.isFirstFragmentPresent()).as(nodeName + " should have first fragment").isTrue();
        assertThat(dialog.isSecondFragmentPresent()).as(nodeName + " should have second fragment").isTrue();
        assertThat(dialog.getCellContent(1, row, col))
                .as(nodeName + " cell[" + row + "," + col + "] in first file")
                .isEqualTo(expectedValue1);
        assertThat(dialog.getCellContent(2, row, col))
                .as(nodeName + " cell[" + row + "," + col + "] in second file")
                .isEqualTo(expectedValue2);
        dialog.isCellHighlighted(row, col, 1);
        dialog.isCellHighlighted(row, col, 2);
    }
}
