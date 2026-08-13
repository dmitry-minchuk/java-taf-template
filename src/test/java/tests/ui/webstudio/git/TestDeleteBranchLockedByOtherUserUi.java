package tests.ui.webstudio.git;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import domain.serviceclasses.constants.User;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.repositorytabcomponents.DeleteBranchModalComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.ProjectDetailPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.service.WorkflowService;
import helpers.utils.TestDataUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDeleteBranchLockedByOtherUserUi extends BaseTest {

    private static final String BRANCH = "locked-branch";
    private static final String LOCKER_LOGIN = "locker_16255";
    private static final String UPLOAD_FILE = "TestFileAddDelete.rules.xls";

    @Test
    @TestCaseId("EPBDS-16255")
    @Description("EPBDS-16255: a branch on which the project is locked by another user must not be deletable - "
            + "the attempt is rejected with the lock message naming the locking user and the branch survives.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testBranchLockedByAnotherUserIsNotDeletable() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        RepositoryPage repositoryPage = new EditorPage().getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        ProjectDetailPage detail = repositoryPage.openProjectDetail(projectName);
        detail.createBranch(BRANCH, true);
        String projectId = ProtectedBranchBypassFixture.resolveProjectId(projectName);
        ProtectedBranchBypassFixture.provisionUser(projectId, LOCKER_LOGIN, LOCKER_LOGIN, "CONTRIBUTOR");
        detail.openUserMenu().signOut();

        new LoginService(DriverPool.getPage()).login(new UserData(LOCKER_LOGIN, LOCKER_LOGIN));
        repositoryPage = new EditorPage().getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.openProject(projectName);
        detail = repositoryPage.openProjectDetail(projectName);
        detail.switchBranch(BRANCH);
        detail.uploadFile(TestDataUtil.getFilePathFromResources(UPLOAD_FILE));
        assertThat(detail.getStatus())
                .as("Precondition: the uploading user must hold the edit lock on the branch")
                .contains("In Editing");
        detail.openUserMenu().signOut();

        new LoginService(DriverPool.getPage()).login(UserService.getUser(User.ADMIN));
        repositoryPage = new EditorPage().getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        detail = repositoryPage.openProjectDetail(projectName);
        detail.switchBranch(BRANCH);
        DeleteBranchModalComponent dialog = detail.openDeleteBranchDialog();
        dialog.attemptDelete();

        assertThat(detail.getErrorNotification())
                .as("Deleting a branch locked by another user must be rejected with the lock message naming the user")
                .contains("locked by user")
                .contains(LOCKER_LOGIN);
        dialog.clickCancel();
        assertThat(detail.isBranchPresent(BRANCH))
                .as("The locked branch must survive the rejected deletion")
                .isTrue();
    }
}
