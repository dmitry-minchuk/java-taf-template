package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.components.admincomponents.UsersPageComponent;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.repositorytabcomponents.RepositoryContentButtonsPanelComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import configuration.core.ui.WebElement;
import helpers.utils.StringUtil;
import helpers.utils.WaitUtil;
import org.testng.Assert;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class TestAdminUsersProjects extends BaseTest {

    @Test
    @TestCaseId("IPBQA-32785") // New ID
    @Description("Users management for project roles: create user, assign project-specific roles and verify permissions.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testProjectRolesManagement() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());

        // ============ Admin Setup: Log in and create two projects ============
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));
        RepositoryPage adminRepoPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        String project1Name = StringUtil.generateUniqueName("ProjectRoleTest1");
        adminRepoPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, project1Name, "Example 1 - Bank Rating");

        // Wait for the first project to appear in the table, confirming the refresh is complete
        WaitUtil.waitForCondition(
                () -> adminRepoPage.getAllVisibleProjectsInTable().contains(project1Name),
                15000, // timeout in ms
                500,   // polling interval in ms
                "Waiting for project '" + project1Name + "' to appear in the repository table."
        );

        String project2Name = StringUtil.generateUniqueName("ProjectRoleTest2");
        adminRepoPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, project2Name, "Sample Project");


        // ============ Start Test: Navigate to users page ============
        UsersPageComponent usersComponent = editorPage
                .openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();

        Assert.assertTrue(usersComponent.isUserInList("admin"), "Admin user should be in the users list");

        // ============ Steps 4-5: Add new user 'test' ============
        int initialUserCount = usersComponent.getUsersCount();
        usersComponent.clickAddUser()
                .setUsername("test")
                .setPassword("test")
                .inviteUser();

        Assert.assertTrue(usersComponent.isUserInList("test"), "User 'test' should be added to the list");
        Assert.assertEquals(usersComponent.getUsersCount(), initialUserCount + 1, "User count should increase by 1");

        // ============ Step 10: Login as 'test' user and verify no projects/options ============
        editorPage.openUserMenu().signOut();
        UserData testUser = new UserData("test", "test");
        editorPage = loginService.login(testUser);
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        List<String> visibleProjects = repositoryPage.getAllVisibleProjectsInTable();
        assertThat(visibleProjects).isEmpty();
        editorPage.openUserMenu().signOut();

        // ============ Step 11: Admin adds Manager role for BOTH projects to 'test' user ============
        editorPage = loginService.login(UserService.getUser(User.ADMIN));
        usersComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();

        usersComponent.clickEditUser("test")
                .clickProjectsTab()
                .clickAddRoleBtn()
                .setProject(0, project1Name)
                .setProjectRole(0, "Manager")
                .clickAddRoleBtn()
                .setProject(1, project2Name)
                .setProjectRole(1, "Manager")
                .saveUser();

        // ============ Step 12: Login as 'test' and verify Manager access to BOTH projects ============
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(testUser);
        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        visibleProjects = repositoryPage.getAllVisibleProjectsInTable();
        assertThat(visibleProjects)
            .as("User 'test' should see BOTH projects with assigned Manager roles")
            .contains(project1Name)
            .contains(project2Name);

        // Verify Project-level Manager permissions
        // Note: Project-level Manager has different permissions than Repository-level Manager.
        // Project Manager does NOT have Delete permission (unlike Repository Manager).
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", project1Name);
        RepositoryContentButtonsPanelComponent managerButtonsPanel = repositoryPage.getRepositoryContentButtonsPanelComponent();
        assertThat(managerButtonsPanel.isCopyBtnVisible()).as("Project Manager should see Copy button").isTrue();
        assertThat(managerButtonsPanel.isDeleteBtnVisible()).as("Project Manager should NOT see Delete button (unlike Repository Manager)").isFalse();
        assertThat(managerButtonsPanel.isDeployBtnVisible()).as("Project Manager should NOT see Deploy button").isFalse();
        assertThat(managerButtonsPanel.isExportBtnVisible()).as("Project Manager should see Export button").isTrue();

        editorPage.openUserMenu().signOut();

        // ============ Step 13: Admin changes 'test' role to Viewer for BOTH projects ============
        editorPage = loginService.login(UserService.getUser(User.ADMIN));

        usersComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();

        usersComponent.clickEditUser("test")
                .clickProjectsTab()
                .setProjectRole(0, "Viewer")
                .setProjectRole(1, "Viewer")
                .saveUser();

        // ============ Step 14: Login as 'test' and verify Viewer access to BOTH projects ============
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(testUser);
        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        visibleProjects = repositoryPage.getAllVisibleProjectsInTable();
        assertThat(visibleProjects)
            .as("User 'test' should still see BOTH projects with Viewer roles")
            .contains(project1Name)
            .contains(project2Name);

        // Verify Viewer-restricted options are NOT available
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", project1Name);
        RepositoryContentButtonsPanelComponent viewerButtonsPanel = repositoryPage.getRepositoryContentButtonsPanelComponent();
        assertThat(viewerButtonsPanel.isCopyBtnVisible()).as("Viewer should NOT see Copy button").isFalse();
        assertThat(viewerButtonsPanel.isDeleteBtnVisible()).as("Viewer should NOT see Delete button").isFalse();
        assertThat(viewerButtonsPanel.isDeployBtnVisible()).as("Viewer should NOT see Deploy button").isFalse();
        assertThat(viewerButtonsPanel.isSaveBtnVisible()).as("Viewer should NOT see Save button").isFalse();
        assertThat(viewerButtonsPanel.isExportBtnVisible()).as("Viewer should still see Export button").isTrue();
    }
}
