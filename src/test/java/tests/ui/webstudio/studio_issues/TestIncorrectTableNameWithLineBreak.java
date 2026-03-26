package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestIncorrectTableNameWithLineBreak extends BaseTest {

    @Test
    @TestCaseId("EPBDS-13819")
    @Description("EPBDS-13819: Table name in tree must show method name, not a fragment from title with line break")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testTableNameCorrectWithLineBreakInTitle() {
        String projectName = WorkflowService.loginCreateProjectFromExcelFile(User.ADMIN, "EPBDS-13819_incorrectNAme.xlsx");
        EditorPage editorPage = new EditorPage();

        String moduleName = "EPBDS-13819_incorrectNAme";
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, moduleName);

        EditorLeftRulesTreeComponent tree = editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision");

        // EPBDS-13819: The table title is "SmartRules String FamilyDeductibleConversion (String \nparam)"
        // with a line break before "param)".
        // Bug: tree showed "param)" instead of "FamilyDeductibleConversion"
        // Fix: tree must show "FamilyDeductibleConversion"
        assertThat(tree.isItemExistsInTree("FamilyDeductibleConversion"))
                .as("EPBDS-13819: Table name should be 'FamilyDeductibleConversion', not a fragment like 'param)'")
                .isTrue();
    }
}
