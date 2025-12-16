package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.common.TableComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDetermineSpreadsheetResultCellType extends BaseTest {

    private static final String PROJECT_NAME = "TestDetermineSpreadsheetResultCellType";

    @Test
    @TestCaseId("IPBQA-29824")
    @Description("Rules Editor - Verify that cell variable hints show correct data types from SpreadsheetResult")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testDetermineSpreadsheetResultCellType() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        repositoryPage.createProject(CreateNewProjectComponent.TabName.EXCEL_FILES,
                PROJECT_NAME, "TestDetermineSpreadsheetResultCellType.xlsx");

        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);

        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(PROJECT_NAME, PROJECT_NAME);

        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", "mainSpr");

        // Enter table edit mode to see hints
        editorPage.getEditorToolbarPanelComponent().getEditBtn().click();

        TableComponent table = editorPage.getCenterTable();

        checkCellHintContainsLines(table, 4, 2, "$Bla3",
                "SpreadsheetResult",
                "Double in TestSrp2",
                "double in TestSrp",
                "Double $Bla3");

        checkCellHintContainsLines(table, 6, 2, "$Bla2",
                "SpreadsheetResult",
                "int in TestSrp",
                "Integer[] in TestSrp2",
                "Integer[] $Bla2");

        checkCellHintContainsLines(table, 7, 2, "$Bla3",
                "SpreadsheetResult",
                "Double in TestSrp2",
                "double in TestSrp",
                "Double $Bla3");

        checkCellHintContainsLines(table, 8, 2, "$Bla4",
                "SpreadsheetResult",
                "SpreadsheetResultsomeSpr in TestSrp",
                "double in TestSrp2",
                "Object $Bla4");

        checkCellHintContainsLines(table, 9, 2, "$Bla5",
                "SpreadsheetResult",
                "SpreadsheetResultsomeSpr in TestSrp",
                "SpreadsheetResultsomeAnotherSpr in TestSrp2",
                "SRsomeAnotherSpr & SRsomeSpr $Bla5");

        checkCellHintContainsLines(table, 10, 2, "$Bla6",
                "SpreadsheetResult",
                "SpreadsheetResultsomeSpr in TestSrp",
                "null-Class in TestSrp2",
                "SpreadsheetResultsomeSpr $Bla6");

        checkCellHintContainsLines(table, 11, 2, "$Bla7",
                "SpreadsheetResult",
                "MyDatatypeExtended in TestSrp2",
                "MyDatatype1 in TestSrp",
                "MyDatatype1 $Bla7");

        checkCellHintContainsLines(table, 12, 2, "$Bla8",
                "SpreadsheetResult",
                "MyDatatype2 in TestSrp2",
                "MyDatatype1 in TestSrp",
                "Object $Bla8");

        checkCellHintContainsLines(table, 13, 2, "$Bla9",
                "SpreadsheetResult",
                "MyAlias2 in TestSrp2",
                "MyAlias1 in TestSrp",
                "MyAlias2 $Bla9");

        checkCellHintContainsLines(table, 14, 2, "$Bla10",
                "SpreadsheetResult",
                "MyAlias1 in TestSrp",
                "MyAlias4 in TestSrp2",
                "String $Bla10");
    }

    private void checkCellHintContainsLines(TableComponent table,
                                            int rowIndex,
                                            int columnIndex,
                                            String variableName,
                                            String... expectedLines) {
        String hintText = table.getCellHintText(rowIndex, columnIndex, variableName);
        List<String> expectedLinesList = Arrays.asList(expectedLines);

        assertThat(hintText)
                .as("Hint for variable '%s' in cell (%d,%d) should contain all expected lines in any order", variableName, rowIndex, columnIndex)
                .contains(expectedLinesList);
    }
}
