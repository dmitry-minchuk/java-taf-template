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

/**
 * Versioning through the "Copy table" modal, which EPBDS-16313 put in place of the JSF copy wizard.
 *
 * <p>The wizard's "Copy as New Version" mode also deactivated the source table; the modal only writes the copy,
 * so the module is left with two active tables of the same name.
 */
public class TestCopyTableVersioningUi extends BaseTest {

    private static final String MODULE_NAME = "TestMethodTable";
    private static final String TABLE_FOLDER = "Method";
    private static final String TABLE_NAME = "getGreetings";
    private static final String NEXT_VERSION = "0.0.2";
    private static final String MALFORMED_VERSION = "v2";
    // The modal takes the property's technical name, which is what the table header carries.
    private static final String BUSINESS_DIMENSION = "lob";
    private static final String BUSINESS_DIMENSION_VALUE = "test";

    @Test
    @TestCaseId("EPBDS-16357")
    @Description("Copying a table as a new version must leave the module compilable. KNOWN-FAILING: the modal does "
            + "not deactivate the source table, so both versions stay active and the module reports "
            + "\"There can be only one active table.\""
            + " Known bug: EPBDS-16357.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCopyAsNewVersionKeepsModuleCompilable() {
        EditorPage editorPage = openTable();

        editorPage.getEditorToolbarPanelComponent().copyTableAsNewVersion(NEXT_VERSION);
        editorPage.waitUntilSpinnerLoaded();

        assertThat(editorPage.getProblemsPanelComponent().getAllErrors())
                .as("A new version of a table must not leave two active tables behind")
                .isEmpty();
    }

    @Test
    @TestCaseId("IPBQA-31319")
    @Description("Copying a table with a business dimension property set keeps the module compilable: the "
            + "dimension tells the two tables apart, so no deactivation is needed.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCopyAsBusinessDimensionVersionKeepsModuleCompilable() {
        EditorPage editorPage = openTable();

        editorPage.getEditorToolbarPanelComponent().copyTableAsBusinessDimension(BUSINESS_DIMENSION, BUSINESS_DIMENSION_VALUE);
        editorPage.waitUntilSpinnerLoaded();

        assertThat(editorPage.getProblemsPanelComponent().getAllErrors())
                .as("A copy carrying its own business dimension must compile alongside the original")
                .isEmpty();
    }

    private EditorPage openTable() {
        String projectName = WorkflowService.loginCreateProjectFromExcelFile(User.ADMIN, "TestMethodTable.xlsx");
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, MODULE_NAME);
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree(TABLE_FOLDER)
                .selectItemInFolder(TABLE_FOLDER, TABLE_NAME);
        editorPage.getProblemsPanelComponent().checkNoProblems();
        return editorPage;
    }
}
