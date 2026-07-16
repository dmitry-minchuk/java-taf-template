package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.repositorytabcomponents.DeployModalComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.ProjectDetailPage;
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
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Covered atomic tests (IPBQA-30010) — React repository (build 032c60a664ce+):
 *   Project status lifecycle (Opened -> Editing -> Opened -> Closed -> Opened), filter by name,
 *   folder creation, permanent delete, multi-user workspace isolation, deploy both projects.
 *
 * React adaptations (removed/changed behaviour — verified live):
 *   - The projects table has NO Status/Modified By/Modified At columns (status lives in the project detail),
 *     so the legacy "6 columns" structure check is dropped and status is read via getProjectStatusFromDetail.
 *   - Status vocabulary changed: "No Changes" -> "Opened", "In Editing" -> "Editing", "Closed" -> "Closed".
 *   - The per-user "locked" status was removed: a second user viewing a project another user is editing just
 *     sees it as "Closed" in their own workspace, so the lock check becomes a workspace-isolation check.
 */
public class TestRepositoryBrowsingFilterStatus extends BaseTest {

    private static final Map<String, String> additionalContainerFiles = new HashMap<>();
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

    private static final String PROJECT_1 = "TestRepositoryBrowsingFilterStatus";
    private static final String PROJECT_2 = "TestRepositoryBrowsingFilterStatus2";
    private static final String TEMPLATE_NAME = "Sample Project";
    private static final String SECOND_USER = "repo_filter_second_user";
    private static final String SECOND_USER_PASSWORD = "Test123!";
    private static final String STATUS_OPENED = "Opened";
    private static final String STATUS_EDITING = "Editing";
    private static final String STATUS_CLOSED = "Closed";

    @Test
    @TestCaseId("IPBQA-30010")
    @Description("Repository - Browsing, filter by name, status lifecycle, folder creation, multi-user workspace isolation")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEPLOY_STUDIO_PARAMS)
    public void testRepositoryBrowsingFilterStatus() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        // ===== Step 1: Create second user with Contributor access =====
        editorPage.openUserMenu().navigateToAdministration().navigateToUsersPage()
                .clickAddUser()
                .setUsername(SECOND_USER)
                .setEmail(SECOND_USER + "@test.com")
                .setPassword(SECOND_USER_PASSWORD)
                .setFirstName("Repo")
                .setLastName("Viewer")
                .clickAddRoleBtn()
                .setRoleRepository(0, "Design")
                .setRole(0, "Contributor")
                .saveUser();
        UserData secondUser = new UserData(SECOND_USER, SECOND_USER_PASSWORD);

        // ===== Step 2: Create two projects from template =====
        editorPage = new EditorPage();
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, PROJECT_1, TEMPLATE_NAME);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, PROJECT_2, TEMPLATE_NAME);

        // ===== Step 3: Deploy both projects via the React row Deploy action =====
        DeployModalComponent deployModal = repositoryPage.clickDeploy(PROJECT_1);
        deployModal.deployWithAllFields(null, StringUtil.generateUniqueName("Dep1"), "Deploy project 1");
        assertThat(deployModal.isSuccessNotificationVisible()).as("Deploy of PROJECT_1 should succeed").isTrue();
        repositoryPage.closeAllMessages();

        deployModal = repositoryPage.clickDeploy(PROJECT_2);
        deployModal.deployWithAllFields(null, StringUtil.generateUniqueName("Dep2"), "Deploy project 2");
        assertThat(deployModal.isSuccessNotificationVisible()).as("Deploy of PROJECT_2 should succeed").isTrue();
        repositoryPage.closeAllMessages();

        // ===== Step 5: Row actions for an open project (repo-level admin: Copy/Export/Delete) =====
        List<String> p1Actions = repositoryPage.getProjectActionLabels(PROJECT_1);
        assertThat(p1Actions)
                .as("Open project should expose Copy/Export/Delete row actions. Actual: %s", p1Actions)
                .contains("Copy", "Export", "Delete");

        // ===== Step 7: Freshly created project status is "Opened" =====
        assertThat(repositoryPage.getProjectStatusFromDetail(PROJECT_1))
                .as("Newly created project status should be 'Opened'").isEqualTo(STATUS_OPENED);

        // ===== Step 9-10: Put project into "Editing" via the editor edit-project dialog =====
        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(PROJECT_1);
        editorPage.openEditProjectDialog(PROJECT_1).setDescription("test edit").clickUpdateButton();
        editorPage.waitUntilSpinnerLoaded();
        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        assertThat(repositoryPage.getProjectStatusFromDetail(PROJECT_1))
                .as("Project status should be 'Editing' after an edit").isEqualTo(STATUS_EDITING);

        // ===== Step 11: Save project → back to "Opened" =====
        repositoryPage.saveProject(PROJECT_1, "Save after edit");
        assertThat(repositoryPage.getProjectStatusFromDetail(PROJECT_1))
                .as("Project status should be 'Opened' after save").isEqualTo(STATUS_OPENED);

        // ===== Step 12: Close project → "Closed" =====
        repositoryPage.closeProject(PROJECT_1);
        assertThat(repositoryPage.getProjectStatusFromDetail(PROJECT_1))
                .as("Project status should be 'Closed' after closing").isEqualTo(STATUS_CLOSED);
        assertThat(repositoryPage.isProjectActionAvailable(PROJECT_1, "Open"))
                .as("Open row action should appear for a closed project").isTrue();

        // ===== Step 13: Open the closed project → "Opened" =====
        repositoryPage.openProject(PROJECT_1);
        assertThat(repositoryPage.getProjectStatusFromDetail(PROJECT_1))
                .as("Project status should be 'Opened' after opening").isEqualTo(STATUS_OPENED);

        // ===== Step 17: Multi-user workspace isolation (React removed the per-user "locked" status) =====
        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(PROJECT_1);
        editorPage.openEditProjectDialog(PROJECT_1).setDescription("isolation test").clickUpdateButton();
        editorPage.waitUntilSpinnerLoaded();

        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(secondUser);
        RepositoryPage secondUserPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        WaitUtil.waitForCondition(
                () -> secondUserPage.getAllVisibleProjectsInTable().contains(PROJECT_1),
                10000, 500, "Waiting for project to appear for the second user");
        assertThat(secondUserPage.getProjectStatusFromDetail(PROJECT_1))
                .as("A second user sees the project as 'Closed' in their own workspace (no per-user lock in React)")
                .isEqualTo(STATUS_CLOSED);

        // Admin still sees "Editing" for their own workspace
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(UserService.getUser(User.ADMIN));
        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        assertThat(repositoryPage.getProjectStatusFromDetail(PROJECT_1))
                .as("Admin should still see 'Editing' for their own workspace").isEqualTo(STATUS_EDITING);
        repositoryPage.saveProject(PROJECT_1, "Save isolation edit");

        // ===== Step 18: Filter by name =====
        repositoryPage.filterByName(PROJECT_2);
        assertThat(repositoryPage.countVisibleProjectsInTable())
                .as("Filter by name should show exactly 1 project").isEqualTo(1);
        assertThat(repositoryPage.getAllVisibleProjectsInTable().getFirst())
                .as("Only PROJECT_2 should be visible after filter").contains(PROJECT_2);
        repositoryPage.clearNameFilter();
        assertThat(repositoryPage.countVisibleProjectsInTable())
                .as("After clearing the filter all projects should be visible again").isGreaterThanOrEqualTo(2);

        // ===== Step 19: Create a folder inside the project (Files tab) =====
        ProjectDetailPage detail = repositoryPage.openProjectDetail(PROJECT_1).openFilesTab();
        detail.createFolder("TestFolder");
        assertThat(detail.isFolderPresent("TestFolder"))
                .as("Created folder should be visible in the project's Files tab").isTrue();
        repositoryPage.openProjectsList();

        // ===== Step 20: Permanent delete removes the project =====
        repositoryPage.deleteProject(PROJECT_1)
                .enterDeletionComment("Removed by automated regression test")
                .acknowledgePermanentDeletion()
                .clickDelete();
        repositoryPage.openProjectsList();
        assertThat(repositoryPage.getAllVisibleProjectsInTable())
                .as("Permanently deleted project must not be listed").doesNotContain(PROJECT_1);
    }
}
