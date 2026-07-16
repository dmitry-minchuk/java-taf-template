package tests.ui.webstudio.git;

import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.EditorToolbarPanelComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.ProjectDetailPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

// Validates the React conflict-resolution flow end-to-end: two branches edit the same cell, merging raises
// the "Resolve Conflicts" dialog, and "Use yours" keeps the current branch's version. Foundation for
// TestMergeBranchesWithConflicts.
public class TestReactMergeConflictSmoke extends BaseTest {

    private static final String PROJECT = "ConflictSmoke";
    private static final String BRANCH = "MyBranch";
    private static final String MASTER = "master";

    @Test
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void resolveConflictUseYoursKeepsMasterVersion() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, PROJECT,
                "TestMergeBranchesNoConflicts_NoConflicts.zip");

        ProjectDetailPage detail = repositoryPage.openProjectDetail(PROJECT);
        detail.createBranch(BRANCH, true);

        // MyBranch: edit MySpr1 cell, commit.
        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        EditorToolbarPanelComponent toolbar = editorPage.getEditorToolbarPanelComponent();
        editMySpr1(editorPage, "TheirValue");
        toolbar.clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();

        // master: edit the SAME cell differently, commit.
        toolbar.switchBranch(MASTER);
        editMySpr1(editorPage, "YourValue");
        toolbar.clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();

        // Merge MyBranch into master -> conflict on that cell -> Resolve Conflicts dialog.
        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        detail = repositoryPage.openProjectDetail(PROJECT);
        detail.openMergeDialog(BRANCH).clickReceive();
        repositoryPage.getResolveConflictsDialogComponent().waitForDialogToAppear();
        assertThat(repositoryPage.getResolveConflictsDialogComponent().isDialogVisible())
                .as("Resolve Conflicts dialog should appear on a conflicting merge").isTrue();
        repositoryPage.getResolveConflictsDialogComponent().resolveConflictUseYours();
        repositoryPage.fillCommitInfo();
        repositoryPage.waitUntilSpinnerLoaded();

        // master must keep its own (yours) value.
        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.reloadPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(PROJECT, "Module1");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", "MySpr1");
        assertThat(editorPage.getCenterTable().getCellText(3, 1))
                .as("master should keep its own version after 'Use yours'").contains("YourValue");
    }

    private void editMySpr1(EditorPage editorPage, String value) {
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(PROJECT, "Module1");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", "MySpr1");
        editorPage.getCenterTable().editCell(3, 1, value);
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();
    }
}
