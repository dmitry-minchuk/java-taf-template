package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.UsersPageComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import helpers.utils.StringUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestACLParsedGroupsUserView extends BaseTest {

    // BRD FR3.5: Successfully parsed and applied group permissions must be
    // visually indicated in the user profile Access Management view.
    // This test verifies that assigned roles are correctly displayed
    // in the Admin → Users → Edit User form.

    @Test
    @TestCaseId("EPBDS-15712")
    @Description("ACL: Verify assigned roles are displayed in user edit form — Design repo role")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testDesignRepoRolesVisibleInUserEditForm() {
        // ============ Admin setup: create project and users with different roles ============
        WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Example 1 - Bank Rating");

        EditorPage editorPage = new EditorPage();
        UsersPageComponent usersComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();

        // Create user with Manager role
        String managerUser = StringUtil.generateUniqueName("mgr_view");
        usersComponent.clickAddUser()
                .setUsername(managerUser)
                .setPassword(managerUser)
                .saveUser();

        usersComponent.clickEditUser(managerUser)
                .clickAddRoleBtn()
                .setRoleRepository(0, "Design")
                .setRole(0, "Manager")
                .saveUser();

        // Create user with Contributor role
        String contributorUser = StringUtil.generateUniqueName("contrib_view");
        usersComponent.clickAddUser()
                .setUsername(contributorUser)
                .setPassword(contributorUser)
                .saveUser();

        usersComponent.clickEditUser(contributorUser)
                .clickAddRoleBtn()
                .setRoleRepository(0, "Design")
                .setRole(0, "Contributor")
                .saveUser();

        // Create user with Viewer role
        String viewerUser = StringUtil.generateUniqueName("viewer_view");
        usersComponent.clickAddUser()
                .setUsername(viewerUser)
                .setPassword(viewerUser)
                .saveUser();

        usersComponent.clickEditUser(viewerUser)
                .clickAddRoleBtn()
                .setRoleRepository(0, "Design")
                .setRole(0, "Viewer")
                .saveUser();

        // ============ Verify roles are persisted and visible when reopening edit form ============

        // Check Manager
        usersComponent.clickEditUser(managerUser);
        assertThat(usersComponent.getRoleRepository(0))
                .as("Manager user should have Design repository assigned")
                .isEqualTo("Design");
        assertThat(usersComponent.getRole(0))
                .as("Manager user should have Manager role")
                .isEqualTo("Manager");
        usersComponent.cancelUser();

        // Check Contributor
        usersComponent.clickEditUser(contributorUser);
        assertThat(usersComponent.getRoleRepository(0))
                .as("Contributor user should have Design repository assigned")
                .isEqualTo("Design");
        assertThat(usersComponent.getRole(0))
                .as("Contributor user should have Contributor role")
                .isEqualTo("Contributor");
        usersComponent.cancelUser();

        // Check Viewer
        usersComponent.clickEditUser(viewerUser);
        assertThat(usersComponent.getRoleRepository(0))
                .as("Viewer user should have Design repository assigned")
                .isEqualTo("Design");
        assertThat(usersComponent.getRole(0))
                .as("Viewer user should have Viewer role")
                .isEqualTo("Viewer");
        usersComponent.cancelUser();
    }

    @Test
    @TestCaseId("EPBDS-15712")
    @Description("ACL: Verify project-level roles are displayed in user edit form")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testProjectLevelRolesVisibleInUserEditForm() {
        // ============ Admin setup ============
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Example 1 - Bank Rating");

        EditorPage editorPage = new EditorPage();
        UsersPageComponent usersComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();

        // Create user with project-level Contributor role
        String username = StringUtil.generateUniqueName("projrole_view");
        usersComponent.clickAddUser()
                .setUsername(username)
                .setPassword(username)
                .saveUser();

        usersComponent.clickEditUser(username)
                .clickProjectsTab()
                .clickAddRoleBtn()
                .setProject(0, projectName)
                .setProjectRole(0, "Contributor")
                .saveUser();

        // ============ Verify project-level role is persisted ============
        usersComponent.clickEditUser(username);
        usersComponent.clickProjectsTab();

        // Verify project role is displayed using the project selector template
        String projectValue = LocalDriverPool.getPage()
                .locator("xpath=//input[@id='projects_0_id']/..").getAttribute("title");
        String roleValue = LocalDriverPool.getPage()
                .locator("xpath=//input[@id='projects_0_role']/..").getAttribute("title");

        assertThat(projectValue)
                .as("Project-level role should show the correct project name")
                .contains(projectName);
        assertThat(roleValue)
                .as("Project-level role should show Contributor")
                .isEqualTo("Contributor");
        usersComponent.cancelUser();
    }
}
