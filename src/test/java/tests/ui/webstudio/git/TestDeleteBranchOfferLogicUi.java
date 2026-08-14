package tests.ui.webstudio.git;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.ProjectDetailPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDeleteBranchOfferLogicUi extends BaseTest {

    private static final String PROTECTED_BRANCH = "release-guard";

    @Test
    @TestCaseId("IPBQA-33018")
    @Description("EPBDS-16378 Negative: Delete Branch must not be offered on the default branch - deleting it can never succeed.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testDeleteBranchNotOfferedOnDefaultBranch() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        RepositoryPage repositoryPage = new EditorPage().getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        ProjectDetailPage detail = repositoryPage.openProjectDetail(projectName);
        assertThat(detail.getCurrentBranch())
                .as("Precondition: the project must sit on the default branch")
                .isEqualTo("master");
        assertThat(detail.isHeaderActionAvailable("Delete Branch"))
                .as("Delete Branch must not be offered on the default branch")
                .isFalse();
        assertThat(detail.isHeaderActionAvailable("Copy"))
                .as("Sanity: other header actions must still be offered on the default branch")
                .isTrue();
    }

    @Test
    @TestCaseId("IPBQA-33019")
    @Description("EPBDS-16378 Negative: with the protected-branch bypass disabled, Delete Branch must not be offered on a "
            + "protected branch - the server would refuse the deletion with HTTP 403.")
    @AppContainerConfig(startParams = AppContainerStartParameters.STUDIO_PROTECTED_NO_BYPASS_PARAMS)
    public void testDeleteBranchNotOfferedOnProtectedBranch() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        RepositoryPage repositoryPage = new EditorPage().getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        ProjectDetailPage detail = repositoryPage.openProjectDetail(projectName);
        detail.createBranch(PROTECTED_BRANCH, true);

        assertThat(detail.getCurrentBranch())
                .as("Precondition: the project must sit on the protected branch")
                .isEqualTo(PROTECTED_BRANCH);
        assertThat(detail.isHeaderActionAvailable("Copy"))
                .as("Sanity: the header actions must be rendered on the protected branch")
                .isTrue();
        assertThat(detail.isHeaderActionAvailable("Delete Branch"))
                .as("Delete Branch must not be offered on a protected branch when bypass is disabled")
                .isFalse();
    }
}
