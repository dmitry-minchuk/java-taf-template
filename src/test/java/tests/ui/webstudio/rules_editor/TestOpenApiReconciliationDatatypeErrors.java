package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOpenApiReconciliationDatatypeErrors extends BaseTest {

    private static final String ZIP_RECONCILIATION = "openApiReconciliationFeature.zip";

    @Test
    @TestCaseId("EPBDS-13215")
    @Description("OpenAPI reconciliation: Datatype validation errors - unexpected fields, type incompatibilities, circular types, parent mismatch")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testOpenApiReconciliationDatatypeErrors() {
        String projectName = "TestOpenApiReconciliationDT_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, projectName, ZIP_RECONCILIATION);

        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "validationTest");

        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Datatype")
                .selectItemInFolder("Datatype", "MyDatatype");

        List<String> myDatatypeErrors = editorPage.getEditorMainContentProblemsPanelComponent().getErrorMessages();
        assertThat(myDatatypeErrors)
                .as("MyDatatype should contain all expected reconciliation errors")
                .contains(
                        "OpenAPI Reconciliation: Expected non transient field for schema property 'field17' is not found in type 'MyDatatype'.",
                        "OpenAPI Reconciliation: Type of field 'field28' in type 'MyDatatype' must be compatible with OpenAPI type 'integer(int32)' that incompatible with actual schema 'object'.",
                        "OpenAPI Reconciliation: Expected non transient field for schema property 'field29' is not found in type 'MyDatatype'.",
                        "OpenAPI Reconciliation: Type of field 'field1' in type 'MyDatatype' must be compatible with OpenAPI type 'integer(int32)' that incompatible with actual type 'integer(int64)'.",
                        "OpenAPI Reconciliation: Type of field 'field10' in type 'MyDatatype' must be compatible with OpenAPI type 'integer(int64)' that incompatible with actual type 'boolean'.",
                        "OpenAPI Reconciliation: Type of field 'field16' in type 'MyDatatype' must be compatible with OpenAPI type 'string' that incompatible with actual schema 'MyDatatype1'.",
                        "OpenAPI Reconciliation: Type of field 'field9' in type 'MyDatatype' must be compatible with OpenAPI type 'boolean' that incompatible with actual type 'integer(int32)'.",
                        "OpenAPI Reconciliation: Type of field 'field3' in type 'MyDatatype' must be compatible with OpenAPI type 'number(float)' that incompatible with actual type 'string(date-time)'.",
                        "OpenAPI Reconciliation: Unexpected field 'field171' is found in type 'MyDatatype'."
                );

        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Datatype", "CircleDatatype3");

        List<String> circleErrors = editorPage.getEditorMainContentProblemsPanelComponent().getErrorMessages();
        assertThat(circleErrors)
                .as("CircleDatatype3 should contain exactly 1 circular type error")
                .containsExactly("OpenAPI Reconciliation: Type of field 'anotheField2' in type 'CircleDatatype3' must be compatible with OpenAPI schema 'CircleDatatype3' that incompatible with actual schema 'CircleDatatype1'.");

        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Datatype", "childDatatype2");

        List<String> childErrors = editorPage.getEditorMainContentProblemsPanelComponent().getErrorMessages();
        assertThat(childErrors)
                .as("childDatatype2 should contain exactly 1 parent mismatch error")
                .containsExactly("OpenAPI Reconciliation: Parent 'MyDatatype3' of type 'childDatatype2' mismatches to declared schema:\n" +
                        "{\n" +
                        "  \"$ref\" : \"#/components/schemas/parentDatatype1\"\n" +
                        "}");
    }
}
