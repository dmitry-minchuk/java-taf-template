package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.ChangesDialogComponent;
import domain.ui.webstudio.components.editortabcomponents.CompareLocalChangesDialogComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDisplayChangedRowsResolveConflicts extends BaseTest {

    private static final String EPBDS_12417_FILE = "EPBDS-12417.xlsx";

    @Test
    @TestCaseId("IPBQA-32105")
    @Description("Display Changed Rows: equal rows checkbox in Resolve Conflicts for project created from Excel file")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testEqualRowsCheckboxInResolveConflictsProjFromExcelFile() {
        String projectName = WorkflowService.loginCreateProjectFromExcelFile(User.ADMIN, EPBDS_12417_FILE);
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "EPBDS-12417");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Test")
                .selectItemInFolder("Test", "BenefitPremium");

        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(2, 2, "changedValue");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        ChangesDialogComponent changesDialog = editorPage.getEditorToolbarPanelComponent()
                .clickMore()
                .clickChanges();

        changesDialog.setCompareCheckbox(1, true);
        changesDialog.setCompareCheckbox(2, true);
        CompareLocalChangesDialogComponent compareDialog = changesDialog.clickCompare();
        compareDialog.waitForDialogToAppear();

        compareDialog.openTreeNode("Sheet1");
        compareDialog.clickTreeNode("Test BenefitPremium");

        compareDialog.setShowEqualRows(false);
        assertThat(compareDialog.getNumberOfRows(1))
                .as("Left fragment: only 1 changed row when equal rows hidden")
                .isEqualTo(1);
        assertThat(compareDialog.getNumberOfRows(2))
                .as("Right fragment: only 1 changed row when equal rows hidden")
                .isEqualTo(1);
        assertThat(compareDialog.isCellHighlightedWhite(2, 1, "1"))
                .as("Changed cell in left fragment should be highlighted white")
                .isTrue();
        assertThat(compareDialog.isCellHighlightedWhite(2, 1, "2"))
                .as("Changed cell in right fragment should be highlighted white")
                .isTrue();

        compareDialog.setShowEqualRows(true);
        assertThat(compareDialog.getNumberOfRows(1))
                .as("Left fragment should have more than 1 row when equal rows shown")
                .isGreaterThan(1);
        assertThat(compareDialog.getNumberOfRows(2))
                .as("Right fragment should have more than 1 row when equal rows shown")
                .isGreaterThan(1);
        assertThat(compareDialog.isCellHighlightedWhite(2, 1, "1"))
                .as("Changed cell in left fragment still highlighted white")
                .isTrue();
        assertThat(compareDialog.isCellHighlightedWhite(2, 1, "2"))
                .as("Changed cell in right fragment still highlighted white")
                .isTrue();

        compareDialog.close();
    }

    @Test
    @TestCaseId("IPBQA-32105")
    @Description("Display Changed Rows: equal rows checkbox when a row is added to a table")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testEqualRowsCheckboxInAddingRowComparison() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Example 1 - Bank Rating");
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "Bank Rating");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "BankLimitIndex");

        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getEditorTableActionsPanelComponent().clickInsertRowAfter();
        editorPage.getCenterTable().editCell(2, 1, "changedValue");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        ChangesDialogComponent changesDialog = editorPage.getEditorToolbarPanelComponent()
                .clickMore()
                .clickChanges();

        changesDialog.setCompareCheckbox(1, true);
        changesDialog.setCompareCheckbox(2, true);
        CompareLocalChangesDialogComponent compareDialog = changesDialog.clickCompare();
        compareDialog.waitForDialogToAppear();

        compareDialog.openTreeNode("Limit");
        compareDialog.clickTreeNode("Rules Double BankLimitIndex (Bank bank, RatingGroup bankRatingGroup)");

        compareDialog.setShowEqualRows(false);
        assertThat(compareDialog.getNumberOfRows(1))
                .as("Left fragment: 1 row (empty added row in original)")
                .isEqualTo(1);
        assertThat(compareDialog.getNumberOfRows(2))
                .as("Right fragment: 1 row (the new row with value)")
                .isEqualTo(1);
        assertThat(compareDialog.isCellContainsExpectedValue(2, 1, "1", "Rule"))
                .as("Added row in left fragment should be empty")
                .isTrue();
        assertThat(compareDialog.isCellContainsExpectedValue(2, 1, "2", "changedValue"))
                .as("Added row in right fragment should contain 'changedValue'")
                .isTrue();

        compareDialog.setShowEqualRows(true);
        assertThat(compareDialog.getNumberOfRows(1))
                .as("Left fragment should have more than 1 row when equal rows shown")
                .isGreaterThan(1);
        assertThat(compareDialog.getNumberOfRows(2))
                .as("Right fragment should have more than 1 row when equal rows shown")
                .isGreaterThan(1);
        assertThat(compareDialog.isCellContainsExpectedValue(2, 1, "2", "changedValue"))
                .as("New row value in right fragment still present")
                .isTrue();

        compareDialog.close();
    }

    @Test
    @TestCaseId("IPBQA-32105")
    @Description("Display Changed Rows: equal rows checkbox when a table is deleted")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testEqualRowsCheckboxInTableDeletionComparison() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Example 1 - Bank Rating");
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "Bank Rating");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "BankLimitIndex");

        editorPage.getEditorToolbarPanelComponent().removeCurrentTable();
        editorPage.waitUntilSpinnerLoaded();

        ChangesDialogComponent changesDialog = editorPage.getEditorToolbarPanelComponent()
                .clickMore()
                .clickChanges();

        changesDialog.setCompareCheckbox(1, true);
        changesDialog.setCompareCheckbox(2, true);
        CompareLocalChangesDialogComponent compareDialog = changesDialog.clickCompare();
        compareDialog.waitForDialogToAppear();

        compareDialog.openTreeNode("Limit");
        compareDialog.clickTreeNode("Rules Double BankLimitIndex (Bank bank, RatingGroup bankRatingGroup)");

        compareDialog.setShowEqualRows(false);
        assertThat(compareDialog.getNumberOfRows(1))
                .as("Left fragment (original): should have 17 rows")
                .isEqualTo(17);
        assertThat(compareDialog.getNumberOfRows(2))
                .as("Right fragment (after delete): should have 0 rows")
                .isEqualTo(0);

        compareDialog.setShowEqualRows(true);
        assertThat(compareDialog.getNumberOfRows(1))
                .as("Left fragment stays 17 rows (no equal rows to show since table is deleted)")
                .isEqualTo(17);
        assertThat(compareDialog.getNumberOfRows(2))
                .as("Right fragment stays 0 rows (table deleted)")
                .isEqualTo(0);

        compareDialog.close();
    }
}
