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

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestAllStepsDisplayedInTrace extends BaseTest {

    // Step names as shown in the Traced Table "Step" column (no $ prefix; $ only appears in
    // formulas that reference another step).
    private final List<String> EXPECTED_STEPS = Arrays.asList(
            "Coverage_ID",
            "UnitArea",
            "Coverage_Name",
            "Coverage_Level",
            "Coverage_Previous_Time",
            "Coverage_Previous_Revision",
            "Coverage_Rules",
            "Applicable_Strategy_Rules",
            "Inactive_Rules",
            "FeeCurrentPA",
            "Coverage_Active_Rules_Amounts",
            "Coverage_Inactive_Rules_Amounts"
    );

    @Test
    @TestCaseId("EPBDS-8215")
    @Description("BUG: Some Steps are not displayed in Trace")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAllStepsDisplayedInTrace() {
        String projectName = WorkflowService.loginCreateProjectFromZip(User.ADMIN, "TestAllStepsDisplayedInTrace.zip");
        EditorPage editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "CO-rules");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", "DetermineCoverageFeeOrTax2");

        EditorToolbarPanelComponent.ITraceWindow traceWindow = editorPage.getEditorToolbarPanelComponent()
                .clickTrace()
                .setFactorTextField("123")
                .clickTraceInsideMenu();

        String tracedTable = traceWindow.getTracedTableText();
        EXPECTED_STEPS.forEach(step -> assertThat(tracedTable)
                .as("Traced Table must list step %s (EPBDS-8215: no steps are dropped)", step)
                .contains(step));
        traceWindow.close();
    }
}
