package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.EditModuleDialogComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import helpers.utils.WaitUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestMigratedMethodFilterReloadUi extends BaseTest {

    private static final String TEMPLATE_NAME = "Sample Project";
    private static final String MODULE_NAME = "Main";
    private static final String TABLE_NAME = "Hello";
    private static final int RELOAD_SETTLE_TIMEOUT_MS = 30000;

    @Test
    @TestCaseId("EPBDS-16275")
    @Description("After module method filters are migrated to project level, reloading the project must load the "
            + "module once. Guards the reload loop reported in EPBDS-16275, which these steps do not reproduce "
            + "on 6.4.0-ef53e0bec1d7 - the test is green here and turns red if the loop comes back."
            + " Known bug: EPBDS-16275.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testProjectReloadAfterMethodFilterMigration() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, TEMPLATE_NAME);
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

        // Give the module a method filter, which is what the migration then converts.
        editorPage.getProjectDetailsComponent().openEditModuleDialog(MODULE_NAME);
        EditModuleDialogComponent editModule = editorPage.getEditModuleDialogComponent();
        editModule.waitForDialogToAppear();
        editModule.setIncludedMethods("a");
        editModule.setExcludedMethods("b");
        editModule.clickSave();
        editorPage.waitUntilSpinnerLoaded();

        // Migrate first and commit afterwards, which is the order the defect was reported in.
        assertThat(editorPage.getProjectDetailsComponent().isMigrateMethodFiltersVisible())
                .as("Migrate Method Filters should be offered while the module still carries a filter")
                .isTrue();
        editorPage.getProjectDetailsComponent().clickMigrateMethodFilters();

        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().clickSave();
        editorPage.waitUntilSpinnerLoaded();

        // Open a table of that module, then ask the editor to reload the project.
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, MODULE_NAME);
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", TABLE_NAME);

        editorPage.getEditorToolbarPanelComponent().clickProjectRefresh();

        // A single load leaves the table on screen; a reload loop never lets it settle.
        boolean tableSettled = WaitUtil.waitForCondition(() -> editorPage.getCenterTable().isVisible(),
                RELOAD_SETTLE_TIMEOUT_MS, 500, "Waiting for the reloaded module's table to settle");
        assertThat(tableSettled)
                .as("The module must load once after a project reload instead of reloading endlessly")
                .isTrue();
        assertThat(editorPage.getCenterTable().isVisible())
                .as("The table must stay on screen once the reload finished")
                .isTrue();
    }
}
