package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.SystemSettingsPageComponent;
import domain.ui.webstudio.components.editortabcomponents.ChangesDialogComponent;
import domain.ui.webstudio.components.editortabcomponents.CompareLocalChangesDialogComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestLocalChangesLifecycleAndSettings extends BaseTest {

    @Test
    @TestCaseId("IPBQA-30730")
    @Description("Steps 14-15: Close/reopen project discards local changes and clears history; new edit after reopen creates fresh Local Changes (1).")
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
        // Close discards the uncommitted change (confirmed via the "Discard unsaved changes?" prompt).
        repositoryPage.closeProject(projectName);
        repositoryPage.waitUntilAppIdle();

        assertThat(repositoryPage.openProjectDetail(projectName).openFilesTab().isFolderPresent(".history"))
                .as(".history folder should not be visible in repository after project close")
                .isFalse();

        repositoryPage.openProjectsList().openProject(projectName);
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
