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

public class TestCreateTableNameValidationUi extends BaseTest {

    private static final String MODULE = "Main";
    private static final String NAME_HINT = "Enter a table name";
    private static final String LONG_NAME = "QaVeryLongTableNameOverThirtyOneChars";

    @Test
    @TestCaseId("EPBDS-16313")
    @Description("Negative: the Create Table modal must block creation while the name is empty or starts with a "
            + "digit, showing the specific naming hint; a name longer than Excel's 31-symbol sheet limit must "
            + "still create cleanly because the mirrored sheet name is clipped (EPBDS-16355).")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCreateTableNameValidation() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        EditorPage editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, MODULE);

        editorPage.getEditorToolbarPanelComponent().clickCreateTable();
        CreateTableDialogComponent dialog = editorPage.getCreateTableDialogComponent().waitForDialogToAppear();
        dialog.selectType("Rules");
        assertThat(dialog.getBlockedHint())
                .as("An empty table name must show the specific naming hint")
                .contains(NAME_HINT);

        dialog.setTechnicalName("9BadName");
        assertThat(dialog.isBlockedHintShown())
                .as("A name starting with a digit must keep the naming hint on screen")
                .isTrue();
        assertThat(dialog.isCreateButtonEnabled())
                .as("Create must stay unavailable while the name starts with a digit")
                .isFalse();

        dialog.setTechnicalName(LONG_NAME);
        dialog.save();
        editorPage.waitUntilSpinnerLoaded();
        editorPage.getProblemsPanelComponent().checkNoProblems();
        assertThat(editorPage.getCenterTable().getCellText(1, 1))
                .as("A 31+ character table name must create cleanly with the sheet name clipped internally")
                .contains(LONG_NAME);
    }
}
