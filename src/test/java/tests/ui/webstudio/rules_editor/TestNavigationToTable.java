package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TableComponent;
import domain.ui.webstudio.components.editortabcomponents.EditorToolbarPanelComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNavigationToTable extends BaseTest {

    private static final String EXCEL_FILE = "TestNavigationToTable.xlsx";
    private static final String MODULE_NAME = "TestNavigationToTable";

    private static final String LINK_POSTFIX_TEST = "Test (1 test case)";
    private static final String LINK_POSTFIX_RUN = "Run (1 run)";
    private static final String HEADER_POSTFIX_TEST = "Test";
    private static final String HEADER_POSTFIX_RUN = "Run";

    @Test
    @TestCaseId("IPBQA-25912")
    @Description("Navigation from various source tables to their corresponding Test and Run tables via the 'Available Tests/Runs' panel; multi-result popup expansion for SpreadsheetTable3")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testNavigationToTable() {
        String projectName = WorkflowService.loginCreateProjectFromExcelFile(User.ADMIN, EXCEL_FILE);
        EditorPage editorPage = new EditorPage();
        EditorLeftRulesTreeComponent rulesTree = editorPage.getEditorLeftRulesTreeComponent();

        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, MODULE_NAME);
        rulesTree.setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE);

        verifyNavigationWorkForTable(editorPage, rulesTree, "Decision", "SimpleLookupTable1", LINK_POSTFIX_TEST, HEADER_POSTFIX_TEST);
        verifyNavigationWorkForTable(editorPage, rulesTree, "Decision", "SimpleRulesTable1", LINK_POSTFIX_TEST, HEADER_POSTFIX_TEST);
        verifyNavigationWorkForTable(editorPage, rulesTree, "Decision", "SmartLookup1", LINK_POSTFIX_TEST, HEADER_POSTFIX_TEST);
        verifyNavigationWorkForTable(editorPage, rulesTree, "Decision", "SmartRules1", LINK_POSTFIX_TEST, HEADER_POSTFIX_TEST);
        verifyNavigationWorkForTable(editorPage, rulesTree, "Spreadsheet", "SpreadsheetTable1", LINK_POSTFIX_TEST, HEADER_POSTFIX_TEST);
        verifyNavigationWorkForTable(editorPage, rulesTree, "TBasic", "TBasicTable1", LINK_POSTFIX_TEST, HEADER_POSTFIX_TEST);
        verifyNavigationWorkForTable(editorPage, rulesTree, "Column Match", "ColumnMatchTable1", LINK_POSTFIX_TEST, HEADER_POSTFIX_TEST);
        verifyNavigationWorkForTable(editorPage, rulesTree, "Method", "MethodTable1", LINK_POSTFIX_TEST, HEADER_POSTFIX_TEST);

        verifyNavigationWorkForTable(editorPage, rulesTree, "Decision", "SimpleLookupTable2", LINK_POSTFIX_RUN, HEADER_POSTFIX_RUN);
        verifyNavigationWorkForTable(editorPage, rulesTree, "Decision", "SimpleRulesTable2", LINK_POSTFIX_RUN, HEADER_POSTFIX_RUN);
        verifyNavigationWorkForTable(editorPage, rulesTree, "Decision", "SmartLookup2", LINK_POSTFIX_RUN, HEADER_POSTFIX_RUN);
        verifyNavigationWorkForTable(editorPage, rulesTree, "Decision", "SmartRules2", LINK_POSTFIX_RUN, HEADER_POSTFIX_RUN);
        verifyNavigationWorkForTable(editorPage, rulesTree, "Spreadsheet", "SpreadsheetTable2", LINK_POSTFIX_RUN, HEADER_POSTFIX_RUN);
        verifyNavigationWorkForTable(editorPage, rulesTree, "TBasic", "TBasicTable2", LINK_POSTFIX_RUN, HEADER_POSTFIX_RUN);
        verifyNavigationWorkForTable(editorPage, rulesTree, "Column Match", "ColumnMatchTable2", LINK_POSTFIX_RUN, HEADER_POSTFIX_RUN);
        verifyNavigationWorkForTable(editorPage, rulesTree, "Method", "MethodTable2", LINK_POSTFIX_RUN, HEADER_POSTFIX_RUN);

        rulesTree.expandFolderInTree("Spreadsheet").selectItemInFolder("Spreadsheet", "SpreadsheetTable4");
        assertThat(editorPage.getEditorToolbarPanelComponent().isAvailableTestRunsLinkVisible())
                .as("'Available Tests/Runs' panel should not be visible for SpreadsheetTable4")
                .isFalse();

        rulesTree.expandFolderInTree("Spreadsheet").selectItemInFolder("Spreadsheet", "SpreadsheetTable3");
        EditorToolbarPanelComponent toolbar = editorPage.getEditorToolbarPanelComponent();
        toolbar.clickAvailableTestRunsExpandLink();
        assertThat(toolbar.getAvailableTestRunsPopupText())
                .as("Expanded popup contents for SpreadsheetTable3")
                .isEqualTo("SpreadsheetTable3Run2 (2 runs)\n"
                        + "SpreadsheetTable3Run3 (3 runs)\n"
                        + "SpreadsheetTable3Test1 (1 test case)\n"
                        + "SpreadsheetTable3Test2 (2 test cases)\n"
                        + "SpreadsheetTable3Test3 (3 test cases)");
        toolbar.clickAvailableTestRunsInlineLink();

        TableComponent appearedTable = editorPage.getCenterTable();
        assertThat(appearedTable.isVisible())
                .as("Center table should be visible after navigation to SpreadsheetTable3Run1")
                .isTrue();
        assertThat(appearedTable.getCellText(1, 2))
                .as("Header cell of SpreadsheetTable3Run1 should reference SpreadsheetTable3")
                .contains("SpreadsheetTable3");
    }

    private void verifyNavigationWorkForTable(EditorPage editorPage,
                                              EditorLeftRulesTreeComponent rulesTree,
                                              String folderName,
                                              String tableName,
                                              String linkPostfix,
                                              String headerPostfix) {
        rulesTree.expandFolderInTree(folderName)
                .selectItemInFolder(folderName, tableName);

        EditorToolbarPanelComponent toolbar = editorPage.getEditorToolbarPanelComponent();
        assertThat(toolbar.isAvailableTestRunsLinkVisible())
                .as("'Available Tests/Runs' panel should be visible for %s/%s", folderName, tableName)
                .isTrue();

        toolbar.clickAvailableTestRunsInlineLink();

        TableComponent appearedTable = editorPage.getCenterTable();
        assertThat(appearedTable.isVisible())
                .as("Center table should be visible after navigation from %s/%s", folderName, tableName)
                .isTrue();
        assertThat(appearedTable.getCellText(1, 2))
                .as("Header cell of %s%s should reference %s", tableName, headerPostfix, tableName)
                .contains(tableName + headerPostfix);
    }
}
