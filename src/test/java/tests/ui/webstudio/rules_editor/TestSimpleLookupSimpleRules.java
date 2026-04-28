package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TableComponent;
import domain.ui.webstudio.components.editortabcomponents.CreateTableDialogComponent;
import domain.ui.webstudio.components.editortabcomponents.EditorToolbarPanelComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSimpleLookupSimpleRules extends BaseTest {

    private static final String EXCEL_FILE = "TestSimpleLookupSimpleRules.xlsx";
    private static final String MODULE_NAME = "TestSimpleLookupSimpleRules";

    @Test
    @TestCaseId("IPBQA-29967")
    @Description("SimpleLookup and SimpleRules tables: open, recreate, run, edit, copy and create test table")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testSimpleLookupSimpleRules() {
        String projectName = WorkflowService.loginCreateProjectFromExcelFile(User.ADMIN, EXCEL_FILE);
        EditorPage editorPage = new EditorPage();
        EditorLeftRulesTreeComponent rulesTree = editorPage.getEditorLeftRulesTreeComponent();

        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, MODULE_NAME);
        rulesTree.setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "SimpleREx1");

        TableComponent table = editorPage.getCenterTable();
        assertThat(table.getColumn(1)).isEqualTo(List.of(
                "SimpleRules Double SimpleREx1(Integer Age, Gender gender)", "Age", "18-30",
                "female", "31-50", "female", "51-60", "female", "61-90", "female"));
        assertThat(table.getColumn(2)).isEqualTo(List.of(
                "Gender", "male", "0.2", "male", "0.4", "male", "0.6", "male", "0.8"));
        assertThat(table.getColumn(3)).isEqualTo(List.of("Result", "0.1", "0.3", "0.5", "0.7"));
        editorPage.getProblemsPanelComponent().checkNoProblems();

        editorPage.getEditorToolbarPanelComponent().removeCurrentTable();
        rulesTree.expandFolderInTree("Decision")
                .checkRulesTableAbsent("Decision", "SimpleREx1");

        editorPage.getEditorToolbarPanelComponent().clickCreateTable();
        CreateTableDialogComponent createTableDialog = editorPage.getCreateTableDialogComponent();
        createTableDialog.selectType("Simple Rules Table")
                .clickNext()
                .setSimpleRulesInitialParameters("SimpleREx1", "Double")
                .addSimpleRulesParameter("Integer", false, "age")
                .addSimpleRulesParameter("Gender", false, "gender")
                .clickNext()
                .addSimpleRule("age", "18-30", 5)
                .setSimpleRule("male", 6)
                .setSimpleRule("0.1", 7)
                .addSimpleRule("age", "18-30", 8)
                .setSimpleRule("female", 9)
                .setSimpleRule("0.2", 10)
                .addSimpleRule("age", "31-60", 11)
                .setSimpleRule("male", 12)
                .setSimpleRule("0.3", 13)
                .addSimpleRule("age", "31-60", 14)
                .setSimpleRule("female", 15)
                .setSimpleRule("0.4", 16)
                .addSimpleRule("age", "61-70", 17)
                .setSimpleRule("0.5", 19)
                .deleteSimpleRuleRow(7)
                .clickNext()
                .save();

        runSimpleRule(editorPage, "20", "female");
        assertThat(editorPage.getTestResultValidationComponent().getTestResult(1))
                .isEqualTo(List.of("1", "20", "female", "0.2"));

        rulesTree.selectItemInFolder("Decision", "SimpleREx1");
        table.doubleClickCell(2, 2);
        editorPage.getEditorTableActionsPanelComponent().clickInsertColumnBefore();
        table.editCell(2, 2, "Status");
        table.editCell(1, 1, "SimpleRules Double SimpleREx1 (Integer age, Marital_Status Status, Gender gender)");
        saveTableAndCheckNoProblems(editorPage);
        table.editCell(3, 2, "Married");
        table.editCell(4, 2, "Divorced");
        table.editCell(5, 2, "Single");
        table.editCell(6, 2, "Married");
        editorPage.getProblemsPanelComponent().checkNoProblems();
        assertThat(table.getColumn(2)).isEqualTo(List.of("Status", "Married", "Divorced", "Single", "Married"));

        table.doubleClickCell(6, 1);
        editorPage.getEditorTableActionsPanelComponent().clickRemoveRow();
        saveTableAndCheckNoProblems(editorPage);
        assertThat(table.getRowsCount()).isEqualTo(5);

        editorPage.getEditorToolbarPanelComponent().copyTableAsBusinessDimension("Countries", "AU");
        editorPage.waitUntilSpinnerLoaded();
        assertThat(rulesTree.getSelectedItemText()).isEqualTo("SimpleREx1 [country=AU]");
        rulesTree.checkRulesTablePresent("Decision", "SimpleREx1");
        assertThat(table.getColumn(1)).isEqualTo(List.of(
                "SimpleRules Double SimpleREx1 (Integer age, Marital_Status Status, Gender gender)",
                "properties", "age", "18-30", "18-30", "31-60"));
        assertThat(table.getColumn(2)).isEqualTo(List.of("country", "Status", "Married", "Divorced", "Single"));
        assertThat(table.getColumn(3)).isEqualTo(List.of("AU", "gender", "male", "female", "male"));
        assertThat(table.getColumn(4)).isEqualTo(List.of("\u00a0", "RETURN", "0.1", "0.2", "0.3"));

        rulesTree.selectItemInFolder("Decision", "SimpleLEx2");
        editorPage.getProblemsPanelComponent().checkNoProblems();
        assertThat(normalizeNonBreakingSpaces(table.getColumn(1))).isEqualTo(List.of(
                "SimpleLookup Double SimpleLEx2 (Gender gender, Marital_Status status)",
                "Gender\\\n                 Marital_Status", "male", "female", " "));
        assertThat(table.getColumn(2)).isEqualTo(List.of("Married", "700", "300", "500"));
        assertThat(table.getColumn(3)).isEqualTo(List.of("Single", "720", "350", "550"));

        runSimpleLookup(editorPage, "female", "Single");
        assertThat(editorPage.getTestResultValidationComponent().getTestResult(1))
                .isEqualTo(List.of("1", "female", "Single", "350"));

        rulesTree.selectItemInFolder("Decision", "SimpleLEx2");
        runSimpleLookup(editorPage, null, "Married");
        assertThat(editorPage.getTestResultValidationComponent().getTestResult(1))
                .isEqualTo(List.of("1", "Empty", "Married", "500"));

        rulesTree.selectItemInFolder("Decision", "SimpleLEx2");
        table.doubleClickCell(4, 1);
        editorPage.getEditorTableActionsPanelComponent().clickInsertRowBefore();
        table.editCell(5, 1, "male");
        table.editCell(5, 2, "700");
        table.editCell(5, 3, "750");
        saveTableAndCheckNoProblems(editorPage);
        assertThat(table.getRow(5).getValue()).isEqualTo(List.of("male", "700", "750"));

        table.doubleClickCell(3, 2);
        editorPage.getEditorTableActionsPanelComponent().clickRemoveColumn();
        saveTableAndCheckNoProblems(editorPage);
        assertThat(table.getColumn(2).get(3)).isEqualTo("750");

        editorPage.getEditorToolbarPanelComponent().createDefaultTestTable();
        rulesTree.checkRulesTablePresent("Test", "SimpleLEx2Test");
        assertThat(rulesTree.getSelectedItemText()).isEqualTo("SimpleLEx2Test");
        assertThat(table.getRow(2).getValue()).isEqualTo(List.of("gender", "status", "_res_"));
    }

    private void runSimpleRule(EditorPage editorPage, String age, String gender) {
        editorPage.getEditorToolbarPanelComponent()
                .clickRun()
                .setInputTextField("1", age)
                .setInputSelectField("1", gender)
                .clickRunInsideMenu();
    }

    private void runSimpleLookup(EditorPage editorPage, String gender, String status) {
        EditorToolbarPanelComponent.IRunMenu runMenu = editorPage.getEditorToolbarPanelComponent().clickRun();
        if (gender != null) {
            runMenu.setInputSelectField("1", gender);
        }
        runMenu.setInputSelectField("2", status)
                .clickRunInsideMenu();
    }

    private void saveTableAndCheckNoProblems(EditorPage editorPage) {
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();
        editorPage.getProblemsPanelComponent().checkNoProblems();
    }

    private List<String> normalizeNonBreakingSpaces(List<String> values) {
        return values.stream()
                .map(value -> value.replace('\u00a0', ' '))
                .toList();
    }
}
