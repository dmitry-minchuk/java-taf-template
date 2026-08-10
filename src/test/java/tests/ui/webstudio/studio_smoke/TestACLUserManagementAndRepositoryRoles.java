package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import domain.serviceclasses.constants.User;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.components.admincomponents.UsersPageComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.EditorToolbarPanelComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestACLUserManagementAndRepositoryRoles extends BaseTest {

    @Test
    @TestCaseId("IPBQA-32912")
    @Description("ACL: user management (create/edit/delete) and repository-level role assignment (Manager/Viewer) without external auth system")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testACLUserManagementAndRepositoryRoles() {
        LoginService loginService = new LoginService(DriverPool.getPage());

        // ============ Steps 1-3: Admin login and verify users page ============
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));
        UsersPageComponent usersComponent = editorPage
                .openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();

        assertThat(usersComponent.isUserInList("admin")).as("Admin user should be in the users list").isTrue();
        assertThat(usersComponent.areActionsAvailableForUser("admin")).as("Edit and delete actions should be available for admin user").isTrue();

        // ============ Steps 4-5: Add new user 'test' ============
        int initialUserCount = usersComponent.getUsersCount();
        usersComponent.clickAddUser()
                .setUsername("test")
                .setPassword("test")
                .saveUser();

        assertThat(usersComponent.isUserInList("test")).as("User 'test' should be added to the list").isTrue();
        assertThat(usersComponent.getUsersCount()).as("User count should increase by 1").isEqualTo(initialUserCount + 1);

        // Verify alphabetical order
        List<String> allUsers = usersComponent.getAllUsernames();
        assertThat(allUsers).containsSequence("admin", "test");

        // ============ Step 6: Edit user 'test' email ============
        usersComponent.clickEditUser("test")
                .setEmail("test@example.com")
                .saveUser();

        // Verify email was updated
        usersComponent.clickEditUser("test");
        assertThat(usersComponent.getEmail()).as("Email should be updated to 'test@example.com'").isEqualTo("test@example.com");
        usersComponent.cancelUser();

        // ============ Step 7: Delete user 'test' ============
        usersComponent.clickDeleteUser("test");
        assertThat(usersComponent.isUserInList("test")).as("User 'test' should be removed from the list").isFalse();
        assertThat(usersComponent.getUsersCount()).as("User count should return to initial value").isEqualTo(initialUserCount);

        // ============ Step 8: Try to create duplicate 'Admin' user ============
        usersComponent.clickAddUser()
                .setUsername("admin")
                .setPassword("admin123")
                .saveUser(false);

        usersComponent.closeAllMessages();
        assertThat(usersComponent.getUsersCount()).as("User count should be equal to previous value").isEqualTo(initialUserCount);
        usersComponent.cancelUser();

        // ============ Step 9: Re-create user 'test' ============
        usersComponent.clickAddUser()
                .setUsername("test")
                .setPassword("test")
                .saveUser();

        assertThat(usersComponent.isUserInList("test")).as("User 'test' should be created successfully").isTrue();

        // ============ Step 10: Login as 'test' user and verify no projects/options ============
        editorPage.openUserMenu().signOut();
        UserData testUser = new UserData("test", "test");
        editorPage = loginService.login(testUser);

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        List<String> visibleProjects = repositoryPage.getAllVisibleProjectsInTable();
        assertThat(visibleProjects).isEmpty();
        assertThat(repositoryPage.getCreateProjectLink().isVisible())
                .as("Create Project link should not be visible for user without roles")
                .isFalse();
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
        assertThat(usersComponent.getRoleRepository(0)).as("Repository should be 'Design'").isEqualTo("Design");
        assertThat(usersComponent.getRole(0)).as("Role should be 'Manager'").isEqualTo("Manager");
        usersComponent.cancelUser();

        // ============ Step 12: Login as 'test' and verify Manager access ============
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(testUser);
        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        visibleProjects = repositoryPage.getAllVisibleProjectsInTable();
        assertThat(visibleProjects).as("User 'test' should see the project with Manager role").isNotEmpty().contains(projectName);

        // Verify Manager-specific options are available (repository-level Manager: Copy/Delete/Export, no Deploy)
        List<String> managerActions = repositoryPage.getProjectActionLabels(projectName);
        assertThat(managerActions)
                .as("Repository Manager: Copy/Delete/Export visible, no Deploy. Actual: %s", managerActions)
                .contains("Copy", "Delete", "Export")
                .doesNotContain("Deploy");

        // Verify Manager CAN edit tables in Editor tab (open the project first if it is closed)
        if (repositoryPage.isProjectActionAvailable(projectName, "Open")) {
            repositoryPage.openProject(projectName);
        }
        editorPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "Bank Rating");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Rating Algorithm")
                .selectItemInFolder("Rating Algorithm", "BankRatingCalculation");
        EditorToolbarPanelComponent managerToolbar = editorPage.getEditorToolbarPanelComponent();
        assertThat(managerToolbar.getEditTableBtn().isVisible(2000))
                .as("Manager should see Edit button for tables in Editor")
                .isTrue();

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
        assertThat(usersComponent.getRole(0)).as("Role should be changed to 'Viewer'").isEqualTo("Viewer");
        usersComponent.cancelUser();

        // ============ Step 14: Login as 'test' and verify Viewer access ============
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(testUser);
        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        visibleProjects = repositoryPage.getAllVisibleProjectsInTable();
        assertThat(visibleProjects).as("User 'test' should still see the project with Viewer role").isNotEmpty().contains(projectName);

        // Verify Viewer-restricted options are NOT available (repository-level Viewer: Export only)
        List<String> viewerActions = repositoryPage.getProjectActionLabels(projectName);
        assertThat(viewerActions)
                .as("Repository Viewer: Export visible, no Copy/Delete/Deploy/Save. Actual: %s", viewerActions)
                .contains("Export")
                .doesNotContain("Copy", "Delete", "Deploy", "Save");

        // ============ Step 15: Verify Viewer cannot edit tables in Editor tab ============
        if (repositoryPage.isProjectActionAvailable(projectName, "Open")) {
            repositoryPage.openProject(projectName);
        }
        editorPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);

        // Open project and select a module (Example 1 - Bank Rating has "Bank Rating" module)
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "Bank Rating");

        // Expand rules tree and select a table to verify Edit button visibility
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Rating Algorithm")
                .selectItemInFolder("Rating Algorithm", "BankRatingCalculation");

        // Verify Viewer cannot edit tables - Edit button should NOT be visible
        EditorToolbarPanelComponent toolbarPanel = editorPage.getEditorToolbarPanelComponent();
        assertThat(toolbarPanel.getEditTableBtn().isVisible(2000))
                .as("Viewer should NOT see Edit button for tables in Editor")
                .isFalse();
    }
}
