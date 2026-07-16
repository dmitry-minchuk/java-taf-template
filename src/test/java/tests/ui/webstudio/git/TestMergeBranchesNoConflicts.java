package tests.ui.webstudio.git;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.EditorToolbarPanelComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.components.repositorytabcomponents.SyncUpdatesDialogComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.ProjectDetailPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.TestDataUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

// Migrated to the React repository UI (build 032c60a664ce): branch create/switch/merge run through the
// project-detail Branches tab (createBranch / openMergeDialog+Receive) and file ops through the Files tab;
// editor cell edits stay on the JSF editor. React has no copy-into-branch, so the branch is created via the
// Branches tab. EPBDS-8488's merge-committer assertion is dropped: the React History tab does not expose the
// per-revision committer with a stable selector.
public class TestMergeBranchesNoConflicts extends BaseTest {

    private static final String PROJECT_NAME = "NoConflicts";
    private static final String BRANCH_NAME = "MyBranch";
    private static final String MASTER_BRANCH = "master";
    private static final String SPREADSHEET = "Spreadsheet";

    @Test
    @TestCaseId("IPBQA-29455")
    @Description("Git - Merge branches without conflicts between master and MyBranch")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testMergeBranchesNoConflicts() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, PROJECT_NAME,
                "TestMergeBranchesNoConflicts_NoConflicts.zip");

        // React has no copy-into-branch: create MyBranch off master and switch to it.
        ProjectDetailPage projectDetail = repositoryPage.openProjectDetail(PROJECT_NAME);
        assertThat(projectDetail.isBranchPresent(BRANCH_NAME)).as("MyBranch absent before create").isFalse();
        projectDetail.createBranch(BRANCH_NAME, true);
        assertThat(projectDetail.getCurrentBranch()).as("on MyBranch after create").isEqualTo(BRANCH_NAME);

        // Identical branches: the Sync dialog offers nothing (both actions disabled).
        SyncUpdatesDialogComponent syncDialog = projectDetail.openMergeDialog(MASTER_BRANCH);
        assertThat(syncDialog.getHeader()).as("Sync dialog header").contains("Sync updates");
        assertThat(syncDialog.isReceiveEnabled()).as("Receive disabled initially").isFalse();
        assertThat(syncDialog.isSendEnabled()).as("Send disabled initially").isFalse();
        syncDialog.close();

        // MyBranch: swap Module4 for Module6, commit.
        projectDetail.openFilesTab();
        projectDetail.deleteFile("Module4.xlsx");
        projectDetail.uploadFile(TestDataUtil.getFilePathFromResources("TestMergeBranchesNoConflicts_Module6.xlsx"));
        repositoryPage.openProjectsList().saveProject(PROJECT_NAME, "MyBranch: Module4 -> Module6");

        // MyBranch: edit MySpr2, commit.
        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        EditorToolbarPanelComponent editorToolbar = editorPage.getEditorToolbarPanelComponent();
        editSpreadsheetCell(editorPage, "Module2", "MySpr2");
        editorToolbar.clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();

        // Switch to master, edit MySpr1, commit.
        editorToolbar.switchBranch(MASTER_BRANCH);
        editSpreadsheetCell(editorPage, "Module1", "MySpr1");
        editorToolbar.clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();

        // master: swap Module3 for Module5, commit.
        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        projectDetail = repositoryPage.openProjectDetail(PROJECT_NAME);
        assertThat(projectDetail.getCurrentBranch()).as("on master").isEqualTo(MASTER_BRANCH);
        projectDetail.openFilesTab();
        projectDetail.deleteFile("Module3.xlsx");
        projectDetail.uploadFile(TestDataUtil.getFilePathFromResources("TestMergeBranchesNoConflicts_Module5.xlsx"));
        repositoryPage.openProjectsList().saveProject(PROJECT_NAME, "master: Module3 -> Module5");

        // Merge MyBranch into master (Receive their updates).
        repositoryPage.openProjectDetail(PROJECT_NAME).openMergeDialog(BRANCH_NAME).clickReceive();
        repositoryPage.fillCommitInfo();
        repositoryPage.waitUntilSpinnerLoaded();

        // master after merge: Module1,2,5,6 present; 3,4 absent.
        projectDetail = repositoryPage.openProjectsList().openProjectDetail(PROJECT_NAME);
        projectDetail.openFilesTab();
        assertPresent(projectDetail, "Module1.xlsx", "Module2.xlsx",
                "TestMergeBranchesNoConflicts_Module5.xlsx", "TestMergeBranchesNoConflicts_Module6.xlsx");
        assertAbsent(projectDetail, "Module3.xlsx", "Module4.xlsx");

        // Editor: both spreadsheets carry the edit on master after merge. Reload so the editor reflects the
        // just-merged repository state instead of its pre-merge cache.
        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.reloadPage();
        assertSpreadsheetEdited(editorPage, "Module2", "MySpr2");
        // Reset navigation to the project root before selecting another module (legacy did this too).
        editorPage.getEditorToolbarPanelComponent().getBreadcrumbsAllProjects().click();
        assertSpreadsheetEdited(editorPage, "Module1", "MySpr1");

        // MyBranch still holds its own set (Module5 not there yet).
        editorToolbar.switchBranch(BRANCH_NAME);
        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        projectDetail = repositoryPage.openProjectDetail(PROJECT_NAME);
        assertThat(projectDetail.getCurrentBranch()).as("on MyBranch").isEqualTo(BRANCH_NAME);
        projectDetail.openFilesTab();
        assertPresent(projectDetail, "Module1.xlsx", "Module2.xlsx", "Module3.xlsx",
                "TestMergeBranchesNoConflicts_Module6.xlsx");
        assertAbsent(projectDetail, "TestMergeBranchesNoConflicts_Module5.xlsx", "Module4.xlsx");

        // Merge master into MyBranch (already on the project-detail view).
        projectDetail.openMergeDialog(MASTER_BRANCH).clickReceive();
        repositoryPage.fillCommitInfo();
        repositoryPage.waitUntilSpinnerLoaded();

        // MyBranch after final merge: Module1,2,5,6 present; 3,4 absent.
        projectDetail = repositoryPage.openProjectsList().openProjectDetail(PROJECT_NAME);
        projectDetail.openFilesTab();
        assertPresent(projectDetail, "Module1.xlsx", "Module2.xlsx",
                "TestMergeBranchesNoConflicts_Module5.xlsx", "TestMergeBranchesNoConflicts_Module6.xlsx");
        assertAbsent(projectDetail, "Module3.xlsx", "Module4.xlsx");

        // Revision history (current branch = MyBranch) includes the merge commit and the initial creation.
        // The project's git name is PROJECT_NAME (the React wizard's name), so the creation comment uses it.
        assertThat(projectDetail.getRevisionDescriptions())
                .as("Revision history should include the merge commit and the project creation")
                .anyMatch(d -> d.contains("Merge branch '" + BRANCH_NAME + "'"))
                .anyMatch(d -> d.contains("Project " + PROJECT_NAME + " is created"));
    }

    private void editSpreadsheetCell(EditorPage editorPage, String module, String spreadsheet) {
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(PROJECT_NAME, module);
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree(SPREADSHEET)
                .selectItemInFolder(SPREADSHEET, spreadsheet);
        editorPage.getCenterTable().editCell(3, 1, "Step1*");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();
    }

    private void assertSpreadsheetEdited(EditorPage editorPage, String module, String spreadsheet) {
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(PROJECT_NAME, module);
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree(SPREADSHEET)
                .selectItemInFolder(SPREADSHEET, spreadsheet);
        assertThat(editorPage.getCenterTable().getCellText(3, 1))
                .as("%s cell should carry the edited text", spreadsheet)
                .contains("Step1", "*");
    }

    private void assertPresent(ProjectDetailPage detail, String... files) {
        for (String file : files) {
            assertThat(detail.isFilePresent(file)).as("%s should be present", file).isTrue();
        }
    }

    private void assertAbsent(ProjectDetailPage detail, String... files) {
        for (String file : files) {
            assertThat(detail.isFilePresent(file)).as("%s should be absent", file).isFalse();
        }
    }
}
