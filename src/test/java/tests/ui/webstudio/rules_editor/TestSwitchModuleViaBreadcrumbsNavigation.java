package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerPool;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import helpers.utils.LogsUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSwitchModuleViaBreadcrumbsNavigation extends BaseTest {

    private static final String NAME_PROJECT_FIRST = "TestSwitchModuleViaBreadcrumbsNavigationFirst";
    private static final String ZIP_FILE_FIRST = "RulesEditor.TestSwitchModuleViaBreadcrumbsNavigationFirst.zip";
    private static final String NAME_PROJECT_SECOND = "TestSwitchModuleViaBreadcrumbsNavigationSecond";
    private static final String ZIP_FILE_SECOND = "RulesEditor.TestSwitchModuleViaBreadcrumbsNavigationSecond.zip";

    @Test
    @TestCaseId("IPBQA-31804")
    @Description("Switch Module Via Breadcrumbs Navigation - breadcrumb switching through multiple modules, performance check")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testSwitchModuleViaBreadcrumbsNavigation() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, NAME_PROJECT_FIRST, ZIP_FILE_FIRST);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, NAME_PROJECT_SECOND, ZIP_FILE_SECOND);

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(NAME_PROJECT_FIRST, "module_AR");
        editorPage.getEditorToolbarPanelComponent().selectProjectBreadcrumbs(NAME_PROJECT_SECOND);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(NAME_PROJECT_SECOND, "module_AZ");
        editorPage.getProblemsPanelComponent().checkNoProblems();

        // 2 + 3 + 4: Switch through multiple modules via breadcrumbs
        editorPage.getEditorToolbarPanelComponent().selectProjectBreadcrumbs(NAME_PROJECT_FIRST);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(NAME_PROJECT_FIRST, "module_AR");
        editorPage.getEditorToolbarPanelComponent().selectBreadcrumbModule(NAME_PROJECT_FIRST, "module_AZ");
        editorPage.getEditorToolbarPanelComponent().selectBreadcrumbModule(NAME_PROJECT_FIRST, "module_MN");
        editorPage.getEditorToolbarPanelComponent().selectBreadcrumbModule(NAME_PROJECT_FIRST, "module_MT");
        editorPage.getEditorToolbarPanelComponent().selectBreadcrumbModule(NAME_PROJECT_FIRST, "module_LA");
        editorPage.getEditorToolbarPanelComponent().selectBreadcrumbModule(NAME_PROJECT_FIRST, "module_OH");
        editorPage.getEditorToolbarPanelComponent().selectBreadcrumbModule(NAME_PROJECT_FIRST, "module_OK");
        editorPage.getEditorToolbarPanelComponent().selectBreadcrumbModule(NAME_PROJECT_FIRST, "module_OR");
        editorPage.getEditorToolbarPanelComponent().selectBreadcrumbModule(NAME_PROJECT_FIRST, "module_TN");
        editorPage.getEditorToolbarPanelComponent().selectBreadcrumbModule(NAME_PROJECT_FIRST, "module_KS");
        editorPage.getEditorToolbarPanelComponent().selectBreadcrumbModule(NAME_PROJECT_FIRST, "module_KY");
        editorPage.getEditorToolbarPanelComponent().selectBreadcrumbModule(NAME_PROJECT_FIRST, "module_RI");
        editorPage.getEditorToolbarPanelComponent().selectBreadcrumbModule(NAME_PROJECT_FIRST, "module_SC");

        long startTime = System.currentTimeMillis();
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "SmartRule1");
        long elapsedTime = System.currentTimeMillis() - startTime;
        assertThat(elapsedTime)
                .as("Expanding and selecting tree item should take less than 10 seconds")
                .isLessThanOrEqualTo(10000);

        LogsUtil.inspectLogFile(AppContainerPool.get());
    }
}
