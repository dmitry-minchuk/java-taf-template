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
import domain.ui.webstudio.components.editortabcomponents.EditorToolbarPanelComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.service.WorkflowService;
import helpers.utils.StringUtil;
import helpers.utils.WaitUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestACLContributorRole extends BaseTest {

    @Test
    @TestCaseId("EPBDS-15712")
    @Description("ACL: Contributor role on repository level — verify V+C+E+D permissions, no Manage, no Deploy")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testACLContributorRoleOnRepositoryLevel() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());

        // ============ Admin setup: create project and contributor user ============
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Example 1 - Bank Rating");

        EditorPage editorPage = new EditorPage();
        UsersPageComponent usersComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();

        usersComponent.clickAddUser()
                .setUsername("contributor")
                .setPassword("contributor")
                .saveUser();

        // Assign Contributor role on Design repository
        usersComponent.clickEditUser("contributor")
                .clickAddRoleBtn()
                .setRoleRepository(0, "Design")
                .setRole(0, "Contributor")
                .saveUser();

        // Verify role was saved
        usersComponent.clickEditUser("contributor");
        assertThat(usersComponent.getRole(0)).as("Role should be saved as Contributor").isEqualTo("Contributor");
        usersComponent.cancelUser();

        // ============ Login as contributor user ============
        editorPage.openUserMenu().signOut();
        UserData contributorUser = new UserData("contributor", "contributor");
        editorPage = loginService.login(contributorUser);

        // ============ Verify project is visible (V permission) ============
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        List<String> visibleProjects = repositoryPage.getAllVisibleProjectsInTable();
        assertThat(visibleProjects)
                .as("Contributor should see project with assigned Contributor role")
                .contains(projectName);

        // ============ Verify Contributor's React row actions (Copy+Export+Delete, no Deploy/Save) ============
        List<String> contributorActions = repositoryPage.getProjectActionLabels(projectName);
        assertThat(contributorActions)
                .as("Contributor should see Copy/Export/Delete (C+V+D), not Deploy/Save. Actual: %s", contributorActions)
                .contains("Copy", "Export", "Delete")
                .doesNotContain("Deploy", "Save");

        // ============ Verify Contributor CAN edit tables in Editor (E permission) ============
        // A freshly-logged-in user has the project CLOSED in their workspace, so open it before the editor.
        repositoryPage.openProject(projectName);
        editorPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "Bank Rating");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Rating Algorithm")
                .selectItemInFolder("Rating Algorithm", "BankRatingCalculation");

        EditorToolbarPanelComponent toolbar = editorPage.getEditorToolbarPanelComponent();
        assertThat(toolbar.getEditTableBtn().isVisible(2000))
                .as("Contributor should see Edit button for tables (E permission)")
                .isTrue();
    }

    @Test
    @TestCaseId("EPBDS-15712")
    @Description("ACL: Contributor vs Viewer comparison — Contributor has Edit+Delete+Copy, Viewer has Export only")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testACLContributorVsViewerComparison() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());

        // ============ Admin setup: create project and two users ============
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Example 1 - Bank Rating");

        EditorPage editorPage = new EditorPage();
        UsersPageComponent usersComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();

        // Create contributor_cmp user
        String contributorUsername = StringUtil.generateUniqueName("contributor");
        usersComponent.clickAddUser()
                .setUsername(contributorUsername)
                .setPassword(contributorUsername)
                .saveUser();

        usersComponent.clickEditUser(contributorUsername)
                .clickAddRoleBtn()
                .setRoleRepository(0, "Design")
                .setRole(0, "Contributor")
                .saveUser();

        // Create viewer_cmp user
        String viewerUsername = StringUtil.generateUniqueName("viewer");
        usersComponent.clickAddUser()
                .setUsername(viewerUsername)
                .setPassword(viewerUsername)
                .saveUser();

        usersComponent.clickEditUser(viewerUsername)
                .clickAddRoleBtn()
                .setRoleRepository(0, "Design")
                .setRole(0, "Viewer")
                .saveUser();

        // ============ Check Contributor permissions ============
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(new UserData(contributorUsername, contributorUsername));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        final RepositoryPage contributorRepoPage = repositoryPage;
        WaitUtil.waitForCondition(
                () -> contributorRepoPage.getAllVisibleProjectsInTable().contains(projectName),
                10000, 500, "Waiting for project to appear for contributor"
        );

        List<String> cmpContributorActions = repositoryPage.getProjectActionLabels(projectName);
        assertThat(cmpContributorActions)
                .as("Contributor: Copy/Delete/Export visible (C+D+V), no Deploy. Actual: %s", cmpContributorActions)
                .contains("Copy", "Delete", "Export")
                .doesNotContain("Deploy");

        // Verify Contributor CAN edit in Editor
        repositoryPage.openProject(projectName);
        editorPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Bank Rating");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Rating Algorithm")
                .selectItemInFolder("Rating Algorithm", "BankRatingCalculation");
        assertThat(editorPage.getEditorToolbarPanelComponent().getEditTableBtn().isVisible(2000))
                .as("Contributor: Edit table button visible (E permission)").isTrue();

        editorPage.openUserMenu().signOut();

        // ============ Check Viewer permissions ============
        editorPage = loginService.login(new UserData(viewerUsername, viewerUsername));
        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        final RepositoryPage viewerRepoPage = repositoryPage;
        WaitUtil.waitForCondition(
                () -> viewerRepoPage.getAllVisibleProjectsInTable().contains(projectName),
                10000, 500, "Waiting for project to appear for viewer"
        );

        List<String> viewerActions = repositoryPage.getProjectActionLabels(projectName);
        assertThat(viewerActions)
                .as("Viewer: Export visible (V), no Copy/Delete/Deploy. Actual: %s", viewerActions)
                .contains("Export")
                .doesNotContain("Copy", "Delete", "Deploy");

        // Verify Viewer CANNOT edit in Editor
        repositoryPage.openProject(projectName);
        editorPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Bank Rating");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Rating Algorithm")
                .selectItemInFolder("Rating Algorithm", "BankRatingCalculation");
        assertThat(editorPage.getEditorToolbarPanelComponent().getEditTableBtn().isVisible(2000))
                .as("Viewer: Edit table button NOT visible (no E permission)").isFalse();
    }

    @Test
    @TestCaseId("EPBDS-15712")
    @Description("ACL: Contributor role on project level — access scoped to permitted project only")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testACLContributorRoleOnProjectLevel() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());

        // ============ Admin setup: create two projects ============
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));
        RepositoryPage adminRepoPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        String project1Name = StringUtil.generateUniqueName("ContribProject1");
        adminRepoPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, project1Name, "Example 1 - Bank Rating");
        WaitUtil.waitForCondition(
                () -> adminRepoPage.getAllVisibleProjectsInTable().contains(project1Name),
                15000, 500, "Waiting for project1 to appear"
        );

        String project2Name = StringUtil.generateUniqueName("ContribProject2");
        adminRepoPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, project2Name, "Sample Project");

        // ============ Create contributor user and assign project-level Contributor role ============
        UsersPageComponent usersComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();

        String username = StringUtil.generateUniqueName("contrib_proj");
        usersComponent.clickAddUser()
                .setUsername(username)
                .setPassword(username)
                .saveUser();

        // Assign Contributor role only on project1 (not project2)
        usersComponent.clickEditUser(username)
                .clickProjectsTab()
                .clickAddRoleBtn()
                .setProject(0, project1Name)
                .setProjectRole(0, "Contributor")
                .saveUser();

        // ============ Login as user and verify scoped access ============
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(new UserData(username, username));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        List<String> visibleProjects = repositoryPage.getAllVisibleProjectsInTable();
        assertThat(visibleProjects)
                .as("User should see project1 (has Contributor role)")
                .contains(project1Name);
        assertThat(visibleProjects)
                .as("User should NOT see project2 (no role assigned)")
                .doesNotContain(project2Name);

        // Verify project-level Contributor's React row actions. A PROJECT-level Contributor gets only
        // A project-scoped role may branch the project, and 6.4.0 gates Copy on either canCopy OR
        // canManageBranches (the Copy dialog does both), so Copy/Sync/Compare/Open Revision are offered.
        // Deleting the project and deploying it stay repo-level and are not.
        List<String> projActions = repositoryPage.getProjectActionLabels(project1Name);
        assertThat(projActions)
                .as("Project-level Contributor: Export visible, no Delete/Deploy. Actual: %s", projActions)
                .contains("Export")
                .doesNotContain("Delete", "Deploy");

        // Verify Contributor CAN edit in Editor
        repositoryPage.openProject(project1Name);
        editorPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(project1Name, "Bank Rating");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Rating Algorithm")
                .selectItemInFolder("Rating Algorithm", "BankRatingCalculation");
        assertThat(editorPage.getEditorToolbarPanelComponent().getEditTableBtn().isVisible(2000))
                .as("Project-level Contributor: Edit table button visible").isTrue();
    }
}
