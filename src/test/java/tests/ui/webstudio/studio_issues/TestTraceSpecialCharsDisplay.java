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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EPBDS-16151 (automated per EPBDS-16186) — the traced-table HTML was double-escaped, so special
 * characters showed up as literal entities (e.g. {@code &lt;} instead of {@code <}) in the Trace
 * screen. The project's callTheMessage spreadsheet returns a string full of special characters;
 * after tracing it, the trace tree nodes, the Returned Result and the Traced Table must show the
 * raw characters, never literal {@code &lt;}/{@code &gt;}/{@code &quot;}/{@code &amp;}.
 */
public class TestTraceSpecialCharsDisplay extends BaseTest {

    private static final String MODULE_NAME = "generalProject";
    private static final String TRACED_TABLE = "callTheMessage";
    // Stable fragments of the special-characters value: $'{'"[[]!"№%:,.;(()_+-{}`'"|./>?,<
    private static final String VALUE_CORE = "№%:,.;(()_+-{}";
    private static final String VALUE_TAIL = "/>?,<";
    private static final List<String> LITERAL_ENTITIES = List.of("&lt;", "&gt;", "&quot;", "&amp;");

    @Test
    @TestCaseId("EPBDS-16151")
    @Description("BUG EPBDS-16151 (QAA EPBDS-16186): double quotes and angle brackets are displayed "
            + "correctly (not as literal HTML entities) in the trace tree, Returned Result and Traced Table.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testSpecialCharsDisplayedCorrectlyInTrace() {
        String projectName = WorkflowService.loginCreateProjectFromZip(User.ADMIN, "StudioIssues.TestTraceSpecialCharsDisplay.zip");
        EditorPage editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, MODULE_NAME);
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", TRACED_TABLE);

        // callTheMessage has no parameters, so Trace opens the trace popup directly (no params menu).
        EditorToolbarPanelComponent.ITraceWindow traceWindow = editorPage.getEditorToolbarPanelComponent()
                .clickTraceExpectTraceWindow();

        List<String> treeNodes = traceWindow.expandItemInTree(0).getVisibleItemsFromTree();
        assertThat(treeNodes)
                .as("trace tree shows the root spreadsheet node and its Call step")
                .hasSizeGreaterThanOrEqualTo(2);
        treeNodes.forEach(node -> {
            assertThat(node)
                    .as("trace tree node shows the raw special characters: %s", node)
                    .contains(VALUE_CORE)
                    .contains(VALUE_TAIL);
            LITERAL_ENTITIES.forEach(entity -> assertThat(node)
                    .as("trace tree node must not show the literal entity %s: %s", entity, node)
                    .doesNotContain(entity));
        });

        traceWindow.selectItemInTree(0);
        assertThat(traceWindow.isReturnedResultSectionDisplayed(10000))
                .as("Returned Result section is displayed for the selected node")
                .isTrue();
        String traceViewText = traceWindow.getTraceViewText();
        assertThat(traceViewText)
                .as("trace view (details + Traced Table) shows the raw special characters")
                .contains(VALUE_CORE)
                .contains(VALUE_TAIL);
        LITERAL_ENTITIES.forEach(entity -> assertThat(traceViewText)
                .as("trace view must not show the literal entity %s anywhere", entity)
                .doesNotContain(entity));
        traceWindow.close();
    }
}
