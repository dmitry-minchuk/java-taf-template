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
import domain.ui.webstudio.components.repositorytabcomponents.RepositoryContentTabPropertiesComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * Covered atomic tests (IPBQA-30010):
 *   2.2.1  - Browsing Design repo: project status lifecycle (No Changes → In Editing → No Changes → Closed → Archived → Closed → No Changes)
 *   2.2.2  - Design repo: Filter by name
 *   2.2.3  - Design repo: Advanced filter (show/hide archived/deleted projects)
 *   2.2.12 - Closing a Project
 *   2.2.14 - Saving a Project
 *   Multi-user locking status (Closed + locked by other user)
 *
 * NOT covered (deploy automation required):
 *   2.2.4  - Browsing Deployment repo: project pictures by status
 *   2.2.5  - Deployment repo: Filter by name
 *   Deploy Configuration creation and verification (steps 8-11 of legacy IPBQA-30010)
 *   Production repository browsing, expand deployments, file presence check
 *   Production filter by name (steps 31-38 of legacy IPBQA-30010)
 *
 * TODO: Steps marked [DEPLOY BLOCKED] will be added when deploy automation is ready.
 */
public class TestRepositoryBrowsingFilterStatus extends BaseTest {

    private static final String PROJECT_1 = "TestRepositoryBrowsingFilterStatus";
    private static final String PROJECT_2 = "TestRepositoryBrowsingFilterStatus2";
    private static final String TEMPLATE_NAME = "Sample Project";
    private static final String SECOND_USER = "repo_filter_second_user";
    private static final String SECOND_USER_PASSWORD = "Test123!";

    @Test
    @TestCaseId("IPBQA-30010")
    @Description("Repository - Browsing, filter by name, advanced filter, project status lifecycle, multi-user locking")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testRepositoryBrowsingFilterStatus() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        // ===== Step 1: Create second user with Contributor access =====
        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        adminPage.navigateToUsersPage()
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

        // [DEPLOY BLOCKED] Step 3: Deploy both projects and verify deployment status icons
        // Requires: ButtonsPanel.deployProject() automation and deploy configuration support.
        // When unblocked: verify status after deploy and check deploy confirmation.

        // ===== Step 4: Verify table structure (6 columns) =====
        repositoryPage.refresh();
        List<String> headers = repositoryPage.getProjectsTable().getHeaders();
        assertThat(headers)
                .as("Projects table should have 6 columns: Name, Branch, Status, Modified By, Modified At, Actions")
                .containsExactly("Name", "Branch", "Status", "Modified By", "Modified At", "Actions");

        // ===== Step 5: Verify action buttons present for open project =====
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_1);

        assertThat(repositoryPage.getRepositoryContentButtonsPanelComponent().isCloseBtnVisible())
                .as("Close button should be present for opened project")
                .isTrue();
        assertThat(repositoryPage.getRepositoryContentButtonsPanelComponent().isCopyBtnVisible())
                .as("Copy button should be present for opened project")
                .isTrue();
        assertThat(repositoryPage.getRepositoryContentButtonsPanelComponent().isExportBtnVisible())
                .as("Export button should be present for opened project")
                .isTrue();

        // ===== Step 6: Verify Properties tab available and tabs list =====
        List<String> availableTabs = repositoryPage.getRepositoryContentTabSwitcherComponent()
                .getAvailableTabNames();
        assertThat(availableTabs)
                .as("Properties tab should be available")
                .contains("Properties", "Revisions");

        // ===== Step 7: Status "No Changes" for freshly created project =====
        RepositoryContentTabPropertiesComponent propertiesTab = repositoryPage
                .getRepositoryContentTabSwitcherComponent()
                .selectPropertiesTab();
        assertThat(propertiesTab.getStatus())
                .as("Newly created project status should be 'No Changes'")
                .isEqualTo("No Changes");

        // [DEPLOY BLOCKED] Step 8: Create Deploy Configuration and verify its table (5 columns: no Branch)
        // Requires: repositoryPage.createDeployConfiguration(), deploy config tree navigation.
        // When unblocked: verify 5-column table for deploy config, and tabs contain "Projects to Deploy".

        // ===== Step 9: Put project into "In Editing" state via Editor =====
        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(PROJECT_1);
        editorPage.openEditProjectDialog(PROJECT_1).setDescription("test edit").clickUpdateButton();

        // ===== Step 10: Verify "In Editing" status in Repository =====
        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_1);

        propertiesTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        assertThat(propertiesTab.getStatus())
                .as("Project status should be 'In Editing' after edit")
                .isEqualTo("In Editing");

        // ===== Step 11: Save project → back to "No Changes" =====
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickSaveBtn();
        repositoryPage.getSaveChangesComponent().getSaveBtn().click();
        repositoryPage.waitUntilSpinnerLoaded();
        repositoryPage.refresh();

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_1);

        propertiesTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        assertThat(propertiesTab.getStatus())
                .as("Project status should be 'No Changes' after save")
                .isEqualTo("No Changes");

        // ===== Step 12: Close project → status "Closed" =====
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickCloseBtn();
        repositoryPage.refresh();

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_1);

        propertiesTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        assertThat(propertiesTab.getStatus())
                .as("Project status should be 'Closed' after closing")
                .isEqualTo("Closed");
        assertThat(repositoryPage.getRepositoryContentButtonsPanelComponent().isOpenBtnVisible())
                .as("Open button should appear for closed project")
                .isTrue();

        // ===== Step 13: Delete (archive) project → not visible when "hide deleted" active =====
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickDeleteBtn();
        repositoryPage.getConfirmDeleteDialogComponent().clickDelete();
        repositoryPage.waitUntilSpinnerLoaded();
        repositoryPage.refresh();

        assertThat(repositoryPage.getAllVisibleProjectsInTable())
                .as("Archived project should not be visible when 'Hide deleted' filter is active")
                .doesNotContain(PROJECT_1);

        // ===== Step 14: Advanced filter — show deleted → project visible with Archived status =====
        repositoryPage.setShowDeletedProjects(true);

        assertThat(repositoryPage.getAllVisibleProjectsInTable())
                .as("Archived project should be visible when 'Show deleted' filter is active")
                .anyMatch(name -> name.contains(PROJECT_1));

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_1);

        propertiesTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        assertThat(propertiesTab.getStatus())
                .as("Deleted project status should be 'Archived'")
                .isEqualTo("Archived");
        assertThat(repositoryPage.getRepositoryContentButtonsPanelComponent().isUndeleteBtnVisible())
                .as("Undelete button should be visible for archived project")
                .isTrue();

        // ===== Step 15: Undelete → status back to "Closed" =====
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickUndeleteBtn();
        repositoryPage.getConfirmUndeleteDialogComponent().clickUndelete();
        repositoryPage.waitUntilSpinnerLoaded();
        repositoryPage.refresh();

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_1);

        propertiesTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        assertThat(propertiesTab.getStatus())
                .as("Project status should be 'Closed' after undelete")
                .isEqualTo("Closed");

        // ===== Step 16: Open project → status "No Changes" =====
        repositoryPage.getRepositoryContentButtonsPanelComponent().openProject();
        repositoryPage.waitUntilSpinnerLoaded();
        repositoryPage.refresh();

        // Restore filter to hide deleted
        repositoryPage.setShowDeletedProjects(false);

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_1);

        propertiesTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        assertThat(propertiesTab.getStatus())
                .as("Project status should be 'No Changes' after opening")
                .isEqualTo("No Changes");

        // ===== Step 17: Multi-user locking — put project "In Editing" then login as second user =====
        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(PROJECT_1);
        editorPage.openEditProjectDialog(PROJECT_1).setDescription("lock test").clickUpdateButton();

        // Login as second user — should see project as locked
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(secondUser);

        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_1);

        String statusForSecondUser = repositoryPage.getRepositoryContentTabSwitcherComponent()
                .selectPropertiesTab()
                .getStatus();
        assertThat(statusForSecondUser)
                .as("Project should appear locked to second user while admin has it In Editing")
                .containsIgnoringCase("locked");

        // Login back as admin — project still "In Editing" for owner
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(UserService.getUser(User.ADMIN));

        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_1);

        propertiesTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        assertThat(propertiesTab.getStatus())
                .as("Admin should still see 'In Editing' for their own project")
                .isEqualTo("In Editing");

        // ===== Step 18: Filter by name =====
        repositoryPage.refresh();
        repositoryPage.getLeftRepositoryTreeComponent().expandFolderInTree("Projects");

        repositoryPage.filterByName(PROJECT_2);
        assertThat(repositoryPage.countVisibleProjectsInTable())
                .as("Filter by name should show exactly 1 project")
                .isEqualTo(1);
        assertThat(repositoryPage.getAllVisibleProjectsInTable().getFirst())
                .as("Only PROJECT_2 should be visible after filter")
                .contains(PROJECT_2);

        repositoryPage.clearNameFilter();
        assertThat(repositoryPage.countVisibleProjectsInTable())
                .as("After clearing filter all projects should be visible again")
                .isGreaterThanOrEqualTo(2);

        // [DEPLOY BLOCKED] Step 19: Production repository — filter and status verification
        // Requires: deploy integration to have projects present in Production repository.
        // When unblocked:
        //   - Click Production tab
        //   - Expand Production tree, expand deployed project
        //   - Verify tabs: Properties, Elements
        //   - Verify file present in Elements tab (rules.xml etc.)
        //   - Filter by name in Production
        //   - Verify deployment count matches
        //   - Clear production filter
    }
}
