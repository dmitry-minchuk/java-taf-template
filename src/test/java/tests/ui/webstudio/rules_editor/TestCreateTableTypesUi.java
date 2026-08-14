package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.CreateTableDialogComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCreateTableTypesUi extends BaseTest {

    private static final String MODULE = "Main";

    @Test
    @TestCaseId("IPBQA-33020")
    @Description("EPBDS-16313 The Create Table modal must create a decision Rules table that compiles and renders in the module.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCreateRulesTableViaModal() {
        EditorPage editorPage = openEditor();

        openCreateTableDialog(editorPage)
                .selectType("Rules")
                .setSimpleRulesInitialParameters("QaDecisionRules", "Integer")
                .setCell(0, 1, "42")
                .save();
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getProblemsPanelComponent().checkNoProblems();
        assertThat(editorPage.getCenterTable().getCellText(1, 1))
                .as("The created decision table header must carry the technical name and the Integer result type")
                .contains("QaDecisionRules")
                .contains("Integer");
    }

    @Test
    @TestCaseId("IPBQA-33021")
    @Description("EPBDS-16313 The Create Table modal must create a Spreadsheet table that compiles and renders in the module.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCreateSpreadsheetTableViaModal() {
        EditorPage editorPage = openEditor();

        openCreateTableDialog(editorPage)
                .selectType("Spreadsheet")
                .setTechnicalName("QaSpreadsheet")
                .save();
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getProblemsPanelComponent().checkNoProblems();
        assertThat(editorPage.getCenterTable().getCellText(1, 1))
                .as("The created spreadsheet header must carry the technical name")
                .contains("QaSpreadsheet");
    }

    @Test
    @TestCaseId("IPBQA-33022")
    @Description("The Create Table modal must create a transposed Test table for the template's Hello table "
            + "(the transposed orientation is the EPBDS-6912 addition).")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCreateTransposedTestTableViaModal() {
        EditorPage editorPage = openEditor();

        openCreateTableDialog(editorPage)
                .selectType("Test")
                .setTechnicalName("QaHelloTest")
                .toggleTransposed()
                .save();
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getProblemsPanelComponent().checkNoProblems();
        assertThat(editorPage.getCenterTable().getCellText(1, 1))
                .as("The created test table header must reference the tested Hello table")
                .contains("Test Hello");
    }

    private EditorPage openEditor() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        EditorPage editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, MODULE);
        return editorPage;
    }

    private CreateTableDialogComponent openCreateTableDialog(EditorPage editorPage) {
        editorPage.getEditorToolbarPanelComponent().clickCreateTable();
        return editorPage.getCreateTableDialogComponent().waitForDialogToAppear();
    }
}
