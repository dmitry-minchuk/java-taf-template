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

public class TestTraceBusinessModeUi extends BaseTest {

    private static final String MODULE = "Main";
    private static final String TABLE_FOLDER = "Decision";
    private static final String TABLE_NAME = "Hello";

    @Test
    @TestCaseId("IPBQA-33031")
    @Description("EPBDS-16292: without the Advanced tracer switch the trace opens in the business view - no "
            + "debugger toolbar, the result-oriented tree names the fired rule, and the detailed toggle breaks "
            + "the decision table into its conditions; closing and relaunching works cleanly.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testBusinessTraceShowsFiredRuleAndConditions() {
        EditorPage editorPage = openHelloTable();

        ITraceWindow trace = editorPage.getEditorToolbarPanelComponent().clickTrace()
                .setParameterField("hour", "10")
                .clickTraceInsideMenuBusiness();

        assertThat(trace.isBusinessToggleShown(10000))
                .as("The business view must offer the detailed-trace toggle")
                .isTrue();
        assertThat(trace.isDebugToolbarShown(2000))
                .as("The business view must not show the step-debugger toolbar")
                .isFalse();
        assertThat(trace.getSimpleTreeText())
                .as("The business tree must show the traced result and the fired rule")
                .contains("Good Morning")
                .contains("Returned rule: [R10]");

        trace.toggleDetailedTrace();
        assertThat(trace.getSimpleTreeText())
                .as("The detailed toggle must break the decision table into its conditions")
                .contains("Condition: C1")
                .contains("Condition: C2");
        trace.close();

        ITraceWindow secondTrace = editorPage.getEditorToolbarPanelComponent().clickTrace()
                .setParameterField("hour", "23")
                .clickTraceInsideMenuBusiness();
        assertThat(secondTrace.getSimpleTreeText())
                .as("A relaunch after closing the trace must work and reflect the new input")
                .contains("Good Night");
        secondTrace.close();
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
