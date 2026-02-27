package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestProjectHasTheSameModuleInDependency extends BaseTest {

    private static final String NAME_PROJECT_DATATYPE = "DatatypeProject";
    private static final String NAME_PROJECT_CALC1 = "CalcProject1";
    private static final String NAME_PROJECT_CALC2 = "CalcProject2";

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
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", "mySpr");
        assertThat(editorPage.getTopProblemsPanelComponent().isAbsent())
                .as("No errors should be shown for CalcProject1")
                .isTrue();

        editorPage.getEditorToolbarPanelComponent().selectBreadcrumbModule(NAME_PROJECT_CALC2, "CalcModule");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", "mySpr");
        assertThat(editorPage.getTopProblemsPanelComponent().isAbsent())
                .as("No errors should be shown for CalcProject2")
                .isTrue();

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
}
