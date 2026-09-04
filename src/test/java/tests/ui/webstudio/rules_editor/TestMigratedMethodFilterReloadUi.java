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
            + "module once and settle. Guards the reload loop of EPBDS-16275: a JSF POST fired with the shell's "
            + "evicted ViewState dies with ViewExpiredException, the onError handler reloads the panels without "
            + "renewing the ViewState, and the next auto-fired POST dies again - endlessly. On screen the loop "
            + "manifests as the loading overlay never settling (and, in some builds, the table dropping out), so "
            + "the test asserts BOTH: the overlay must reach a quiet window and the table must stay. The failure "
            + "message points at the loop; the server log of a red run carries the matching ViewExpiredException "
            + "storm. Verified red on 6.4.0-a86c25210eff and 6.4.0-ef53e0bec1d7 (an earlier weaker version of "
            + "this test was green there only because it never watched the overlay).")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testProjectReloadAfterMethodFilterMigration() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, TEMPLATE_NAME);
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

        editorPage.getProjectDetailsComponent().openEditModuleDialog(MODULE_NAME);
        EditModuleDialogComponent editModule = editorPage.getEditModuleDialogComponent();
        editModule.waitForDialogToAppear();
        editModule.setIncludedMethods("a");
        editModule.setExcludedMethods("b");
        editModule.clickSave();
        editorPage.waitUntilSpinnerLoaded();

        assertThat(editorPage.getProjectDetailsComponent().isMigrateMethodFiltersVisible())
                .as("Migrate Method Filters should be offered while the module still carries a filter")
                .isTrue();
        editorPage.getProjectDetailsComponent().clickMigrateMethodFilters();

        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().clickSave();
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, MODULE_NAME);
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", TABLE_NAME);

        editorPage.getEditorToolbarPanelComponent().clickProjectRefresh();

        assertThat(editorPage.waitUntilAppIdle())
                .as("The loading overlay must settle after a project reload; an overlay that never leaves "
                        + "is the EPBDS-16275 reload loop (endless ViewExpiredException storm)")
                .isTrue();

        boolean tableSettled = WaitUtil.waitForCondition(() -> editorPage.getCenterTable().isVisible(),
                RELOAD_SETTLE_TIMEOUT_MS, 500, "Waiting for the reloaded module's table to settle");
        assertThat(tableSettled)
                .as("The module must load once after a project reload instead of reloading endlessly")
                .isTrue();

        assertThat(editorPage.waitUntilAppIdle())
                .as("The app must stay idle once the reload finished - a re-appearing overlay means the "
                        + "reload loop resumed")
                .isTrue();
        assertThat(editorPage.getCenterTable().isVisible())
                .as("The table must stay on screen once the reload finished")
                .isTrue();
    }
}
