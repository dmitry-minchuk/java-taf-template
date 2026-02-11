package tests.ui.webstudio.git;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.SyncChangesDialogComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.components.repositorytabcomponents.*;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.TestDataUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestMergeBranchesWithConflicts extends BaseTest {

    private static final String PROJECT_NAME = "TestMergeBranchesWithConflicts";
    private static final String BRANCH_1 = "Branch1";
    private static final String BRANCH_2 = "Branch2";
    private static final String MASTER_BRANCH = "master";
    private static final String MODULE_NAME = "TestMergeBranchesWithConflicts_Initial";
    private static final String TABLE_NAME = "mySpr";
    private static final String SPREADSHEET_TYPE = "Spreadsheet";

    @Test
    @TestCaseId("IPBQA-32850")
    @Description("Git - Merge branches with conflicts: Export with Use Yours resolution")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testMergeBranchesWithConflictsExportUseYours() {
        // Setup: Login and create project with conflicting branches
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        createProjectWithConflictingBranches(repositoryPage, editorPage);

        // Navigate to Branch2 and initiate sync with Branch1
        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);

        RepositoryContentTabPropertiesComponent propertiesTab = repositoryPage
                .getRepositoryContentTabSwitcherComponent()
                .selectPropertiesTab();
        propertiesTab.selectBranch(BRANCH_2);

        repositoryPage.getRepositoryContentButtonsPanelComponent().clickSync();
        SyncChangesDialogComponent syncDialog = repositoryPage.getSyncChangesDialogComponent();
        syncDialog.waitForDialogToAppear();
        syncDialog.selectBranch(BRANCH_1);

        // Conflict expected: attempt to export changes from Branch2 to Branch1
        syncDialog.clickExportYourChanges();

        // Resolve conflict: Use Yours (Branch2 version)
        ResolveConflictsDialogComponent conflictDialog = repositoryPage.getResolveConflictsDialogComponent();
        conflictDialog.waitForDialogToAppear();
        assertThat(conflictDialog.isDialogVisible())
                .as("Resolve Conflicts dialog should appear after export with conflicts")
                .isTrue();
        conflictDialog.resolveConflictUseYours();

        // Verify: Branch1 now contains Branch2's version
        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(PROJECT_NAME);
        editorPage.getEditorToolbarPanelComponent().switchBranch(BRANCH_1);

        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(PROJECT_NAME, MODULE_NAME);
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree(SPREADSHEET_TYPE)
                .selectItemInFolder(SPREADSHEET_TYPE, TABLE_NAME);

        String cellContentBranch1 = editorPage.getCenterTable().getCellText(3, 1);
        assertThat(cellContentBranch1)
                .as("Branch1 should contain Branch2's version after 'Use Yours' resolution")
                .contains("InitialSteps_Branch2");

        // Verify: Branch2 still contains its original version
        editorPage.getEditorToolbarPanelComponent().switchBranch(BRANCH_2);
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(PROJECT_NAME, MODULE_NAME);
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree(SPREADSHEET_TYPE)
                .selectItemInFolder(SPREADSHEET_TYPE, TABLE_NAME);

        String cellContentBranch2 = editorPage.getCenterTable().getCellText(3, 1);
        assertThat(cellContentBranch2)
                .as("Branch2 should still contain its original version")
                .contains("InitialSteps_Branch2");
    }

    @Test
    @TestCaseId("IPBQA-32851")
    @Description("Git - Merge branches with conflicts: Import with Use Theirs resolution")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testMergeBranchesWithConflictsImportUseTheirs() {
        // Setup: Login and create project with conflicting branches
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        createProjectWithConflictingBranches(repositoryPage, editorPage);

        // Navigate to Branch2 and initiate sync with Branch1
        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);

        RepositoryContentTabPropertiesComponent propertiesTab = repositoryPage
                .getRepositoryContentTabSwitcherComponent()
                .selectPropertiesTab();
        propertiesTab.selectBranch(BRANCH_2);

        repositoryPage.getRepositoryContentButtonsPanelComponent().clickSync();
        SyncChangesDialogComponent syncDialog = repositoryPage.getSyncChangesDialogComponent();
        syncDialog.waitForDialogToAppear();
        syncDialog.selectBranch(BRANCH_1);

        // Conflict expected: attempt to import changes from Branch1 to Branch2
        syncDialog.clickImportTheirChanges();

        // Resolve conflict: Use Theirs (Branch1 version)
        ResolveConflictsDialogComponent conflictDialog = repositoryPage.getResolveConflictsDialogComponent();
        conflictDialog.waitForDialogToAppear();
        assertThat(conflictDialog.isDialogVisible())
                .as("Resolve Conflicts dialog should appear after import with conflicts")
                .isTrue();
        conflictDialog.resolveConflictUseTheirs();

        // Verify: Branch2 now contains Branch1's version
        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(PROJECT_NAME);
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(PROJECT_NAME, MODULE_NAME);
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree(SPREADSHEET_TYPE)
                .selectItemInFolder(SPREADSHEET_TYPE, TABLE_NAME);

        String cellContentBranch2 = editorPage.getCenterTable().getCellText(3, 1);
        assertThat(cellContentBranch2)
                .as("Branch2 should contain Branch1's version after 'Use Theirs' resolution")
                .contains("InitialSteps_Banch1");

        // Verify: Branch1 retains its original version
        editorPage.getEditorToolbarPanelComponent().switchBranch(BRANCH_1);
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(PROJECT_NAME, MODULE_NAME);
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree(SPREADSHEET_TYPE)
                .selectItemInFolder(SPREADSHEET_TYPE, TABLE_NAME);

        String cellContentBranch1 = editorPage.getCenterTable().getCellText(3, 1);
        assertThat(cellContentBranch1)
                .as("Branch1 should retain its original version")
                .contains("InitialSteps_Banch1");
    }

    private void createProjectWithConflictingBranches(RepositoryPage repositoryPage, EditorPage editorPage) {
        // Step 1: Create project from initial Excel file (legacy: RepositoryTab.createProjectFromExcelFile)
        repositoryPage.createProject(
                CreateNewProjectComponent.TabName.EXCEL_FILES,
                PROJECT_NAME,
                "TestMergeBranchesWithConflicts_Initial.xlsx"
        );
        repositoryPage.refresh();

        // Step 2: Copy project to Branch1 (legacy: copyGitProjectViaIconCustomBranchName)
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickCopyBtn();

        CopyProjectDialogComponent copyDialog = repositoryPage.getCopyProjectDialogComponent();
        copyDialog.waitForDialogToAppear();
        copyDialog.setNewBranchName(BRANCH_1);
        copyDialog.clickCopyButton();
        repositoryPage.refresh();

        // Step 3: Update file in Branch1 (legacy: selectItemInTree + expandItemInProjectsTree + updateFile)
        // We stay in Branch1 after copy, no need to switch
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree(PROJECT_NAME)
                .selectItemInFolder(PROJECT_NAME, "TestMergeBranchesWithConflicts_Initial.xlsx");

        repositoryPage.getRepositoryContentButtonsPanelComponent().clickUpdateFileBtn();
        UpdateFileDialogComponent updateDialog = repositoryPage.getUpdateFileDialogComponent();
        updateDialog.waitForDialogToAppear();
        updateDialog.updateFile(TestDataUtil.getFilePathFromResources("TestMergeBranchesWithConflicts_Branch1.xlsx"));
        repositoryPage.getFileChangedWarningComponent().clickOkIfPresent();
        updateDialog.clickUpdateButton();

        // Save changes in Branch1 (legacy: selectProjectInTree + saveChanges)
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickSaveBtn();
        repositoryPage.getSaveChangesComponent().getSaveBtn().click();

        // Step 4: Switch to master (legacy: selectProjectInTree + branchSelect.setValue("master"))
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);
        RepositoryContentTabPropertiesComponent propertiesTab = repositoryPage
                .getRepositoryContentTabSwitcherComponent()
                .selectPropertiesTab();
        propertiesTab.selectBranch(MASTER_BRANCH);
        repositoryPage.refresh();

        // Step 5: Copy project to Branch2 from master (legacy: copyGitProjectViaIconCustomBranchName)
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickCopyBtn();

        copyDialog = repositoryPage.getCopyProjectDialogComponent();
        copyDialog.waitForDialogToAppear();
        copyDialog.setNewBranchName(BRANCH_2);
        copyDialog.clickCopyButton();
        repositoryPage.refresh();

        // Step 6: Update file in Branch2 (legacy: selectItemInTree + expandItemInProjectsTree + updateFile)
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree(PROJECT_NAME)
                .selectItemInFolder(PROJECT_NAME, "TestMergeBranchesWithConflicts_Initial.xlsx");

        repositoryPage.getRepositoryContentButtonsPanelComponent().clickUpdateFileBtn();
        updateDialog = repositoryPage.getUpdateFileDialogComponent();
        updateDialog.waitForDialogToAppear();
        updateDialog.updateFile(TestDataUtil.getFilePathFromResources("TestMergeBranchesWithConflicts_Branch2.xlsx"));
        repositoryPage.getFileChangedWarningComponent().clickOkIfPresent();
        updateDialog.clickUpdateButton();

        // Save changes in Branch2 (legacy: selectProjectInTree + saveChanges)
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickSaveBtn();
        repositoryPage.getSaveChangesComponent().getSaveBtn().click();
    }
}
