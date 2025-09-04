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

public class TestTraceIntoFileJsonRequest extends BaseTest {

    @Test
    @TestCaseId("EPBDS-8215")
    @Description("BUG: Internal Server error appears if user entered JSON request in 'Trace into File'")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testPlaywrightTraceIntoFileJsonRequest() {
        String projectName = WorkflowService.loginCreateProjectFromZip(User.ADMIN, "testTraceIntoFileJsonRequest.zip");
        EditorPage editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "the_nulls_input_parameters");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_EXCEL_SHEET)
                .expandFolderInTree("Sheet1")
                .selectItemInFolder("Sheet1", "mySpreadsheet");

        editorPage.getTableToolbarPanelComponent()
                .clickTrace()
                .selectJSONTrace("{\n  \"myId\": \"string\",\n  \"someArr\": null\n  \"intA\": 0,\n  \"subElement\": null\n  \"someText\": null\n}")
                .clickTraceIntoFile();

        assertThat(editorPage.locator("text=Internal Server Error").isVisible())
                .as("Internal Server Error is shown!")
                .isFalse();
    }
}