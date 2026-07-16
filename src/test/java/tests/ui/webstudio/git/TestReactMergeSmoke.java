package tests.ui.webstudio.git;

import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.repositorytabcomponents.SyncUpdatesDialogComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.ProjectDetailPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.WorkflowService;
import helpers.utils.TestDataUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

// Validates the full React merge primitive end-to-end (createBranch -> diverge -> switchBranch ->
// openMergeDialog -> Receive) and reveals the post-Receive flow. Foundation for the git merge tests.
public class TestReactMergeSmoke extends BaseTest {

    private static final String BRANCH = "MyBranch";
    private static final String MASTER = "master";
    private static final String ADDED_MODULE = "TestNavigationToTable.xlsx";

    @Test
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void receiveMergesBranchIntoMaster() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        RepositoryPage repositoryPage = new EditorPage().getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        // Create MyBranch off master and switch to it, then commit a divergent change (a new module).
        ProjectDetailPage projectDetail = repositoryPage.openProjectDetail(projectName);
        projectDetail.createBranch(BRANCH, true);
        projectDetail.uploadFile(TestDataUtil.getFilePathFromResources(ADDED_MODULE));
        repositoryPage.openProjectsList().saveProject(projectName, "MyBranch: add module");

        // Switch back to master (which lacks the new module) via the editor.
        EditorPage editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);
        editorPage.getEditorToolbarPanelComponent().switchBranch(MASTER);

        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        projectDetail = repositoryPage.openProjectDetail(projectName);
        assertThat(projectDetail.getCurrentBranch()).as("on master before merge").isEqualTo(MASTER);

        // Merge MyBranch into master: Receive is enabled because the branches diverged.
        SyncUpdatesDialogComponent syncDialog = projectDetail.openMergeDialog(BRANCH);
        assertThat(syncDialog.isReceiveEnabled())
                .as("Receive their updates should be enabled when MyBranch is ahead of master").isTrue();
        syncDialog.clickReceive();
        repositoryPage.fillCommitInfo();
        repositoryPage.waitUntilSpinnerLoaded();

        // master must now contain the module that only existed on MyBranch. The merge left us on the
        // project-detail view, so return to the projects list before re-opening the detail.
        projectDetail = repositoryPage.openProjectsList().openProjectDetail(projectName);
        projectDetail.openFilesTab();
        assertThat(projectDetail.isFilePresent(ADDED_MODULE))
                .as("the module merged from MyBranch should now be present on master").isTrue();
    }
}
