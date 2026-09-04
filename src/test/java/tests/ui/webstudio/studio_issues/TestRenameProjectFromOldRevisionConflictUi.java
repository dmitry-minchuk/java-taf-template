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
    @Description("Renaming a project, then renaming it again from an older revision must offer Resolve Conflicts "
            + "instead of refusing the save (regression guard for EPBDS-16269).")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testRenameFromOldRevisionOffersConflictResolution() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, TEMPLATE_NAME);
        String renamedOnce = projectName + "2";
        String renamedTwice = projectName + "3";
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

        editorPage.openEditProjectDialog(projectName).setProjectName(renamedOnce).clickUpdateButton();
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().clickSave();
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getEditorToolbarPanelComponent().clickMore().clickRevisions();
        EditorRevisionsTabComponent revisionsTab = new EditorRevisionsTabComponent();
        revisionsTab.waitForTableToLoad();
        revisionsTab.openRevision(2);

        editorPage.openEditProjectDialog(projectName).setProjectName(renamedTwice).clickUpdateButton();
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().clickSave();
        editorPage.waitUntilSpinnerLoaded();

        assertThat(new ResolveConflictsDialogComponent().isDialogVisible())
                .as("Resolve Conflicts should be offered when a rename from an old revision conflicts with HEAD")
                .isTrue();
    }
}
