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
import domain.ui.webstudio.components.editortabcomponents.EditorToolbarPanelComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.components.repositorytabcomponents.CopyProjectDialogComponent;
import domain.ui.webstudio.components.repositorytabcomponents.RepositoryContentRevisionsTabComponent;
import domain.ui.webstudio.components.repositorytabcomponents.RepositoryContentTabPropertiesComponent;
import domain.ui.webstudio.components.repositorytabcomponents.UploadFileDialogComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.TestDataUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestMergeBranchesNoConflicts extends BaseTest {

    private static final String PROJECT_NAME = "NoConflicts";
    private static final String BRANCH_NAME = "MyBranch";
    private static final String MASTER_BRANCH = "master";

    @Test
    @TestCaseId("IPBQA-29455")
    @Description("Git - Merge branches without conflicts between master and MyBranch")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testMergeBranchesNoConflicts() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, PROJECT_NAME, "TestMergeBranchesNoConflicts_NoConflicts.zip");
        repositoryPage.refresh();

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);
        assertThat(repositoryPage.getRepositoryContentButtonsPanelComponent().isSyncButtonVisible())
                .as("Sync button should not be visible initially")
                .isFalse();

        repositoryPage.refresh();
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickCopyBtn();

        CopyProjectDialogComponent copyDialog = repositoryPage.getCopyProjectDialogComponent();
        copyDialog.waitForDialogToAppear();
        copyDialog.setNewBranchName(BRANCH_NAME);
        copyDialog.clickCopyButton();
        repositoryPage.refresh();

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);
        assertThat(repositoryPage.getRepositoryContentButtonsPanelComponent().isSyncButtonVisible())
                .as("Sync button should be visible after creating branch")
                .isTrue();
        assertThat(repositoryPage.getRepositoryContentButtonsPanelComponent().getSyncButtonTitle())
                .as("Sync button should have correct title")
                .isEqualTo("Synchronize updates");

        repositoryPage.getRepositoryContentButtonsPanelComponent().clickSync();
        SyncChangesDialogComponent syncDialog = repositoryPage.getSyncChangesDialogComponent();
        syncDialog.waitForDialogToAppear();
        assertThat(syncDialog.getDialogHeader())
                .as("Sync dialog header should be correct")
                .isEqualTo("Sync updates");
        assertThat(syncDialog.isExportButtonEnabled())
                .as("Export button should be disabled initially")
                .isFalse();
        assertThat(syncDialog.getExportButtonTitle())
                .as("Export button should have correct title")
                .isEqualTo("Send updates from 'MyBranch' to 'master'");
        assertThat(syncDialog.isImportButtonEnabled())
                .as("Import button should be disabled initially")
                .isFalse();
        assertThat(syncDialog.getImportButtonTitle())
                .as("Import button should have correct title")
                .isEqualTo("Receive the latest updates from 'master' to 'MyBranch'");
        syncDialog.clickCancel();

        repositoryPage.getLeftRepositoryTreeComponent()
                .selectItemInFolder(PROJECT_NAME, "Module4.xlsx");
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickDeleteBtn();
        repositoryPage.getConfirmDeleteDialogComponent().getDeleteBtn().click();
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickSaveBtn();
        repositoryPage.getSaveChangesComponent().getSaveBtn().click();

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickUploadFileBtn();

        UploadFileDialogComponent uploadDialog = repositoryPage.getUploadFileDialogComponent();
        uploadDialog.setFileName("TestMergeBranchesNoConflicts_Module6.xlsx");
        uploadDialog.uploadFile(TestDataUtil.getFilePathFromResources("TestMergeBranchesNoConflicts_Module6.xlsx"));
        uploadDialog.clickUploadButton();
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickSaveBtn();
        repositoryPage.getSaveChangesComponent().getSaveBtn().click();

        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        EditorToolbarPanelComponent editorToolbar = editorPage.getEditorToolbarPanelComponent();
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(PROJECT_NAME, "Module2");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", "MySpr2");
        editorPage.getCenterTable().editCell(3, 1, "Step1*");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();
        editorToolbar.clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();

        editorToolbar.switchBranch(MASTER_BRANCH);

        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(PROJECT_NAME, "Module1");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", "MySpr1");
        editorPage.getCenterTable().editCell(3, 1, "Step1*");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();
        editorToolbar.clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();

        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);
        repositoryPage.getRepositoryContentTabSwitcherComponent()
                .selectPropertiesTab()
                .selectBranch(MASTER_BRANCH);

        repositoryPage.getLeftRepositoryTreeComponent()
                .selectItemInFolder(PROJECT_NAME, "Module3.xlsx");
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickDeleteBtn();
        repositoryPage.getConfirmDeleteDialogComponent().getDeleteBtn().click();
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickSaveBtn();
        repositoryPage.getSaveChangesComponent().getSaveBtn().click();

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickUploadFileBtn();

        uploadDialog.setFileName("TestMergeBranchesNoConflicts_Module5.xlsx");
        uploadDialog.uploadFile(TestDataUtil.getFilePathFromResources("TestMergeBranchesNoConflicts_Module5.xlsx"));
        uploadDialog.clickUploadButton();
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickSaveBtn();
        repositoryPage.getSaveChangesComponent().getSaveBtn().click();

        repositoryPage.getRepositoryContentButtonsPanelComponent().clickSync();
        syncDialog = repositoryPage.getSyncChangesDialogComponent();
        syncDialog.waitForDialogToAppear();
        assertThat(syncDialog.getCannotImportMessage())
                .as("Cannot import message should be empty")
                .isEmpty();
        assertThat(syncDialog.getCannotExportMessage())
                .as("Cannot export message should be empty")
                .isEmpty();
        syncDialog.clickImportTheirChanges();

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree(PROJECT_NAME);
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("Module1.xlsx"))
                .as("Module1.xlsx should exist in master after merge")
                .isTrue();
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("Module2.xlsx"))
                .as("Module2.xlsx should exist in master after merge")
                .isTrue();
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("TestMergeBranchesNoConflicts_Module5.xlsx"))
                .as("TestMergeBranchesNoConflicts_Module5.xlsx should exist in master after merge")
                .isTrue();
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("TestMergeBranchesNoConflicts_Module6.xlsx"))
                .as("TestMergeBranchesNoConflicts_Module6.xlsx should exist in master after merge")
                .isTrue();
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("Module3.xlsx"))
                .as("Module3.xlsx should not exist in master after merge")
                .isFalse();
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("Module4.xlsx"))
                .as("Module4.xlsx should not exist in master after merge")
                .isFalse();

        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(PROJECT_NAME, "Module2");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", "MySpr2");
        String cellText = editorPage.getCenterTable().getCellText(3, 1);
        assertThat(cellText)
                .as("MySpr2 cell should contain edited text after merge")
                .contains("Step1", "*");

        editorPage.getEditorToolbarPanelComponent().getBreadcrumbsAllProjects().click();
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(PROJECT_NAME, "Module1");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", "MySpr1");
        cellText = editorPage.getCenterTable().getCellText(3, 1);
        assertThat(cellText)
                .as("MySpr1 cell should contain edited text in master")
                .contains("Step1", "*");

        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);
        RepositoryContentTabPropertiesComponent propertiesTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        propertiesTab.selectBranch(BRANCH_NAME);

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree(PROJECT_NAME);
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("Module1.xlsx"))
                .as("Module1.xlsx should exist in MyBranch")
                .isTrue();
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("Module2.xlsx"))
                .as("Module2.xlsx should exist in MyBranch")
                .isTrue();
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("Module3.xlsx"))
                .as("Module3.xlsx should exist in MyBranch")
                .isTrue();
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("TestMergeBranchesNoConflicts_Module6.xlsx"))
                .as("TestMergeBranchesNoConflicts_Module6.xlsx should exist in MyBranch")
                .isTrue();
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("TestMergeBranchesNoConflicts_Module5.xlsx"))
                .as("TestMergeBranchesNoConflicts_Module5.xlsx should not exist in MyBranch yet")
                .isFalse();
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("Module4.xlsx"))
                .as("Module4.xlsx should not exist in MyBranch")
                .isFalse();

        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(PROJECT_NAME, "Module1");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", "MySpr1");
        cellText = editorPage.getCenterTable().getCellText(3, 1);
        assertThat(cellText)
                .as("MySpr1 cell should not be edited in MyBranch yet")
                .isEqualTo("Step1");

        editorToolbar.clickSync();
        syncDialog = editorPage.getSyncChangesDialogComponent();
        syncDialog.waitForDialogToAppear();
        assertThat(syncDialog.isExportButtonEnabled())
                .as("Export button should be disabled")
                .isFalse();
        assertThat(syncDialog.getCannotExportMessage())
                .as("Cannot export message should indicate master has all updates")
                .contains("They have all your updates. Nothing to send to 'master'");
        syncDialog.clickImportTheirChanges();

        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree(PROJECT_NAME);
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("Module1.xlsx"))
                .as("Module1.xlsx should exist in MyBranch after final merge")
                .isTrue();
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("Module2.xlsx"))
                .as("Module2.xlsx should exist in MyBranch after final merge")
                .isTrue();
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("TestMergeBranchesNoConflicts_Module5.xlsx"))
                .as("TestMergeBranchesNoConflicts_Module5.xlsx should exist in MyBranch after final merge")
                .isTrue();
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("TestMergeBranchesNoConflicts_Module6.xlsx"))
                .as("TestMergeBranchesNoConflicts_Module6.xlsx should exist in MyBranch after final merge")
                .isTrue();
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("Module3.xlsx"))
                .as("Module3.xlsx should not exist in MyBranch after final merge")
                .isFalse();
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("Module4.xlsx"))
                .as("Module4.xlsx should not exist in MyBranch after final merge")
                .isFalse();

        repositoryPage.refresh();

        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(PROJECT_NAME, "Module2");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", "MySpr2");
        cellText = editorPage.getCenterTable().getCellText(3, 1);
        assertThat(cellText)
                .as("MySpr2 cell should contain edited text in MyBranch after final merge")
                .contains("Step1", "*");

        editorPage.getEditorToolbarPanelComponent().getBreadcrumbsAllProjects().click();
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(PROJECT_NAME, "Module1");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", "MySpr1");
        cellText = editorPage.getCenterTable().getCellText(3, 1);
        assertThat(cellText)
                .as("MySpr1 cell should contain edited text in MyBranch after final merge")
                .contains("Step1", "*");

        editorToolbar.clickSync();
        syncDialog = editorPage.getSyncChangesDialogComponent();
        syncDialog.waitForDialogToAppear();
        assertThat(syncDialog.getCannotExportMessage())
                .as("Cannot export message should indicate master has all updates")
                .contains("They have all your updates. Nothing to send to 'master");
        assertThat(syncDialog.getCannotImportMessage())
                .as("Cannot import message should indicate MyBranch has all updates")
                .contains("You have all their updates: nothing to receive from 'master");
        assertThat(syncDialog.isExportButtonEnabled())
                .as("Export button should be disabled when branches are in sync")
                .isFalse();
        assertThat(syncDialog.isImportButtonEnabled())
                .as("Import button should be disabled when branches are in sync")
                .isFalse();
        syncDialog.clickCancel();

        editorToolbar.switchBranch(MASTER_BRANCH);
        editorToolbar.clickSync();
        syncDialog = editorPage.getSyncChangesDialogComponent();
        syncDialog.waitForDialogToAppear();
        assertThat(syncDialog.getCannotExportMessage())
                .as("Cannot export message should indicate MyBranch has all updates")
                .contains("They have all your updates. Nothing to send to 'MyBranch'");
        assertThat(syncDialog.getCannotImportMessage())
                .as("Cannot import message should indicate master has all updates")
                .contains("You have all their updates: nothing to receive from 'MyBranch");
        syncDialog.clickCancel();

        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);
        RepositoryContentRevisionsTabComponent revisionsTab =
                repositoryPage.getRepositoryContentTabSwitcherComponent().selectRevisionsTab();

        List<String> expectedComments = List.of(
                "Merge branch 'MyBranch'",
                "Project NoConflicts is saved.",
                "Project NoConflicts is saved.",
                "Project NoConflicts is saved.",
                "Project NoConflicts is saved.",
                "Project NoConflicts is saved.",
                "Project NoConflicts is saved.",
                "Project TestMergeBranchesNoConflicts_NoConflicts is created."
        );

        List<String> actualComments = new ArrayList<>();
        for (int i = 1; i <= revisionsTab.getRevisionsCount(); i++) {
            actualComments.add(revisionsTab.getRevisionDescription(i));
        }
        assertThat(actualComments)
                .as("Revision history in master should contain all expected comments")
                .containsExactlyInAnyOrderElementsOf(expectedComments);

        propertiesTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        propertiesTab.selectBranch(BRANCH_NAME);
        revisionsTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectRevisionsTab();

        actualComments.clear();
        for (int i = 1; i <= revisionsTab.getRevisionsCount(); i++) {
            actualComments.add(revisionsTab.getRevisionDescription(i));
        }
        assertThat(actualComments)
                .as("Revision history in MyBranch should contain all expected comments")
                .containsExactlyInAnyOrderElementsOf(expectedComments);
    }
}
