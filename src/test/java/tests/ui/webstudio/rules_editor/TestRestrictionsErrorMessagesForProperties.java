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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestRestrictionsErrorMessagesForProperties extends BaseTest {

    private static final String PROJECT_NAME = "TestRestrictionsErrorMessagesForProperties";

    @Test
    @TestCaseId("IPBQA-23780")
    @Description("Rules Editor - Verify restrictions error messages for properties in different table types")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testRestrictionsErrorMessagesForProperties() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        repositoryPage.createProject(CreateNewProjectComponent.TabName.EXCEL_FILES,
                PROJECT_NAME, "TestRestrictionsErrorMessagesForProperties.xlsx");

        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);

        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(PROJECT_NAME, PROJECT_NAME);

        Map<List<String>, String[]> testData = getTableTypeWithErrorMessagesMap();

        for (Map.Entry<List<String>, String[]> entry : testData.entrySet()) {
            List<String> catalogAndTable = entry.getKey();
            String[] expectedMessages = entry.getValue();

            String catalog = catalogAndTable.get(0);
            String tableName = catalogAndTable.get(1);

            editorPage.getEditorLeftRulesTreeComponent()
                    .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                    .expandFolderInTree(catalog)
                    .selectItemInFolder(catalog, tableName);

            for (String expectedError : expectedMessages) {
                assertThat(editorPage.getProblemsPanelComponent().isErrorPresent(expectedError))
                        .as("Error message '%s' should be present for table %s/%s", expectedError, catalog, tableName)
                        .isTrue();
            }
        }
    }

    private Map<List<String>, String[]> getTableTypeWithErrorMessagesMap() {
        Map<List<String>, String[]> testData = new HashMap<>();

        testData.put(Arrays.asList("Decision", "CarPrice"), new String[]{
                "Property 'autoType' cannot be defined in 'Decision' table",
                "Property 'datatypePackage' cannot be defined in 'Decision' table",
                "Property 'scope' cannot be defined in 'Decision' table",
                "Property 'precision' cannot be defined in 'Decision' table"
        });

        testData.put(Arrays.asList("Decision", "DriverPremium3"), new String[]{
                "Property 'autoType' cannot be defined in 'Decision' table",
                "Property 'datatypePackage' cannot be defined in 'Decision' table",
                "Property 'scope' cannot be defined in 'Decision' table",
                "Property 'precision' cannot be defined in 'Decision' table"
        });

        testData.put(Arrays.asList("Decision", "DriverPremium4"), new String[]{
                "Property 'autoType' cannot be defined in 'Decision' table",
                "Property 'datatypePackage' cannot be defined in 'Decision' table",
                "Property 'scope' cannot be defined in 'Decision' table",
                "Property 'precision' cannot be defined in 'Decision' table"
        });

        testData.put(Arrays.asList("Spreadsheet", "TotalAssets"), new String[]{
                "Property 'datatypePackage' cannot be defined in 'Spreadsheet' table",
                "Property 'validateDT' cannot be defined in 'Spreadsheet' table",
                "Property 'failOnMiss' cannot be defined in 'Spreadsheet' table",
                "Property 'scope' cannot be defined in 'Spreadsheet' table",
                "Property 'precision' cannot be defined in 'Spreadsheet' table"
        });

        testData.put(Arrays.asList("TBasic", "ValidationPolicy"), new String[]{
                "Property 'validateDT' cannot be defined in 'TBasic' table",
                "Property 'failOnMiss' cannot be defined in 'TBasic' table",
                "Property 'scope' cannot be defined in 'TBasic' table",
                "Property 'precision' cannot be defined in 'TBasic' table",
                "Property 'autoType' cannot be defined in 'TBasic' table",
                "Property 'datatypePackage' cannot be defined in 'TBasic' table"
        });

        testData.put(Arrays.asList("Column Match", "NeedApprovalOf"), new String[]{
                "Property 'validateDT' cannot be defined in 'Column Match' table",
                "Property 'failOnMiss' cannot be defined in 'Column Match' table",
                "Property 'scope' cannot be defined in 'Column Match' table",
                "Property 'precision' cannot be defined in 'Column Match' table",
                "Property 'autoType' cannot be defined in 'Column Match' table",
                "Property 'datatypePackage' cannot be defined in 'Column Match' table"
        });

        testData.put(Arrays.asList("Data", "numbers"), new String[]{
                "Property 'validateDT' cannot be defined in 'Data' table",
                "Property 'failOnMiss' cannot be defined in 'Data' table",
                "Property 'scope' cannot be defined in 'Data' table",
                "Property 'origin' cannot be defined in 'Data' table",
                "Property 'precision' cannot be defined in 'Data' table",
                "Property 'autoType' cannot be defined in 'Data' table",
                "Property 'datatypePackage' cannot be defined in 'Data' table",
                "Property 'active' cannot be defined in 'Data' table",
                "Property 'id' cannot be defined in 'Data' table",
                "Property 'version' cannot be defined in 'Data' table"
        });

        testData.put(Arrays.asList("Run", "DriverPremium5Run"), new String[]{
                "Property 'validateDT' cannot be defined in 'Run' table",
                "Property 'failOnMiss' cannot be defined in 'Run' table",
                "Property 'scope' cannot be defined in 'Run' table",
                "Property 'origin' cannot be defined in 'Run' table",
                "Property 'precision' cannot be defined in 'Run' table",
                "Property 'autoType' cannot be defined in 'Run' table",
                "Property 'datatypePackage' cannot be defined in 'Run' table",
                "Property 'active' cannot be defined in 'Run' table",
                "Property 'id' cannot be defined in 'Run' table",
                "Property 'version' cannot be defined in 'Run' table"
        });

        testData.put(Arrays.asList("Test", "DriverPremium5Test"), new String[]{
                "Property 'validateDT' cannot be defined in 'Test' table",
                "Property 'failOnMiss' cannot be defined in 'Test' table",
                "Property 'scope' cannot be defined in 'Test' table",
                "Property 'origin' cannot be defined in 'Test' table",
                "Property 'autoType' cannot be defined in 'Test' table",
                "Property 'datatypePackage' cannot be defined in 'Test' table",
                "Property 'active' cannot be defined in 'Test' table",
                "Property 'id' cannot be defined in 'Test' table",
                "Property 'version' cannot be defined in 'Test' table"
        });

        testData.put(Arrays.asList("Datatype", "Customer"), new String[]{
                "Property 'validateDT' cannot be defined in 'Datatype' table",
                "Property 'failOnMiss' cannot be defined in 'Datatype' table",
                "Property 'scope' cannot be defined in 'Datatype' table",
                "Property 'origin' cannot be defined in 'Datatype' table",
                "Property 'precision' cannot be defined in 'Datatype' table",
                "Property 'autoType' cannot be defined in 'Datatype' table",
                "Property 'active' cannot be defined in 'Datatype' table",
                "Property 'id' cannot be defined in 'Datatype' table",
                "Property 'version' cannot be defined in 'Datatype' table"
        });

        testData.put(Arrays.asList("Method", "getGreeting"), new String[]{
                "Property 'validateDT' cannot be defined in 'Method' table",
                "Property 'failOnMiss' cannot be defined in 'Method' table",
                "Property 'scope' cannot be defined in 'Method' table",
                "Property 'precision' cannot be defined in 'Method' table",
                "Property 'datatypePackage' cannot be defined in 'Method' table",
                "Property 'autoType' cannot be defined in 'Method' table"
        });

        testData.put(Arrays.asList("Configuration", "Environment"), new String[]{
                "Property 'validateDT' cannot be defined in 'Environment' table",
                "Property 'failOnMiss' cannot be defined in 'Environment' table",
                "Property 'scope' cannot be defined in 'Environment' table",
                "Property 'origin' cannot be defined in 'Environment' table",
                "Property 'precision' cannot be defined in 'Environment' table",
                "Property 'autoType' cannot be defined in 'Environment' table",
                "Property 'datatypePackage' cannot be defined in 'Environment' table",
                "Property 'active' cannot be defined in 'Environment' table",
                "Property 'id' cannot be defined in 'Environment' table",
                "Property 'version' cannot be defined in 'Environment' table"
        });

        return testData;
    }
}
