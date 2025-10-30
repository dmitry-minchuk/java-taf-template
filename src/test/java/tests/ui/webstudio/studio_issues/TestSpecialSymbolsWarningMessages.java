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
import helpers.utils.WaitUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for IPBQA-32176 and EPBDS-12618
 * Verifies that warning messages display correctly with special symbols in cells.
 * Tests 13 different special symbol combinations to ensure:
 * - UI is NOT corrupted when cell contains $ symbol (EPBDS-12618 fix verification)
 * - Warning messages display fully with correct symbol values (IPBQA-32176 test case)
 * - No truncation or alternating display of symbols
 * - Formula comparison not mixed with warning message
 */
@TestCaseId("IPBQA-32176")
@Description("Verify warning messages with 13 special symbols - IPBQA-32176 & EPBDS-12618 bugfix verification")
public class TestSpecialSymbolsWarningMessages extends BaseTest {

    private static final String TEST_FILE = "test.xlsx";
    private static final String TABLE_NAME = "mySpr1";

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
        // Step 3: Define 13 special symbol test cases
        List<SymbolTestCase> testCases = defineTestCases();
        // Step 4: Test each special symbol
        ProblemsPanelComponent problemsPanel = editorPage.getProblemsPanelComponent();

        int passCount = 0;

        for (int i = 0; i < testCases.size(); i++) {
            SymbolTestCase testCase = testCases.get(i);
            LOGGER.info("-----------------------------------------------------");
            LOGGER.info("Test Case {}/{}: Testing symbol: '{}'", i + 1, testCases.size(), testCase.symbol);
            LOGGER.info("Description: {}", testCase.description);

            // Select the row with the special symbol from the table
            selectTableRowBySymbol(editorPage, testCase.rowIndex);

            // Trigger validation by waiting for the Problems Panel to update
            WaitUtil.waitForCondition(
                    problemsPanel::hasWarnings,
                    5000,
                    250,
                    "Waiting for warning message to appear for symbol: " + testCase.symbol
            );

            // Get all warning messages
            List<String> warnings = problemsPanel.getAllWarnings();
            // Find the warning related to this symbol
            String relevantWarning = findWarningBySymbol(warnings, testCase.symbol);
            assertThat(relevantWarning)
                    .as("Warning message should be found for symbol '%s'", testCase.symbol)
                    .isNotEmpty();
            // Validate the warning message contains required elements
            assertThat(relevantWarning)
                    .as("Warning message for symbol '%s' should contain 'Warning: Object'", testCase.symbol)
                    .contains("Warning: Object");

            assertThat(relevantWarning)
                    .as("Warning message for symbol '%s' should contain the symbol value", testCase.symbol)
                    .contains("'" + testCase.symbol + "'");

            assertThat(relevantWarning)
                    .as("Warning message for symbol '%s' should NOT contain formula comparison in warning text", testCase.symbol)
                    .doesNotContain("someColor == ");

            assertThat(relevantWarning)
                    .as("Warning message for symbol '%s' should not be truncated", testCase.symbol)
                    .doesNotContain("??");

            // Special check for $ symbols: verify all are visible
            if (testCase.symbol.contains("$")) {
                long expectedDollarCount = testCase.symbol.chars().filter(ch -> ch == '$').count();
                long actualDollarCount = relevantWarning.chars().filter(ch -> ch == '$').count();
                assertThat(actualDollarCount)
                        .as("Warning message for symbol '%s' should contain all dollar signs (expected: %d, found: %d)",
                                testCase.symbol, expectedDollarCount, actualDollarCount)
                        .isGreaterThanOrEqualTo(expectedDollarCount);
            }

            LOGGER.info("✓ PASS: Symbol '{}' warning message is valid and NOT corrupted", testCase.symbol);
            passCount++;

            // Clear warnings before next iteration
            problemsPanel.hideProblemsPanel();
            WaitUtil.sleep(300, "Clearing previous warnings");
            problemsPanel.showProblemsPanel();
            WaitUtil.sleep(300, "Re-opening problems panel");
        }

        // Assert that all tests passed
        assertThat(passCount)
                .as("All 13 special symbol warning messages should display correctly without corruption")
                .isEqualTo(testCases.size());
    }

    private List<SymbolTestCase> defineTestCases() {
        return Arrays.asList(
                // Step 1-3: Basic symbols
                new SymbolTestCase(1, "<>", "Angle brackets"),
                new SymbolTestCase(2, "$", "Single dollar sign"),
                new SymbolTestCase(3, "$$", "Double dollar sign (EPBDS-12618: test alternating display fix)"),

                // Step 4-6: Dollar with angle brackets
                new SymbolTestCase(4, "$$<A>", "Double dollar with angle brackets and letter"),
                new SymbolTestCase(5, "$<S>", "Dollar with angle brackets and letter"),
                new SymbolTestCase(6, "$<I>", "Dollar with angle brackets and letter"),

                // Step 7-10: Dollar with special characters
                new SymbolTestCase(7, "$'", "Dollar with apostrophe"),
                new SymbolTestCase(8, "$&", "Dollar with ampersand"),
                new SymbolTestCase(9, "$`", "Dollar with backtick"),
                new SymbolTestCase(10, "$1", "Dollar with digit"),

                // Step 11-13: Additional combinations
                new SymbolTestCase(11, "$A", "Dollar with letter"),
                new SymbolTestCase(12, "$\\|", "Dollar with backslash and pipe"),
                new SymbolTestCase(13, "\\|", "Backslash with pipe (no dollar)")
        );
    }

    private void selectTableRowBySymbol(EditorPage editorPage, int rowIndex) {
        editorPage.getCenterTable().clickCell(rowIndex, 1);
        WaitUtil.sleep(200, "Waiting for validation to trigger after cell selection");
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

    private static class SymbolTestCase {
        int rowIndex;
        String symbol;
        String description;

        SymbolTestCase(int rowIndex, String symbol, String description) {
            this.rowIndex = rowIndex;
            this.symbol = symbol;
            this.description = description;
        }

        @Override
        public String toString() {
            return String.format("Row %d: '%s' - %s", rowIndex, symbol, description);
        }
    }
}
