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

public class TestDisplayChangedRowsTableStructure extends BaseTest {

    @Test
    @TestCaseId("IPBQA-32105")
    @Description("Display Changed Rows: equal rows checkbox when a new table is created by copying")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testEqualRowsCheckboxInTableCreationComparison() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Example 1 - Bank Rating");
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "Bank Rating");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "BankLimitIndex");

        editorPage.getEditorToolbarPanelComponent().copyTableAsNew("newTable", "");
        editorPage.waitUntilSpinnerLoaded();

        ChangesDialogComponent changesDialog = editorPage.getEditorToolbarPanelComponent()
                .clickMore()
                .clickChanges();

        changesDialog.setCompareCheckbox(1, true);
        changesDialog.setCompareCheckbox(2, true);
        CompareLocalChangesDialogComponent compareDialog = changesDialog.clickCompare();
        compareDialog.waitForDialogToAppear();

        compareDialog.openTreeNode("Rating Algorithm");
        compareDialog.clickTreeNode("Rules Double newTable (Bank bank, RatingGroup bankRatingGroup)");

        compareDialog.setShowEqualRows(false);
        assertThat(compareDialog.getNumberOfRows(1))
                .as("Left fragment (original, no new table): 0 rows")
                .isEqualTo(0);
        assertThat(compareDialog.getNumberOfRows(2))
                .as("Right fragment (new table): 17 rows")
                .isEqualTo(17);

        compareDialog.setShowEqualRows(true);
        assertThat(compareDialog.getNumberOfRows(1))
                .as("Left fragment stays 0 (table didn't exist)")
                .isEqualTo(0);
        assertThat(compareDialog.getNumberOfRows(2))
                .as("Right fragment stays 17 rows")
                .isEqualTo(17);

        compareDialog.close();
    }

    @Test
    @TestCaseId("IPBQA-32105")
    @Description("Display Changed Rows: equal rows and column counts when both a row and a column are added")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testEqualRowsCheckboxInTableAddingRowAndColumnComparison() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "Main");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(2, 1, "changedValue");
        editorPage.getCenterTable().clickCell(6, 4);
        editorPage.getEditorTableActionsPanelComponent().clickInsertColumnBefore();
        editorPage.getCenterTable().editCell(2, 4, "addedValue");
        editorPage.getEditorTableActionsPanelComponent().clickInsertRowAfter();
        editorPage.getCenterTable().editCell(3, 4, "addedValue2");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        ChangesDialogComponent changesDialog = editorPage.getEditorToolbarPanelComponent()
                .clickMore()
                .clickChanges();

        changesDialog.setCompareCheckbox(1, true);
        changesDialog.setCompareCheckbox(2, true);
        CompareLocalChangesDialogComponent compareDialog = changesDialog.clickCompare();
        compareDialog.waitForDialogToAppear();

        compareDialog.openTreeNode("Rules");
        compareDialog.clickTreeNode("Rules String Hello (Integer hour)");

        compareDialog.setShowEqualRows(false);
        assertThat(compareDialog.getNumberOfRows(1))
                .as("Left fragment (original): 9 rows")
                .isEqualTo(9);
        assertThat(compareDialog.getNumberOfColumns(1))
                .as("Left fragment (original): 4 columns")
                .isEqualTo(4);
        assertThat(compareDialog.getNumberOfRows(2))
                .as("Right fragment (with added row): 10 rows")
                .isEqualTo(10);
        assertThat(compareDialog.getNumberOfColumns(2))
                .as("Right fragment (with added column): 5 columns")
                .isEqualTo(5);

        compareDialog.setShowEqualRows(true);
        assertThat(compareDialog.getNumberOfRows(1))
                .as("Left fragment rows unchanged when showing equal rows (column added)")
                .isEqualTo(9);
        assertThat(compareDialog.getNumberOfColumns(1))
                .as("Left fragment columns unchanged")
                .isEqualTo(4);
        assertThat(compareDialog.getNumberOfRows(2))
                .as("Right fragment rows unchanged")
                .isEqualTo(10);
        assertThat(compareDialog.getNumberOfColumns(2))
                .as("Right fragment columns unchanged")
                .isEqualTo(5);

        compareDialog.close();
    }

    @Test
    @TestCaseId("IPBQA-32105")
    @Description("Display Changed Rows: equal rows and column counts when only a column is added")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testEqualRowsCheckboxInTableAddingColumnComparison() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "Main");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().clickCell(2, 2);
        editorPage.getEditorTableActionsPanelComponent().clickInsertColumnBefore();
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        ChangesDialogComponent changesDialog = editorPage.getEditorToolbarPanelComponent()
                .clickMore()
                .clickChanges();

        changesDialog.setCompareCheckbox(1, true);
        changesDialog.setCompareCheckbox(2, true);
        CompareLocalChangesDialogComponent compareDialog = changesDialog.clickCompare();
        compareDialog.waitForDialogToAppear();

        compareDialog.openTreeNode("Rules");
        compareDialog.clickTreeNode("Rules String Hello (Integer hour)");

        compareDialog.setShowEqualRows(false);
        assertThat(compareDialog.getNumberOfRows(1))
                .as("Left fragment (original): 9 rows")
                .isEqualTo(9);
        assertThat(compareDialog.getNumberOfColumns(1))
                .as("Left fragment (original): 4 columns")
                .isEqualTo(4);
        assertThat(compareDialog.getNumberOfRows(2))
                .as("Right fragment (column added): still 9 rows")
                .isEqualTo(9);
        assertThat(compareDialog.getNumberOfColumns(2))
                .as("Right fragment (column added): 5 columns")
                .isEqualTo(5);

        compareDialog.setShowEqualRows(true);
        assertThat(compareDialog.getNumberOfRows(1))
                .as("Left fragment rows unchanged")
                .isEqualTo(9);
        assertThat(compareDialog.getNumberOfColumns(1))
                .as("Left fragment columns unchanged")
                .isEqualTo(4);
        assertThat(compareDialog.getNumberOfRows(2))
                .as("Right fragment rows unchanged")
                .isEqualTo(9);
        assertThat(compareDialog.getNumberOfColumns(2))
                .as("Right fragment columns unchanged")
                .isEqualTo(5);

        compareDialog.close();
    }
}
