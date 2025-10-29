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
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.service.WorkflowService;
import org.testng.Assert;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestAdminUsers extends BaseTest {

    @Test
    @TestCaseId("IPBQA-32784")
    @Description("Users management without external authentication system: create, edit, delete users and manage roles")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testUsersManagementWithoutExternalAuth() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());

        // ============ Steps 1-3: Admin login and verify users page ============
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));
        UsersPageComponent usersComponent = editorPage
                .openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();

        Assert.assertTrue(usersComponent.isUserInList("admin"), "Admin user should be in the users list");
        Assert.assertTrue(usersComponent.areActionsAvailableForUser("admin"), "Edit and delete actions should be available for admin user");

        // ============ Steps 4-5: Add new user 'test' ============
        int initialUserCount = usersComponent.getUsersCount();
        usersComponent.clickAddUser()
                .setUsername("test")
                .setPassword("test")
                .inviteUser();

        Assert.assertTrue(usersComponent.isUserInList("test"), "User 'test' should be added to the list");
        Assert.assertEquals(usersComponent.getUsersCount(), initialUserCount + 1,
                "User count should increase by 1");

        // Verify alphabetical order
        List<String> allUsers = usersComponent.getAllUsernames();
        assertThat(allUsers).containsSequence("admin", "test");

        // ============ Step 6: Edit user 'test' email ============
        usersComponent.clickEditUser("test")
                .setEmail("test@example.com")
                .saveUser();

        // Verify email was updated
        usersComponent.clickEditUser("test");
        Assert.assertEquals(usersComponent.getEmail(), "test@example.com", "Email should be updated to 'test@example.com'");
        usersComponent.cancelUser();

        // ============ Step 7: Delete user 'test' ============
        usersComponent.clickDeleteUser("test");
        Assert.assertFalse(usersComponent.isUserInList("test"), "User 'test' should be removed from the list");
        Assert.assertEquals(usersComponent.getUsersCount(), initialUserCount, "User count should return to initial value");

        // ============ Step 8: Try to create duplicate 'Admin' user ============
        usersComponent.clickAddUser()
                .setUsername("admin")
                .setPassword("admin123")
                .inviteUser();

        usersComponent.closeAllMessages();
        Assert.assertEquals(usersComponent.getUsersCount(), initialUserCount, "User count should be equal to previous value");

        // ============ Step 9: Re-create user 'test' ============
        usersComponent.clickAddUser()
                .setUsername("test")
                .setPassword("test")
                .inviteUser();

        Assert.assertTrue(usersComponent.isUserInList("test"), "User 'test' should be created successfully");

        // ============ Step 10: Login as 'test' user and verify no projects/options ============
        editorPage.openUserMenu().signOut();
        UserData testUser = new UserData("test", "test");
        editorPage = loginService.login(testUser);

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        List<String> visibleProjects = repositoryPage.getAllVisibleProjectsInTable();
        assertThat(visibleProjects).isEmpty();
        editorPage.openUserMenu().signOut();

        // ============ Step 11: Admin adds Manager role for Project 1 to 'test' user ============
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Example 1 - Bank Rating");
        usersComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();

        usersComponent.clickEditUser("test")
                .clickAddRoleBtn()
                .setRoleRepository(0, "Design")
                .setRole(0, "Manager")
                .saveUser();

        // Verify role was added
        usersComponent.clickEditUser("test");
        Assert.assertEquals(usersComponent.getRoleRepository(0), "Design", "Repository should be 'Design'");
        Assert.assertEquals(usersComponent.getRole(0), "Manager", "Role should be 'Manager'");
        usersComponent.cancelUser();

        // ============ Step 12: Login as 'test' and verify Manager access ============
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(testUser);
        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        visibleProjects = repositoryPage.getAllVisibleProjectsInTable();
        assertThat(visibleProjects).as("User 'test' should see the project with Manager role").isNotEmpty().contains(projectName);
        editorPage.openUserMenu().signOut();

        // ============ Step 13: Admin changes 'test' role to Viewer ============
        editorPage = loginService.login(UserService.getUser(User.ADMIN));

        usersComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();

        usersComponent.clickEditUser("test")
                .setRole(0, "Viewer")
                .saveUser();

        // Verify role was changed
        usersComponent.clickEditUser("test");
        Assert.assertEquals(usersComponent.getRole(0), "Viewer", "Role should be changed to 'Viewer'");
        usersComponent.cancelUser();

        // ============ Step 14: Login as 'test' and verify Viewer access ============
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(testUser);
        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        visibleProjects = repositoryPage.getAllVisibleProjectsInTable();
        assertThat(visibleProjects).as("User 'test' should still see the project with Viewer role").isNotEmpty().contains(projectName);
    }
}
