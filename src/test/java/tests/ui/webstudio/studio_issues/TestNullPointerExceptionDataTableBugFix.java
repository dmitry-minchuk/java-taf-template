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

public class TestNullPointerExceptionDataTableBugFix extends BaseTest {

    private static final String TEST_FILE = "file_used_for_table_load.xlsx";
    private static final String TABLE_NAME = "AddressData";
    private static final String EXPECTED_ERROR_PATTERN = "Cannot bind node";
    private static final String UNEXPECTED_ERROR_PATTERN = "NullPointerException";

    @Test
    @TestCaseId("EPBDS-12614")
    @Description("NullPointerException is shown in WebStudio Editor for the data table if incorrect datatype is specified - BUGFIX VERIFICATION")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testNullPointerExceptionBugFix() {
        // Step 1 & 2: Login and create project from Excel file with incorrect datatype
        String projectName = WorkflowService.loginCreateProjectFromExcelFile(User.ADMIN, TEST_FILE);
        EditorPage editorPage = new EditorPage()
                .getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);

        String moduleName = TEST_FILE.replace(".xlsx", "");
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, moduleName);
        // Step 3: Navigate to Data table with incorrect datatype
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Data")
                .selectItemInFolder("Data", TABLE_NAME);

        ProblemsPanelComponent problemsPanel = editorPage.getProblemsPanelComponent();
        problemsPanel.showProblemsPanel();

        // Step 4 & 5: Verify error messages
        List<String> allErrors = problemsPanel.getAllErrors();
        List<String> allWarnings = problemsPanel.getAllWarnings();

        List<String> allMessages = new java.util.ArrayList<>();
        allMessages.addAll(allErrors);
        allMessages.addAll(allWarnings);

        // Assertion 1: There should be errors related to data binding
        assertThat(allErrors.size())
                .as("Should have at least one error for incorrect datatype")
                .isGreaterThan(0);
        // Assertion 2: Verify "Cannot bind node" errors exist
        boolean hasExpectedError = allErrors.stream()
                .anyMatch(error -> error.contains(EXPECTED_ERROR_PATTERN));
        assertThat(hasExpectedError)
                .as("Should contain 'Cannot bind node' error message for incorrect datatype")
                .isTrue();
        // Assertion 3: CRITICAL - Verify NO NullPointerException messages
        boolean hasNullPointerException = allMessages.stream()
                .anyMatch(message -> message.contains(UNEXPECTED_ERROR_PATTERN));
        assertThat(hasNullPointerException)
                .as("NullPointerException should NOT be shown to user (EPBDS-12614 bugfix)")
                .isFalse();
        // Assertion 4: Verify error messages don't contain stack traces
        boolean hasStackTrace = allMessages.stream()
                .anyMatch(message -> message.contains("at ") && message.contains(".java"));
        assertThat(hasStackTrace)
                .as("Error messages should not contain Java stack trace (proper error formatting)")
                .isFalse();
        // Assertion 5: Verify error message contains meaningful information
        String errorMessage = allErrors.stream()
                .filter(error -> error.contains(EXPECTED_ERROR_PATTERN))
                .findFirst()
                .orElse("");
        assertThat(errorMessage)
                .as("Error message should be non-empty and meaningful")
                .isNotEmpty()
                .isNotBlank();
    }
}
