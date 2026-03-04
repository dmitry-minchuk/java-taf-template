package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.SystemSettingsPageComponent;
import domain.ui.webstudio.components.editortabcomponents.ChangesDialogComponent;
import domain.ui.webstudio.components.editortabcomponents.CompareLocalChangesDialogComponent;
import domain.ui.webstudio.components.editortabcomponents.EditorRevisionsTabComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestLocalChangesRestoreCompare extends BaseTest {

    @Test
    @TestCaseId("IPBQA-30730")
    @Description("Steps 1-8.1: Fresh state shows no history; undo does not create history; single/double edits create Local Changes with compare dialog; .history absent in repo during local edits; restore to row 2 reverts only the second edit.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCoreHistoryMechanics() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        // Step 1: Fresh project shows no changes
        ChangesDialogComponent changesDialog = editorPage.getEditorToolbarPanelComponent()
                .clickMore()
                .clickChanges();
        assertThat(changesDialog.getNoChangesMessage())
                .as("Fresh project should show no changes message")
                .isEqualTo("No changes in history");

        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        // Step 2: Undone edit does not create history
        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(6, 4, "Good Morning1");
        editorPage.getEditorTableActionsPanelComponent().undoClickChanges();

        changesDialog = editorPage.getEditorToolbarPanelComponent()
                .clickMore()
                .clickChanges();
        assertThat(changesDialog.getNoChangesMessage())
                .as("Undone edit should not create a history entry")
                .isEqualTo("No changes in history");

        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        // Steps 3-4: Single edit creates Local Changes (1); compare shows highlighted cell
        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(6, 4, "Good Morning1");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        changesDialog = editorPage.getEditorToolbarPanelComponent()
                .clickMore()
                .clickChanges();
        assertThat(changesDialog.getChangesTitle())
                .as("Changes title should show 1 local change")
                .isEqualTo("Local Changes (1)");
        assertThat(changesDialog.getRowCount())
                .as("Should be 2 rows in history (current + previous)")
                .isEqualTo(2);
        assertThat(changesDialog.getCompareCheckboxValue(1))
                .as("Checkbox at row 1 should be auto-checked (current version)")
                .isTrue();

        changesDialog.setCompareCheckbox(1, true);
        changesDialog.setCompareCheckbox(2, true);
        CompareLocalChangesDialogComponent compareDialog = changesDialog.clickCompare();
        compareDialog.waitForDialogToAppear();
        compareDialog.openTreeNode("Rules");
        compareDialog.clickTreeNode("Rules String Hello (Integer hour)");

        assertThat(compareDialog.isCellHighlighted(6, 4, 1))
                .as("Cell (6,4) in fragment 1 should be highlighted after single edit")
                .isTrue();
        assertThat(compareDialog.isCellContainsExpectedValue(6, 4, "1", "Good Morning"))
                .as("Fragment 1 (older state) cell (6,4) should show original value 'Good Morning'")
                .isTrue();
        assertThat(compareDialog.isCellContainsExpectedValue(6, 4, "2", "Good Morning1"))
                .as("Fragment 2 (newer state) cell (6,4) should show edited value 'Good Morning1'")
                .isTrue();
        compareDialog.close();

        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        // Steps 5-6: Second edit creates Local Changes (2); compare shows second edit diff
        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(7, 4, "Good Afternoon1");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        changesDialog = editorPage.getEditorToolbarPanelComponent()
                .clickMore()
                .clickChanges();
        assertThat(changesDialog.getChangesTitle())
                .as("Changes title should show 2 local changes")
                .isEqualTo("Local Changes (2)");
        assertThat(changesDialog.getRowCount())
                .as("Should be 3 rows in history")
                .isEqualTo(3);
        assertThat(changesDialog.getCompareCheckboxValue(1))
                .as("Checkbox at row 1 should be auto-checked (current version)")
                .isTrue();

        changesDialog.setCompareCheckbox(1, true);
        changesDialog.setCompareCheckbox(2, true);
        compareDialog = changesDialog.clickCompare();
        compareDialog.waitForDialogToAppear();
        compareDialog.openTreeNode("Rules");
        compareDialog.clickTreeNode("Rules String Hello (Integer hour)");

        assertThat(compareDialog.isCellHighlighted(7, 4, 1))
                .as("Cell (7,4) in fragment 1 should be highlighted after second edit")
                .isTrue();
        assertThat(compareDialog.isCellContainsExpectedValue(7, 4, "1", "Good Afternoon"))
                .as("Fragment 1 (older state) cell (7,4) should show original value 'Good Afternoon'")
                .isTrue();
        assertThat(compareDialog.isCellContainsExpectedValue(7, 4, "2", "Good Afternoon1"))
                .as("Fragment 2 (newer state) cell (7,4) should show edited value 'Good Afternoon1'")
                .isTrue();
        compareDialog.close();

        // Step 7: .history folder is absent in repository while local changes exist
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree(projectName);

        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isFolderExistsInTree(".history"))
                .as(".history folder should not be visible in repository while local changes exist (not committed)")
                .isFalse();

        // Steps 8-8.1: Restore to row 2 reverts only the second edit
        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        editorPage.getEditorToolbarPanelComponent().clickMore().clickChanges();
        changesDialog.clickRestoreAtRow(2);
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        assertThat(editorPage.getCenterTable().getCellText(6, 4))
                .as("Cell (6,4) should still have the first edit value after restoring to row 2")
                .isEqualTo("Good Morning1");
        assertThat(editorPage.getCenterTable().getCellText(7, 4))
                .as("Cell (7,4) should be reverted to original value after restoring to row 2")
                .isEqualTo("Good Afternoon");
    }

    @Test
    @TestCaseId("IPBQA-30730")
    @Description("Steps 9-13: Three edits and restore to row 2; saving clears local history and creates 2 revisions; new edit after save creates fresh Local Changes (1).")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testExtendedRestoreAndSave() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        // Steps 9-10.1: Three edits; check LC(3)/4 rows; restore to row 2; verify 3 cells
        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(6, 4, "Good Morning1");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(7, 4, "Good Afternoon1");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(8, 4, "Good Evening3");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        ChangesDialogComponent changesDialog = editorPage.getEditorToolbarPanelComponent()
                .clickMore()
                .clickChanges();
        assertThat(changesDialog.getChangesTitle())
                .as("Changes title should show 3 local changes")
                .isEqualTo("Local Changes (3)");
        assertThat(changesDialog.getRowCount())
                .as("Should be 4 rows in history")
                .isEqualTo(4);

        changesDialog.clickRestoreAtRow(2);
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        assertThat(editorPage.getCenterTable().getCellText(6, 4))
                .as("Cell (6,4) should retain first edit value after restoring to row 2")
                .isEqualTo("Good Morning1");
        assertThat(editorPage.getCenterTable().getCellText(7, 4))
                .as("Cell (7,4) should retain second edit value after restoring to row 2")
                .isEqualTo("Good Afternoon1");
        assertThat(editorPage.getCenterTable().getCellText(8, 4))
                .as("Cell (8,4) should be reverted to original after restoring to row 2")
                .isEqualTo("Good Evening");

        // Step 11: Save project clears local history, creates 2 revisions, no .history in repo
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        changesDialog = editorPage.getEditorToolbarPanelComponent()
                .clickMore()
                .clickChanges();
        assertThat(changesDialog.getNoChangesMessage())
                .as("Local history should be cleared after project save")
                .isEqualTo("No changes in history");

        editorPage.getEditorToolbarPanelComponent().clickMore().clickRevisions();
        EditorRevisionsTabComponent revisionsTab = new EditorRevisionsTabComponent();
        revisionsTab.waitForTableToLoad();
        assertThat(revisionsTab.getRowCount())
                .as("Should have 2 revisions after save (creation + save)")
                .isEqualTo(2);

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree(projectName);

        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isFolderExistsInTree(".history"))
                .as(".history folder should not be visible in the repository tree after save")
                .isFalse();

        // Steps 12-13: New edit after save creates Local Changes (1) with compare dialog
        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(6, 4, "Good Morning2");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        changesDialog = editorPage.getEditorToolbarPanelComponent()
                .clickMore()
                .clickChanges();
        assertThat(changesDialog.getChangesTitle())
                .as("Should show 1 local change after editing a saved project")
                .isEqualTo("Local Changes (1)");
        assertThat(changesDialog.getRowCount())
                .as("Should be 2 rows in history")
                .isEqualTo(2);

        changesDialog.setCompareCheckbox(1, true);
        changesDialog.setCompareCheckbox(2, true);
        CompareLocalChangesDialogComponent compareDialog = changesDialog.clickCompare();
        compareDialog.waitForDialogToAppear();
        compareDialog.openTreeNode("Rules");
        compareDialog.clickTreeNode("Rules String Hello (Integer hour)");

        assertThat(compareDialog.isCellHighlighted(6, 4, 1))
                .as("Cell (6,4) in fragment 1 should be highlighted after edit on saved project")
                .isTrue();
        compareDialog.close();
    }

    @Test
    @TestCaseId("IPBQA-30730")
    @Description("Steps 14-16: Close/reopen project discards local changes and clears history; new edit after reopen creates fresh Local Changes (1); delete/undelete project clears history and reverts cell.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testProjectLifecycleClearsHistory() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        // Step 14: Close project discards local changes; cell reverts; history cleared
        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(6, 4, "Good Morning1");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickCloseBtn();
        repositoryPage.getConfirmCloseProjectDialogComponent().clickClose();
        repositoryPage.waitUntilSpinnerLoaded();

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree(projectName);

        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isFolderExistsInTree(".history"))
                .as(".history folder should not be visible in repository after project close")
                .isFalse();

        repositoryPage.getLeftRepositoryTreeComponent()
                .selectItemInFolder("Projects", projectName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().openProject();
        repositoryPage.waitUntilSpinnerLoaded();

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        assertThat(editorPage.getCenterTable().getCellText(6, 4))
                .as("Cell value should be reverted to original after close and reopen")
                .isEqualTo("Good Morning");

        ChangesDialogComponent changesDialog = editorPage.getEditorToolbarPanelComponent()
                .clickMore()
                .clickChanges();
        assertThat(changesDialog.getNoChangesMessage())
                .as("No history should exist after close and reopen")
                .isEqualTo("No changes in history");

        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        // Step 15: New edit after close/reopen creates fresh Local Changes (1)
        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(7, 4, "Good Afternoon1");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        changesDialog = editorPage.getEditorToolbarPanelComponent()
                .clickMore()
                .clickChanges();
        assertThat(changesDialog.getChangesTitle())
                .as("After close/reopen, new edit should create fresh Local Changes (1)")
                .isEqualTo("Local Changes (1)");
        assertThat(changesDialog.getRowCount())
                .as("Should be 2 rows in fresh history after close/reopen")
                .isEqualTo(2);
        assertThat(changesDialog.getCompareCheckboxValue(1))
                .as("Checkbox at row 1 should be auto-checked (current version)")
                .isTrue();

        // Step 16: Delete and undelete clears history; .history absent; cell reverts
        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.setShowDeletedProjects(true);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickDeleteBtn();
        repositoryPage.getConfirmDeleteDialogComponent().clickDelete();
        repositoryPage.waitUntilSpinnerLoaded();

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickUndeleteBtn();
        repositoryPage.getConfirmUndeleteDialogComponent().clickUndelete();
        repositoryPage.waitUntilSpinnerLoaded();

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree(projectName);

        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isFolderExistsInTree(".history"))
                .as(".history folder should not be visible after delete/undelete")
                .isFalse();
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("Main.xlsx"))
                .as("Main.xlsx should be present after undelete")
                .isTrue();

        repositoryPage.getRepositoryContentButtonsPanelComponent().openProject();
        repositoryPage.waitUntilSpinnerLoaded();

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");

        changesDialog = editorPage.getEditorToolbarPanelComponent()
                .clickMore()
                .clickChanges();
        assertThat(changesDialog.getNoChangesMessage())
                .as("No history should exist after delete and undelete")
                .isEqualTo("No changes in history");

        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");
        assertThat(editorPage.getCenterTable().getCellText(7, 4))
                .as("Cell (7,4) should be reverted to original 'Good Afternoon' after delete/undelete")
                .isEqualTo("Good Afternoon");
    }

    @Test
    @TestCaseId("IPBQA-30730")
    @Description("Steps 17-19 and cancel: Cancel Clear All History preserves history; Clear All History removes all entries; historyCount=0 disables tracking; historyCount=10 re-enables tracking with compare dialog.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAdminHistorySettings() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(6, 4, "Good Morning1");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        // Before Step 19: Cancel Clear All History does not remove history
        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        SystemSettingsPageComponent systemSettings = adminPage.navigateToSystemSettingsPage();
        systemSettings.cancelClearAllHistory();
        adminPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);

        editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        ChangesDialogComponent changesDialog = editorPage.getEditorToolbarPanelComponent()
                .clickMore()
                .clickChanges();
        assertThat(changesDialog.getChangesTitle())
                .as("Cancel should not clear local change history")
                .isEqualTo("Local Changes (1)");
        assertThat(changesDialog.getRowCount())
                .as("Should still have 2 rows after cancel")
                .isEqualTo(2);

        // Step 19: Clear All History removes all entries
        adminPage = editorPage.openUserMenu().navigateToAdministration();
        systemSettings = adminPage.navigateToSystemSettingsPage();
        systemSettings.clearAllHistory();
        adminPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);

        editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        changesDialog = editorPage.getEditorToolbarPanelComponent()
                .clickMore()
                .clickChanges();
        assertThat(changesDialog.getNoChangesMessage())
                .as("Clear All History should remove all local change history")
                .isEqualTo("No changes in history");

        // Step 17: historyCount=0 disables local change tracking
        adminPage = editorPage.openUserMenu().navigateToAdministration();
        systemSettings = adminPage.navigateToSystemSettingsPage();
        systemSettings.setProjectHistoryCount("0");
        systemSettings.applySettingsAndRelogin(User.ADMIN);

        editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(7, 4, "Good Afternoon1");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        changesDialog = editorPage.getEditorToolbarPanelComponent()
                .clickMore()
                .clickChanges();
        assertThat(changesDialog.getNoChangesMessage())
                .as("historyCount=0 should disable local change tracking")
                .isEqualTo("No changes in history");

        // Steps 17.1-18: historyCount=10 re-enables tracking; compare dialog shows highlighted cell
        adminPage = editorPage.openUserMenu().navigateToAdministration();
        systemSettings = adminPage.navigateToSystemSettingsPage();
        systemSettings.setProjectHistoryCount("10");
        systemSettings.applySettingsAndRelogin(User.ADMIN);

        editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(8, 4, "Good Evening3");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        changesDialog = editorPage.getEditorToolbarPanelComponent()
                .clickMore()
                .clickChanges();
        assertThat(changesDialog.getChangesTitle())
                .as("historyCount=10 should re-enable local change tracking")
                .isEqualTo("Local Changes (1)");

        changesDialog.setCompareCheckbox(1, true);
        changesDialog.setCompareCheckbox(2, true);
        CompareLocalChangesDialogComponent compareDialog = changesDialog.clickCompare();
        compareDialog.waitForDialogToAppear();
        compareDialog.openTreeNode("Rules");
        compareDialog.clickTreeNode("Rules String Hello (Integer hour)");

        assertThat(compareDialog.isCellHighlighted(8, 4, 1))
                .as("Cell (8,4) in fragment 1 should be highlighted in compare dialog")
                .isTrue();
        compareDialog.close();
    }
}
