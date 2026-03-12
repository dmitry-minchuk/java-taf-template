package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.components.admincomponents.MyProfilePageComponent;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.repositorytabcomponents.DeployModalComponent;
import domain.ui.webstudio.components.repositorytabcomponents.RepositoryContentTabPropertiesComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Covered atomic tests:
 *   EPBDS-12712 / IPBQA-32158 — Table action buttons (open/close/deploy) in repository projects table
 *   IPBQA-29847               — Repository tab properties (ModifiedBy, ModifiedAt, Revision) multi-user verification
 *
 * Deploy automation: uses DeployInfrastructureService with PostgreSQL production repository.
 * Deploy Configuration entity was removed (EPBDS-15093) — deployment is done directly via DeployModal.
 */
public class TestRepositoryTableActions extends BaseTest {

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

    private static final String TEMPLATE_NAME = "Sample Project";

    // Table action button titles — actual @title attributes of <a> tags in Actions column
    private static final String TABLE_ACTION_OPEN   = "Open project";
    private static final String TABLE_ACTION_CLOSE  = "Close project";
    private static final String TABLE_ACTION_DEPLOY = "Deploy project";

    // Status values (from legacy: ProjectStatus constants)
    private static final String STATUS_NO_CHANGES = "No Changes";
    private static final String STATUS_CLOSED     = "Closed";

    // Test data file names (unique across all test resources, per naming convention)
    private static final String MAIN_XLS  = "TestRepositoryTableActions.Main.xls";
    private static final String RULES_XLS = "TestRepositoryTableActions.rules.xls";

    // Second user credentials for testRepositoryTabProperties
    private static final String SECOND_USER          = "repo_table_second_user";
    private static final String SECOND_USER_PASSWORD = "Test123!";
    private static final String SECOND_USER_FIRST    = "Second";
    private static final String SECOND_USER_LAST     = "User";

    // Viewer user credentials for testTableActionButtons
    private static final String VIEWER_USER          = "repo_table_viewer_user";
    private static final String VIEWER_USER_PASSWORD = "Test123!";

    @Test
    @TestCaseId("EPBDS-12712")
    @Description("Repository table action buttons: open/close/deploy icons in Actions column; ButtonsPanel open/close; viewer user access")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEPLOY_STUDIO_PARAMS)
    public void testTableActionButtons() {
        String projectName1 = "TestTableActionButtons_P1_" + System.currentTimeMillis();
        String projectName2 = "TestTableActionButtons_P2_" + System.currentTimeMillis();
        String projectName3 = "TestTableActionButtons_P3_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
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

        // ===== Navigate to Repository and create project1 from template =====
        editorPage = new EditorPage();
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, projectName1, TEMPLATE_NAME);

        // ===== Deploy project1 via right panel Deploy button =====
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName1);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickDeploy();
        DeployModalComponent deployModal = repositoryPage.getDeployModalComponent();
        String deploymentName = StringUtil.generateUniqueName("Deploy");
        deployModal.deployWithAllFields(null, deploymentName, "First deploy");
        assertThat(deployModal.isSuccessNotificationVisible())
                .as("Deploy should succeed with success notification")
                .isTrue();
        repositoryPage.closeAllMessages();
        repositoryPage.refresh();

        // ===== Verify Deploy table action button in Actions column =====
        // After deploy, the projects table should show a Deploy action icon for the project.
        // TABLE_ACTION_DEPLOY title comes from <a[@title]> in the Actions column of the projects table.
        assertThat(repositoryPage.isTableActionButtonPresent(projectName1, TABLE_ACTION_DEPLOY))
                .as("'Deploy' table action button should be present in projects table when deploy repo is configured")
                .isTrue();

        // ===== Close project1 via table action button → status becomes "Closed" =====
        assertThat(repositoryPage.isTableActionButtonPresent(projectName1, TABLE_ACTION_CLOSE))
                .as("'Close project' table action button should be present for open project")
                .isTrue();
        repositoryPage.clickTableActionButton(projectName1, TABLE_ACTION_CLOSE);
        assertThat(repositoryPage.getProjectStatusFromTable(projectName1))
                .as("Project status in table should be 'Closed' after clicking Close table action")
                .isEqualTo(STATUS_CLOSED);

        // ===== Open project1 via table action button → status becomes "No Changes" =====
        assertThat(repositoryPage.isTableActionButtonPresent(projectName1, TABLE_ACTION_OPEN))
                .as("'Open project' table action button should be present for closed project")
                .isTrue();
        repositoryPage.clickTableActionButton(projectName1, TABLE_ACTION_OPEN);
        handleOpenProjectDialog(repositoryPage);
        assertThat(repositoryPage.getProjectStatusFromTable(projectName1))
                .as("Project status in table should be 'No Changes' after clicking Open table action")
                .isEqualTo(STATUS_NO_CHANGES);

        // ===== Click Deploy table action for already-deployed project → DeployModal opens for redeploy =====
        repositoryPage.clickTableActionButton(projectName1, TABLE_ACTION_DEPLOY);
        deployModal = repositoryPage.getDeployModalComponent();
        assertThat(deployModal.isModalVisible())
                .as("Clicking Deploy table action should open DeployModal for redeploy")
                .isTrue();
        deployModal.clickCancel();

        // ===== Create project2 from Excel file =====
        repositoryPage.createProject(CreateNewProjectComponent.TabName.EXCEL_FILES, projectName2, MAIN_XLS);

        // ===== Select project2 in tree and verify status "No Changes" via Properties tab =====
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName2);
        RepositoryContentTabPropertiesComponent propertiesTab = repositoryPage
                .getRepositoryContentTabSwitcherComponent()
                .selectPropertiesTab();
        assertThat(propertiesTab.getStatus())
                .as("Newly created Excel project status should be 'No Changes'")
                .isEqualTo(STATUS_NO_CHANGES);

        // ===== Close project2 via ButtonsPanel → status "Closed" =====
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickCloseBtn();
        propertiesTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        assertThat(propertiesTab.getStatus())
                .as("Project status should be 'Closed' after ButtonsPanel Close")
                .isEqualTo(STATUS_CLOSED);

        // ===== Open project2 via ButtonsPanel → status "No Changes" =====
        repositoryPage.getRepositoryContentButtonsPanelComponent().openProject();
        handleOpenProjectDialog(repositoryPage);
        propertiesTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        assertThat(propertiesTab.getStatus())
                .as("Project status should be 'No Changes' after ButtonsPanel Open")
                .isEqualTo(STATUS_NO_CHANGES);

        // ===== Create project3 from template (for viewer user test) =====
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, projectName3, TEMPLATE_NAME);

        // ===== Logout admin → login as viewer =====
        editorPage = new EditorPage();
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(viewerUser);

        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        // ===== Viewer: Open project3 via table action button (project3 is "Closed" for viewer) =====
        assertThat(repositoryPage.isTableActionButtonPresent(projectName3, TABLE_ACTION_OPEN))
                .as("'Open project' table action button should be present for viewer on closed project")
                .isTrue();
        repositoryPage.clickTableActionButton(projectName3, TABLE_ACTION_OPEN);
        handleOpenProjectDialog(repositoryPage);
        assertThat(repositoryPage.getProjectStatusFromTable(projectName3))
                .as("Project status in table should be 'No Changes' after viewer clicks Open table action")
                .isEqualTo(STATUS_NO_CHANGES);

        // ===== Viewer: Close project3 via table action button =====
        assertThat(repositoryPage.isTableActionButtonPresent(projectName3, TABLE_ACTION_CLOSE))
                .as("'Close project' table action button should be present for viewer on open project")
                .isTrue();
        repositoryPage.clickTableActionButton(projectName3, TABLE_ACTION_CLOSE);
        assertThat(repositoryPage.getProjectStatusFromTable(projectName3))
                .as("Project status in table should be 'Closed' after viewer clicks Close table action")
                .isEqualTo(STATUS_CLOSED);
    }

    @Test
    @TestCaseId("IPBQA-29847")
    @Description("Repository tab Properties: ModifiedBy, ModifiedAt, Revision verified across multi-user project modifications")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEPLOY_STUDIO_PARAMS)
    public void testRepositoryTabProperties() {
        String projectName = "TestRepositoryTabProperties_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        // ===== Create second user (contributor access) with known names =====
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

        // ===== Navigate to Repository and create project from template as admin (firstUser) =====
        // createProject() internally calls fillCommitInfo() which may show the commit info dialog.
        // After creation, the profile reflects the actual commit author name (filled by the dialog or pre-existing).
        editorPage = new EditorPage();
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, projectName, TEMPLATE_NAME);

        // ===== Read admin's actual commit author name from My Profile after project creation =====
        // The commit info dialog (if it appeared) updates the profile. We read what is now there.
        // If firstName+lastName are empty → username is used as commit author.
        editorPage = new EditorPage();
        MyProfilePageComponent myProfile = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToMyProfilePage();
        String adminFirstName = myProfile.getFirstName();
        String adminLastName = myProfile.getLastName();
        String firstLastFirstUser;
        if (adminFirstName.isEmpty() && adminLastName.isEmpty()) {
            firstLastFirstUser = myProfile.getUsername();
        } else {
            firstLastFirstUser = adminFirstName.trim();
        }

        // ===== Navigate back to Repository and select project =====
        editorPage = new EditorPage();
        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        // ===== Select project in tree, verify Properties tab =====
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        RepositoryContentTabPropertiesComponent propertiesTab = repositoryPage
                .getRepositoryContentTabSwitcherComponent()
                .selectPropertiesTab();

        // Assert ModifiedBy = admin's actual commit author name
        assertThat(propertiesTab.getModifiedBy())
                .as("ModifiedBy should equal admin's commit author name after project creation")
                .isEqualTo(firstLastFirstUser);

        // Assert ModifiedAt contains today/yesterday/tomorrow (timezone tolerance — same as legacy)
        String creationProjectTime = propertiesTab.getModifiedAt();
        assertThat(containsValidDate(creationProjectTime))
                .as("ModifiedAt should contain a valid current date, but was: " + creationProjectTime)
                .isTrue();

        // Assert Revision length == 6
        assertThat(propertiesTab.getRevision().length())
                .as("Revision ID should be 6 characters long")
                .isEqualTo(6);

        // ===== Logout admin =====
        editorPage = new EditorPage();
        editorPage.openUserMenu().signOut();

        // ===== Login as secondUser =====
        editorPage = loginService.login(secondUser);
        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        // ===== Second user: select project, open it, upload file, save changes =====
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().openProject();
        handleOpenProjectDialog(repositoryPage);

        repositoryPage.getRepositoryContentButtonsPanelComponent().clickUploadFileBtn();
        repositoryPage.getUploadFileDialogComponent()
                .uploadFile(TestDataUtil.getFilePathFromResources(RULES_XLS))
                .clickUploadButton();

        repositoryPage.getRepositoryContentButtonsPanelComponent().clickSaveBtn();
        repositoryPage.getSaveChangesComponent().getSaveBtn().click();
        repositoryPage.fillCommitInfo();
        repositoryPage.waitUntilSpinnerLoaded();
        repositoryPage.refresh();

        // ===== Read secondUser's actual commit author name from My Profile after save =====
        editorPage = new EditorPage();
        MyProfilePageComponent secondUserProfile = editorPage.openUserMenu()
                .navigateToMyProfile()
                .navigateToMyProfilePage();
        String secondFirstName = secondUserProfile.getFirstName();
        String firstLastSecondUser = secondFirstName.isEmpty() ? secondUserProfile.getUsername() : secondFirstName;

        // ===== Navigate back to Repository and re-select project =====
        editorPage = new EditorPage();
        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        // ===== Re-select project and verify Properties updated by secondUser =====
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        propertiesTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();

        // Assert ModifiedBy = second user's actual commit author name
        assertThat(propertiesTab.getModifiedBy())
                .as("ModifiedBy should equal second user's commit author name after modification")
                .isEqualTo(firstLastSecondUser);

        // Assert ModifiedAt contains today/yesterday/tomorrow
        String modifiedAt = propertiesTab.getModifiedAt();
        assertThat(containsValidDate(modifiedAt))
                .as("ModifiedAt should contain a valid current date after modification, but was: " + modifiedAt)
                .isTrue();

        // Assert ModifiedAt changed from creation time
        assertThat(modifiedAt)
                .as("ModifiedAt should have changed after second user's modification")
                .isNotEqualTo(creationProjectTime);

        // ===== Deploy project and verify deploy button is available =====
        // NOTE: Legacy Deploy Configuration entity was removed (EPBDS-15093).
        // Deployment now works directly via DeployModal.
        // Legacy steps 1-10 (Deploy Configuration CRUD) are obsolete and skipped.

        // Logout secondUser → Login as admin (firstUser)
        editorPage = new EditorPage();
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(UserService.getUser(User.ADMIN));

        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);

        // Deploy project via DeployModal
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickDeploy();
        DeployModalComponent deployModal = repositoryPage.getDeployModalComponent();
        String deploymentName = StringUtil.generateUniqueName("Deploy");
        deployModal.deployWithAllFields(null, deploymentName, "Deploy for properties verification");
        assertThat(deployModal.isSuccessNotificationVisible())
                .as("Deploy should succeed with success notification")
                .isTrue();
        repositoryPage.closeAllMessages();

        // Verify project properties after deploy — ModifiedBy still reflects last save author
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        propertiesTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        String postDeployModifiedAt = propertiesTab.getModifiedAt();
        assertThat(containsValidDate(postDeployModifiedAt))
                .as("ModifiedAt should still be a valid date after deploy")
                .isTrue();
        assertThat(propertiesTab.getRevision().length())
                .as("Revision should still be 6 characters after deploy")
                .isEqualTo(6);
    }

    private void handleOpenProjectDialog(RepositoryPage repositoryPage) {
        if (repositoryPage.getConfirmOpeningDialogBtn().isVisible(1000)) {
            repositoryPage.getConfirmOpeningDialogBtn().click();
            repositoryPage.getConfirmOpeningDialogShade().waitForHidden(3000);
        }
    }

    // Same date validation logic as legacy: Extensions.getCurrentDate/getYesterdayDate/getTomorrowDate("MM/dd/yyyy")
    private boolean containsValidDate(String value) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        String today     = LocalDate.now().format(fmt);
        String yesterday = LocalDate.now().minusDays(1).format(fmt);
        String tomorrow  = LocalDate.now().plusDays(1).format(fmt);
        return value.contains(today) || value.contains(yesterday) || value.contains(tomorrow);
    }
}
