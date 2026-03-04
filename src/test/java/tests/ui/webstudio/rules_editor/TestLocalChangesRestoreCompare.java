package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.SystemSettingsPageComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.ChangesDialogComponent;
import domain.ui.webstudio.components.editortabcomponents.CompareLocalChangesDialogComponent;
import domain.ui.webstudio.components.editortabcomponents.EditorRevisionsTabComponent;
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
    @Description("Step 1: Fresh project shows 'No changes in history'.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testNoChangesInFreshProject() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        ChangesDialogComponent changesDialog = editorPage.getEditorToolbarPanelComponent()
                .clickMore()
                .clickChanges();

        assertThat(changesDialog.getNoChangesMessage())
                .as("Fresh project should show no changes message")
                .isEqualTo("No changes in history");
    }

    @Test
    @TestCaseId("IPBQA-30730")
    @Description("Step 2: Editing a cell then undoing does not create a history entry.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testUndoneEditDoesNotCreateHistory() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(6, 4, "Good Morning1");
        editorPage.getEditorTableActionsPanelComponent().undoClickChanges();

        ChangesDialogComponent changesDialog = editorPage.getEditorToolbarPanelComponent()
                .clickMore()
                .clickChanges();

        assertThat(changesDialog.getNoChangesMessage())
                .as("Undone edit should not create a history entry")
                .isEqualTo("No changes in history");
    }

    @Test
    @TestCaseId("IPBQA-30730")
    @Description("Steps 3-4: Single edit creates Local Changes (1) with 2 rows; compare dialog shows highlighted cell.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testSingleEditCreatesLocalChangeWithCompare() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(6, 4, "Good Morning1");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        ChangesDialogComponent changesDialog = editorPage.getEditorToolbarPanelComponent()
                .clickMore()
                .clickChanges();

        assertThat(changesDialog.getChangesTitle())
                .as("Changes title should show 1 local change")
                .isEqualTo("Local Changes (1)");
        assertThat(changesDialog.getRowCount())
                .as("Should be 2 rows in history (current + previous)")
                .isEqualTo(2);

        changesDialog.setCompareCheckbox(1, true);
        changesDialog.setCompareCheckbox(2, true);
        CompareLocalChangesDialogComponent compareDialog = changesDialog.clickCompare();
        compareDialog.waitForDialogToAppear();
        compareDialog.openTreeNode("Rules");
        compareDialog.clickTreeNode("Rules String Hello (Integer hour)");

        assertThat(compareDialog.isCellHighlighted(6, 4, 1))
                .as("Cell (6,4) in fragment 1 should be highlighted after single edit")
                .isTrue();

        compareDialog.close();
    }

    @Test
    @TestCaseId("IPBQA-30730")
    @Description("Steps 5-8.1: Two edits create Local Changes (2) with 3 rows; restoring to row 2 reverts second edit only.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testMultipleEditsAndRestoreToVersion() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(6, 4, "Good Morning1");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(7, 4, "Good Afternoon1");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        ChangesDialogComponent changesDialog = editorPage.getEditorToolbarPanelComponent()
                .clickMore()
                .clickChanges();

        assertThat(changesDialog.getChangesTitle())
                .as("Changes title should show 2 local changes")
                .isEqualTo("Local Changes (2)");
        assertThat(changesDialog.getRowCount())
                .as("Should be 3 rows in history")
                .isEqualTo(3);

        changesDialog.clickRestoreAtRow(2);
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getEditorLeftRulesTreeComponent()
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
    @Description("Step 11: Saving the project clears local history and creates 2 revisions; no .history folder in repository.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testSaveProjectClearsLocalHistory() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(6, 4, "Good Morning1");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        ChangesDialogComponent changesDialog = editorPage.getEditorToolbarPanelComponent()
                .clickMore()
                .clickChanges();

        assertThat(changesDialog.getNoChangesMessage())
                .as("Local history should be cleared after project save")
                .isEqualTo("No changes in history");

        editorPage.getEditorToolbarPanelComponent().clickMore().clickRevisions();
        EditorRevisionsTabComponent revisionsTab = new EditorRevisionsTabComponent();
        revisionsTab.waitForTableToLoad();

        assertThat(revisionsTab.getRowCount())
                .as("Should have 2 revisions after save")
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
    }

    @Test
    @TestCaseId("IPBQA-30730")
    @Description("Steps 12-13: After saving, a new edit creates Local Changes (1); compare dialog shows highlighted cell.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testLocalChangesAfterSavedProject() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(projectName);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(6, 4, "Good Morning1");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        ChangesDialogComponent changesDialog = editorPage.getEditorToolbarPanelComponent()
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
                .as("Cell (6,4) in fragment 1 should be highlighted")
                .isTrue();

        compareDialog.close();
    }

    @Test
    @TestCaseId("IPBQA-30730")
    @Description("Step 14: Closing and reopening a project discards unsaved local changes; original cell value is restored.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCloseProjectClearsHistory() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(6, 4, "Good Morning1");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickCloseBtn();
        repositoryPage.waitUntilSpinnerLoaded();

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().openProject();
        repositoryPage.waitUntilSpinnerLoaded();

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
        editorPage.getEditorLeftRulesTreeComponent()
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
    }

    @Test
    @TestCaseId("IPBQA-30730")
    @Description("Step 17: Setting project history count to 0 disables local change tracking.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testProjectHistoryCountZeroDisablesHistory() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        EditorPage editorPage = new EditorPage();

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        SystemSettingsPageComponent systemSettings = adminPage.navigateToSystemSettingsPage();
        try {
            systemSettings.setProjectHistoryCount("0");
            systemSettings.applySettingsAndRelogin(User.ADMIN);

            editorPage = new EditorPage();
            editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
            editorPage.getEditorLeftRulesTreeComponent()
                    .expandFolderInTree("Decision")
                    .selectItemInFolder("Decision", "Hello");

            editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
            editorPage.getCenterTable().editCell(6, 4, "Good Morning1");
            editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

            ChangesDialogComponent changesDialog = editorPage.getEditorToolbarPanelComponent()
                    .clickMore()
                    .clickChanges();

            assertThat(changesDialog.getNoChangesMessage())
                    .as("History count 0 should disable local change tracking")
                    .isEqualTo("No changes in history");
        } finally {
            EditorPage finalPage = new EditorPage();
            AdminPage finalAdminPage = finalPage.openUserMenu().navigateToAdministration();
            SystemSettingsPageComponent finalSettings = finalAdminPage.navigateToSystemSettingsPage();
            finalSettings.setProjectHistoryCount("10");
            finalSettings.applySettingsAndRelogin(User.ADMIN);
        }
    }

    @Test
    @TestCaseId("IPBQA-30730")
    @Description("Steps 17.1 & 18: History count set to 10 re-enables tracking; compare shows highlighted cell.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testProjectHistoryCountRestored() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        EditorPage editorPage = new EditorPage();

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        SystemSettingsPageComponent systemSettings = adminPage.navigateToSystemSettingsPage();
        try {
            systemSettings.setProjectHistoryCount("10");
            systemSettings.applySettingsAndRelogin(User.ADMIN);

            editorPage = new EditorPage();
            editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
            editorPage.getEditorLeftRulesTreeComponent()
                    .expandFolderInTree("Decision")
                    .selectItemInFolder("Decision", "Hello");

            editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
            editorPage.getCenterTable().editCell(7, 4, "Good Afternoon1");
            editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

            ChangesDialogComponent changesDialog = editorPage.getEditorToolbarPanelComponent()
                    .clickMore()
                    .clickChanges();

            assertThat(changesDialog.getChangesTitle())
                    .as("History count 10 should enable local change tracking")
                    .isEqualTo("Local Changes (1)");

            changesDialog.setCompareCheckbox(1, true);
            changesDialog.setCompareCheckbox(2, true);
            CompareLocalChangesDialogComponent compareDialog = changesDialog.clickCompare();
            compareDialog.waitForDialogToAppear();
            compareDialog.openTreeNode("Rules");
            compareDialog.clickTreeNode("Rules String Hello (Integer hour)");

            assertThat(compareDialog.isCellHighlighted(7, 4, 1))
                    .as("Cell (7,4) in fragment 1 should be highlighted in compare dialog")
                    .isTrue();

            compareDialog.close();
        } finally {
            EditorPage finalPage = new EditorPage();
            AdminPage finalAdminPage = finalPage.openUserMenu().navigateToAdministration();
            SystemSettingsPageComponent finalSettings = finalAdminPage.navigateToSystemSettingsPage();
            finalSettings.setProjectHistoryCount("10");
            finalSettings.applySettingsAndRelogin(User.ADMIN);
        }
    }

    @Test
    @TestCaseId("IPBQA-30730")
    @Description("Step 19: Clear All History button removes all local change history.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testClearAllHistoryButton() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        EditorPage editorPage = new EditorPage();

        try {
            editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
            editorPage.getEditorLeftRulesTreeComponent()
                    .expandFolderInTree("Decision")
                    .selectItemInFolder("Decision", "Hello");

            editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
            editorPage.getCenterTable().editCell(6, 4, "Good Morning1");
            editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

            ChangesDialogComponent changesDialog = editorPage.getEditorToolbarPanelComponent()
                    .clickMore()
                    .clickChanges();

            assertThat(changesDialog.getChangesTitle())
                    .as("Should have 1 local change before clearing history")
                    .isEqualTo("Local Changes (1)");

            AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
            SystemSettingsPageComponent systemSettings = adminPage.navigateToSystemSettingsPage();
            systemSettings.clearAllHistory();
            systemSettings.applySettingsAndRelogin(User.ADMIN);

            editorPage = new EditorPage();
            editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Main");
            editorPage.getEditorLeftRulesTreeComponent()
                    .expandFolderInTree("Decision")
                    .selectItemInFolder("Decision", "Hello");

            changesDialog = editorPage.getEditorToolbarPanelComponent()
                    .clickMore()
                    .clickChanges();

            assertThat(changesDialog.getNoChangesMessage())
                    .as("History should be empty after Clear All History")
                    .isEqualTo("No changes in history");
        } finally {
            EditorPage finalPage = new EditorPage();
            AdminPage finalAdminPage = finalPage.openUserMenu().navigateToAdministration();
            SystemSettingsPageComponent finalSettings = finalAdminPage.navigateToSystemSettingsPage();
            finalSettings.setProjectHistoryCount("10");
            finalSettings.applySettingsAndRelogin(User.ADMIN);
        }
    }
}
