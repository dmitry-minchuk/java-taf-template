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

public class TestOpenApiErrorMessages extends BaseTest {

    @Test
    @TestCaseId("EPBDS-10789")
    @Description("OpenAPI Reconciliation errors are displayed in Datatype table's inline Problems Panel")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testOpenApiErrorMessageDisplaysInDatatypeTable() {
        String projectName = WorkflowService.loginCreateProjectFromZip(User.ADMIN,
                "StudioIssues.TestOpenApiErrorMessagesDisplaysInDatatypeTable.zip");

        EditorPage editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "sprMessageBug");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Datatype")
                .selectItemInFolder("Datatype", "MyDatatype");

        assertThat(editorPage.getEditorMainContentProblemsPanelComponent().getErrorMessages())
                .as("OpenAPI Reconciliation errors should be displayed in the Datatype table's problems panel")
                .contains(
                        "OpenAPI Reconciliation: Expected non transient field for schema property 'field1' is not found in type 'MyDatatype'.",
                        "OpenAPI Reconciliation: Unexpected field 'field12' is found in type 'MyDatatype'."
                );
    }
}
