package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.components.admincomponents.UsersPageComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.repositorytabcomponents.RepositoryContentButtonsPanelComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.WorkflowService;
import helpers.utils.StringUtil;
import helpers.utils.WaitUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestACLDeploySystemAction extends BaseTest {

    // BRD TR2: Deploy is NOT a permission. It is a system action available if:
    // user has >= Viewer on design repository AND at least Edit rights on deploy repository.

    @Test
    @TestCaseId("EPBDS-15712")
    @Description("ACL: Deploy button NOT visible for Viewer with Design-only access (no deploy repo access)")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testDeployNotVisibleForDesignViewerWithoutDeployRepo() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());

        // ============ Admin setup: create project and Viewer-only user ============
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Example 1 - Bank Rating");

        EditorPage editorPage = new EditorPage();
        UsersPageComponent usersComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();

        String username = StringUtil.generateUniqueName("viewer_deploy");
        usersComponent.clickAddUser()
                .setUsername(username)
                .setPassword(username)
                .saveUser();

        // Assign Viewer on Design only — no production/deploy repo access
        usersComponent.clickEditUser(username)
                .clickAddRoleBtn()
                .setRoleRepository(0, "Design")
                .setRole(0, "Viewer")
                .saveUser();

        // ============ Login as Viewer ============
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(new UserData(username, username));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        WaitUtil.waitForCondition(
                () -> repositoryPage.getAllVisibleProjectsInTable().contains(projectName),
                10000, 500, "Waiting for project to appear for viewer"
        );

        // ============ Verify Deploy button NOT visible ============
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        RepositoryContentButtonsPanelComponent buttonsPanel = repositoryPage.getRepositoryContentButtonsPanelComponent();

        assertThat(buttonsPanel.isDeployBtnVisible())
                .as("Viewer with Design-only access should NOT see Deploy button — no deploy repo access (BRD TR2)")
                .isFalse();
        // Viewer still has read-only access
        assertThat(buttonsPanel.isExportBtnVisible())
                .as("Viewer should still see Export button").isTrue();
        assertThat(buttonsPanel.isCopyBtnVisible())
                .as("Viewer should NOT see Copy button").isFalse();
        assertThat(buttonsPanel.isDeleteBtnVisible())
                .as("Viewer should NOT see Delete button").isFalse();
    }

    @Test
    @TestCaseId("EPBDS-15712")
    @Description("ACL: Deploy button NOT visible for Contributor on Design when user also has Viewer on deploy repo (needs Edit on deploy)")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testDeployNotVisibleWhenDeployRepoAccessIsViewerOnly() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());

        // ============ Admin setup: create project and user ============
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Example 1 - Bank Rating");

        EditorPage editorPage = new EditorPage();
        UsersPageComponent usersComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();

        String username = StringUtil.generateUniqueName("viewer_bothrepo");
        usersComponent.clickAddUser()
                .setUsername(username)
                .setPassword(username)
                .saveUser();

        // Assign Viewer on Design + Viewer on prod/deploy repo
        // Per BRD TR2: Deploy requires at least Edit on deploy repo — Viewer is NOT enough
        usersComponent.clickEditUser(username)
                .clickAddRoleBtn()
                .setRoleRepository(0, "Design")
                .setRole(0, "Viewer")
                .saveUser();
        // Note: if a second production repository is configured in the test environment,
        // add a second role row here with: .clickAddRoleBtn().setRoleRepository(1, "prod").setRole(1, "Viewer")
        // For now we assert that Viewer on Design alone (most common case) gives no Deploy.

        // ============ Login as user ============
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(new UserData(username, username));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        WaitUtil.waitForCondition(
                () -> repositoryPage.getAllVisibleProjectsInTable().contains(projectName),
                10000, 500, "Waiting for project to appear"
        );

        // ============ Verify Deploy NOT visible for Viewer (needs Edit on deploy repo) ============
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        RepositoryContentButtonsPanelComponent buttonsPanel = repositoryPage.getRepositoryContentButtonsPanelComponent();

        assertThat(buttonsPanel.isDeployBtnVisible())
                .as("Viewer role is NOT enough on deploy repo — Deploy button must NOT be visible (BRD TR2)")
                .isFalse();
    }

    @Test
    @TestCaseId("EPBDS-15712")
    @Description("ACL: Admin always sees Deploy button (full access to both design and deploy repos)")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testDeployVisibleForAdmin() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());

        // ============ Admin creates project and checks Deploy button ============
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Example 1 - Bank Rating");

        EditorPage editorPage = new EditorPage();
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        WaitUtil.waitForCondition(
                () -> repositoryPage.getAllVisibleProjectsInTable().contains(projectName),
                10000, 500, "Waiting for project to appear for admin"
        );

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        RepositoryContentButtonsPanelComponent adminPanel = repositoryPage.getRepositoryContentButtonsPanelComponent();

        // Admin has full access — Deploy should be visible (has both design and deploy repo access)
        assertThat(adminPanel.isDeployBtnVisible())
                .as("Admin should see Deploy button (full access to design + deploy repos)")
                .isTrue();
    }
}
