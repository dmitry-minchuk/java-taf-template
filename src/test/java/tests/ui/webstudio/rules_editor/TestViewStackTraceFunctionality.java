package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestViewStackTraceFunctionality extends BaseTest {

    private static final List<List<String>> CATALOG_AND_TABLE_NAMES = Arrays.asList(
            Arrays.asList("Decision", "SimpleLookupTable"),
            Arrays.asList("Decision", "SimpleRulesTable"),
            Arrays.asList("Decision", "SmartLookup1"),
            Arrays.asList("Decision", "SmartRules1"),
            Arrays.asList("Spreadsheet", "SpreadsheetTable"),
            Arrays.asList("TBasic", "TBasicTable"),
            Arrays.asList("Column Match", "ColumnMatchTable"),
            Arrays.asList("Data", "myTable"),
            Arrays.asList("Run", "RunTable"),
            Arrays.asList("Test", "Test1"),
            Arrays.asList("Datatype", "dataTest"),
            Arrays.asList("Method", "MethodTable"),
            Arrays.asList("Constants", "Constants")
    );

    @Test
    @TestCaseId("IPBQA-25869")
    @Description("Test stack trace show/hide functionality for all table types")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testViewStackTraceFunctionality() {
        String projectName = WorkflowService.loginCreateProjectFromZip(User.ADMIN, "TestViewStackTraceFunctionality.zip");
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "All_tables_type");

        CATALOG_AND_TABLE_NAMES.forEach(item -> checkStackTraceView(editorPage, item.get(0), item.get(1)));
    }

    private void checkStackTraceView(EditorPage editorPage, String catalogName, String tableName) {
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree(catalogName)
                .selectItemInFolder(catalogName, tableName);

        // Show stack trace and verify it's visible
        editorPage.getProblemsPanelComponent().showStackTrace();
        assertThat(editorPage.getProblemsPanelComponent().isStackTraceVisible())
                .as("Stack trace should be visible after clicking show button for %s/%s", catalogName, tableName)
                .isTrue();

        // Hide via bottom button and verify it's hidden
        editorPage.getProblemsPanelComponent().hideStackTraceBottom();
        assertThat(editorPage.getProblemsPanelComponent().isStackTraceVisible())
                .as("Stack trace should be hidden after clicking bottom close button for %s/%s", catalogName, tableName)
                .isFalse();

        // Show again
        editorPage.getProblemsPanelComponent().showStackTrace();
        assertThat(editorPage.getProblemsPanelComponent().isStackTraceVisible())
                .as("Stack trace should be visible again after clicking show button for %s/%s", catalogName, tableName)
                .isTrue();

        // Hide via top button and verify it's hidden
        editorPage.getProblemsPanelComponent().hideStackTraceTop();
        assertThat(editorPage.getProblemsPanelComponent().isStackTraceVisible())
                .as("Stack trace should be hidden after clicking top close button for %s/%s", catalogName, tableName)
                .isFalse();
    }
}
