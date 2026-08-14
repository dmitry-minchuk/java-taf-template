package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TableComponent;
import domain.ui.webstudio.components.editortabcomponents.CreateTableDialogComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCreateTableRangeConditionUi extends BaseTest {

    private static final String MODULE = "Main";
    private static final String TABLE_NAME = "QaRangeRules";
    private static final String RANGE = "18-30";

    @Test
    @TestCaseId("IPBQA-33024")
    @Description("EPBDS-16359: a rules condition column typed Integer must accept the range 18-30 and write it "
            + "as the range OpenL matches by, not as a mangled number like 181.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testIntegerConditionHoldsRange() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        EditorPage editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, MODULE);

        editorPage.getEditorToolbarPanelComponent().clickCreateTable();
        CreateTableDialogComponent dialog = editorPage.getCreateTableDialogComponent().waitForDialogToAppear();
        dialog.selectType("Simple Rules")
                .setSimpleRulesInitialParameters(TABLE_NAME, "Boolean")
                .addSimpleRulesParameter("Integer", false, "age")
                .setCell(0, 0, RANGE)
                .save();
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getProblemsPanelComponent().checkNoProblems();
        assertThat(renderedTableText(editorPage.getCenterTable()))
                .as("The created condition cell must hold the range '%s', not a mangled number", RANGE)
                .contains(RANGE)
                .doesNotContain("181");
    }

    private String renderedTableText(TableComponent table) {
        StringBuilder text = new StringBuilder();
        for (int row = 1; row <= 6; row++) {
            text.append(rowTextOrEmpty(table, row)).append('\n');
        }
        return text.toString();
    }

    private String rowTextOrEmpty(TableComponent table, int row) {
        try {
            return String.join(" | ", table.getRow(row).getValue());
        } catch (RuntimeException absentRow) {
            return "";
        }
    }
}
