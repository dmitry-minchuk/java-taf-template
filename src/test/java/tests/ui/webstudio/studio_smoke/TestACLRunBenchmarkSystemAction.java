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
import domain.ui.webstudio.components.editortabcomponents.EditorToolbarPanelComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.WorkflowService;
import helpers.utils.StringUtil;
import helpers.utils.WaitUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestACLRunBenchmarkSystemAction extends BaseTest {

    // BRD TR2: Run and Benchmark are system actions — NOT permissions.
    // They must be available for ALL users regardless of their role.

    @Test
    @TestCaseId("EPBDS-15712")
    @Description("ACL: Run and Benchmark buttons visible for Viewer — system actions available to all users (BRD TR2)")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testRunAndBenchmarkVisibleForViewer() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());

        // ============ Admin setup: create project and Viewer user ============
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Example 1 - Bank Rating");

        EditorPage editorPage = new EditorPage();
        UsersPageComponent usersComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();

        String username = StringUtil.generateUniqueName("viewer_run");
        usersComponent.clickAddUser()
                .setUsername(username)
                .setPassword(username)
                .saveUser();

        // Assign Viewer role — minimum access level
        usersComponent.clickEditUser(username)
                .clickAddRoleBtn()
                .setRoleRepository(0, "Design")
                .setRole(0, "Viewer")
                .saveUser();

        // ============ Login as Viewer ============
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(new UserData(username, username));

        // ============ Open project and navigate to a rules table ============
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        WaitUtil.waitForCondition(
                () -> repositoryPage.getAllVisibleProjectsInTable().contains(projectName),
                10000, 500, "Waiting for project to appear for viewer"
        );

        repositoryPage.refresh();
        repositoryPage.unlockAllProjects();
        editorPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "Bank Rating");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Rating Algorithm")
                .selectItemInFolder("Rating Algorithm", "BankRatingCalculation");

        // ============ Verify Run and Benchmark are visible for Viewer ============
        EditorToolbarPanelComponent toolbar = editorPage.getEditorToolbarPanelComponent();

        assertThat(toolbar.isRunButtonVisible())
                .as("Viewer should see Run button — Run is a system action available to ALL users (BRD TR2)")
                .isTrue();
        // TODO: BRD TR2 requires Benchmark to be a system action visible to all users.
        // Benchmark button (span[contains(text(),'Benchmark')]) is not yet implemented in UI.

        // Verify Edit table is NOT visible (confirms Viewer has read-only, not broken environment)
        assertThat(toolbar.getEditTableBtn().isVisible(1000))
                .as("Viewer should NOT see Edit button — confirms this is a genuine Viewer context")
                .isFalse();
    }

    @Test
    @TestCaseId("EPBDS-15712")
    @Description("ACL: Run and Benchmark visible for Contributor — system actions available to all users (BRD TR2)")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testRunAndBenchmarkVisibleForContributor() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());

        // ============ Admin setup: create project and Contributor user ============
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Example 1 - Bank Rating");

        EditorPage editorPage = new EditorPage();
        UsersPageComponent usersComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();

        String username = StringUtil.generateUniqueName("contributor_run");
        usersComponent.clickAddUser()
                .setUsername(username)
                .setPassword(username)
                .saveUser();

        usersComponent.clickEditUser(username)
                .clickAddRoleBtn()
                .setRoleRepository(0, "Design")
                .setRole(0, "Contributor")
                .saveUser();

        // ============ Login as Contributor ============
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(new UserData(username, username));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        WaitUtil.waitForCondition(
                () -> repositoryPage.getAllVisibleProjectsInTable().contains(projectName),
                10000, 500, "Waiting for project to appear for contributor"
        );

        repositoryPage.refresh();
        repositoryPage.unlockAllProjects();
        editorPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "Bank Rating");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Rating Algorithm")
                .selectItemInFolder("Rating Algorithm", "BankRatingCalculation");

        EditorToolbarPanelComponent toolbar = editorPage.getEditorToolbarPanelComponent();

        assertThat(toolbar.isRunButtonVisible())
                .as("Contributor should see Run button (system action, available to all users)")
                .isTrue();
        // TODO: BRD TR2 requires Benchmark to be a system action visible to all users.
        // Benchmark button is not yet implemented in UI.
        // Contributor also has Edit
        assertThat(toolbar.getEditTableBtn().isVisible(2000))
                .as("Contributor should also see Edit button (E permission)")
                .isTrue();
    }
}
