package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
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
    private static final String REVISION_SEPARATOR = " · ";
    private static final String REVISION_LABEL = "^\\S+ · .+: .+\\d{1,2}:\\d{2}:\\d{2}.*$";

    private static List<String> committedRevisions(List<String> revisions) {
        return revisions.stream().filter(entry -> !VIEWING.equals(entry)).toList();
    }

    private static String committedRevisionOf(List<String> revisions) {
        List<String> committed = committedRevisions(revisions);
        assertThat(committed).as("Committed revisions offered for export").hasSize(1);
        return committed.getFirst();
    }

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

        LoginService loginService = new LoginService(DriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        repositoryPage.createProject(
                CreateNewProjectComponent.TabName.ZIP_ARCHIVE,
                PROJECT_NAME,
                "TestExportProjectFunctionality.zip"
        );

        ProjectDetailPage createdProject = repositoryPage.openProjectDetail(PROJECT_NAME);
        String author = createdProject.getModifiedBy();
        String revisionId = createdProject.getOverviewRevision();
        assertThat(revisionId)
                .as("The projects screen must show the short revision id of the initial commit")
                .isNotBlank();
        repositoryPage.openProjectsList();

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
                .as("A revision reads as '<revision> · <author>: <date with seconds>' (EPBDS-16458)")
                .matches(REVISION_LABEL)
                .startsWith(revisionId + REVISION_SEPARATOR + author + ":");

        exportDialog.clickCancel();
        assertThat(exportDialog.isDialogVisible())
                .as("Dialog should close after cancel")
                .isFalse();

        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();
        exportAndVerifyDownload(exportDialog, "first export",
                "file1.xls", "file2.xlsx", "pic.png", "dir1/file3.xlsx");

        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();
        exportDialog.selectRevision(revision);
        exportAndVerifyDownload(exportDialog, "specific revision export",
                "file1.xls", "file2.xlsx", "pic.png", "dir1/file3.xlsx");

        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(PROJECT_NAME, MODULE_FILE1);

        CopyModuleDialogComponent copyModuleDialog = editorPage.openCopyModuleDialog();
        copyModuleDialog.setModuleName(MODULE_FILE4);
        copyModuleDialog.clickCopy();

        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();

        revisions = exportDialog.getAllRevisions();
        assertThat(revisions)
                .as("Should show 'In Editing' after module copy")
                .containsExactlyInAnyOrder("In Editing", revision);

        exportAndVerifyDownload(exportDialog, "In Editing export",
                "file1.xls", "file2.xlsx", "file4.xls", "pic.png", "rules.xml", "dir1/file3.xlsx");

        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();
        exportDialog.selectRevision(revision);
        exportAndVerifyDownload(exportDialog, "old revision while In Editing",
                "file1.xls", "file2.xlsx", "pic.png", "dir1/file3.xlsx");

        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().clickSave();

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

        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();
        exportDialog.selectRevision(secondRevision);
        exportDialog.clickExport();

        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();
        exportDialog.selectRevision(revision);
        exportDialog.clickExport();

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

        editorPage = new EditorPage();
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(secondUser);

        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        ProjectDetailPage projectDetail = repositoryPage.openProjectDetail(PROJECT_NAME);
        assertThat(projectDetail.getStatus().toLowerCase())
                .as("Project should be closed for second user")
                .contains("closed");

        ExportProjectModalComponent repoExportDialog = projectDetail.openExportDialog();
        revisions = repoExportDialog.getAllRevisions();
        assertThat(revisions)
                .as("Should show both revisions but not 'Viewing' for closed project")
                .hasSize(2)
                .doesNotContain(VIEWING)
                .allMatch(entry -> entry.matches(REVISION_LABEL));

        repoExportDialog.clickCancel();

        projectDetail = repositoryPage.openProjectsList().openProjectDetail(PROJECT_NAME);
        projectDetail.createBranch(BRANCH_NAME, true);

        repositoryPage.openProjectsList();
        if (repositoryPage.isProjectActionAvailable(PROJECT_NAME, "Open")) {
            repositoryPage.openProject(PROJECT_NAME);
        }
        projectDetail = repositoryPage.openProjectsList().openProjectDetail(PROJECT_NAME);
        assertThat(projectDetail.getStatus().toLowerCase())
                .as("Project should have 'no changes' status in branch")
                .contains("no changes");

        repoExportDialog = projectDetail.openExportDialog();
        revisions = repoExportDialog.getAllRevisions();
        assertThat(revisions)
                .as("Should show Viewing and both revisions in branch")
                .hasSize(3)
                .contains(VIEWING);

        exportAndVerifyDownload(repoExportDialog, "branch export",
                "file1.xls", "file2.xlsx", "file4.xls", "pic.png", "rules.xml", "dir1/file3.xlsx");

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

        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().clickSave();

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


        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(UserService.getUser(User.ADMIN));

        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(
                CreateNewProjectComponent.TabName.TEMPLATE,
                SAMPLE_PROJECT,
                "Empty Project"
        );

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

        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();
        exportDialog.clickExport();

        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();
        exportDialog.selectRevision(revisionSampleProject);
        exportDialog.clickExport();

        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(secondUser);

        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.openProject(SAMPLE_PROJECT);

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(SAMPLE_PROJECT);

        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();
        revisions = exportDialog.getAllRevisions();
        assertThat(revisions)
                .as("Opened project should show Viewing and revision")
                .containsExactlyInAnyOrder(VIEWING, revisionSampleProject);
        exportDialog.clickCancel();

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

        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(UserService.getUser(User.ADMIN));

        editorPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(SAMPLE_PROJECT);

        editorPage.getEditorToolbarPanelComponent().clickExport();
        exportDialog.waitForDialogToAppear();
        revisions = exportDialog.getAllRevisions();
        assertThat(revisions)
                .as("Admin should see Viewing for locked project")
                .containsExactlyInAnyOrder(VIEWING, revisionSampleProject);
    }
}
