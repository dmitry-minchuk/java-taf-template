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
import domain.ui.webstudio.components.repositorytabcomponents.ExportProjectModalComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.ProjectDetailPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.DownloadUtil;
import helpers.utils.ZipUtil;
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
    private static final String VIEWING = "Viewing";

    // Every entry other than "Viewing" is a committed revision, named "<author>: <date>".
    private static List<String> committedRevisions(List<String> revisions) {
        return revisions.stream().filter(entry -> !VIEWING.equals(entry)).toList();
    }

    private static String committedRevisionOf(List<String> revisions) {
        List<String> committed = committedRevisions(revisions);
        assertThat(committed).as("Committed revisions offered for export").hasSize(1);
        return committed.getFirst();
    }

    // The revision that was not there before — the one the latest commit added.
    private static String newRevisionIn(List<String> revisions, List<String> known) {
        return committedRevisions(revisions).stream()
                .filter(entry -> !known.contains(entry))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No new revision in " + revisions + ", known: " + known));
    }

    private void exportAndVerifyDownload(ExportProjectDialogComponent exportDialog, String contextMessage, String... expectedFiles) {
        verifyDownload(exportDialog.clickExportAndDownload(), contextMessage, expectedFiles);
    }

    private void exportAndVerifyDownload(ExportProjectModalComponent exportDialog, String contextMessage, String... expectedFiles) {
        verifyDownload(exportDialog.clickExportAndDownload(), contextMessage, expectedFiles);
    }

    private void verifyDownload(File exportedFile, String contextMessage, String... expectedFiles) {
        assertThat(exportedFile.exists())
                .as("Downloaded file should exist - " + contextMessage)
                .isTrue();
        assertThat(exportedFile.length())
                .as("Downloaded file should not be empty - " + contextMessage)
                .isGreaterThan(0);

        if (expectedFiles.length > 0) {
            List<String> actualFiles = ZipUtil.listFiles(exportedFile);
            assertThat(actualFiles)
                    .as("Archive content - " + contextMessage)
                    .containsExactlyInAnyOrder(expectedFiles);
        }

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

        // Step 3: Who committed the project, as the projects screen reports it. The editor's export window
        // spells the same commit differently (US date and a different time zone), so the revision entries
        // themselves are read from that window and only the author is cross-checked here.
        String author = repositoryPage.openProjectDetail(PROJECT_NAME).getModifiedBy();
        repositoryPage.openProjectsList();

        // Step 4-5: Navigate to Editor and verify Export button, test dialog
        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(PROJECT_NAME);

        editorPage.getEditorToolbarPanelComponent().clickExport();
        ExportProjectDialogComponent exportDialog = editorPage.getExportProjectDialogComponent();
        exportDialog.waitForDialogToAppear();

        List<String> revisions = exportDialog.getAllRevisions();
        assertThat(revisions)
                .as("Should show Viewing and the single initial revision")
                .hasSize(2)
                .contains(VIEWING);
        String revision = committedRevisionOf(revisions);
        assertThat(revision)
                .as("The initial revision should be credited to the project's author")
                .startsWith(author + ":");

        exportDialog.clickCancel();
        assertThat(exportDialog.isDialogVisible())
                .as("Dialog should close after cancel")
                .isFalse();

        // Step 6-7: Export project (first export)
        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();
        exportAndVerifyDownload(exportDialog, "first export",
                "file1.xls", "file2.xlsx", "pic.png", "dir1/file3.xlsx");

        // Step 8-9: Export specific revision
        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();
        exportDialog.selectRevision(revision);
        exportAndVerifyDownload(exportDialog, "specific revision export",
                "file1.xls", "file2.xlsx", "pic.png", "dir1/file3.xlsx");

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

        exportAndVerifyDownload(exportDialog, "In Editing export",
                "file1.xls", "file2.xlsx", "file4.xls", "pic.png", "rules.xml", "dir1/file3.xlsx");

        // Step 12: Export old revision while in "In Editing"
        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();
        exportDialog.selectRevision(revision);
        exportAndVerifyDownload(exportDialog, "old revision while In Editing",
                "file1.xls", "file2.xlsx", "pic.png", "dir1/file3.xlsx");

        // Step 13: Save project to create second revision
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();

        // Step 14: Verify all revisions available
        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(PROJECT_NAME);

        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();

        assertThat(exportDialog.getSelectedRevision())
                .as("Default should be Viewing")
                .isEqualTo(VIEWING);

        revisions = exportDialog.getAllRevisions();
        assertThat(revisions)
                .as("Should show Viewing, first and second revision")
                .hasSize(3)
                .contains(VIEWING, revision);
        String secondRevision = newRevisionIn(revisions, List.of(revision));

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

        // Step 17: Open the project on its first revision and export from there
        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.openProjectsList().openProjectDetail(PROJECT_NAME).openRevisionByPosition(2);
        repositoryPage.openProjectsList();

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
                .saveUser();

        UserData secondUser = new UserData(SECOND_USER_USERNAME, "Test123!");

        // Step 19: Logout admin, login as second user
        editorPage = new EditorPage();
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(secondUser);

        // Step 20: Verify project is closed for second user
        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        ProjectDetailPage projectDetail = repositoryPage.openProjectDetail(PROJECT_NAME);
        assertThat(projectDetail.getStatus().toLowerCase())
                .as("Project should be closed for second user")
                .contains("closed");

        // Step 21: Export the closed project from the projects screen
        ExportProjectModalComponent repoExportDialog = projectDetail.openExportDialog();
        // The projects screen and the editor spell a commit's date differently, so the entries are compared
        // by count and by author rather than against the editor's strings.
        revisions = repoExportDialog.getAllRevisions();
        assertThat(revisions)
                .as("Should show both revisions but not 'Viewing' for closed project")
                .hasSize(2)
                .doesNotContain(VIEWING)
                .allMatch(entry -> entry.contains(":"));

        repoExportDialog.clickCancel();

        // Step 22: Branch the project (6.4.0 branches from the Copy dialog) and stay on the new branch
        projectDetail = repositoryPage.openProjectsList().openProjectDetail(PROJECT_NAME);
        projectDetail.createBranch(BRANCH_NAME, true);

        // Step 23: Verify project status in branch. Branching does not open the project in this user's
        // workspace — it stayed closed for them — so open it first, then it reads as unchanged.
        repositoryPage.openProjectsList();
        if (repositoryPage.isProjectActionAvailable(PROJECT_NAME, "Open")) {
            repositoryPage.openProject(PROJECT_NAME);
        }
        projectDetail = repositoryPage.openProjectsList().openProjectDetail(PROJECT_NAME);
        assertThat(projectDetail.getStatus().toLowerCase())
                .as("Project should have 'no changes' status in branch")
                .contains("no changes");

        // Step 24: Export from branch - should show Viewing
        repoExportDialog = projectDetail.openExportDialog();
        revisions = repoExportDialog.getAllRevisions();
        assertThat(revisions)
                .as("Should show Viewing and both revisions in branch")
                .hasSize(3)
                .contains(VIEWING);

        exportAndVerifyDownload(repoExportDialog, "branch export",
                "file1.xls", "file2.xlsx", "file4.xls", "pic.png", "rules.xml", "dir1/file3.xlsx");

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

        exportAndVerifyDownload(exportDialog, "In Editing with file5",
                "file1.xls", "file2.xlsx", "file4.xls", "file5.xls", "pic.png", "rules.xml", "dir1/file3.xlsx");

        // Step 26: Save and verify third revision
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();

        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(PROJECT_NAME);

        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();
        revisions = exportDialog.getAllRevisions();
        assertThat(revisions)
                .as("Should show all three revisions")
                .hasSize(4)
                .contains(VIEWING, revision, secondRevision);
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

        // Step 30: Verify export for template project
        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(SAMPLE_PROJECT);

        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();
        revisions = exportDialog.getAllRevisions();
        assertThat(revisions)
                .as("Template project should show Viewing and its revision")
                .hasSize(2)
                .contains(VIEWING);
        String revisionSampleProject = committedRevisionOf(revisions);
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
        repositoryPage.openProject(SAMPLE_PROJECT);

        // Step 34: Verify export for opened project
        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(SAMPLE_PROJECT);

        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();
        revisions = exportDialog.getAllRevisions();
        assertThat(revisions)
                .as("Opened project should show Viewing and revision")
                .containsExactlyInAnyOrder(VIEWING, revisionSampleProject);
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

        exportAndVerifyDownload(exportDialog, "edited Sample Project export",
                "Main.xlsx", "rules.xml");

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
                .containsExactlyInAnyOrder(VIEWING, revisionSampleProject);
    }
}
