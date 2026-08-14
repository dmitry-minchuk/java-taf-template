package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TableComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCopyTableStylePreservedUi extends BaseTest {

    private static final String MODULE = "Main";
    private static final String TABLE_FOLDER = "Decision";
    private static final String TABLE_NAME = "Hello";
    private static final String COPY_NAME = "HelloStyledCopy";

    @Test
    @TestCaseId("IPBQA-33025")
    @Description("EPBDS-16354 verification ordered by EPBDS-16412: copying a table must preserve the table style - "
            + "the copy's rendered header carries the same background as the original's.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCopyPreservesTableStyle() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        EditorPage editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, MODULE);
        openTableInTree(editorPage, TABLE_NAME);
        String originalHeaderBackground = headerBackground(editorPage.getCenterTable());
        assertThat(originalHeaderBackground)
                .as("Precondition: the original header must carry a real workbook fill, not a default background")
                .isNotEqualTo("rgba(0, 0, 0, 0)")
                .isNotEqualTo("rgb(255, 255, 255)");

        editorPage.getEditorToolbarPanelComponent().copyTableAsNew(COPY_NAME, null);
        editorPage.waitUntilSpinnerLoaded();
        editorPage.getProblemsPanelComponent().checkNoProblems();

        openTableInTree(editorPage, COPY_NAME);
        assertThat(editorPage.getCenterTable().getCellText(1, 1))
                .as("Precondition: the copy must be open in the editor")
                .contains(COPY_NAME);
        assertThat(headerBackground(editorPage.getCenterTable()))
                .as("The copy's rendered header background must match the original's (EPBDS-16354)")
                .isEqualTo(originalHeaderBackground);
    }

    private void openTableInTree(EditorPage editorPage, String tableName) {
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree(TABLE_FOLDER)
                .selectItemInFolder(TABLE_FOLDER, tableName);
        editorPage.waitUntilSpinnerLoaded();
    }

    private String headerBackground(TableComponent table) {
        return String.valueOf(table.getRow(1).getCells().getFirst().getLocator()
                .evaluate("cell => getComputedStyle(cell).backgroundColor"));
    }
}
