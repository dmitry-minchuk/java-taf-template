package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestWorkWithDuplicateTables extends BaseTest {

    private static final String NAME_PROJECT_SAME_MODULE = "Error_in_table-same_module";
    private static final String NAME_PROJECT_DIFF_MODULES = "Error_in_table-diff_modules";
    private static final String NAME_PROJECT_WITH_DEPENDENCY = "Error_in_table-project_with_dependency";
    private static final String NAME_PROJECT_DEPENDENT = "Project2";

    @Test
    @TestCaseId("IPBQA-31790")
    @Description("Work With Duplicate Tables - error messages, Run/Trace/Benchmark with WithinCurrentModuleOnly checkboxes")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testWorkWithDuplicateTables() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE,
                NAME_PROJECT_SAME_MODULE, "TestWorkWithDuplicateTables/" + NAME_PROJECT_SAME_MODULE + ".zip");

        // Section 1: Same module duplicate tables — select table with error
        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(NAME_PROJECT_SAME_MODULE, "module_AZ");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "someLookupBig2");
        assertThat(editorPage.getTopProblemsPanelComponent().getText())
                .as("Error message for duplicated table in same module")
                .isEqualTo("Found duplicated table 'SmartLookup Double someLookupBig2( String param1, Integer param2)'.");
        editorPage.getCenterTable().editCell(3, 1, "Param 2");
        assertThat(editorPage.getTopProblemsPanelComponent().getText())
                .as("Error message should persist after editing cell")
                .isEqualTo("Found duplicated table 'SmartLookup Double someLookupBig2( String param1, Integer param2)'.");

        // Section 2: Run/Trace/AvailableTestRuns are absent for table with error
        assertThat(editorPage.getEditorToolbarPanelComponent().isRunButtonVisible())
                .as("Run button should be absent for table with error")
                .isFalse();
        assertThat(editorPage.getEditorToolbarPanelComponent().isTraceButtonVisible())
                .as("Trace button should be absent for table with error")
                .isFalse();
        assertThat(editorPage.getEditorToolbarPanelComponent().isAvailableTestRunsLinkVisible())
                .as("AvailableTestRunsLink should be absent for table with error")
                .isFalse();

        // Section 3: Select duplicate table WITHOUT error, check Run/Trace/Test dropdowns
        editorPage.getEditorLeftRulesTreeComponent().selectSecondItemInFolder("Decision", "someLookupBig2");
        editorPage.getEditorToolbarPanelComponent().clickRun();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyInputArgsChecked())
                .as("WithinCurrentModuleOnly should be unchecked after Run click (same module)")
                .isFalse();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyInputArgsEnabled())
                .as("WithinCurrentModuleOnly should be enabled after Run click (same module)")
                .isTrue();
        editorPage.getEditorToolbarPanelComponent().clickTrace();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyInputArgsChecked())
                .as("WithinCurrentModuleOnly should be unchecked after Trace click (same module)")
                .isFalse();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyInputArgsEnabled())
                .as("WithinCurrentModuleOnly should be enabled after Trace click (same module)")
                .isTrue();
        editorPage.getEditorToolbarPanelComponent().clickTableActionsTestDropdown();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyInputArgsChecked())
                .as("WithinCurrentModuleOnly should be unchecked after TestDropdown click (same module)")
                .isFalse();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyInputArgsEnabled())
                .as("WithinCurrentModuleOnly should be enabled after TestDropdown click (same module)")
                .isTrue();
        assertThat(editorPage.getEditorToolbarPanelComponent().getAvailableTestRunsLinkText())
                .as("AvailableTestRunsLink text for duplicate table without error")
                .isEqualTo("someLookupBig2Test (1 test case)");
        editorPage.getEditorToolbarPanelComponent().clickTableActionsTestBtn();
        editorPage.getTestResultValidationComponent().checkAllTablesPassed();

        // Section 4: Select test table, check RunDropdown/Trace/BenchmarkDropdown with WithinCurrentModuleOnlyTestTables
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Test")
                .selectItemInFolder("Test", "someLookupBig2Test");
        assertThat(editorPage.getTopProblemsPanelComponent().isAbsent())
                .as("No errors should be shown for test table in same module")
                .isTrue();
        editorPage.getEditorToolbarPanelComponent().clickRunDropdown();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyTestTablesChecked())
                .as("WithinCurrentModuleOnlyTestTables should be unchecked after RunDropdown click (same module)")
                .isFalse();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyTestTablesEnabled())
                .as("WithinCurrentModuleOnlyTestTables should be enabled after RunDropdown click (same module)")
                .isTrue();
        editorPage.getEditorToolbarPanelComponent().clickTrace();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyTestTablesChecked())
                .as("WithinCurrentModuleOnlyTestTables should be unchecked after Trace click (same module)")
                .isFalse();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyTestTablesEnabled())
                .as("WithinCurrentModuleOnlyTestTables should be enabled after Trace click (same module)")
                .isTrue();
        editorPage.getEditorToolbarPanelComponent().clickBenchmarkDropdown();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyTestTablesChecked())
                .as("WithinCurrentModuleOnlyTestTables should be unchecked after BenchmarkDropdown click (same module)")
                .isFalse();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyTestTablesEnabled())
                .as("WithinCurrentModuleOnlyTestTables should be enabled after BenchmarkDropdown click (same module)")
                .isTrue();
        editorPage.getEditorToolbarPanelComponent().clickRun();
        editorPage.getTestResultValidationComponent().checkAllTablesPassed();

        // Section 5: Different modules duplicate tables
        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE,
                NAME_PROJECT_DIFF_MODULES, "TestWorkWithDuplicateTables/" + NAME_PROJECT_DIFF_MODULES + ".zip");

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(NAME_PROJECT_DIFF_MODULES, "module_AZ");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "someLookupBig2");
        assertThat(editorPage.getTopProblemsPanelComponent().getText())
                .as("Error message for duplicate table in different modules")
                .isEqualTo("There can be only one active table.");
        editorPage.getCenterTable().editCell(3, 1, "Param 2");
        assertThat(editorPage.getTopProblemsPanelComponent().getText())
                .as("Error message should persist after editing cell (diff modules)")
                .isEqualTo("There can be only one active table.");

        // Section 6: Run/Trace with WithinCurrentModuleOnly checked and disabled (diff modules)
        editorPage.getEditorToolbarPanelComponent().clickRun();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyInputArgsChecked())
                .as("WithinCurrentModuleOnly should be checked (diff modules)")
                .isTrue();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyInputArgsEnabled())
                .as("WithinCurrentModuleOnly should be disabled (diff modules)")
                .isFalse();
        editorPage.getEditorToolbarPanelComponent().clickTrace();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyInputArgsChecked())
                .as("WithinCurrentModuleOnly should be checked after Trace (diff modules)")
                .isTrue();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyInputArgsEnabled())
                .as("WithinCurrentModuleOnly should be disabled after Trace (diff modules)")
                .isFalse();
        var runMenu = editorPage.getEditorToolbarPanelComponent().clickRun();
        runMenu.setInputTextField("1", "a1").setInputTextField("2", "11");
        editorPage.getEditorToolbarPanelComponent().clickRunStartBtn();
        assertThat(editorPage.getTestResultValidationComponent().getResultTable().getCellText(1, 4))
                .as("Run result for someLookupBig2 should be 100")
                .isEqualTo("100");

        // Section 7: Test dropdown for decision table (diff modules) — WithinCurrentModuleOnly checked & disabled
        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Decision", "someLookupBig2");
        editorPage.getEditorToolbarPanelComponent().clickTableActionsTestDropdown();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyInputArgsChecked())
                .as("WithinCurrentModuleOnly should be checked in TestDropdown (diff modules)")
                .isTrue();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyInputArgsEnabled())
                .as("WithinCurrentModuleOnly should be disabled in TestDropdown (diff modules)")
                .isFalse();
        editorPage.getEditorToolbarPanelComponent().clickTableActionsTestBtn();
        editorPage.getTestResultValidationComponent().checkAllTablesPassed();
        assertThat(editorPage.getTestResultValidationComponent().isCurrentModuleOnlyChecked())
                .as("currentModuleOnly checkbox should be checked in test results (diff modules)")
                .isTrue();

        // Section 8: Top Panel Test — WithinCurrentModuleOnly unchecked & enabled, then set and run
        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Decision", "someLookupBig2");
        editorPage.getEditorToolbarPanelComponent().clickTopPanelTestDropdown();
        assertThat(editorPage.getEditorToolbarPanelComponent().isTopPanelWithinCurrentModuleOnlyChecked())
                .as("TopPanel WithinCurrentModuleOnly should be unchecked (diff modules)")
                .isFalse();
        assertThat(editorPage.getEditorToolbarPanelComponent().isTopPanelWithinCurrentModuleOnlyEnabled())
                .as("TopPanel WithinCurrentModuleOnly should be enabled (diff modules)")
                .isTrue();
        editorPage.getEditorToolbarPanelComponent().runAllTests();
        editorPage.getTestResultValidationComponent().checkTestTableFailed("someLookupBig2Test");
        editorPage.getEditorToolbarPanelComponent().clickTopPanelTestDropdown();
        editorPage.getEditorToolbarPanelComponent().setTopPanelWithinCurrentModuleOnly(true);
        editorPage.getEditorToolbarPanelComponent().clickTopPanelRunTestBtn();
        editorPage.getTestResultValidationComponent().checkAllTablesPassed();

        // Section 9: Test table errors (diff modules) — WithinCurrentModuleOnlyTestTables checked & disabled
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Test")
                .selectItemInFolder("Test", "someLookupBig2Test");
        assertThat(editorPage.getTopProblemsPanelComponent().getText())
                .as("Error message for test table referencing duplicated rule (diff modules)")
                .isEqualTo("Tested rules have errors");
        editorPage.getEditorToolbarPanelComponent().clickRunDropdown();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyTestTablesChecked())
                .as("WithinCurrentModuleOnlyTestTables should be checked (diff modules)")
                .isTrue();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyTestTablesEnabled())
                .as("WithinCurrentModuleOnlyTestTables should be disabled (diff modules)")
                .isFalse();
        editorPage.getEditorToolbarPanelComponent().clickTrace();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyTestTablesChecked())
                .as("WithinCurrentModuleOnlyTestTables should be checked after Trace (diff modules)")
                .isTrue();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyTestTablesEnabled())
                .as("WithinCurrentModuleOnlyTestTables should be disabled after Trace (diff modules)")
                .isFalse();
        editorPage.getEditorToolbarPanelComponent().clickBenchmarkDropdown();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyTestTablesChecked())
                .as("WithinCurrentModuleOnlyTestTables should be checked after BenchmarkDropdown (diff modules)")
                .isTrue();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyTestTablesEnabled())
                .as("WithinCurrentModuleOnlyTestTables should be disabled after BenchmarkDropdown (diff modules)")
                .isFalse();
        editorPage.getEditorToolbarPanelComponent().clickRun();
        editorPage.getTestResultValidationComponent().checkAllTablesPassed();

        // Section 10: Project with dependency — duplicate table error across projects
        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE,
                NAME_PROJECT_WITH_DEPENDENCY, "TestWorkWithDuplicateTables/" + NAME_PROJECT_WITH_DEPENDENCY + ".zip");
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE,
                NAME_PROJECT_DEPENDENT, "TestWorkWithDuplicateTables/" + NAME_PROJECT_DEPENDENT + ".zip");

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(NAME_PROJECT_WITH_DEPENDENCY, "module_AZ");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "someLookupBig2");
        assertThat(editorPage.getTopProblemsPanelComponent().getText())
                .as("Error message for duplicate table across projects with dependency")
                .isEqualTo("Method 'someLookupBig2(String param1," +
                        " Integer param2)' is already used in modules 'module_AZ' and 'module_KS' with the same version," +
                        " active status, properties set.");
        editorPage.getCenterTable().editCell(3, 1, "Param 2");
        assertThat(editorPage.getTopProblemsPanelComponent().getText())
                .as("Error message should persist after editing cell (dependency project)")
                .isEqualTo("Method 'someLookupBig2(String param1," +
                        " Integer param2)' is already used in modules 'module_AZ' and 'module_KS' with the same version," +
                        " active status, properties set.");

        // Section 11: Run/Trace/AvailableTestRuns absent for table with error (dependency project)
        assertThat(editorPage.getEditorToolbarPanelComponent().isRunButtonVisible())
                .as("Run button should be absent for table with error (dependency project)")
                .isFalse();
        assertThat(editorPage.getEditorToolbarPanelComponent().isTraceButtonVisible())
                .as("Trace button should be absent for table with error (dependency project)")
                .isFalse();
        assertThat(editorPage.getEditorToolbarPanelComponent().isAvailableTestRunsLinkVisible())
                .as("AvailableTestRunsLink should be absent for table with error (dependency project)")
                .isFalse();

        // Section 12: Test table in dependency project — WithinCurrentModuleOnlyTestTables unchecked & enabled
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Test")
                .selectItemInFolder("Test", "someLookupBig2Test");
        assertThat(editorPage.getTopProblemsPanelComponent().isAbsent())
                .as("No errors should be shown for test table in dependency project")
                .isTrue();
        editorPage.getEditorToolbarPanelComponent().clickRunDropdown();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyTestTablesChecked())
                .as("WithinCurrentModuleOnlyTestTables should be unchecked (dependency project)")
                .isFalse();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyTestTablesEnabled())
                .as("WithinCurrentModuleOnlyTestTables should be enabled (dependency project)")
                .isTrue();
        editorPage.getEditorToolbarPanelComponent().clickTrace();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyTestTablesChecked())
                .as("WithinCurrentModuleOnlyTestTables should be unchecked after Trace (dependency project)")
                .isFalse();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyTestTablesEnabled())
                .as("WithinCurrentModuleOnlyTestTables should be enabled after Trace (dependency project)")
                .isTrue();
        editorPage.getEditorToolbarPanelComponent().clickBenchmarkDropdown();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyTestTablesChecked())
                .as("WithinCurrentModuleOnlyTestTables should be unchecked after BenchmarkDropdown (dependency project)")
                .isFalse();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyTestTablesEnabled())
                .as("WithinCurrentModuleOnlyTestTables should be enabled after BenchmarkDropdown (dependency project)")
                .isTrue();
        editorPage.getEditorToolbarPanelComponent().clickRun();
        editorPage.getTestResultValidationComponent().checkAllTablesPassed();
        editorPage.getEditorToolbarPanelComponent().runAllTests();
        editorPage.getTestResultValidationComponent().checkAllTablesPassed();

        // Section 13: Switch to dependent project — no errors, WithinCurrentModuleOnly unchecked & enabled
        editorPage.getEditorToolbarPanelComponent().selectBreadcrumbModule(NAME_PROJECT_DEPENDENT, "module_KS");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "someLookupBig2");
        assertThat(editorPage.getTopProblemsPanelComponent().isAbsent())
                .as("No errors should be shown for decision table in dependent project")
                .isTrue();
        editorPage.getEditorToolbarPanelComponent().clickRun();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyInputArgsChecked())
                .as("WithinCurrentModuleOnly should be unchecked (dependent project)")
                .isFalse();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyInputArgsEnabled())
                .as("WithinCurrentModuleOnly should be enabled (dependent project)")
                .isTrue();
        editorPage.getEditorToolbarPanelComponent().clickTrace();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyInputArgsChecked())
                .as("WithinCurrentModuleOnly should be unchecked after Trace (dependent project)")
                .isFalse();
        assertThat(editorPage.getEditorToolbarPanelComponent().isWithinCurrentModuleOnlyInputArgsEnabled())
                .as("WithinCurrentModuleOnly should be enabled after Trace (dependent project)")
                .isTrue();
    }
}
