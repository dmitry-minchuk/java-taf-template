package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TableComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSmartLookupSmartRules extends BaseTest {

    private static final String EXCEL_FILE = "TestSmartLookupSmartRules.xlsx";
    private static final String MODULE_NAME = "TestSmartLookupSmartRules";

    @Test
    @TestCaseId("IPBQA-29358")
    @Description("SmartLookup and SmartRules tables: open, edit, save, copy, remove and create test table")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testSmartLookupSmartRules() {
        String projectName = WorkflowService.loginCreateProjectFromExcelFile(User.ADMIN, EXCEL_FILE);
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, MODULE_NAME);
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "MySmarRule");

        TableComponent table = editorPage.getCenterTable();
        assertThat(table.getColumn(1)).isEqualTo(List.of(
                "SmartRules Double MySmarRule(Person pers)", "Date of Birth", "01/01/1980 .. 01/01/1990",
                "01/01/1980 .. 01/01/1990", "01/01/1980 .. 01/01/1990", "01/01/1990 .. 01/01/2000",
                "01/01/1990 .. 01/01/2000", "01/01/1990 .. 01/01/2000"));
        assertThat(table.getColumn(2)).isEqualTo(List.of("Gender", "male", "female",
                "male", "female", "male", "female"));
        assertThat(table.getColumn(3)).isEqualTo(List.of("Result", "0.1", "0.2",
                "0.12", "0.12", "0.13", "0.13"));

        table.doubleClickCell(2, 2);
        editorPage.getEditorTableActionsPanelComponent().clickInsertColumnBefore();
        table.editCell(2, 2, "Marital Status");
        saveTableAndCheckNoProblems(editorPage);
        table.editCell(3, 2, "Married");
        table.editCell(4, 2, "NoMarried");
        table.editCell(5, 2, "Divorsed");
        table.editCell(6, 2, "NoMarried");
        table.editCell(7, 2, "Married");
        table.editCell(8, 2, "Divorsed");
        saveTableAndCheckNoProblems(editorPage);
        assertThat(table.getColumn(2)).isEqualTo(List.of("Marital Status", "Married",
                "NoMarried", "Divorsed", "NoMarried", "Married", "Divorsed"));

        table.doubleClickCell(8, 1);
        editorPage.getEditorTableActionsPanelComponent().clickRemoveRow();
        saveTableAndCheckNoProblems(editorPage);
        assertThat(table.getRowsCount()).isEqualTo(7);

        editorPage.getEditorToolbarPanelComponent().copyTableAsBusinessDimension("Countries", "FR");
        editorPage.waitUntilSpinnerLoaded();
        assertThat(table.getRow(2).getValue()).isEqualTo(List.of("properties", "country", "FR", "\u00a0"));

        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Decision", "MySmarRule");
        editorPage.getEditorToolbarPanelComponent().removeCurrentTable();
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Decision")
                .checkRulesTableAbsent("Decision", "MySmarRule [county=FR]");

        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Decision", "MySmartLookup");
        editorPage.getProblemsPanelComponent().checkNoProblems();
        assertThat(table.getColumn(1)).isEqualTo(List.of(
                "SmartLookup Double MySmartLookup(Gender gender, Date dateOfBirth, Married status )", "Gender",
                "male", "female"));
        assertThat(table.getColumn(2)).isEqualTo(List.of("01/01/1980 .. 01/01/1990",
                "0.1", "0.2"));
        assertThat(table.getColumn(3)).isEqualTo(List.of("01/01/1990 .. 01/01/2000",
                "0.3", "0.4"));

        table.doubleClickCell(4, 1);
        editorPage.getEditorTableActionsPanelComponent().clickInsertRowAfter();
        table.editCell(5, 1, "male");
        table.editCell(5, 2, "0.5");
        table.editCell(5, 3, "0.6");
        saveTableAndCheckNoProblems(editorPage);
        assertThat(table.getRow(5).getValue()).isEqualTo(List.of("male", "0.5", "0.6"));

        table.doubleClickCell(3, 2);
        editorPage.getEditorTableActionsPanelComponent().clickRemoveColumn();
        saveTableAndCheckNoProblems(editorPage);
        assertThat(table.getColumn(2).get(3)).isEqualTo("0.6");

        editorPage.getEditorToolbarPanelComponent().createDefaultTestTable();
        editorPage.getEditorLeftRulesTreeComponent().checkRulesTablePresent("Test", "MySmartLookupTest");
        assertThat(editorPage.getEditorLeftRulesTreeComponent().getSelectedItemText()).isEqualTo("MySmartLookupTest");
        assertThat(table.getRow(2).getValue()).isEqualTo(List.of("gender", "dateOfBirth", "status", "_res_"));
    }

    private void saveTableAndCheckNoProblems(EditorPage editorPage) {
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();
        editorPage.getProblemsPanelComponent().checkNoProblems();
    }
}
