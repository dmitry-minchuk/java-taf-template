package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import domain.serviceclasses.constants.User;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.components.admincomponents.UsersPageComponent;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.DeployInfrastructureService;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.StringUtil;
import helpers.utils.WaitUtil;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestACLDeployWithDeployRepo extends BaseTest {

    // BRD TR2: Deploy is available if user has >= Viewer on design repo
    // AND at least Edit rights on deploy repo.
    // This test requires a production/deploy repository (PostgreSQL).

    private static final Map<String, String> additionalContainerFiles = new HashMap<>();

    @Override
    protected Map<String, String> additionalContainerFiles() {
        return additionalContainerFiles;
    }
    private DeployInfrastructureService deployInfra;

    @Override
    @BeforeMethod
    public void beforeMethod(ITestResult result) {
        additionalContainerFiles.clear();
        deployInfra = DeployInfrastructureService.builder()
                .withPostgres()
                .build();
        deployInfra.start();
        additionalContainerFiles.putAll(deployInfra.getFilesToCopy());
        super.beforeMethod(result);
    }

    @Override
    @AfterMethod
    public void afterMethod(ITestResult result) {
        super.afterMethod(result);
        deployInfra.cleanup();
    }

    @Test
    @TestCaseId("EPBDS-15712")
    @Description("ACL: Deploy button visible for admin when deploy repo is configured")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEPLOY_STUDIO_PARAMS)
    public void testDeployVisibleForAdminWithDeployRepo() {
        String projectName = StringUtil.generateUniqueName("DeployACL");

        // ============ Admin creates project ============
        EditorPage editorPage = new LoginService(DriverPool.getPage())
                .login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE,
                projectName, "Example 1 - Bank Rating");

        // Verify the Deploy action IS available for admin (React project-row action)
        assertThat(repositoryPage.isDeployAvailable(projectName))
                .as("Admin should see Deploy when deploy repo is configured (BRD TR2)")
                .isTrue();
    }

    @Test
    @TestCaseId("EPBDS-15712")
    @Description("ACL: Contributor on design + Contributor on deploy repo → Deploy button visible and functional")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEPLOY_STUDIO_PARAMS)
    public void testDeployVisibleForContributorWithDeployRepoAccess() {
        LoginService loginService = new LoginService(DriverPool.getPage());
        String projectName = StringUtil.generateUniqueName("DeployACL");

        // ============ Admin creates project and user ============
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE,
                projectName, "Example 1 - Bank Rating");

        // Create user with Contributor on Design + Contributor on Deploy repo
        UsersPageComponent usersComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();

        String username = StringUtil.generateUniqueName("contrib_deploy");
        usersComponent.clickAddUser()
                .setUsername(username)
                .setPassword(username)
                .saveUser();

        // Assign Contributor on Design repo
        usersComponent.clickEditUser(username)
                .clickAddRoleBtn()
                .setRoleRepository(0, "Design")
                .setRole(0, "Contributor")
                // Assign Contributor on Deploy/Production repo
                .clickDeployReposTab()
                .clickAddRoleBtn()
                .setDeployRoleRepository(0, "Deployment")
                .setDeployRole(0, "Contributor")
                .saveUser();

        // ============ Login as Contributor user ============
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(new UserData(username, username));
        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        final RepositoryPage repoRef = repositoryPage;
        WaitUtil.waitForCondition(
                () -> repoRef.getAllVisibleProjectsInTable().contains(projectName),
                10000, 500, "Waiting for project to appear for contributor"
        );

        // Verify the Deploy action IS available, plus Contributor's Copy/Export (React project-row actions)
        assertThat(repositoryPage.isDeployAvailable(projectName))
                .as("Contributor with Deploy repo access should see Deploy (BRD TR2)")
                .isTrue();
        assertThat(repositoryPage.isProjectActionAvailable(projectName, "Copy"))
                .as("Contributor: Copy visible (C permission)").isTrue();
        assertThat(repositoryPage.isProjectActionAvailable(projectName, "Export"))
                .as("Contributor: Export visible (V permission)").isTrue();
    }

    @Test
    @TestCaseId("EPBDS-15712")
    @Description("ACL: Viewer on design + Viewer on deploy repo → Deploy NOT visible (needs Edit on deploy)")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEPLOY_STUDIO_PARAMS)
    public void testDeployNotVisibleForViewerOnBothRepos() {
        LoginService loginService = new LoginService(DriverPool.getPage());
        String projectName = StringUtil.generateUniqueName("DeployACL");

        // ============ Admin creates project and user ============
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE,
                projectName, "Example 1 - Bank Rating");

        // Create user with Viewer on Design + Viewer on Deploy repo
        UsersPageComponent usersComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();

        String username = StringUtil.generateUniqueName("viewer_both");
        usersComponent.clickAddUser()
                .setUsername(username)
                .setPassword(username)
                .saveUser();

        usersComponent.clickEditUser(username)
                .clickAddRoleBtn()
                .setRoleRepository(0, "Design")
                .setRole(0, "Viewer")
                .clickDeployReposTab()
                .clickAddRoleBtn()
                .setDeployRoleRepository(0, "Deployment")
                .setDeployRole(0, "Viewer")
                .saveUser();

        // ============ Login as Viewer user ============
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(new UserData(username, username));
        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        final RepositoryPage repoRef = repositoryPage;
        WaitUtil.waitForCondition(
                () -> repoRef.getAllVisibleProjectsInTable().contains(projectName),
                10000, 500, "Waiting for project to appear for viewer"
        );

        // Verify the Deploy action is NOT available (Viewer on deploy lacks Edit)
        assertThat(repositoryPage.isDeployAvailable(projectName))
                .as("Viewer on both repos should NOT see Deploy — needs Edit on deploy repo (BRD TR2)")
                .isFalse();
    }

    @Test
    @TestCaseId("EPBDS-15712")
    @Description("ACL: Viewer on design + Contributor on deploy repo → Deploy button visible (minimum required combo per BRD TR2)")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEPLOY_STUDIO_PARAMS)
    public void testDeployVisibleForViewerOnDesignWithContributorOnDeployRepo() {
        LoginService loginService = new LoginService(DriverPool.getPage());
        String projectName = StringUtil.generateUniqueName("DeployACL");

        // ============ Admin creates project and user ============
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE,
                projectName, "Example 1 - Bank Rating");

        // Create user with Viewer on Design + Contributor on Deploy repo
        // This is the minimum required combination per BRD TR2:
        // "at least viewer rights in design and editor rights in deploy repo"
        // Contributor has Edit (E) permission on deploy repo.
        UsersPageComponent usersComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();

        String username = StringUtil.generateUniqueName("viewer_contrib");
        usersComponent.clickAddUser()
                .setUsername(username)
                .setPassword(username)
                .saveUser();

        usersComponent.clickEditUser(username)
                .clickAddRoleBtn()
                .setRoleRepository(0, "Design")
                .setRole(0, "Viewer")
                .clickDeployReposTab()
                .clickAddRoleBtn()
                .setDeployRoleRepository(0, "Deployment")
                .setDeployRole(0, "Contributor")
                .saveUser();

        // ============ Login as user ============
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(new UserData(username, username));
        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        final RepositoryPage repoRef = repositoryPage;
        WaitUtil.waitForCondition(
                () -> repoRef.getAllVisibleProjectsInTable().contains(projectName),
                10000, 500, "Waiting for project to appear for viewer+contributor"
        );

        // Verify Deploy IS available (Contributor on deploy has Edit); Viewer design perms unchanged
        assertThat(repositoryPage.isDeployAvailable(projectName))
                .as("Viewer(design) + Contributor(deploy) should see Deploy — minimum combo per BRD TR2")
                .isTrue();
        assertThat(repositoryPage.isProjectActionAvailable(projectName, "Export"))
                .as("Viewer: Export visible (V permission)").isTrue();
        assertThat(repositoryPage.isProjectActionAvailable(projectName, "Copy"))
                .as("Viewer: Copy NOT visible (no C permission)").isFalse();
        assertThat(repositoryPage.isProjectActionAvailable(projectName, "Delete"))
                .as("Viewer: Delete NOT visible (no D permission)").isFalse();
    }
}
