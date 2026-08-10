package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import domain.serviceclasses.constants.User;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.repositorytabcomponents.DeployModalComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.ProjectDetailPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.DeployInfrastructureService;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.StringUtil;
import helpers.utils.TestDataUtil;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Covered atomic tests:
 *   EPBDS-12712 / IPBQA-32158 — Table action buttons (open/close/deploy) in repository projects table
 *   IPBQA-29847               — Repository tab properties (ModifiedBy, ModifiedAt, Revision) multi-user verification
 *
 * React repository (build 032c60a664ce+): the projects table exposes per-row action buttons
 * (project-action-{open,close,copy,export,delete,deploy}-<id>, aria-label Open/Close/...); status lives in
 * the project-detail header/Overview (not a table column), so status is read via getProjectStatusFromDetail /
 * ProjectDetailPage.getStatus ("No Changes"/"Closed" — 6.4.0 restored the legacy wording).
 * The Overview-right column carries Revision (full commit hash) and a combined "Last change" (author+timestamp).
 * Deploy automation: DeployInfrastructureService with a PostgreSQL production repository (DEPLOY_STUDIO_PARAMS).
 */
public class TestRepositoryTableActions extends BaseTest {

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

    private static final String TEMPLATE_NAME = "Sample Project";

    // React project statuses (project-detail header). Legacy "No Changes" is now "Opened".
    private static final String STATUS_OPENED = "No Changes";
    private static final String STATUS_CLOSED = "Closed";

    // React row action aria-labels
    private static final String ACTION_OPEN  = "Open";
    private static final String ACTION_CLOSE = "Close";

    private static final String MAIN_XLS  = "TestRepositoryTableActions.Main.xls";
    private static final String RULES_XLS = "TestRepositoryTableActions.rules.xls";

    private static final String SECOND_USER          = "repo_table_second_user";
    private static final String SECOND_USER_PASSWORD = "Test123!";
    private static final String SECOND_USER_FIRST    = "Second";
    private static final String SECOND_USER_LAST     = "User";

    private static final String VIEWER_USER          = "repo_table_viewer_user";
    private static final String VIEWER_USER_PASSWORD = "Test123!";

    @Test
    @TestCaseId("EPBDS-12712")
    @Description("Repository table action buttons: open/close/deploy row actions; viewer user access")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEPLOY_STUDIO_PARAMS)
    public void testTableActionButtons() {
        String projectName1 = "TestTableActionButtons_P1_" + System.currentTimeMillis();
        String projectName2 = "TestTableActionButtons_P2_" + System.currentTimeMillis();
        String projectName3 = "TestTableActionButtons_P3_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(DriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        // ===== Create viewer user =====
        editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage()
                .clickAddUser()
                .setUsername(VIEWER_USER)
                .setEmail(VIEWER_USER + "@test.com")
                .setPassword(VIEWER_USER_PASSWORD)
                .setFirstName("Viewer")
                .setLastName("User")
                .clickAddRoleBtn()
                .setRoleRepository(0, "Design")
                .setRole(0, "Viewer")
                .saveUser();
        UserData viewerUser = new UserData(VIEWER_USER, VIEWER_USER_PASSWORD);

        // ===== Create project1 from template =====
        editorPage = new EditorPage();
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, projectName1, TEMPLATE_NAME);

        // ===== Deploy project1 via row Deploy action =====
        DeployModalComponent deployModal = repositoryPage.clickDeploy(projectName1);
        String deploymentName = StringUtil.generateUniqueName("Deploy");
        deployModal.deployWithAllFields(null, deploymentName, "First deploy");
        assertThat(deployModal.isSuccessNotificationVisible())
                .as("Deploy should succeed with success notification")
                .isTrue();
        repositoryPage.closeAllMessages();

        // ===== Verify Deploy row action present after deploy =====
        assertThat(repositoryPage.isDeployAvailable(projectName1))
                .as("'Deploy' row action should be present when a deploy repo is configured")
                .isTrue();

        // ===== Close project1 via row action → status "Closed" =====
        assertThat(repositoryPage.isProjectActionAvailable(projectName1, ACTION_CLOSE))
                .as("'Close' row action should be present for open project")
                .isTrue();
        repositoryPage.closeProject(projectName1);
        assertThat(repositoryPage.getProjectStatusFromDetail(projectName1))
                .as("Project status should be 'Closed' after Close row action")
                .isEqualTo(STATUS_CLOSED);

        // ===== Open project1 via row action → status "Opened" =====
        assertThat(repositoryPage.isProjectActionAvailable(projectName1, ACTION_OPEN))
                .as("'Open' row action should be present for closed project")
                .isTrue();
        repositoryPage.openProject(projectName1);
        assertThat(repositoryPage.getProjectStatusFromDetail(projectName1))
                .as("Project status should be 'No Changes' after Open row action")
                .isEqualTo(STATUS_OPENED);

        // ===== Redeploy via Deploy row action → DeployModal opens → cancel =====
        deployModal = repositoryPage.clickDeploy(projectName1);
        assertThat(deployModal.isModalVisible())
                .as("Clicking Deploy row action should open DeployModal for redeploy")
                .isTrue();
        deployModal.clickCancel();

        // ===== Create project2 from Excel, verify status via detail =====
        repositoryPage.createProject(CreateNewProjectComponent.TabName.EXCEL_FILES, projectName2, MAIN_XLS);
        assertThat(repositoryPage.getProjectStatusFromDetail(projectName2))
                .as("Newly created Excel project status should be 'No Changes'")
                .isEqualTo(STATUS_OPENED);

        // ===== Close project2 via row action → "Closed" =====
        repositoryPage.closeProject(projectName2);
        assertThat(repositoryPage.getProjectStatusFromDetail(projectName2))
                .as("Project2 status should be 'Closed' after Close")
                .isEqualTo(STATUS_CLOSED);

        // ===== Open project2 via row action → "Opened" =====
        repositoryPage.openProject(projectName2);
        assertThat(repositoryPage.getProjectStatusFromDetail(projectName2))
                .as("Project2 status should be 'No Changes' after Open")
                .isEqualTo(STATUS_OPENED);

        // ===== Create project3 from template (for viewer test) =====
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, projectName3, TEMPLATE_NAME);

        // ===== Logout admin → login as viewer =====
        editorPage = new EditorPage();
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(viewerUser);
        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        // ===== Viewer: Open project3 (closed in the viewer's workspace) =====
        assertThat(repositoryPage.isProjectActionAvailable(projectName3, ACTION_OPEN))
                .as("'Open' row action should be present for viewer on closed project")
                .isTrue();
        repositoryPage.openProject(projectName3);
        assertThat(repositoryPage.getProjectStatusFromDetail(projectName3))
                .as("Project3 status should be 'No Changes' after viewer Open")
                .isEqualTo(STATUS_OPENED);

        // ===== Viewer: Close project3 =====
        assertThat(repositoryPage.isProjectActionAvailable(projectName3, ACTION_CLOSE))
                .as("'Close' row action should be present for viewer on open project")
                .isTrue();
        repositoryPage.closeProject(projectName3);
        assertThat(repositoryPage.getProjectStatusFromDetail(projectName3))
                .as("Project3 status should be 'Closed' after viewer Close")
                .isEqualTo(STATUS_CLOSED);
    }

    @Test
    @TestCaseId("IPBQA-29847")
    @Description("Repository project properties: Last change (author+date) and Revision verified across multi-user modifications")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEPLOY_STUDIO_PARAMS)
    public void testRepositoryTabProperties() {
        String projectName = "TestRepositoryTabProperties_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(DriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        // ===== Create second user (contributor access) =====
        editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage()
                .clickAddUser()
                .setUsername(SECOND_USER)
                .setEmail(SECOND_USER + "@test.com")
                .setPassword(SECOND_USER_PASSWORD)
                .setFirstName(SECOND_USER_FIRST)
                .setLastName(SECOND_USER_LAST)
                .clickAddRoleBtn()
                .setRoleRepository(0, "Design")
                .setRole(0, "Contributor")
                .saveUser();
        UserData secondUser = new UserData(SECOND_USER, SECOND_USER_PASSWORD);

        // ===== Create project from template as admin =====
        editorPage = new EditorPage();
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, projectName, TEMPLATE_NAME);

        // ===== Verify Overview after creation: last-change (author+date) + revision present =====
        // React couples the commit author to the fill-commit-info dialog (random data on first create), NOT to
        // My Profile, so the exact author name is not asserted; the multi-user check below verifies that the
        // Last-change record CHANGES when a different user modifies the project.
        ProjectDetailPage detail = repositoryPage.openProjectsList().openProjectDetail(projectName);
        String creationLastChange = detail.getOverviewLastChange();
        assertThat(creationLastChange)
                .as("Overview 'Last change' (author + timestamp) should be present after creation")
                .isNotEmpty();
        assertThat(containsValidDate(creationLastChange))
                .as("Overview 'Last change' should contain a valid current date, but was: " + creationLastChange)
                .isTrue();
        assertThat(detail.getOverviewRevision())
                .as("Overview Revision (commit hash) should be present")
                .isNotEmpty();
        repositoryPage.openProjectsList();

        // ===== Logout admin → login secondUser → open project, upload a file, save =====
        editorPage = new EditorPage();
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(secondUser);
        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        repositoryPage.openProject(projectName);
        repositoryPage.openProjectDetail(projectName)
                .openFilesTab()
                .uploadFile(TestDataUtil.getFilePathFromResources(RULES_XLS));
        repositoryPage.openProjectsList().saveProjectWithCommitInfo(projectName, "Second user upload");

        // ===== Verify Overview updated by secondUser (record changed, date valid, revision present) =====
        editorPage = new EditorPage();
        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        detail = repositoryPage.openProjectsList().openProjectDetail(projectName);
        String modifiedLastChange = detail.getOverviewLastChange();
        assertThat(containsValidDate(modifiedLastChange))
                .as("Overview 'Last change' should contain a valid current date after modification")
                .isTrue();
        assertThat(modifiedLastChange)
                .as("Overview 'Last change' should change after the second user's modification (multi-user)")
                .isNotEqualTo(creationLastChange);
        assertThat(detail.getOverviewRevision())
                .as("Overview Revision should be present after modification")
                .isNotEmpty();
        repositoryPage.openProjectsList();

        // ===== Logout secondUser → login admin → deploy → verify Overview still valid =====
        editorPage = new EditorPage();
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(UserService.getUser(User.ADMIN));
        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        DeployModalComponent deployModal = repositoryPage.clickDeploy(projectName);
        deployModal.deployWithAllFields(null, StringUtil.generateUniqueName("Deploy"), "Deploy for properties verification");
        assertThat(deployModal.isSuccessNotificationVisible())
                .as("Deploy should succeed with success notification")
                .isTrue();
        repositoryPage.closeAllMessages();

        detail = repositoryPage.openProjectsList().openProjectDetail(projectName);
        assertThat(containsValidDate(detail.getOverviewLastChange()))
                .as("Overview 'Last change' should still contain a valid date after deploy")
                .isTrue();
        assertThat(detail.getOverviewRevision())
                .as("Overview Revision should still be present after deploy")
                .isNotEmpty();
    }

    // React renders dates as "MMM d, yyyy h:mm a" (e.g. "Jul 16, 2026 6:04 PM"); accept today/yesterday/tomorrow
    // (timezone tolerance, same intent as the legacy MM/dd/yyyy check).
    private boolean containsValidDate(String value) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
        String today     = LocalDate.now().format(fmt);
        String yesterday = LocalDate.now().minusDays(1).format(fmt);
        String tomorrow  = LocalDate.now().plusDays(1).format(fmt);
        return value.contains(today) || value.contains(yesterday) || value.contains(tomorrow);
    }
}
