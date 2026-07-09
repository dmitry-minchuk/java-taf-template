package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.EditorToolbarPanelComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EPBDS-16160 (automated per EPBDS-16183) — selecting the SpreadsheetResult node of a
 * self-referential spreadsheet (MyRule returns {@code new SpreadsheetResult()} / an array of them)
 * in the Trace screen used to fail with "Handler dispatch failed: java.lang.StackOverflowError" and
 * the UI showed "Failed to load node details." — the node details must load without errors.
 */
public class TestTraceSpreadsheetNodeDetails extends BaseTest {

    private static final String MODULE_NAME = "generalProject";
    private static final String TRACED_TABLE = "MyRule";

    @Test
    @TestCaseId("EPBDS-16160")
    @Description("BUG EPBDS-16160 (QAA EPBDS-16183): selecting the SpreadsheetResult trace node of a "
            + "self-referential spreadsheet loads the node details without StackOverflowError.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testSpreadsheetResultNodeDetailsLoadWithoutError() {
        String projectName = WorkflowService.loginCreateProjectFromZip(User.ADMIN, "StudioIssues.TestTraceSpreadsheetNodeDetails.zip");
        EditorPage editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, MODULE_NAME);
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", TRACED_TABLE);

        // MyRule has no parameters, so Trace opens the step debugger directly (no params menu).
        EditorToolbarPanelComponent.ITraceWindow traceWindow = editorPage.getEditorToolbarPanelComponent()
                .clickTraceExpectTraceWindow();

        assertThat(traceWindow.getCallTreeTitles())
                .as("call tree shows the traced spreadsheet frame")
                .anyMatch(node -> node.contains(TRACED_TABLE));

        traceWindow.selectTreeNode(0);

        assertThat(traceWindow.isNodeDetailsErrorDisplayed(3000))
                .as("'Failed to load node details.' must not be shown for the SpreadsheetResult node")
                .isFalse();
        assertThat(traceWindow.areDetailsDisplayed(10000))
                .as("node details are displayed to the user")
                .isTrue();
        assertThat(traceWindow.getDetailsText())
                .as("no StackOverflowError is surfaced in the trace details")
                .doesNotContain("StackOverflowError");
        traceWindow.close();
    }
}
