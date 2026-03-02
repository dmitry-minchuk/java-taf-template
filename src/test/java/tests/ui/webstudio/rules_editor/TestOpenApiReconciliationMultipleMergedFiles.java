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

public class TestOpenApiReconciliationMultipleMergedFiles extends BaseTest {

    private static final String ZIP_CORPORATE_JSON = "Example 2 - Corporate RatingJSON.zip";
    private static final String ZIP_CORPORATE_YAML = "Example 2 - Corporate RatingYAML.zip";

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
