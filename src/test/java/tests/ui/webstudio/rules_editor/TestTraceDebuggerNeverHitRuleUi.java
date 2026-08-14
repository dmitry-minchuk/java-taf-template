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

public class TestTraceDebuggerNeverHitRuleUi extends BaseTest {

    private static final String MODULE = "Main";
    private static final String TABLE_FOLDER = "Decision";
    private static final String TABLE_NAME = "Hello";
    private static final String FIRED_RULE = "R10";
    private static final String NEVER_FIRED_RULE = "R40";

    @Test
    @TestCaseId("IPBQA-33033")
    @Description("EPBDS-16406 Negative: a breakpoint on a rule the input never fires must not suspend the run - with "
            + "hour=10 only R10 fires, so breaking on R40 lets the run finish without an extra stop.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testBreakpointOnNeverFiredRuleDoesNotSuspend() {
        EditorPage editorPage = openHelloTable();

        ITraceWindow trace = editorPage.getEditorToolbarPanelComponent().clickTrace()
                .setParameterField("hour", "10")
                .clickTraceInsideMenu();
        assertThat(trace.getStatus())
                .as("The advanced debugger must open suspended at the start")
                .contains("Paused");

        trace.pickBreakOnRule(FIRED_RULE);
        trace.pickBreakOnRule(NEVER_FIRED_RULE);
        trace.resume();
        trace.waitForDecisionPanelToContain(FIRED_RULE, 10000);
        assertThat(trace.getStatus())
                .as("Positive control: the rule-select breakpoint must suspend on the rule that fires")
                .contains("Paused");

        trace.resume();
        trace.waitForStatus("Finished", 15000);
        assertThat(trace.getStatus())
                .as("A breakpoint on a never-fired rule must not suspend the run")
                .contains("Finished");
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
