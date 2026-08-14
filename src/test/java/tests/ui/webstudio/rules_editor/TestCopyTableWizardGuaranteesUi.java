package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.CopyTableDialogComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCopyTableWizardGuaranteesUi extends BaseTest {

    private static final String MODULE = "Main";
    private static final String TABLE_FOLDER = "Decision";
    private static final String TABLE_NAME = "Hello";
    private static final String VERSION = "0.0.2";

    @Test
    @TestCaseId("IPBQA-33026")
    @Description("The copy wizard guaranteed version uniqueness; the React modal must reject copying a table "
            + "as a version that already exists. KNOWN-FAILING: the modal accepts the duplicate version."
            + " Known bug: EPBDS-16388.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testDuplicateVersionCopyIsRejected() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        EditorPage editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, MODULE);
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree(TABLE_FOLDER)
                .selectItemInFolder(TABLE_FOLDER, TABLE_NAME);

        editorPage.getEditorToolbarPanelComponent().copyTableAsNewVersion(VERSION);
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree(TABLE_FOLDER)
                .selectItemInFolder(TABLE_FOLDER, TABLE_NAME);
        CopyTableDialogComponent copyDialog = editorPage.getEditorToolbarPanelComponent().clickCopy();
        copyDialog.setVersion(VERSION);

        assertThat(copyDialog.isCopyButtonEnabled())
                .as("Copying as the already existing version '%s' must be rejected (EPBDS-16388)", VERSION)
                .isFalse();
    }
}
