package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOrderingModeTableList extends BaseTest {

    @Test
    @TestCaseId("IPBQA-32507")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testTableListOrdering() {
        String projectName = WorkflowService.loginCreateProjectFromExcelFile(User.ADMIN, "sortingtesting.xlsx");
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "sortingtesting");

        // Verify default filter (changed from "By Type" to "By Excel Sheet" in EPBDS-13592)
        assertThat(editorPage.getEditorLeftRulesTreeComponent().getViewFilterValue())
                .containsIgnoringCase("By Excel Sheet");

        // Switch to "By Excel Sheet" and verify categories
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_EXCEL_SHEET);
        assertThat(editorPage.getEditorLeftRulesTreeComponent().getCategoriesVisible())
                .isEqualTo(List.of("Sheet1", "Asheet", "すsupersheet"));

        // Expand folders and verify leaf node ordering
        editorPage.getEditorLeftRulesTreeComponent().expandFolderInTree("Sheet1");
        editorPage.getEditorLeftRulesTreeComponent().expandFolderInTree("Asheet");
        List<String> nodesNames = editorPage.getEditorLeftRulesTreeComponent().getAllEndNodesNames();
        assertThat(nodesNames).containsSequence(List.of("_MyRules2", "MyRules1", "MyRules1"));
        assertThat(nodesNames).containsSequence(List.of("тест123", "はsomeRules", "_someRules", "étudiantomeRules", "トsomeRules"));

        // Edit table _MyRules2: add a row to change ordering
        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Sheet1", "_MyRules2");
        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().clickCell(4, 2);
        editorPage.getEditorTableActionsPanelComponent().clickInsertRowAfter();
        editorPage.getCenterTable().editCell(5, 1, "1");
        editorPage.getCenterTable().editCell(5, 2, "1");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete();

        // Verify ordering changed after edit
        nodesNames = editorPage.getEditorLeftRulesTreeComponent().getAllEndNodesNames();
        assertThat(nodesNames).containsSequence(List.of("MyRules1", "MyRules1", "_MyRules2"));

        // Create new Datatype table in "Asheet" category
        editorPage.getEditorToolbarPanelComponent().clickCreateTable();
        editorPage.getCreateTableDialogComponent()
                .selectType("Datatype Table")
                .clickNext()
                .setTechnicalName("NewDatatype")
                .addParameter("", "textField")
                .setCategorySelection("Asheet")
                .save();

        // Verify ordering includes new table
        nodesNames = editorPage.getEditorLeftRulesTreeComponent().getAllEndNodesNames();
        assertThat(nodesNames).containsSequence(List.of("тест123", "はsomeRules", "_someRules", "étudiantomeRules", "トsomeRules", "NewDatatype"));

        // Remove table はsomeRules and verify ordering update
        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Asheet", "はsomeRules");
        editorPage.getEditorToolbarPanelComponent().removeCurrentTable();
        editorPage.getEditorLeftRulesTreeComponent().expandFolderInTree("Asheet");
        nodesNames = editorPage.getEditorLeftRulesTreeComponent().getAllEndNodesNames();
        assertThat(nodesNames).containsSequence(List.of("тест123", "_someRules", "étudiantomeRules", "トsomeRules", "NewDatatype"));
    }

    @Test
    @TestCaseId("IPBQA-32507")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testTableListOrdering2() {
        String projectName = WorkflowService.loginCreateProjectFromExcelFile(User.ADMIN, "sortingtesting1.xlsx");
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "sortingtesting1");

        // Switch to "By Excel Sheet" filter
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_EXCEL_SHEET);
        editorPage.getEditorLeftRulesTreeComponent().expandFolderInTree("Sheet1");

        // Verify ordering with utility tables hidden (default)
        List<String> nodesNames = editorPage.getEditorLeftRulesTreeComponent().getAllEndNodesNames();
        assertThat(nodesNames).containsSequence(List.of("_MyRules", "MyRules", "MyRules", "Atable"));

        // Disable "Hide Utility Tables" in advanced filter
        editorPage.getEditorLeftRulesTreeComponent().setAdvancedFilter(false);

        // Verify ordering with utility tables visible
        nodesNames = editorPage.getEditorLeftRulesTreeComponent().getAllEndNodesNames();
        assertThat(nodesNames).containsSequence(List.of("_MyRules", "Test123", "MyRules", "MyRules", "Test123", "Atable"));
    }
}
