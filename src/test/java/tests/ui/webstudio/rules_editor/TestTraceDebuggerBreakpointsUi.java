package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.components.editortabcomponents.toolbar.ITraceWindow;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestTraceDebuggerBreakpointsUi extends BaseTest {

    private static final String MODULE = "Main";
    private static final String TABLE_FOLDER = "Decision";
    private static final String TABLE_NAME = "Hello";

    @Test
    @TestCaseId("EPBDS-16195")
    @Description("EPBDS-16195: the advanced trace opens the step debugger suspended at the start; a rule-fire "
            + "breakpoint suspends the run when the rule fires, a watch shows its value, stepping and resuming "
            + "finish the run, and rerun starts the session again.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testBreakpointSuspendsAndDebuggerCompletes() {
        EditorPage editorPage = openHelloTable();

        ITraceWindow trace = editorPage.getEditorToolbarPanelComponent().clickTrace()
                .setParameterField("hour", "10")
                .clickTraceInsideMenu();

        assertThat(trace.getStatus())
                .as("The advanced debugger must open suspended at the start")
                .contains("Paused");
        assertThat(trace.isDebugToolbarShown(5000))
                .as("The advanced debugger must show the step toolbar")
                .isTrue();
        assertThat(trace.getCallTreeTitles())
                .as("The call tree must show the traced decision table frame")
                .anyMatch(node -> node.contains(TABLE_NAME));

        trace.addWatch("R1C1");
        assertThat(trace.getWatchPanelText())
                .as("The added watch must appear in the watch panel")
                .contains("R1C1");

        trace.setBreakWhenRuleFires();
        trace.resume();
        trace.waitForDecisionPanelToContain("R10", 10000);
        assertThat(trace.getStatus())
                .as("The run must suspend when a rule fires with the rule-fire breakpoint set")
                .contains("Paused");

        trace.stepOver();
        trace.resume();
        trace.waitForStatus("Finished", 15000);

        trace.clickRerun();
        trace.waitForStatus("Paused", 15000);
        assertThat(trace.getStatus())
                .as("Rerun must start a new suspended session")
                .contains("Paused");
        trace.close();
    }

    private EditorPage openHelloTable() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        EditorPage editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, MODULE);
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree(TABLE_FOLDER)
                .selectItemInFolder(TABLE_FOLDER, TABLE_NAME);
        return editorPage;
    }
}
