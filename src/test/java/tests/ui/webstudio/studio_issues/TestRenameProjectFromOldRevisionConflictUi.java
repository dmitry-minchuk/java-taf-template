package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.EditorRevisionsTabComponent;
import domain.ui.webstudio.components.repositorytabcomponents.ResolveConflictsDialogComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestRenameProjectFromOldRevisionConflictUi extends BaseTest {

    private static final String TEMPLATE_NAME = "Tutorial 1 - Introduction to Decision Tables";

    @Test
    @TestCaseId("EPBDS-16269")
    @Description("Renaming a project, then renaming it again from an older revision must offer Resolve Conflicts. "
            + "KNOWN-FAILING: the save is refused and no dialog appears — the conflicts request still uses the "
            + "previous project name, so it answers 404")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testRenameFromOldRevisionOffersConflictResolution() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, TEMPLATE_NAME);
        String renamedOnce = projectName + "2";
        String renamedTwice = projectName + "3";
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

        // Rename and save → revision 2, the project now lives under its new name.
        editorPage.openEditProjectDialog(projectName).setProjectName(renamedOnce).clickUpdateButton();
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();
        editorPage.waitUntilSpinnerLoaded();

        // Go back to the revision the project was created in.
        editorPage.getEditorToolbarPanelComponent().clickMore().clickRevisions();
        EditorRevisionsTabComponent revisionsTab = new EditorRevisionsTabComponent();
        revisionsTab.waitForTableToLoad();
        revisionsTab.openRevision(2);

        // The revision being viewed carries the name the project had back then, so it is addressed by that.
        editorPage.openEditProjectDialog(projectName).setProjectName(renamedTwice).clickUpdateButton();
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();
        editorPage.waitUntilSpinnerLoaded();

        assertThat(new ResolveConflictsDialogComponent().isDialogVisible())
                .as("Resolve Conflicts should be offered when a rename from an old revision conflicts with HEAD")
                .isTrue();
    }
}
