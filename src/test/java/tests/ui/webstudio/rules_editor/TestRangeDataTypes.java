package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.RangeEditorComponent;
import domain.ui.webstudio.components.common.TableComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestRangeDataTypes extends BaseTest {

    private static final String EXCEL_FILE = "TestRangeDataTypes.xlsx";
    private static final String MODULE_NAME = "TestRangeDataTypes";

    @Test
    @TestCaseId("EPBDS-7489")
    @Description("Range Editor opens via double-click and closes on Done across all table types (Decision, Data, Run, Test, Vocabulary, Constants)")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testRangeDataTypes() {
        String projectName = WorkflowService.loginCreateProjectFromExcelFile(User.ADMIN, EXCEL_FILE);
        EditorPage editorPage = new EditorPage();
        EditorLeftRulesTreeComponent rulesTree = editorPage.getEditorLeftRulesTreeComponent();

        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, MODULE_NAME);
        rulesTree.setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE);

        validateRangeEditorOpensAndCloses(editorPage, rulesTree, "Decision", "SimpleLookupTable", 3, 2);
        validateRangeEditorOpensAndCloses(editorPage, rulesTree, "Decision", "SimpleRulesTable", 3, 2);
        validateRangeEditorOpensAndCloses(editorPage, rulesTree, "Decision", "SmartLookup1", 3, 1);
        validateRangeEditorOpensAndCloses(editorPage, rulesTree, "Decision", "SmartRules1", 5, 1);
        validateRangeEditorOpensAndCloses(editorPage, rulesTree, "Data", "DataTable1", 4, 2);
        validateRangeEditorOpensAndCloses(editorPage, rulesTree, "Run", "RunTable", 4, 2);
        validateRangeEditorOpensAndCloses(editorPage, rulesTree, "Test", "Test2", 4, 2);
        validateRangeEditorOpensAndCloses(editorPage, rulesTree, "Vocabulary", "Vocabulary1", 2, 1);
        validateRangeEditorOpensAndCloses(editorPage, rulesTree, "Constants", "Constants", 2, 3);
        // SpreadsheetTable validation skipped due to bug EPBDS-7484 (kept disabled in legacy as well).
    }

    private void validateRangeEditorOpensAndCloses(EditorPage editorPage,
                                                   EditorLeftRulesTreeComponent rulesTree,
                                                   String folderName,
                                                   String tableName,
                                                   int row,
                                                   int column) {
        rulesTree.expandFolderInTree(folderName)
                .selectItemInFolder(folderName, tableName);

        RangeEditorComponent rangeEditor = editorPage.getRangeEditorComponent();
        rangeEditor.discardChangesIfPresent();

        TableComponent table = editorPage.getCenterTable();
        table.doubleClickCell(row, column);

        assertThat(rangeEditor.isOpen(3000))
                .as("Range Editor is expected to be seen for the following cell (row: %d, column: %d) of %s/%s",
                        row, column, folderName, tableName)
                .isTrue();

        rangeEditor.clickDone();

        assertThat(rangeEditor.isOpen(1000))
                .as("Range Editor is expected to be closed for the following cell (row: %d, column: %d) of %s/%s",
                        row, column, folderName, tableName)
                .isFalse();
    }
}
