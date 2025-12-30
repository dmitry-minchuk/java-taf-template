package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.createnewproject.ZipArchiveComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.TestDataUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertTrue;

public class TestTableIcons extends BaseTest {

    private static final String PROJECT_NAME = "TestTableIcons";
    private static final String ZIP_FILE_NAME = "TestTableIcons.zip";
    private static final String MODULE_NAME = "All_tables_type";

    private static final List<String> CATALOG_NAMES = Arrays.asList(
            "Decision", "Spreadsheet", "TBasic", "Column Match",
            "Data", "Run", "Test", "Datatype", "Vocabulary", "Method", "Constants"
    );

    private static final Map<String, String> TABLE_NAMES_AND_ICONS = new HashMap<>() {{
        put("SimpleLookupTable", "dt3.png");
        put("SimpleRulesTable", "dt3.png");
        put("SmartLookup1", "dt3.png");
        put("SmartRules1", "dt3.png");
        put("SpreadsheetTable", "spreadsheet.gif");
        put("TBasicTable", "tbasic.gif");
        put("ColumnMatchTable", "cmatch.gif");
        put("DataTable1", "data.gif");
        put("RunTable", "run.gif");
        put("Test1", "test_ok.gif");
        put("Datatype1", "dataobject.gif");
        put("Vocabulary1", "dataobject.gif");
        put("MethodTable", "method.gif");
        put("Constants", "spreadsheet.gif");
    }};

    @Test
    @TestCaseId("IPBQA-25719")
    @Description("Verify that each table type has the correct icon in the rules tree")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testTableIcons() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        // Create project from ZIP file
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, PROJECT_NAME, ZIP_FILE_NAME);

        // Switch to Editor tab and select module
        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(PROJECT_NAME, MODULE_NAME);

        // Expand all catalog folders
        EditorPage finalEditorPage = editorPage;
        finalEditorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE);
        CATALOG_NAMES.forEach(catalog ->
            finalEditorPage.getEditorLeftRulesTreeComponent()
                    .expandFolderInTree(catalog)
        );

        // Verify icons for each table
        TABLE_NAMES_AND_ICONS.forEach((tableName, expectedIcon) -> {
            String actualIconSrc = finalEditorPage.getEditorLeftRulesTreeComponent()
                    .getTableIcon(tableName)
                    .getAttribute("src");
            assertTrue(actualIconSrc.endsWith(expectedIcon),
                    String.format("Table '%s' should have icon '%s', but actual: '%s'",
                            tableName, expectedIcon, actualIconSrc));
        });
    }
}
