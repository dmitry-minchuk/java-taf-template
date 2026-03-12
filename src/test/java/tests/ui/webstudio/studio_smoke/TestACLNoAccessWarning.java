package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import com.microsoft.playwright.Locator;
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

public class TestACLNoAccessWarning extends BaseTest {

    // BRD Use Case 1, Alternate Flow 1: If no group matches template,
    // user sees warning that they have no rights to view any resources.

    @Test
    @TestCaseId("EPBDS-15712")
    @Description("ACL: User with no roles sees no projects and no Create Project link in Repository")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testNoAccessUserSeesEmptyRepositoryAndNoCreateLink() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());

        // ============ Admin setup: create project and user with NO roles ============
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Example 1 - Bank Rating");

        EditorPage editorPage = new EditorPage();
        UsersPageComponent usersComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();

        String username = StringUtil.generateUniqueName("noaccess");
        usersComponent.clickAddUser()
                .setUsername(username)
                .setPassword(username)
                .saveUser();
        // No role assigned — user has zero access

        // ============ Login as no-roles user ============
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(new UserData(username, username));

        // ============ STEP 1: Verify Repository tab — no projects, no Create Project link ============
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        WaitUtil.sleep(1000, "Allow time for projects to load if any");

        assertThat(repositoryPage.getAllVisibleProjectsInTable())
                .as("User with no roles should see no projects in Repository")
                .isEmpty();

        Locator createProjectLink = LocalDriverPool.getPage()
                .locator("xpath=//div[@id='top']//a[contains(text(), 'Create Project')]");
        assertThat(createProjectLink.count())
                .as("User with no roles should NOT see 'Create Project' link")
                .isZero();

        // ============ STEP 2: Verify Editor tab — no projects in workspace ============
        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);

        Locator totalProjectsHeader = LocalDriverPool.getPage()
                .locator("xpath=//h1[contains(text(),'Total projects: 0')]");
        assertThat(totalProjectsHeader.isVisible())
                .as("Editor should show 'Total projects: 0' for user with no roles")
                .isTrue();

        // ============ STEP 3: Verify no Administration menu item ============
        UserSlidingRightMenuComponent userMenu = editorPage.openUserMenu();
        assertThat(userMenu.isAdministrationMenuItemVisible())
                .as("User with no roles should NOT see Administration menu item")
                .isFalse();
    }

    @Test
    @TestCaseId("EPBDS-15712")
    @Description("ACL: Warning disappears after assigning a role and re-logging in")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testWarningDisappearsAfterRoleAssignment() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());

        // ============ Admin setup ============
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Example 1 - Bank Rating");

        EditorPage editorPage = new EditorPage();
        UsersPageComponent usersComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();

        String username = StringUtil.generateUniqueName("no_then_vw");
        usersComponent.clickAddUser()
                .setUsername(username)
                .setPassword(username)
                .saveUser();

        // ============ Verify no projects as no-roles user ============
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(new UserData(username, username));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        WaitUtil.sleep(1000, "Allow time for projects to load if any");

        assertThat(repositoryPage.getAllVisibleProjectsInTable())
                .as("User with no roles should see no projects initially")
                .isEmpty();

        // ============ Admin assigns Viewer role ============
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(new UserData("admin", "admin"));
        usersComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();

        usersComponent.clickEditUser(username)
                .clickAddRoleBtn()
                .setRoleRepository(0, "Design")
                .setRole(0, "Viewer")
                .saveUser();

        // ============ Re-login and verify projects are now visible ============
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(new UserData(username, username));
        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        final RepositoryPage repoPageRef = repositoryPage;
        WaitUtil.waitForCondition(
                () -> repoPageRef.getAllVisibleProjectsInTable().contains(projectName),
                10000, 500, "Waiting for project to appear after role assignment"
        );

        assertThat(repositoryPage.getAllVisibleProjectsInTable())
                .as("User with Viewer role should now see the project")
                .contains(projectName);
    }
}
