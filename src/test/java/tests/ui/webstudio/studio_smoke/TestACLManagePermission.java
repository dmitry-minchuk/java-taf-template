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
import domain.ui.webstudio.components.common.UserSlidingRightMenuComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.WorkflowService;
import helpers.utils.StringUtil;
import helpers.utils.WaitUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestACLManagePermission extends BaseTest {

    // BRD: Manage (M) permission — only Manager role has it.
    // Manager can assign roles to other users (i.e., access Administration).
    // Contributor and Viewer cannot access Administration at all.

    // TODO: BRD requires Manager role to grant Administration access (M permission).
    // Currently only built-in 'admin' user has Administration access — feature not yet implemented.
    @Test(enabled = false)
    @TestCaseId("EPBDS-15712")
    @Description("ACL: Manager role user can see Administration menu item (has M permission)")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testManagerCanAccessAdministration() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());

        // ============ Admin setup: create project and Manager user ============
        WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Example 1 - Bank Rating");

        EditorPage editorPage = new EditorPage();
        UsersPageComponent usersComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();

        String username = StringUtil.generateUniqueName("manager_m");
        usersComponent.clickAddUser()
                .setUsername(username)
                .setPassword(username)
                .saveUser();

        // Assign Manager role on Design repository — Manager has M+V+C+E+D
        usersComponent.clickEditUser(username)
                .clickAddRoleBtn()
                .setRoleRepository(0, "Design")
                .setRole(0, "Manager")
                .saveUser();

        // ============ Login as Manager ============
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(new UserData(username, username));

        // ============ Verify Administration menu item is visible ============
        UserSlidingRightMenuComponent userMenu = editorPage.openUserMenu();

        assertThat(userMenu.isAdministrationMenuItemVisible())
                .as("Manager should see Administration menu item (has M permission)")
                .isTrue();
    }

    @Test
    @TestCaseId("EPBDS-15712")
    @Description("ACL: Contributor role user does NOT see Administration menu item (no M permission)")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testContributorCannotAccessAdministration() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());

        // ============ Admin setup: create project and Contributor user ============
        WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Example 1 - Bank Rating");

        EditorPage editorPage = new EditorPage();
        UsersPageComponent usersComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();

        String username = StringUtil.generateUniqueName("contrib_m");
        usersComponent.clickAddUser()
                .setUsername(username)
                .setPassword(username)
                .saveUser();

        // Assign Contributor role — Contributor has V+C+E+D but NOT M
        usersComponent.clickEditUser(username)
                .clickAddRoleBtn()
                .setRoleRepository(0, "Design")
                .setRole(0, "Contributor")
                .saveUser();

        // ============ Login as Contributor ============
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(new UserData(username, username));

        // ============ Verify Administration menu item is NOT visible ============
        UserSlidingRightMenuComponent userMenu = editorPage.openUserMenu();

        assertThat(userMenu.isAdministrationMenuItemVisible())
                .as("Contributor should NOT see Administration menu item (no M permission)")
                .isFalse();
    }

    @Test
    @TestCaseId("EPBDS-15712")
    @Description("ACL: Viewer role user does NOT see Administration menu item (no M permission)")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testViewerCannotAccessAdministration() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());

        // ============ Admin setup: create project and Viewer user ============
        WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Example 1 - Bank Rating");

        EditorPage editorPage = new EditorPage();
        UsersPageComponent usersComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();

        String username = StringUtil.generateUniqueName("viewer_m");
        usersComponent.clickAddUser()
                .setUsername(username)
                .setPassword(username)
                .saveUser();

        // Assign Viewer role — Viewer has V only, no M
        usersComponent.clickEditUser(username)
                .clickAddRoleBtn()
                .setRoleRepository(0, "Design")
                .setRole(0, "Viewer")
                .saveUser();

        // ============ Login as Viewer ============
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(new UserData(username, username));

        // ============ Verify Administration menu item is NOT visible ============
        UserSlidingRightMenuComponent userMenu = editorPage.openUserMenu();

        assertThat(userMenu.isAdministrationMenuItemVisible())
                .as("Viewer should NOT see Administration menu item (no M permission)")
                .isFalse();
    }

    @Test
    @TestCaseId("EPBDS-15712")
    @Description("ACL: User with no roles sees no projects (no access at all)")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testUserWithNoRolesHasNoAccess() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());

        // ============ Admin setup: create project and user with NO roles ============
        WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Example 1 - Bank Rating");

        EditorPage editorPage = new EditorPage();
        UsersPageComponent usersComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();

        String username = StringUtil.generateUniqueName("noroles_user");
        usersComponent.clickAddUser()
                .setUsername(username)
                .setPassword(username)
                .saveUser();

        // No role assigned — user has zero access

        // ============ Login as no-roles user ============
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(new UserData(username, username));

        // ============ Verify user cannot see any projects in Repository ============
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        WaitUtil.sleep(1000, "Allow time for projects to load if any");

        assertThat(repositoryPage.getAllVisibleProjectsInTable())
                .as("User with no roles should see no projects")
                .isEmpty();

        // ============ Verify no Administration menu item ============
        UserSlidingRightMenuComponent userMenu = editorPage.openUserMenu();
        assertThat(userMenu.isAdministrationMenuItemVisible())
                .as("User with no roles should NOT see Administration menu item")
                .isFalse();
    }
}
