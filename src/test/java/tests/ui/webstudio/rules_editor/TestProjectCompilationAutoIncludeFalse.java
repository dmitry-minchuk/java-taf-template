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
import helpers.utils.WaitUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestProjectCompilationAutoIncludeFalse extends BaseTest {

    private static final String NAME_PROJECT_MY = "MyProject";
    private static final String NAME_PROJECT_INCLUDE_FALSE = "TestProjectAutoIncludedFalse";
    private static final String NAME_PROJECT_INCLUDE_TRUE = "TestProjectAutoIncludedTrue";

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

        LocalDriverPool.getPage().reload();
        editorPage = new EditorPage();
        editorPage.getProblemsPanelComponent().waitForCompilationToComplete();
        verifyProgressBarAbsentOrQuicklyDisappears(editorPage.getProblemsPanelComponent());

        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Test")
                .selectItemInFolder("Test", "SmartRule1Test");
        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(4, 5, "600");
        assertThat(editorPage.getProblemsPanelComponent().isCompilationProgressBarVisible())
                .as("Compilation progress bar should be absent after edit")
                .isFalse();

        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();
        editorPage.getEditorToolbarPanelComponent().clickSave();
        editorPage.getSaveChangesComponent().getSaveBtn().click();
        assertThat(editorPage.getProblemsPanelComponent().isCompilationProgressBarVisible())
                .as("Compilation progress bar should be absent after save")
                .isFalse();

        editorPage.getEditorToolbarPanelComponent().selectBreadcrumbModule(NAME_PROJECT_MY, "module_KS");
        ProblemsPanelComponent problemsPanel = editorPage.getProblemsPanelComponent();
        WaitUtil.waitForCondition(
                () -> problemsPanel.isCompilationProgressBarNotSavedProjectVisible()
                        || problemsPanel.isCompilationProgressBarVisible(),
                15000, 1000,
                "Waiting for compilation progress bar to appear for large project"
        );
    }

    private void verifyProgressBarAbsentOrQuicklyDisappears(ProblemsPanelComponent problemsPanel) {
        if (problemsPanel.isCompilationProgressBarVisible()) {
            WaitUtil.sleep(11000, "Waiting for compilation progress bar to disappear");
        }
        assertThat(problemsPanel.isCompilationProgressBarVisible())
                .as("Compilation progress bar should be absent")
                .isFalse();
    }
}
