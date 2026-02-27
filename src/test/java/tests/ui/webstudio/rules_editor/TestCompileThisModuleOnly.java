package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.EditModuleDialogComponent;
import domain.ui.webstudio.components.editortabcomponents.EditorToolbarPanelComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.WaitUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCompileThisModuleOnly extends BaseTest {

    private static final String NAME_MY_PROJECT = "MyProject";
    private static final String NAME_TEST3_PROJECT = "TestProject3";
    private static final String NAME_EXAMPLE3_PROJECT = "Ex3TestCompileThisModuleOnly";
    private static final String NAME_TEST2_PROJECT = "TestProject2";
    private static final String NAME_SAMPLE2_PROJECT = "SampleProject2";

    private void openModuleVerifyCompileThisModuleOnlyCheckedAndDisabled(EditorPage editorPage, String moduleName) {
        editorPage.getProjectDetailsComponent().openEditModuleDialog(moduleName);
        EditModuleDialogComponent editDialog = editorPage.getEditModuleDialogComponent();
        editDialog.waitForDialogToAppear();
        assertThat(editDialog.isCompileThisModuleOnlyVisible())
                .as("compileThisModuleOnly should be present").isTrue();
        assertThat(editDialog.isCompileThisModuleOnlyDisabled())
                .as("compileThisModuleOnly should be disabled").isTrue();
        assertThat(editDialog.isCompileThisModuleOnlyChecked())
                .as("compileThisModuleOnly should be checked").isTrue();
        editDialog.clickClose();
    }

    private void verifyThatGlobalTestIsOneAndRun(EditorPage editorPage) {
        EditorToolbarPanelComponent toolbar = editorPage.getEditorToolbarPanelComponent();
        assertThat(toolbar.getTestButtonText())
                .as("Test button should show 'Test 1'").isEqualTo("Test 1");
        toolbar.runAllTests();
        editorPage.getTestResultValidationComponent().checkAllTablesPassed();
        assertThat(editorPage.getTestResultValidationComponent().countTestTables())
                .as("Should have exactly 1 test table").isEqualTo(1);
    }

    @Test
    @TestCaseId("IPBQA-31895")
    @Description("Compile This Module Only - checkbox behavior, Run/Trace/Benchmark, target table, module-only constraints")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCompileThisModuleOnly() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, NAME_EXAMPLE3_PROJECT, "Example 3 - Auto Policy Calculation");

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(NAME_EXAMPLE3_PROJECT);
        editorPage.getProjectDetailsComponent().openEditModuleDialog("AutoPolicyCalculation");
        EditModuleDialogComponent editDialog = editorPage.getEditModuleDialogComponent();
        editDialog.waitForDialogToAppear();
        assertThat(editDialog.isCompileThisModuleOnlyAbsent())
                .as("compileThisModuleOnly should be absent for single-module project").isTrue();
        editDialog.clickClose();

        //2
        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, NAME_MY_PROJECT, "TestCompileThisModuleOnly_MyProject.zip");
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, NAME_TEST3_PROJECT, "TestProject3.zip");

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(NAME_TEST3_PROJECT);
        openModuleVerifyCompileThisModuleOnlyCheckedAndDisabled(editorPage, "TestSomeState2");

        //2.1
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(NAME_TEST3_PROJECT, "TestSomeState");
        EditorToolbarPanelComponent toolbar = editorPage.getEditorToolbarPanelComponent();
        assertThat(editorPage.getProblemsPanelComponent().isCompilationProgressBarVisible())
                .as("Compilation progress bar should be absent").isFalse();

        editorPage.getProjectDetailsComponent().openEditModuleDialog("TestSomeState");
        editDialog = editorPage.getEditModuleDialogComponent();
        editDialog.waitForDialogToAppear();
        assertThat(editDialog.isCompileThisModuleOnlyVisible())
                .as("compileThisModuleOnly should be present").isTrue();
        assertThat(editDialog.isCompileThisModuleOnlyDisabled())
                .as("compileThisModuleOnly should be disabled").isTrue();
        assertThat(editDialog.isCompileThisModuleOnlyChecked())
                .as("compileThisModuleOnly should be checked").isTrue();
        editDialog.clickClose();

        //2.2
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Test")
                .selectItemInFolder("Test", "SmartRule1Test");
        toolbar = editorPage.getEditorToolbarPanelComponent();
        toolbar.clickRun().clickRunInsideMenu();
        editorPage.getTestResultValidationComponent().checkAllTablesPassed();

        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Test", "SmartRule1Test");
        EditorToolbarPanelComponent.ITraceWindow traceWindow = toolbar.clickTraceExpectTraceWindow();
        traceWindow.expandItemInTree(0);
        assertThat(traceWindow.getVisibleItemsFromTree())
                .as("Trace should contain 'Empty'").anyMatch(item -> item.contains("Empty"));
        traceWindow.close();

        toolbar.clickBenchmark();
        WaitUtil.sleep(2000, "Waiting for benchmark results");

        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Test", "SmartRule1Test");
        editorPage.getProblemsPanelComponent().checkNoProblems();
        assertThat(toolbar.isTargetTableVisible())
                .as("Target table link should be present").isTrue();
        assertThat(toolbar.getTargetTableText())
                .as("Target table should be SmartRule1").isEqualTo("SmartRule1");

        //2.3
        verifyThatGlobalTestIsOneAndRun(editorPage);
        assertThat(editorPage.getTestResultValidationComponent().isCurrentModuleOnlyChecked())
                .as("currentModuleOnly should be checked").isTrue();
        assertThat(editorPage.getTestResultValidationComponent().isCurrentModuleOnlyEnabled())
                .as("currentModuleOnly should be disabled").isFalse();

        //2.4
        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Test", "SmartRule1Test");
        toolbar = editorPage.getEditorToolbarPanelComponent();
        toolbar.clickRun();
        assertThat(toolbar.isWithinCurrentModuleOnlyTestTablesChecked())
                .as("WithinCurrentModuleOnlyTestTables should be checked for Run").isTrue();
        assertThat(toolbar.isWithinCurrentModuleOnlyTestTablesEnabled())
                .as("WithinCurrentModuleOnlyTestTables should be disabled for Run").isFalse();

        toolbar.clickTrace();
        assertThat(toolbar.isWithinCurrentModuleOnlyTestTablesChecked())
                .as("WithinCurrentModuleOnlyTestTables should be checked for Trace").isTrue();
        assertThat(toolbar.isWithinCurrentModuleOnlyTestTablesEnabled())
                .as("WithinCurrentModuleOnlyTestTables should be disabled for Trace").isFalse();

        toolbar.clickBenchmark();
        assertThat(toolbar.isWithinCurrentModuleOnlyTestTablesChecked())
                .as("WithinCurrentModuleOnlyTestTables should be checked for Benchmark").isTrue();
        assertThat(toolbar.isWithinCurrentModuleOnlyTestTablesEnabled())
                .as("WithinCurrentModuleOnlyTestTables should be disabled for Benchmark").isFalse();

        toolbar.clickTopPanelTestDropdown();
        assertThat(toolbar.isTopPanelWithinCurrentModuleOnlyChecked())
                .as("TopPanel WithinCurrentModuleOnly should be checked").isTrue();
        assertThat(toolbar.isTopPanelWithinCurrentModuleOnlyEnabled())
                .as("TopPanel WithinCurrentModuleOnly should be disabled").isFalse();

        //2.5
        toolbar.selectBreadcrumbModule(NAME_TEST3_PROJECT, "TestSomeState2");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Test")
                .selectItemInFolder("Test", "SmartRule2Test");
        toolbar = editorPage.getEditorToolbarPanelComponent();
        toolbar.clickRun().clickRunInsideMenu();
        editorPage.getTestResultValidationComponent().checkAllTablesPassed();

        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Test", "SmartRule2Test");
        traceWindow = toolbar.clickTraceExpectTraceWindow();
        traceWindow.expandItemInTree(0);
        assertThat(traceWindow.getVisibleItemsFromTree())
                .as("Trace should contain 'Empty'").anyMatch(item -> item.contains("Empty"));
        traceWindow.close();

        toolbar.clickBenchmark();
        WaitUtil.sleep(2000, "Waiting for benchmark results");

        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Test", "SmartRule2Test");
        editorPage.getProblemsPanelComponent().checkNoProblems();
        assertThat(toolbar.isTargetTableVisible())
                .as("Target table link should be present").isTrue();
        assertThat(toolbar.getTargetTableText())
                .as("Target table should be SmartRule2").isEqualTo("SmartRule2");

        //2.6
        verifyThatGlobalTestIsOneAndRun(editorPage);

        //2.7
        toolbar.selectBreadcrumbModule(NAME_TEST3_PROJECT, "TestSomeState3");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Test")
                .selectItemInFolder("Test", "SmartRule2Test");
        toolbar = editorPage.getEditorToolbarPanelComponent();
        assertThat(editorPage.getProblemsPanelComponent().isCompilationProgressBarVisible())
                .as("Compilation progress bar should be absent").isFalse();
        assertThat(toolbar.isRunButtonVisible()).as("Run button should be present").isTrue();
        assertThat(toolbar.isTraceButtonVisible()).as("Trace button should be present").isTrue();
        assertThat(toolbar.isBenchmarkButtonVisible()).as("Benchmark button should be present").isTrue();
        assertThat(toolbar.getTargetTableText()).as("Target table should be SmartRule2").isEqualTo("SmartRule2");
        editorPage.getProblemsPanelComponent().checkNoProblems();

        //2.8
        verifyThatGlobalTestIsOneAndRun(editorPage);

        //2.9
        toolbar.selectBreadcrumbModule(NAME_TEST3_PROJECT, "TestSomeState2");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Test")
                .selectItemInFolder("Test", "SmartRule2Test");
        toolbar = editorPage.getEditorToolbarPanelComponent();
        toolbar.clickTargetTable();
        toolbar.checkBreadcrumbs("Projects", NAME_MY_PROJECT, "module_ALL");
        assertThat(editorPage.getEditorLeftRulesTreeComponent().getSelectedItemText())
                .as("Selected tree item should be SmartRule2").isEqualTo("SmartRule2");
        assertThat(editorPage.getProblemsPanelComponent().isCompilationProgressBarVisible())
                .as("Compilation progress bar should be present for large project").isTrue();

        //3
        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, NAME_TEST2_PROJECT, "TestProject2.zip");

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(NAME_TEST2_PROJECT);
        openModuleVerifyCompileThisModuleOnlyCheckedAndDisabled(editorPage, "TestSomeState");
        openModuleVerifyCompileThisModuleOnlyCheckedAndDisabled(editorPage, "TestSomeState2");
        openModuleVerifyCompileThisModuleOnlyCheckedAndDisabled(editorPage, "TestSomeState3");

        //3.1
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(NAME_TEST2_PROJECT, "TestSomeState");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Test")
                .selectItemInFolder("Test", "SmartRule1Test");
        toolbar = editorPage.getEditorToolbarPanelComponent();
        assertThat(toolbar.isRunButtonVisible()).as("Run button should be present").isTrue();
        assertThat(toolbar.isTraceButtonVisible()).as("Trace button should be present").isTrue();
        assertThat(toolbar.isBenchmarkButtonVisible()).as("Benchmark button should be present").isTrue();
        editorPage.getProblemsPanelComponent().checkNoProblems();

        //3.2
        toolbar.selectBreadcrumbModule(NAME_TEST2_PROJECT, "TestSomeState2");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Test")
                .selectItemInFolder("Test", "SmartRule2Test");
        toolbar = editorPage.getEditorToolbarPanelComponent();
        toolbar.clickRun().clickRunInsideMenu();
        editorPage.getTestResultValidationComponent().checkAllTablesPassed();

        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Test", "SmartRule2Test");
        traceWindow = toolbar.clickTraceExpectTraceWindow();
        traceWindow.expandItemInTree(0);
        assertThat(traceWindow.getVisibleItemsFromTree())
                .as("Trace should contain 'Empty'").anyMatch(item -> item.contains("Empty"));
        traceWindow.close();

        toolbar.clickBenchmark();
        WaitUtil.sleep(2000, "Waiting for benchmark results");

        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Test", "SmartRule2Test");
        editorPage.getProblemsPanelComponent().checkNoProblems();
        assertThat(toolbar.isTargetTableVisible()).as("Target table should be present").isTrue();
        assertThat(toolbar.getTargetTableText()).as("Target table should be SmartRule2").isEqualTo("SmartRule2");

        //3.3
        verifyThatGlobalTestIsOneAndRun(editorPage);

        //3.4
        // Reopen editor tab
        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(NAME_TEST2_PROJECT, "TestSomeState");
        assertThat(editorPage.getProblemsPanelComponent().isCompilationProgressBarVisible())
                .as("Compilation progress bar should be absent").isFalse();
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Test")
                .selectItemInFolder("Test", "SmartRule1Test");
        toolbar = editorPage.getEditorToolbarPanelComponent();
        assertThat(toolbar.isRunButtonVisible()).as("Run button should be present").isTrue();
        assertThat(toolbar.isTraceButtonVisible()).as("Trace button should be present").isTrue();
        assertThat(toolbar.isBenchmarkButtonVisible()).as("Benchmark button should be present").isTrue();
        editorPage.getProblemsPanelComponent().checkNoProblems();
        assertThat(toolbar.getTargetTableText()).as("Target table should be SmartRule1").isEqualTo("SmartRule1");

        //3.5
        verifyThatGlobalTestIsOneAndRun(editorPage);

        //3.6
        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Test", "SmartRule1Test");
        toolbar = editorPage.getEditorToolbarPanelComponent();
        toolbar.clickTrace();
        assertThat(toolbar.isWithinCurrentModuleOnlyTestTablesChecked())
                .as("WithinCurrentModuleOnlyTestTables should be checked").isTrue();
        assertThat(toolbar.isWithinCurrentModuleOnlyTestTablesEnabled())
                .as("WithinCurrentModuleOnlyTestTables should be disabled").isFalse();
        toolbar.clickTopPanelTestDropdown();
        assertThat(toolbar.isTopPanelWithinCurrentModuleOnlyChecked())
                .as("TopPanel WithinCurrentModuleOnly should be checked").isTrue();
        assertThat(toolbar.isTopPanelWithinCurrentModuleOnlyEnabled())
                .as("TopPanel WithinCurrentModuleOnly should be disabled").isFalse();

        //4
        // Reopen editor tab
        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(NAME_TEST2_PROJECT);
        editorPage.getProjectDetailsComponent().openEditModuleDialog("TestSomeState");
        editDialog = editorPage.getEditModuleDialogComponent();
        editDialog.waitForDialogToAppear();
        editDialog.setModuleName("bla");
        editDialog.clickSave();

        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();

        openModuleVerifyCompileThisModuleOnlyCheckedAndDisabled(editorPage, "bla");

        //5
        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, NAME_SAMPLE2_PROJECT, "SampleProject2.zip");

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(NAME_SAMPLE2_PROJECT, "Main");
        toolbar = editorPage.getEditorToolbarPanelComponent();
        assertThat(toolbar.isTestButtonVisible())
                .as("Test button should be absent for Main module").isFalse();

        //5.1
        toolbar.selectBreadcrumbModule(NAME_SAMPLE2_PROJECT, "Main2");
        verifyThatGlobalTestIsOneAndRun(editorPage);
        assertThat(editorPage.getTestResultValidationComponent().isCurrentModuleOnlyChecked())
                .as("currentModuleOnly should be checked").isTrue();
        assertThat(editorPage.getTestResultValidationComponent().isCurrentModuleOnlyEnabled())
                .as("currentModuleOnly should be disabled").isFalse();

        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Test")
                .selectItemInFolder("Test", "Hello2Test");
        toolbar = editorPage.getEditorToolbarPanelComponent();
        toolbar.clickRun().clickRunInsideMenu();
        editorPage.getTestResultValidationComponent().checkAllTablesPassed();
        assertThat(editorPage.getTestResultValidationComponent().countTestTables())
                .as("Should have exactly 1 test table").isEqualTo(1);

        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Test")
                .selectItemInFolder("Test", "Hello2Test");
        toolbar = editorPage.getEditorToolbarPanelComponent();
        toolbar.clickTrace();
        assertThat(toolbar.isWithinCurrentModuleOnlyTestTablesChecked())
                .as("WithinCurrentModuleOnlyTestTables should be checked").isTrue();
        assertThat(toolbar.isWithinCurrentModuleOnlyTestTablesEnabled())
                .as("WithinCurrentModuleOnlyTestTables should be disabled").isFalse();
        toolbar.clickTopPanelTestDropdown();
        assertThat(toolbar.isTopPanelWithinCurrentModuleOnlyChecked())
                .as("TopPanel WithinCurrentModuleOnly should be checked").isTrue();
        assertThat(toolbar.isTopPanelWithinCurrentModuleOnlyEnabled())
                .as("TopPanel WithinCurrentModuleOnly should be disabled").isFalse();

        //5.2
        toolbar.selectBreadcrumbModule(NAME_SAMPLE2_PROJECT, "Main_Test");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Test")
                .selectItemInFolder("Test", "HelloTest");
        toolbar = editorPage.getEditorToolbarPanelComponent();
        assertThat(toolbar.isRunButtonVisible()).as("Run button should be present").isTrue();
        assertThat(toolbar.isTraceButtonVisible()).as("Trace button should be present").isTrue();
        assertThat(toolbar.isBenchmarkButtonVisible()).as("Benchmark button should be present").isTrue();

        toolbar.clickRun().clickRunInsideMenu();
        editorPage.getTestResultValidationComponent().checkAllTablesPassed();
        assertThat(editorPage.getTestResultValidationComponent().countTestTables())
                .as("Should have exactly 1 test table").isEqualTo(1);

        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Test")
                .selectItemInFolder("Test", "HelloTest");
        traceWindow = toolbar.clickTraceExpectTraceWindow();
        traceWindow.expandItemInTree(0);
        assertThat(traceWindow.getVisibleItemsFromTree())
                .as("Trace should contain 'Good Evening'").anyMatch(item -> item.contains("Good Evening"));
        traceWindow.close();

        toolbar.clickBenchmark();
        WaitUtil.sleep(2000, "Waiting for benchmark results");

        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Test", "HelloTest");
        editorPage.getProblemsPanelComponent().checkNoProblems();
        assertThat(toolbar.isTargetTableVisible()).as("Target table should be present").isTrue();
        assertThat(toolbar.getTargetTableText()).as("Target table should be Hello").isEqualTo("Hello");

        //5.3
        toolbar.clickTargetTable();
        assertThat(toolbar.isTestButtonVisible())
                .as("Test button should be absent after navigating to target table in Main module").isFalse();
    }
}
