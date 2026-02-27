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
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.StringUtil;
import helpers.utils.WaitUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestCompilationProgressBar extends BaseTest {

    private static final String ZIP_FILE = "RulesEditor.TestCompilationProgressBar.zip";

    @Test
    @TestCaseId("IPBQA-31733")
    @Description("Compilation Progress Bar - hide after compilation, appears for unsaved project with copied/removed modules")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCompilationProgressBar() {
        String nameMainProject = StringUtil.generateUniqueName("TestCompilationProgressBar");
        String nameProjectSample = StringUtil.generateUniqueName("TestCompilationProgressBarSample");
        String nameProjectExample3 = StringUtil.generateUniqueName("TestCompilationProgressBarExample3");

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, nameProjectSample, "Sample Project");

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(nameProjectSample, "Main");
        LocalDriverPool.getPage().reload();
        editorPage = new EditorPage();
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete();
        assertThat(editorPage.getProblemsPanelComponent().isCompilationProgressBarVisible())
                .as("Compilation progress bar should be absent for Sample Project")
                .isFalse();

        //2
        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, nameProjectExample3, "Example 3 - Auto Policy Calculation");

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(nameProjectExample3, "AutoPolicyCalculation");
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete(15000, 250);
        assertThat(editorPage.getProblemsPanelComponent().isCompilationProgressBarVisible())
                .as("Compilation progress bar should be absent for Example3 after waiting")
                .isFalse();

        //3
        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, nameMainProject, ZIP_FILE);

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(nameMainProject, "module_KS");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "SmartRule1");

        //6
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete(15000, 250);
        assertThat(editorPage.getProblemsPanelComponent().isCompilationProgressBarVisible())
                .as("Compilation progress bar should be absent after waiting")
                .isFalse();

        //8
        editorPage.getEditorToolbarPanelComponent().selectBreadcrumbModule(nameMainProject, "module_IA");
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete(15000, 250);
        assertThat(editorPage.getProblemsPanelComponent().isCompilationProgressBarVisible())
                .as("Compilation progress bar should be absent after switching to module_IA")
                .isFalse();

        editorPage.getEditorToolbarPanelComponent().selectBreadcrumbModule(nameMainProject, "module_KS");
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete(15000, 250);
        assertThat(editorPage.getProblemsPanelComponent().isCompilationProgressBarVisible())
                .as("Compilation progress bar should be absent after switching back to module_KS")
                .isFalse();

        //9
        editorPage.getEditorToolbarPanelComponent().selectProjectBreadcrumbs(nameMainProject);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(nameMainProject, "module_KS");
        var copyModuleDialog = editorPage.openCopyModuleDialog();
        copyModuleDialog.setModuleName("module_KS2");
        copyModuleDialog.clickCopy();

        editorPage.getEditorToolbarPanelComponent().selectProjectBreadcrumbs(nameMainProject);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(nameMainProject, "module_KS2");
        ProblemsPanelComponent problemsPanel = editorPage.getProblemsPanelComponent();
        problemsPanel.waitForCompilationProgressBarToContain("Loaded 100% (37/37)", 200000);

        //10
        editorPage.getEditorToolbarPanelComponent().selectProjectBreadcrumbs(nameMainProject);
        editorPage.getProjectDetailsComponent().openRemoveModuleDialog("module_KS2");
        editorPage.getRemoveModulePopupComponent().setLeaveFile(false);
        editorPage.getRemoveModulePopupComponent().clickRemove();

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(nameMainProject, "module_KS");
        problemsPanel = editorPage.getProblemsPanelComponent();
        problemsPanel.waitForCompilationProgressBarToContain("Loaded 100% (36/36)", 200000);

        //11
        editorPage.getEditorToolbarPanelComponent().runAllTests();
        editorPage.getTestResultValidationComponent().checkAllTablesPassed();
        assertThat(editorPage.getProblemsPanelComponent().isCompilationProgressBarVisible())
                .as("Compilation progress bar should be absent after running tests")
                .isFalse();
        assertThat(editorPage.getProblemsPanelComponent().isCompilationProgressBarNotSavedProjectVisible())
                .as("Compilation progress bar (not saved) should be absent after running tests")
                .isFalse();

        //12
        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(nameMainProject, "module_KS");
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete(15000, 250);
        assertThat(editorPage.getProblemsPanelComponent().isCompilationProgressBarVisible())
                .as("Compilation progress bar should be absent after switching tabs")
                .isFalse();
        assertThat(editorPage.getProblemsPanelComponent().isCompilationProgressBarNotSavedProjectVisible())
                .as("Compilation progress bar (not saved) should be absent after switching tabs")
                .isFalse();

        //13
        editorPage.getEditorToolbarPanelComponent().selectProjectBreadcrumbs(nameProjectSample);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(nameProjectSample, "Main");
        copyModuleDialog = editorPage.openCopyModuleDialog();
        copyModuleDialog.setModuleName("Main2");
        copyModuleDialog.clickCopy();

        editorPage.getEditorToolbarPanelComponent().selectProjectBreadcrumbs(nameProjectSample);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(nameProjectSample, "Main2");
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete(15000, 250);
        assertThat(editorPage.getProblemsPanelComponent().isCompilationProgressBarVisible())
                .as("Compilation progress bar should be absent for copied small module")
                .isFalse();
        assertThat(editorPage.getProblemsPanelComponent().isCompilationProgressBarNotSavedProjectVisible())
                .as("Compilation progress bar (not saved) should be absent for copied small module")
                .isFalse();
    }

    @Test
    @TestCaseId("EPBDS-11548")
    @Description("Compilation does not start for each module - compilation visible only in second selected module")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCompilationNotStartsForEachModule() {
        String projectName = StringUtil.generateUniqueName("TestCompilationNotStartsForEachModule");

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, projectName, ZIP_FILE);

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "module_ALL");

        // Compilation process is expected NOT to be seen in the first selected module
        boolean compilationVisibleInFirst = editorPage.getProblemsPanelComponent().isCompilationProgressBarVisible()
                || editorPage.getProblemsPanelComponent().isCompilationProgressBarNotSavedProjectVisible();
        assertThat(compilationVisibleInFirst)
                .as("Compilation process is expected NOT to be seen in the first selected module")
                .isFalse();

        editorPage.getProblemsPanelComponent().waitForCompilationToComplete();

        // Switch to second module — compilation IS expected to be seen
        editorPage.getEditorToolbarPanelComponent().selectBreadcrumbModule(projectName, "module_AR");
        boolean compilationVisibleInSecond = editorPage.getProblemsPanelComponent().isCompilationProgressBarVisible()
                || editorPage.getProblemsPanelComponent().isCompilationProgressBarNotSavedProjectVisible();
        assertThat(compilationVisibleInSecond)
                .as("Compilation process IS expected to be seen in the second selected module")
                .isTrue();
    }
}
