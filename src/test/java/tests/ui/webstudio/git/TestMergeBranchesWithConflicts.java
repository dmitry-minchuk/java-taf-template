package tests.ui.webstudio.git;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.ProjectDetailPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

// Migrated to the React repository UI (build 032c60a664ce): the conflict is created by editing the same
// spreadsheet cell on two branches (React has no updateFile content-replacement), merges run through the
// Branches-tab Sync dialog (openMergeDialog + Send/Receive), and conflicts are settled in the React
// "Resolve Conflicts" dialog (Use yours / Use theirs).
public class TestMergeBranchesWithConflicts extends BaseTest {

    private static final String PROJECT_NAME = "MergeConflicts";
    private static final String BRANCH_1 = "Branch1";
    private static final String BRANCH_2 = "Branch2";
    private static final String MASTER_BRANCH = "master";
    private static final String MODULE_NAME = "Module1";
    private static final String SPREADSHEET = "Spreadsheet";
    private static final String TABLE_NAME = "MySpr1";
    private static final String BRANCH_1_VALUE = "Branch1Value";
    private static final String BRANCH_2_VALUE = "Branch2Value";

    @Test
    @TestCaseId("IPBQA-32850")
    @Description("Git - Merge branches with conflicts: Export with Use Yours resolution")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testMergeBranchesWithConflictsExportUseYours() {
        EditorPage editorPage = createProjectWithConflictingBranches();
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        // On Branch2, send our updates to Branch1 -> conflict -> keep ours (Branch2).
        ProjectDetailPage projectDetail = repositoryPage.openProjectDetail(PROJECT_NAME);
        projectDetail.openMergeDialog(BRANCH_1).clickSend();
        repositoryPage.getResolveConflictsDialogComponent().waitForDialogToAppear();
        assertThat(repositoryPage.getResolveConflictsDialogComponent().isDialogVisible())
                .as("Resolve Conflicts dialog should appear on a conflicting export").isTrue();
        repositoryPage.getResolveConflictsDialogComponent().resolveConflictUseYours();
        repositoryPage.fillCommitInfo();
        repositoryPage.waitUntilSpinnerLoaded();

        // Branch1 now holds Branch2's version ("use yours" = the branch we merged from).
        assertThat(readSpreadsheetCell(editorPage, BRANCH_1))
                .as("Branch1 should hold Branch2's version after 'Use yours'").contains(BRANCH_2_VALUE);
        assertThat(readSpreadsheetCell(editorPage, BRANCH_2))
                .as("Branch2 keeps its own version").contains(BRANCH_2_VALUE);
    }

    @Test
    @TestCaseId("IPBQA-32851")
    @Description("Git - Merge branches with conflicts: Import with Use Theirs resolution")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testMergeBranchesWithConflictsImportUseTheirs() {
        EditorPage editorPage = createProjectWithConflictingBranches();
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        // On Branch2, receive Branch1's updates -> conflict -> take theirs (Branch1).
        ProjectDetailPage projectDetail = repositoryPage.openProjectDetail(PROJECT_NAME);
        projectDetail.openMergeDialog(BRANCH_1).clickReceive();
        repositoryPage.getResolveConflictsDialogComponent().waitForDialogToAppear();
        assertThat(repositoryPage.getResolveConflictsDialogComponent().isDialogVisible())
                .as("Resolve Conflicts dialog should appear on a conflicting import").isTrue();
        repositoryPage.getResolveConflictsDialogComponent().resolveConflictUseTheirs();
        repositoryPage.fillCommitInfo();
        repositoryPage.waitUntilSpinnerLoaded();

        // Branch2 now holds Branch1's version ("use theirs" = the branch we merged from).
        assertThat(readSpreadsheetCell(editorPage, BRANCH_2))
                .as("Branch2 should hold Branch1's version after 'Use theirs'").contains(BRANCH_1_VALUE);
        assertThat(readSpreadsheetCell(editorPage, BRANCH_1))
                .as("Branch1 keeps its own version").contains(BRANCH_1_VALUE);
    }

    // Creates the project, then two branches that edit the same cell differently (Branch1 -> Branch1Value,
    // Branch2 -> Branch2Value), leaving the project on Branch2. Returns the editor page.
    private EditorPage createProjectWithConflictingBranches() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, PROJECT_NAME,
                "TestMergeBranchesNoConflicts_NoConflicts.zip");

        ProjectDetailPage projectDetail = repositoryPage.openProjectDetail(PROJECT_NAME);
        projectDetail.createBranch(BRANCH_1, true);
        editSpreadsheetCell(editorPage, BRANCH_1_VALUE);

        // Branch2 must branch off master (not Branch1) so the two diverge on the same cell.
        editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorToolbarPanelComponent().switchBranch(MASTER_BRANCH);
        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        projectDetail = repositoryPage.openProjectDetail(PROJECT_NAME);
        projectDetail.createBranch(BRANCH_2, true);
        editSpreadsheetCell(editorPage, BRANCH_2_VALUE);
        return editorPage;
    }

    private void editSpreadsheetCell(EditorPage editorPage, String value) {
        EditorPage edit = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        edit.getEditorLeftProjectModuleSelectorComponent().selectModule(PROJECT_NAME, MODULE_NAME);
        edit.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree(SPREADSHEET)
                .selectItemInFolder(SPREADSHEET, TABLE_NAME);
        edit.getCenterTable().editCell(3, 1, value);
        edit.getEditorTableActionsPanelComponent().clickSaveChanges();
        edit.getEditorToolbarPanelComponent().clickSave();
        edit.getSaveChangesComponent().getSaveBtn().click();
    }

    private String readSpreadsheetCell(EditorPage editorPage, String branch) {
        // Resolving a conflict can drop the project from the editor workspace, so re-open it first.
        RepositoryPage repo = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        if (repo.isProjectActionAvailable(PROJECT_NAME, "Open")) {
            repo.openProject(PROJECT_NAME);
            repo.waitUntilSpinnerLoaded();
        }
        EditorPage edit = repo.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        // Reload so the editor reflects the post-merge workspace, then select the project so the branch
        // breadcrumb appears before switching to the target branch.
        edit.reloadPage();
        edit.getEditorLeftProjectModuleSelectorComponent().selectProject(PROJECT_NAME);
        edit.getEditorToolbarPanelComponent().switchBranch(branch);
        edit.reloadPage();
        edit.getEditorLeftProjectModuleSelectorComponent().selectModule(PROJECT_NAME, MODULE_NAME);
        edit.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree(SPREADSHEET)
                .selectItemInFolder(SPREADSHEET, TABLE_NAME);
        return edit.getCenterTable().getCellText(3, 1);
    }
}
