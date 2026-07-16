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
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

// Validates the React project-detail Branches-tab primitives (build 032c60a664ce+): create a branch, list
// branches, and open the "Sync updates" merge dialog. Foundation for migrating the git-branch tests.
public class TestReactBranchesSmoke extends BaseTest {

    private static final String BRANCH_NAME = "MyBranch";
    private static final String MASTER = "master";

    @Test
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void createBranchAndOpenMergeDialog() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        RepositoryPage repositoryPage = new EditorPage().getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        ProjectDetailPage projectDetail = repositoryPage.openProjectDetail(projectName);
        projectDetail.createBranch(BRANCH_NAME);

        assertThat(projectDetail.isBranchPresent(MASTER))
                .as("master branch should be listed on the Branches tab").isTrue();
        assertThat(projectDetail.isBranchPresent(BRANCH_NAME))
                .as("the newly created branch '%s' should be listed", BRANCH_NAME).isTrue();

        SyncUpdatesDialogComponent syncDialog = projectDetail.openMergeDialog(BRANCH_NAME);
        assertThat(syncDialog.getHeader())
                .as("merge dialog header should be the 'Sync updates' header").contains("Sync updates");
        assertThat(syncDialog.isReceiveEnabled())
                .as("Receive-their-updates should be disabled when branches are in sync").isFalse();
        assertThat(syncDialog.isSendEnabled())
                .as("Send-your-updates should be disabled when branches are in sync").isFalse();
        syncDialog.close();
    }
}
