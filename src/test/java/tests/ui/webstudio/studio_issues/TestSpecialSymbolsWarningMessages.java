package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.ProblemsPanelComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestCaseId("IPBQA-32176")
@Description("Verify warning messages with 13 special symbols - IPBQA-32176 & EPBDS-12618 bugfix verification")
public class TestSpecialSymbolsWarningMessages extends BaseTest {

    private static final String TEST_FILE = "test.xlsx";
    private static final String TABLE_NAME = "mySpr1";
    private static final String[] expectedSymbols = {"<>", "$", "$$", "$$<A>", "$<S>", "$<I>", "$'", "$&", "$`", "$1", "$A", "$|", "|"};

    @Test
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testSpecialSymbolsWarningMessages() {
        // Step 1: Login and open test project
        String projectName = WorkflowService.loginCreateProjectFromExcelFile(User.ADMIN, TEST_FILE);
        EditorPage editorPage = new EditorPage().getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        String moduleName = TEST_FILE.replace(".xlsx", "");
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, moduleName);

        // Step 2: Navigate to Data table
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", TABLE_NAME);

        // Step 3: Get all warnings from Problems Panel (they are already loaded after project compilation)
        ProblemsPanelComponent problemsPanel = editorPage.getProblemsPanelComponent();
        List<String> allWarnings = problemsPanel.getAllWarnings();

        // Step 5: Validate all 13 symbols are present in warnings with correct format
        for (String symbol : expectedSymbols) {
            LOGGER.info("Validating symbol: '{}'", symbol);

            // Find warning containing this symbol
            String relevantWarning = findWarningBySymbol(allWarnings, symbol);

            assertThat(relevantWarning)
                    .as("Warning message should be found for symbol '%s'", symbol)
                    .isNotEmpty();

            // Validate warning contains required elements
            assertThat(relevantWarning)
                    .as("Warning message for symbol '%s' should contain 'Warning: Object'", symbol)
                    .contains("Warning: Object");

            assertThat(relevantWarning)
                    .as("Warning message for symbol '%s' should contain the symbol value", symbol)
                    .contains("'" + symbol + "'");

            assertThat(relevantWarning)
                    .as("Warning message for symbol '%s' should NOT contain formula comparison in warning text (EPBDS-12618 bugfix)", symbol)
                    .doesNotContain("someColor == ");

            assertThat(relevantWarning)
                    .as("Warning message for symbol '%s' should NOT contain ternary operator formula ?0:1 (EPBDS-12618 bugfix)", symbol)
                    .doesNotContain("?0:1");

            assertThat(relevantWarning)
                    .as("Warning message for symbol '%s' should not be truncated", symbol)
                    .doesNotContain("??");

            // Special check for $ symbols: verify all are visible
            if (symbol.contains("$")) {
                long expectedDollarCount = symbol.chars().filter(ch -> ch == '$').count();
                long actualDollarCount = relevantWarning.chars().filter(ch -> ch == '$').count();
                assertThat(actualDollarCount)
                        .as("Warning message for symbol '%s' should contain all dollar signs (expected: %d, found: %d)",
                                symbol, expectedDollarCount, actualDollarCount)
                        .isGreaterThanOrEqualTo(expectedDollarCount);
            }
        }
    }


    private String findWarningBySymbol(List<String> warnings, String symbol) {
        return warnings.stream()
                .filter(warning -> warning.contains("'" + symbol + "'"))
                .findFirst()
                .orElseGet(() -> {
                    LOGGER.debug("No warning found with symbol in quotes, searching for symbol anywhere in message");
                    return warnings.stream()
                            .filter(warning -> warning.contains(symbol) && warning.contains("Warning: Object"))
                            .findFirst()
                            .orElse("");
                });
    }

}
