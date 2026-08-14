package tests.ui.webstudio.git;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.api.ProjectBranchesMethod;
import domain.serviceclasses.constants.User;
import io.restassured.response.Response;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.repositorytabcomponents.DeleteBranchModalComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.ProjectDetailPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDeleteBranchDialogUi extends BaseTest {

    private static final String BRANCH = "qa-delete-me";
    private static final String MASTER = "master";

    @Test
    @TestCaseId("IPBQA-33015")
    @Description("Delete Branch happy path: the dialog names the branch it is about to delete (guards the "
            + "EPBDS-16440 stale-target defect on a settled page), the branch disappears from the UI and from "
            + "Git after confirmation, and the project falls back to the default branch.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testDeleteBranchRemovesBranchAndFallsBackToDefault() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        RepositoryPage repositoryPage = new EditorPage().getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        ProjectDetailPage detail = repositoryPage.openProjectDetail(projectName);
        detail.createBranch(BRANCH, true);
        String projectId = ProtectedBranchBypassFixture.resolveProjectId(projectName);

        DeleteBranchModalComponent dialog = detail.openDeleteBranchDialog();
        assertThat(dialog.getBodyText())
                .as("The confirmation dialog must name the branch it is about to delete (EPBDS-16440 guard)")
                .contains(BRANCH);
        dialog.clickDelete();
        detail.waitUntilSpinnerLoaded();

        assertThat(detail.getCurrentBranch())
                .as("After deleting its branch the project must fall back to the default branch")
                .isEqualTo(MASTER);
        assertThat(detail.isBranchPresent(BRANCH))
                .as("The deleted branch must disappear from the branch switcher")
                .isFalse();
        Response branches = new ProjectBranchesMethod().listBranches(projectId);
        assertThat(branches.getStatusCode())
                .as("The branch list must be readable through REST after the deletion")
                .isEqualTo(200);
        List<String> branchNames = branches.jsonPath().getList("name");
        assertThat(branchNames)
                .as("The deleted branch must be gone from Git, not just from the UI")
                .doesNotContain(BRANCH)
                .contains(MASTER);
    }
}
