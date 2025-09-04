package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerPool;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.editortabcomponents.TestResultValidationComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import helpers.utils.LogsUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestClassCastException extends BaseTest {

    @Test
    @TestCaseId("EPBDS-7018")
    @Description("Test that ClassCastException doesn't occur when running a spreadsheet and validate result table appears")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testClassCastException() {
        String projectName = WorkflowService.loginCreateProjectFromExcelFile(User.ADMIN,
                "TestClassCastException.xlsx");
        EditorPage editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "TestClassCastException");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", "calc");

        editorPage.getTableToolbarPanelComponent().clickRun();
        
        // Check that "Something went wrong" error is NOT visible
        assertThat(editorPage.isStudioMessageDisplayed("Sorry! Something went wrong."))
                .as("'Something went wrong' message should not be displayed")
                .isFalse();
        
        // Validate result table presence and header
        TestResultValidationComponent resultComponent = editorPage.getTestResultValidationComponent();
        assertThat(resultComponent.getResultTable().isVisible(500))
                .as("Result table should be present after running spreadsheet")
                .isTrue();
        
        assertThat(resultComponent.getResultTableHeader())
                .as("Result table header should contain 'Result'")
                .contains("Result");

        LogsUtil.inspectLogFile(AppContainerPool.get());
    }
}