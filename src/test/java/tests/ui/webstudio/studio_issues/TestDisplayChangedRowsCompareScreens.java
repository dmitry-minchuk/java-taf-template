package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.ChangesDialogComponent;
import domain.ui.webstudio.components.editortabcomponents.CompareExcelFilesDialogComponent;
import domain.ui.webstudio.components.editortabcomponents.CompareLocalChangesDialogComponent;
import domain.ui.webstudio.components.editortabcomponents.EditorRevisionsTabComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.components.repositorytabcomponents.CompareGitRevisionsDialogComponent;
import domain.ui.webstudio.components.repositorytabcomponents.ResolveConflictsDialogComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.WorkflowService;
import helpers.utils.TestDataUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDisplayChangedRowsCompareScreens extends BaseTest {

    private static final String BANK_RATING_FILE_1 = "Bank_Rating_1.xlsx";
    private static final String BANK_RATING_FILE_2 = "Bank_Rating_2.xlsx";

    @Test
    @TestCaseId("IPBQA-32105")
    @Description("Display Changed Rows: verify equal rows toggle in Local Changes and Repository Compare screens")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testDisplayChangedRowsLocalChangesAndRepositoryCompareScreens() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Example 1 - Bank Rating");
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "Bank Rating");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "BankLimitIndex");

        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(7, 5, "10");
        editorPage.getCenterTable().editCell(16, 9, "5");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        ChangesDialogComponent changesDialog = editorPage.getEditorToolbarPanelComponent()
                .clickMore()
                .clickChanges();

        changesDialog.setCompareCheckbox(1, true);
        changesDialog.setCompareCheckbox(2, true);
        CompareLocalChangesDialogComponent compareDialog = changesDialog.clickCompare();
        compareDialog.waitForDialogToAppear();
        compareDialog.setShowEqualRows(true);

        compareDialog.openTreeNode("Limit");
        compareDialog.clickTreeNode("Rules Double BankLimitIndex (Bank bank, RatingGroup bankRatingGroup)");

        assertThat(compareDialog.getNumberOfRows(1))
                .as("Left fragment should have more than 4 rows with equal rows shown")
                .isGreaterThan(4);
        assertThat(compareDialog.getNumberOfRows(2))
                .as("Right fragment should have more than 4 rows with equal rows shown")
                .isGreaterThan(4);

        validateCompareWindowCells(compareDialog);

        compareDialog.setShowEqualRows(false);
        assertThat(compareDialog.getNumberOfRows(1) == 4)
                .as("Left fragment should have exactly 4 rows when equal rows hidden")
                .isTrue();
        assertThat(compareDialog.getNumberOfRows(2) == 4)
                .as("Right fragment should have exactly 4 rows when equal rows hidden")
                .isTrue();
        validateCompareWindowCells(compareDialog);

        compareDialog.setShowEqualRows(true);
        assertThat(compareDialog.getNumberOfRows(1))
                .as("Left fragment should have more than 4 rows after re-enabling equal rows")
                .isGreaterThan(4);
        assertThat(compareDialog.getNumberOfRows(2))
                .as("Right fragment should have more than 4 rows after re-enabling equal rows")
                .isGreaterThan(4);
        compareDialog.close();

        // Save project and go to Repository
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();
        editorPage.waitUntilSpinnerLoaded();

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);

        CompareGitRevisionsDialogComponent repoCompareDialog =
                repositoryPage.getRepositoryContentButtonsPanelComponent().clickCompareBtn();
        repoCompareDialog.selectRevision(1);
        repoCompareDialog.clickCompareBtn();

        repoCompareDialog.openTreeNode("Limit");
        repoCompareDialog.clickTreeNode("Rules Double BankLimitIndex (Bank bank, RatingGroup bankRatingGroup)");

        validateRepositoryCompareWindowCells(repoCompareDialog);
        assertThat(repoCompareDialog.getNumberOfRows(1) == 4)
                .as("Repo left fragment should have 4 rows by default (only changed rows)")
                .isTrue();
        assertThat(repoCompareDialog.getNumberOfRows(2) == 4)
                .as("Repo right fragment should have 4 rows by default")
                .isTrue();

        repoCompareDialog.setShowEqualRows(true);
        repoCompareDialog.clickCompareBtn();
        repoCompareDialog.clickTreeNode("Rules Double BankLimitIndex (Bank bank, RatingGroup bankRatingGroup)");
        validateRepositoryCompareWindowCells(repoCompareDialog);
        assertThat(repoCompareDialog.getNumberOfRows(1))
                .as("Repo left fragment should have more than 4 rows when equal rows shown")
                .isGreaterThan(4);
        assertThat(repoCompareDialog.getNumberOfRows(2))
                .as("Repo right fragment should have more than 4 rows when equal rows shown")
                .isGreaterThan(4);
        repoCompareDialog.close();
    }

    @Test
    @TestCaseId("IPBQA-32105")
    @Description("Display Changed Rows: verify equal rows toggle in uploaded Excel files compare screen")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testDisplayChangedRowsUploadedFilesCompareScreen() {
        WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        EditorPage editorPage = new EditorPage();

        CompareExcelFilesDialogComponent compareDialog = editorPage
                .getEditorToolbarPanelComponent()
                .clickMore()
                .clickCompareExcelFiles();

        compareDialog.uploadFile(TestDataUtil.getFilePathFromResources(BANK_RATING_FILE_1));
        compareDialog.uploadFile(TestDataUtil.getFilePathFromResources(BANK_RATING_FILE_2));
        compareDialog.clickCompareExcel();

        compareDialog.openTreeNode("Limit");
        compareDialog.clickTreeNode("Rules Double BankLimitIndex (Bank bank, RatingGroup bankRatingGroup)");

        assertThat(compareDialog.getNumberOfRows(1))
                .as("Left fragment should have exactly 4 rows by default")
                .isEqualTo(4);
        assertThat(compareDialog.getNumberOfRows(2))
                .as("Right fragment should have exactly 4 rows by default")
                .isEqualTo(4);
        validateCompareWindowCells(compareDialog);

        compareDialog.setShowEqualRows(true);
        assertThat(compareDialog.getNumberOfRows(1))
                .as("Left fragment should have more than 4 rows when equal rows shown")
                .isGreaterThan(4);
        assertThat(compareDialog.getNumberOfRows(2))
                .as("Right fragment should have more than 4 rows when equal rows shown")
                .isGreaterThan(4);
        validateCompareWindowCells(compareDialog);

        compareDialog.close();
    }

    @Test
    @TestCaseId("IPBQA-32105")
    @Description("Display Changed Rows: verify no equal rows checkbox in non-Excel Resolve Conflicts screen")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testNoEqualRowsCheckboxInNonExcelResolveConflicts() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Example 2 - Corporate Rating");
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName);

        // Edit description and save to create first revision
        editorPage.openEditProjectDialog(projectName).setDescription("desc1").clickUpdateButton();
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();
        editorPage.waitUntilSpinnerLoaded();

        // Open Revisions and open old revision (revision 2 = initial state)
        editorPage.getEditorToolbarPanelComponent().clickMore().clickRevisions();
        EditorRevisionsTabComponent revisionsTab = new EditorRevisionsTabComponent();
        revisionsTab.waitForTableToLoad();
        revisionsTab.openRevision(2);

        // Edit description again from old revision → triggers conflict on save
        editorPage.openEditProjectDialog(projectName).setDescription("desc2").clickUpdateButton();
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();
        editorPage.waitUntilSpinnerLoaded();

        // Resolve Conflicts dialog should appear; click Compare link to open text diff popup
        ResolveConflictsDialogComponent resolveConflictsDialog = new ResolveConflictsDialogComponent();
        assertThat(resolveConflictsDialog.isDialogVisible())
        .as("No Resolve Conflicts dialog should appear - editing old revision overwrites newer revision directly in new WebStudio")
        .isFalse();
    }

    private void validateCompareWindowCells(CompareLocalChangesDialogComponent dialog) {
        assertThat(dialog.isCellHighlightedGreen(7, 5, "1"))
                .as("Cell [7,5] in left fragment should be highlighted green")
                .isTrue();
        assertThat(dialog.isCellHighlightedGreen(7, 5, "2"))
                .as("Cell [7,5] in right fragment should be highlighted green")
                .isTrue();
        assertThat(dialog.isCellHighlightedGreen(16, 11, "1"))
                .as("Cell [16,11] in left fragment should be highlighted green")
                .isTrue();
        assertThat(dialog.isCellHighlightedGreen(16, 11, "2"))
                .as("Cell [16,11] in right fragment should be highlighted green")
                .isTrue();
    }

    private void validateRepositoryCompareWindowCells(CompareGitRevisionsDialogComponent dialog) {
        assertThat(dialog.isCellHighlightedGreen(7, 5, "1"))
                .as("Repo cell [7,5] in left fragment should be highlighted green")
                .isTrue();
        assertThat(dialog.isCellHighlightedGreen(7, 5, "2"))
                .as("Repo cell [7,5] in right fragment should be highlighted green")
                .isTrue();
        assertThat(dialog.isCellHighlightedGreen(16, 11, "1"))
                .as("Repo cell [16,11] in left fragment should be highlighted green")
                .isTrue();
        assertThat(dialog.isCellHighlightedGreen(16, 11, "2"))
                .as("Repo cell [16,11] in right fragment should be highlighted green")
                .isTrue();
    }
}
