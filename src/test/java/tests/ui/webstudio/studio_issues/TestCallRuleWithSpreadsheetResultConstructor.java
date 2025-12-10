package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerPool;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import helpers.utils.LogsUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCallRuleWithSpreadsheetResultConstructor extends BaseTest {

    @Test
    @TestCaseId("EPBDS-12238")
    @Description("Test call rule with spreadsheet result constructor")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCallRuleWithSpreadsheetResultConstructor() {
        String projectName = WorkflowService.loginCreateProjectFromExcelFile(User.ADMIN, "TestCallRuleWithSpreadsheetResultConstructor.xlsx");
        EditorPage editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "TestCallRuleWithSpreadsheetResultConstructor");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Test")
                .selectItemInFolder("Test", "test");
        
        // Run the tests
        editorPage.getEditorTableToolbarPanelComponent().clickRun();
        
        // Check that "Something went wrong" error is NOT visible
        assertThat(editorPage.isStudioMessageDisplayed("Sorry! Something went wrong."))
                .as("'Something went wrong' message should not be displayed")
                .isFalse();
        
        // Validate that the test table failed as expected
        assertThat(editorPage.getTestResultValidationComponent().isTestTableFailed())
                .as("Test table '%s' should have failed status", "test")
                .isTrue();
        LogsUtil.inspectLogFile(AppContainerPool.get());
    }
}