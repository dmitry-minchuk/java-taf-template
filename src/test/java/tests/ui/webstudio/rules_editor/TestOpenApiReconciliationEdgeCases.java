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

/*
 * Covered atomic tests:
 *   EPBDS-13215 - OpenAPI reconciliation: Datatype errors, dependent project, spreadsheet cell errors
 *   IPBQA-30970 - OpenAPI reconciliation with multiple merged files (JSON + YAML)
 */
public class TestOpenApiReconciliationEdgeCases extends BaseTest {

    private static final String ZIP_RECONCILIATION = "openApiReconciliationFeature.zip";
    private static final String ZIP_ARR_VALIDATION_DATATYPE = "ArrValidation_Datatype.zip";
    private static final String ZIP_ARR_VALIDATION_123 = "ArrValidation123.zip";
    private static final String ZIP_OPEN_API_BBB = "openApiBBB.zip";
    private static final String ZIP_CORPORATE_JSON = "Example 2 - Corporate RatingJSON.zip";
    private static final String ZIP_CORPORATE_YAML = "Example 2 - Corporate RatingYAML.zip";

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
                        "  \"$ref\" : \"#/components/schemas/parentDatatype1\",\n" +
                        "  \"exampleSetFlag\" : false\n" +
                        "}");
    }

    @Test
    @TestCaseId("EPBDS-13215")
    @Description("OpenAPI reconciliation: Dependent project - errors propagate from dependency to dependent project")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testOpenApiReconciliationDependentProject() {
        String projectName2 = "TestOpenApiArrDT_" + System.currentTimeMillis();
        String projectName3 = "TestOpenApiArr123_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, projectName2, ZIP_ARR_VALIDATION_DATATYPE);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, projectName3, ZIP_ARR_VALIDATION_123);

        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(projectName3);

        editorPage.openManageDependenciesDialog().addDependency(projectName2, true);
        editorPage.waitUntilSpinnerLoaded();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName3, "ArrValidation");

        assertThat(editorPage.getProblemsPanelComponent().isErrorPresent("OpenAPI Reconciliation: Unexpected field 'someField' is found in type 'MyDatatype'."))
                .as("Error about unexpected field 'someField' should be present in Problems Panel")
                .isTrue();
        assertThat(editorPage.getProblemsPanelComponent().isErrorPresent("OpenAPI Reconciliation: Unexpected method 'myRule2' is found for path '/myRule2'."))
                .as("Error about unexpected method 'myRule2' should be present in Problems Panel")
                .isTrue();
        assertThat(editorPage.getProblemsPanelComponent().getErrorsCount())
                .as("Problems Panel should contain exactly 2 errors")
                .isEqualTo(2);
    }

    @Test
    @TestCaseId("EPBDS-13215")
    @Description("OpenAPI reconciliation: Spreadsheet cell type errors - cell type mismatch with OpenAPI schema")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testOpenApiReconciliationSpreadsheetErrors() {
        String projectName = "TestOpenApiSpreadsheet_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, projectName, ZIP_OPEN_API_BBB);

        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Algorithm");

        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Spreadsheet")
                .expandFolderInTree("MySpr")
                .selectItemInFolder("MySpr", "MySpr [region=NCSA]");

        List<String> ncsaErrors = editorPage.getEditorMainContentProblemsPanelComponent().getErrorMessages();
        assertThat(ncsaErrors)
                .as("MySpr [region=NCSA] should have cell type mismatch error for $Step5")
                .containsExactly("OpenAPI Reconciliation: Type of cell '$Step5' must be compatible with OpenAPI type 'integer(int32)' that incompatible with actual schema 'object'.");

        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Spreadsheet", "MySpr");

        List<String> mysprErrors = editorPage.getEditorMainContentProblemsPanelComponent().getErrorMessages();
        assertThat(mysprErrors)
                .as("MySpr should have cell type mismatch error for $Step6")
                .containsExactly("OpenAPI Reconciliation: Type of cell '$Step6' must be compatible with OpenAPI type 'number(double)' that incompatible with actual schema 'object'.");

        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Spreadsheet", "MySpr2d");

        List<String> myspr2dErrors = editorPage.getEditorMainContentProblemsPanelComponent().getErrorMessages();
        assertThat(myspr2dErrors)
                .as("MySpr2d should contain both spreadsheet cell type errors")
                .contains(
                        "OpenAPI Reconciliation: Type of cell '$Calc2 Hello$Step5' must be compatible with OpenAPI type 'number' that incompatible with actual type 'string'.",
                        "OpenAPI Reconciliation: Type of cell '$Calc1$Step3' must be compatible with OpenAPI type 'string' that incompatible with actual type 'number(double)'."
                );
        assertThat(myspr2dErrors).hasSize(2);
    }

    @Test
    @TestCaseId("IPBQA-30970")
    @Description("OpenAPI reconciliation: Multiple merged JSON and YAML files - spreadsheet cells and datatype fields not found")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testOpenApiReconciliationMultipleMergedFiles() {
        String jsonProjectName = "TestOpenApiCorporateJSON_" + System.currentTimeMillis();
        String yamlProjectName = "TestOpenApiCorporateYAML_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, jsonProjectName, ZIP_CORPORATE_JSON);

        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(jsonProjectName, "Corporate Rating");

        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", "CorporateRatingCalculation");

        List<String> jsonSpreadsheetErrors = editorPage.getEditorMainContentProblemsPanelComponent().getErrorMessages();
        assertThat(jsonSpreadsheetErrors)
                .as("JSON project: CorporateRatingCalculation should have 2 missing cell errors")
                .containsExactlyInAnyOrder(
                        "OpenAPI Reconciliation: Expected non transient cell for schema property 'Description_RiskOfOperations' is not found.",
                        "OpenAPI Reconciliation: Expected non transient cell for schema property 'Value_RiskOfOperations' is not found."
                );

        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Datatype")
                .selectItemInFolder("Datatype", "Corporate");

        List<String> jsonCorporateErrors = editorPage.getEditorMainContentProblemsPanelComponent().getErrorMessages();
        assertThat(jsonCorporateErrors)
                .as("JSON project: Corporate datatype should have missing field 'ownership' error")
                .containsExactly("OpenAPI Reconciliation: Expected non transient field for schema property 'ownership' is not found in type 'Corporate'.");

        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Datatype", "FinancialData");

        List<String> jsonFinancialErrors = editorPage.getEditorMainContentProblemsPanelComponent().getErrorMessages();
        assertThat(jsonFinancialErrors)
                .as("JSON project: FinancialData datatype should have missing field 'inventory' error")
                .containsExactly("OpenAPI Reconciliation: Expected non transient field for schema property 'inventory' is not found in type 'FinancialData'.");

        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, yamlProjectName, ZIP_CORPORATE_YAML);

        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(yamlProjectName, "Corporate Rating");

        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", "CorporateRatingCalculation");

        List<String> yamlSpreadsheetErrors = editorPage.getEditorMainContentProblemsPanelComponent().getErrorMessages();
        assertThat(yamlSpreadsheetErrors)
                .as("YAML project: CorporateRatingCalculation should have 2 missing cell errors")
                .containsExactlyInAnyOrder(
                        "OpenAPI Reconciliation: Expected non transient cell for schema property 'Description_RiskOfOperations' is not found.",
                        "OpenAPI Reconciliation: Expected non transient cell for schema property 'Value_RiskOfOperations' is not found."
                );

        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Datatype")
                .selectItemInFolder("Datatype", "Corporate");

        List<String> yamlCorporateErrors = editorPage.getEditorMainContentProblemsPanelComponent().getErrorMessages();
        assertThat(yamlCorporateErrors)
                .as("YAML project: Corporate datatype should have missing field 'ownership' error")
                .containsExactly("OpenAPI Reconciliation: Expected non transient field for schema property 'ownership' is not found in type 'Corporate'.");

        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Datatype", "QualityIndicators");

        List<String> yamlQualityErrors = editorPage.getEditorMainContentProblemsPanelComponent().getErrorMessages();
        assertThat(yamlQualityErrors)
                .as("YAML project: QualityIndicators datatype should have missing field 'isAnyCredits' error")
                .containsExactly("OpenAPI Reconciliation: Expected non transient field for schema property 'isAnyCredits' is not found in type 'QualityIndicators'.");
    }
}
