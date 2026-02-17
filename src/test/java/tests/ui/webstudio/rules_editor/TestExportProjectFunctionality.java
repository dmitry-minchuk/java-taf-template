package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.CopyModuleDialogComponent;
import domain.ui.webstudio.components.editortabcomponents.EditProjectDialogComponent;
import domain.ui.webstudio.components.editortabcomponents.ExportProjectDialogComponent;
import domain.ui.webstudio.components.repositorytabcomponents.CopyProjectDialogComponent;
import domain.ui.webstudio.components.repositorytabcomponents.RepositoryContentRevisionsTabComponent;
import domain.ui.webstudio.components.repositorytabcomponents.RepositoryContentTabPropertiesComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.DownloadUtil;
import helpers.utils.WaitUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestExportProjectFunctionality extends BaseTest {

    private static final String PROJECT_NAME = "RulesEditor.TestExportProjectFunctionality";
    private static final String MODULE_FILE1 = "file1";
    private static final String MODULE_FILE4 = "file4";
    private static final String MODULE_FILE5 = "file5";
    private static final String SAMPLE_PROJECT = "SampleProject";
    private static final String BRANCH_NAME = "branch1";
    private static final String SECOND_USER_USERNAME = "test_analyst_user";

    private void exportAndVerifyDownload(ExportProjectDialogComponent exportDialog, String contextMessage) {
        File exportedFile = exportDialog.clickExportAndDownload();
        assertThat(exportedFile.exists())
                .as("Downloaded file should exist - " + contextMessage)
                .isTrue();
        assertThat(exportedFile.length())
                .as("Downloaded file should not be empty - " + contextMessage)
                .isGreaterThan(0);
        DownloadUtil.cleanupDownloadFile(exportedFile);
    }

    @Test
    @TestCaseId("IPBQA-25697")
    @Description("Rules Editor - Export Project: Full functionality with revision selection, branches, and multi-user scenarios")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testExportProjectFunctionality() {
        // ========== PART 1: Main user - Create project and verify basic export ==========

        // Step 1-2: Login and create project from ZIP
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        repositoryPage.createProject(
                CreateNewProjectComponent.TabName.ZIP_ARCHIVE,
                PROJECT_NAME,
                "TestExportProjectFunctionality.zip"
        );

        // Step 3: Get initial revision (ModifiedBy + ModifiedAt)
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);

        RepositoryContentTabPropertiesComponent propertiesTab = repositoryPage
                .getRepositoryContentTabSwitcherComponent()
                .selectPropertiesTab();

        String modifiedBy = propertiesTab.getProperty(RepositoryContentTabPropertiesComponent.Property.MODIFIED_BY);
        String modifiedAt = propertiesTab.getProperty(RepositoryContentTabPropertiesComponent.Property.MODIFIED_AT);
        String revision = modifiedBy + ": " + modifiedAt;

        // Step 4-5: Navigate to Editor and verify Export button, test dialog
        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(PROJECT_NAME);

        editorPage.getEditorToolbarPanelComponent().clickExport();
        ExportProjectDialogComponent exportDialog = editorPage.getExportProjectDialogComponent();
        exportDialog.waitForDialogToAppear();

        List<String> revisions = exportDialog.getAllRevisions();
        assertThat(revisions)
                .as("Should show Viewing and initial revision")
                .containsExactlyInAnyOrder("Viewing", revision);

        exportDialog.clickCancel();
        assertThat(exportDialog.isDialogVisible())
                .as("Dialog should close after cancel")
                .isFalse();

        // Step 6-7: Export project (first export)
        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();
        exportAndVerifyDownload(exportDialog, "first export");

        // Step 8-9: Export specific revision
        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();
        exportDialog.selectRevision(revision);
        exportDialog.clickExport();

        // Step 10: Copy module file1 to file4
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(PROJECT_NAME, MODULE_FILE1);

        CopyModuleDialogComponent copyModuleDialog = editorPage.openCopyModuleDialog();
        copyModuleDialog.setModuleName(MODULE_FILE4);
        copyModuleDialog.clickCopy();

        // Step 11: Export in "In Editing" status
        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();

        revisions = exportDialog.getAllRevisions();
        assertThat(revisions)
                .as("Should show 'In Editing' after module copy")
                .containsExactlyInAnyOrder("In Editing", revision);

        exportAndVerifyDownload(exportDialog, "In Editing export");

        // Step 12: Export old revision while in "In Editing"
        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();
        exportDialog.selectRevision(revision);
        exportDialog.clickExport();

        // Step 13: Save project to create second revision
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();

        // Get second revision
        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .selectItemInFolder("Projects", PROJECT_NAME);
        propertiesTab = repositoryPage.getRepositoryContentTabSwitcherComponent()
                .selectPropertiesTab();

        String modifiedBy2 = propertiesTab.getProperty(RepositoryContentTabPropertiesComponent.Property.MODIFIED_BY);
        String modifiedAt2 = propertiesTab.getProperty(RepositoryContentTabPropertiesComponent.Property.MODIFIED_AT);
        String secondRevision = modifiedBy2 + ": " + modifiedAt2;

        // Step 14: Verify all revisions available
        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(PROJECT_NAME);

        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();

        assertThat(exportDialog.getSelectedRevision())
                .as("Default should be Viewing")
                .isEqualTo("Viewing");

        revisions = exportDialog.getAllRevisions();
        assertThat(revisions)
                .as("Should show Viewing, first and second revision")
                .containsExactlyInAnyOrder("Viewing", revision, secondRevision);

        exportDialog.clickExport();

        // Step 15-16: Export second and first revision
        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();
        exportDialog.selectRevision(secondRevision);
        exportDialog.clickExport();

        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();
        exportDialog.selectRevision(revision);
        exportDialog.clickExport();

        // Step 17: Open Revisions tab and export from history
        editorPage.getEditorToolbarPanelComponent().clickMore().clickRevisions();

        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        RepositoryContentRevisionsTabComponent revisionsTab = repositoryPage
                .getRepositoryContentTabSwitcherComponent()
                .selectRevisionsTab();

        WaitUtil.waitForCondition(() -> revisionsTab.getRevisionsCount() > 0, 500, 100, "Waiting for revisions to load in Revisions tab");
        revisionsTab.openRevision(2);
        WaitUtil.sleep(1000, "Waiting for revision to open");

        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(PROJECT_NAME);

        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();
        assertThat(exportDialog.getSelectedRevision())
                .as("Should show Viewing when viewing historical revision")
                .isEqualTo("Viewing");
        exportDialog.clickExport();

        // ========== PART 2: Second user workflow ==========

        // Step 18: Create second user via Admin with access to Design repository
        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        adminPage.navigateToUsersPage()
                .clickAddUser()
                .setUsername(SECOND_USER_USERNAME)
                .setEmail(SECOND_USER_USERNAME + "@test.com")
                .setPassword("Test123!")
                .setFirstName("Test")
                .setLastName("Analyst")
                .clickAddRoleBtn()
                .setRoleRepository(0, "Design")
                .setRole(0, "Contributor")
                .inviteUser();

        UserData secondUser = new UserData(SECOND_USER_USERNAME, "Test123!");

        // Step 19: Logout admin, login as second user
        editorPage = new EditorPage();
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(secondUser);

        // Step 20: Verify project is closed for second user
        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);

        propertiesTab = repositoryPage.getRepositoryContentTabSwitcherComponent()
                .selectPropertiesTab();
        String status = propertiesTab.getProperty(RepositoryContentTabPropertiesComponent.Property.STATUS);
        assertThat(status.toLowerCase())
                .as("Project should be closed for second user")
                .contains("closed");

        // Step 21: Export closed project from Repository tab
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickExportBtn();

        exportDialog = repositoryPage.getExportProjectDialogComponent();
        exportDialog.waitForDialogToAppear();
        revisions = exportDialog.getAllRevisions();
        assertThat(revisions)
                .as("Should show both revisions but not 'Viewing' for closed project")
                .containsExactlyInAnyOrder(revision, secondRevision)
                .doesNotContain("Viewing");

        exportDialog.clickCancel();

        // Step 22: Copy project to new branch
        repositoryPage.refresh();
        repositoryPage.getLeftRepositoryTreeComponent()
                .selectItemInFolder("Projects", PROJECT_NAME);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickCopyBtn();

        CopyProjectDialogComponent copyProjectDialog = repositoryPage.getCopyProjectDialogComponent();
        copyProjectDialog.waitForDialogToAppear();
        copyProjectDialog.setNewBranchName(BRANCH_NAME);
        copyProjectDialog.clickCopyButton();
        repositoryPage.fillCommitInfo();
        repositoryPage.refresh();

        // Step 23: Verify project status in branch
        repositoryPage.getLeftRepositoryTreeComponent()
                .selectItemInFolder("Projects", PROJECT_NAME);
        propertiesTab = repositoryPage.getRepositoryContentTabSwitcherComponent()
                .selectPropertiesTab();
        status = propertiesTab.getProperty(RepositoryContentTabPropertiesComponent.Property.STATUS);
        assertThat(status.toLowerCase())
                .as("Project should have 'no changes' status in branch")
                .contains("no changes");

        // Step 24: Export from branch - should show Viewing
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickExportBtn();
        exportDialog = repositoryPage.getExportProjectDialogComponent();
        exportDialog.waitForDialogToAppear();
        revisions = exportDialog.getAllRevisions();
        assertThat(revisions)
                .as("Should show Viewing and both revisions in branch")
                .containsExactlyInAnyOrder("Viewing", revision, secondRevision);

        exportAndVerifyDownload(exportDialog, "branch export");

        // Step 25: Copy another module and export in "In Editing"
        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(PROJECT_NAME, MODULE_FILE1);

        copyModuleDialog = editorPage.openCopyModuleDialog();
        copyModuleDialog.setModuleName(MODULE_FILE5);
        copyModuleDialog.clickCopy();

        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();
        revisions = exportDialog.getAllRevisions();
        assertThat(revisions)
                .as("Should show In Editing after second module copy")
                .containsExactlyInAnyOrder("In Editing", revision, secondRevision);
        exportDialog.clickExport();

        // Step 26: Save and verify third revision
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();

        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .selectItemInFolder("Projects", PROJECT_NAME);
        propertiesTab = repositoryPage.getRepositoryContentTabSwitcherComponent()
                .selectPropertiesTab();

        String modifiedBy3 = propertiesTab.getProperty(RepositoryContentTabPropertiesComponent.Property.MODIFIED_BY);
        String modifiedAt3 = propertiesTab.getProperty(RepositoryContentTabPropertiesComponent.Property.MODIFIED_AT);
        String revisionBranch = modifiedBy3 + ": " + modifiedAt3;

        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(PROJECT_NAME);

        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();
        revisions = exportDialog.getAllRevisions();
        assertThat(revisions)
                .as("Should show all three revisions")
                .containsExactlyInAnyOrder("Viewing", revision, secondRevision, revisionBranch);
        exportDialog.clickExport();

        // ========== PART 3: Template project workflow ==========

        // Step 27: Logout second user, login as admin
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(UserService.getUser(User.ADMIN));

        // Step 28: Create project from template
        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(
                CreateNewProjectComponent.TabName.TEMPLATE,
                SAMPLE_PROJECT,
                "Empty Project"
        );

        // Step 29: Get sample project revision
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", SAMPLE_PROJECT);
        propertiesTab = repositoryPage.getRepositoryContentTabSwitcherComponent()
                .selectPropertiesTab();

        String sampleModifiedBy = propertiesTab.getProperty(RepositoryContentTabPropertiesComponent.Property.MODIFIED_BY);
        String sampleModifiedAt = propertiesTab.getProperty(RepositoryContentTabPropertiesComponent.Property.MODIFIED_AT);
        String revisionSampleProject = sampleModifiedBy + ": " + sampleModifiedAt;

        // Step 30: Verify export for template project
        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(SAMPLE_PROJECT);

        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();
        revisions = exportDialog.getAllRevisions();
        assertThat(revisions)
                .as("Template project should show Viewing and its revision")
                .containsExactlyInAnyOrder("Viewing", revisionSampleProject);
        exportDialog.clickCancel();

        // Step 31: Export template project
        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();
        exportDialog.clickExport();

        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();
        exportDialog.selectRevision(revisionSampleProject);
        exportDialog.clickExport();

        // Step 32: Logout admin, login as second user
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(secondUser);

        // Step 33: Open sample project
        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", SAMPLE_PROJECT);
        repositoryPage.getRepositoryContentButtonsPanelComponent().openProject();

        // Step 34: Verify export for opened project
        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(SAMPLE_PROJECT);

        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();
        revisions = exportDialog.getAllRevisions();
        assertThat(revisions)
                .as("Opened project should show Viewing and revision")
                .containsExactlyInAnyOrder("Viewing", revisionSampleProject);
        exportDialog.clickCancel();

        // Step 35: Edit project description and export "In Editing"
        EditProjectDialogComponent editDialog = editorPage.openEditProjectDialog(SAMPLE_PROJECT);
        editDialog.setDescription("Updated description");
        editDialog.clickUpdateButton();

        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();
        revisions = exportDialog.getAllRevisions();
        assertThat(revisions)
                .as("Should show In Editing after project edit")
                .containsExactlyInAnyOrder("In Editing", revisionSampleProject);

        exportAndVerifyDownload(exportDialog, "edited Sample Project export");

        // Step 36: Logout second user, login as admin, verify locked status
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(UserService.getUser(User.ADMIN));

        editorPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(SAMPLE_PROJECT);

        // Project should be locked by second user - admin can still export Viewing
        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();
        revisions = exportDialog.getAllRevisions();
        assertThat(revisions)
                .as("Admin should see Viewing for locked project")
                .containsExactlyInAnyOrder("Viewing", revisionSampleProject);
    }
}
