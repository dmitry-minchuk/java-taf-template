package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.ProblemsPanelComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.WaitUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestProjectCompilation extends BaseTest {

    private static final String NAME_PROJECT_MY = "MyProject";
    private static final String NAME_PROJECT_INCLUDE_FALSE = "TestProjectAutoIncludedFalse";
    private static final String NAME_PROJECT_INCLUDE_TRUE = "TestProjectAutoIncludedTrue";
    private static final String NAME_PROJECT_DATATYPE = "DatatypeProject";
    private static final String NAME_PROJECT_CALC1 = "CalcProject1";
    private static final String NAME_PROJECT_CALC2 = "CalcProject2";

    private void verifyProgressBarAbsentOrQuicklyDisappears(ProblemsPanelComponent problemsPanel) {
        if (problemsPanel.isCompilationProgressBarVisible()) {
            WaitUtil.sleep(11000, "Waiting for compilation progress bar to disappear");
        }
        assertThat(problemsPanel.isCompilationProgressBarVisible())
                .as("Compilation progress bar should be absent")
                .isFalse();
    }

    @Test
    @TestCaseId("IPBQA-31701")
    @Description("Project Compilation - AutoInclude=false: progress bar absent for small project, appears for large project")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testProjectCompilationAutoIncludeFalse() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, NAME_PROJECT_INCLUDE_FALSE, "TestProjectAutoIncludedFalse.zip");
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, NAME_PROJECT_INCLUDE_TRUE, "TestProjectAutoIncludedTrue.zip");
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, NAME_PROJECT_MY, "MyProject.zip");

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(NAME_PROJECT_INCLUDE_FALSE, "TestSomeState");
        verifyProgressBarAbsentOrQuicklyDisappears(editorPage.getProblemsPanelComponent());

        //2
        LocalDriverPool.getPage().reload();
        editorPage = new EditorPage();
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete();
        verifyProgressBarAbsentOrQuicklyDisappears(editorPage.getProblemsPanelComponent());

        //3
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Test")
                .selectItemInFolder("Test", "SmartRule1Test");
        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(4, 5, "600");
        assertThat(editorPage.getProblemsPanelComponent().isCompilationProgressBarVisible())
                .as("Compilation progress bar should be absent after edit")
                .isFalse();

        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();
        assertThat(editorPage.getProblemsPanelComponent().isCompilationProgressBarVisible())
                .as("Compilation progress bar should be absent after save")
                .isFalse();

        //4
        editorPage.getEditorToolbarPanelComponent().selectBreadcrumbModule(NAME_PROJECT_MY, "module_KS");
        ProblemsPanelComponent problemsPanel = editorPage.getProblemsPanelComponent();
        WaitUtil.waitForCondition(
                () -> problemsPanel.isCompilationProgressBarNotSavedProjectVisible()
                        || problemsPanel.isCompilationProgressBarVisible(),
                15000, 1000,
                "Waiting for compilation progress bar to appear for large project"
        );
    }

    @Test
    @TestCaseId("IPBQA-31701")
    @Description("Project Compilation - AutoInclude=true: progress bar absent for small project, absent when switching to large project")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testProjectCompilationAutoIncludeTrue() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, NAME_PROJECT_INCLUDE_FALSE, "TestProjectAutoIncludedFalse.zip");
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, NAME_PROJECT_INCLUDE_TRUE, "TestProjectAutoIncludedTrue.zip");
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, NAME_PROJECT_MY, "MyProject.zip");

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(NAME_PROJECT_INCLUDE_TRUE, "TestSomeState");
        verifyProgressBarAbsentOrQuicklyDisappears(editorPage.getProblemsPanelComponent());

        //2
        LocalDriverPool.getPage().reload();
        editorPage = new EditorPage();
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete();
        verifyProgressBarAbsentOrQuicklyDisappears(editorPage.getProblemsPanelComponent());

        //3
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Test")
                .selectItemInFolder("Test", "SmartRule1Test");
        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(4, 5, "600");
        assertThat(editorPage.getProblemsPanelComponent().isCompilationProgressBarVisible())
                .as("Compilation progress bar should be absent after edit")
                .isFalse();

        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();
        assertThat(editorPage.getProblemsPanelComponent().isCompilationProgressBarVisible())
                .as("Compilation progress bar should be absent after save")
                .isFalse();

        //4
        editorPage.getEditorToolbarPanelComponent().selectBreadcrumbModule(NAME_PROJECT_MY, "module_KS");
        assertThat(editorPage.getProblemsPanelComponent().isCompilationProgressBarVisible())
                .as("Compilation progress bar should be absent for AutoInclude=true project")
                .isFalse();
    }

    @Test
    @TestCaseId("IPBQA-31701")
    @Description("Project Compilation - Project has same module in dependency: no errors on Spreadsheet table")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testProjectHasTheSameModuleInDependency() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, NAME_PROJECT_DATATYPE, "DatatypeProject.zip");
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, NAME_PROJECT_CALC1, "CalcProject1.zip");
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, NAME_PROJECT_CALC2, "CalcProject2.zip");

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(NAME_PROJECT_CALC1, "CalcModule");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", "mySpr");
        assertThat(editorPage.getTopProblemsPanelComponent().isAbsent())
                .as("No errors should be shown for CalcProject1")
                .isTrue();

        //2
        editorPage.getEditorToolbarPanelComponent().selectBreadcrumbModule(NAME_PROJECT_CALC2, "CalcModule");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", "mySpr");
        assertThat(editorPage.getTopProblemsPanelComponent().isAbsent())
                .as("No errors should be shown for CalcProject2")
                .isTrue();

        //3
        LocalDriverPool.getPage().reload();
        editorPage = new EditorPage();
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete();
        assertThat(editorPage.getEditorLeftRulesTreeComponent().getSelectedItemText())
                .as("Selected tree item should be mySpr after refresh")
                .isEqualTo("mySpr");
        assertThat(editorPage.getTopProblemsPanelComponent().isAbsent())
                .as("No errors should be shown after refresh")
                .isTrue();
    }

    @Test
    @TestCaseId("IPBQA-31701")
    @Description("Project Compilation - Test button available, run tests, copy module, compilation progress for large project")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testTestButtonAvailable() {
        String nameExample3Project = "Example 3";
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, nameExample3Project, "Example 3 - Auto Policy Calculation");

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(nameExample3Project, "AutoPolicyCalculation");
        EditorPage editorPageRef = editorPage;
        WaitUtil.waitForCondition(
                () -> editorPageRef.getEditorToolbarPanelComponent().isTestButtonVisible(),
                5000, 500, "Waiting for Test button to become visible"
        );
        editorPage.getEditorToolbarPanelComponent().runAllTests();
        editorPage.getTestResultValidationComponent().checkAllTablesPassed();

        //2
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "AccidentPremium");
        editorPage.getEditorToolbarPanelComponent().runAllTests();
        editorPage.getTestResultValidationComponent().checkAllTablesPassed();

        //3
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "AccidentPremium");
        LocalDriverPool.getPage().reload();
        editorPage = new EditorPage();
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete();
        assertThat(editorPage.getEditorToolbarPanelComponent().getTestButtonText())
                .as("Test button should show 'Test 3'")
                .isEqualTo("Test 3");

        //4
        LocalDriverPool.getPage().reload();
        editorPage = new EditorPage();
        EditorPage editorPageRef2 = editorPage;
        WaitUtil.waitForCondition(
                () -> editorPageRef2.getEditorToolbarPanelComponent().isTestButtonVisible(),
                5000, 500, "Waiting for Test button after full refresh"
        );
        assertThat(editorPage.getEditorToolbarPanelComponent().getTestButtonText())
                .as("Test button should show 'Test 3' after full refresh")
                .isEqualTo("Test 3");

        //5
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(nameExample3Project, "AutoPolicyTests");
        var copyModuleDialog = editorPage.openCopyModuleDialog();
        copyModuleDialog.setModuleName("AutoPolicyTests2");
        copyModuleDialog.clickCopy();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(nameExample3Project, "AutoPolicyTests2");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("Test")
                .selectItemInFolder("Test", "DriverPremiumTest");
        editorPage.getCenterTable().editCell(1, 1, "Test DetermineDriverPremium DriverPremiumTest1");

        editorPage.getEditorLeftRulesTreeComponent()
                .selectItemInFolder("Test", "PolicyPremiumTest");
        editorPage.getCenterTable().editCell(1, 1, "Test DeterminePolicyPremium PolicyPremiumTest1");

        editorPage.getEditorLeftRulesTreeComponent()
                .selectItemInFolder("Test", "VehiclePremiumTest");
        editorPage.getCenterTable().editCell(1, 1, "Test DetermineVehiclePremium VehiclePremiumTest1");

        assertThat(editorPage.getEditorToolbarPanelComponent().getTestButtonText())
                .as("Test button should show 'Test 6' after copying module and adding tests")
                .isEqualTo("Test 6");

        //6
        LocalDriverPool.getPage().reload();
        editorPage = new EditorPage();
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete();
        assertThat(editorPage.getEditorToolbarPanelComponent().getTestButtonText())
                .as("Test button should show 'Test 6' after refresh")
                .isEqualTo("Test 6");

        //part 5
        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, NAME_PROJECT_MY, "MyProject.zip");

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(nameExample3Project);
        LocalDriverPool.getPage().goBack();
        editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(NAME_PROJECT_MY, "module_KS");
        editorPage.getProblemsPanelComponent().waitForCompilationProgressBarToContain("Loaded", 200000);
    }
}
