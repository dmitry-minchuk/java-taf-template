package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.components.admincomponents.MySettingsPageComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.utils.StringUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOrderingModeDefaults extends BaseTest {

    @Test
    @TestCaseId("IPBQA-32117")
    @AppContainerConfig(startParams = AppContainerStartParameters.SINGLE_USER_STUDIO_PARAMS)
    public void testDefaultOrderForSingleUser() {
        // 1.1 Start Webstudio in single user mode — handled by @AppContainerConfig
        String projectName = StringUtil.generateUniqueName("TestOrderingMode");
        LocalDriverPool.getPage().navigate(LocalDriverPool.getAppUrl());
        EditorPage editorPage = new EditorPage();

        // 1.2 Verification of the default value of "Default Order:"
        AdminPage adminPage = editorPage.openUserMenu().navigateToMySettings();
        MySettingsPageComponent mySettings = adminPage.navigateToMySettingsPage();
        assertThat(mySettings.getDefaultOrder()).isEqualTo("By Excel Sheet");

        // 1.4 Verification that the default "Default Order:" value is applied
        adminPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        RepositoryPage repositoryPage = new RepositoryPage();
        repositoryPage.createProject(
                domain.ui.webstudio.components.common.CreateNewProjectComponent.TabName.ZIP_ARCHIVE,
                projectName, "TestOrderingMode.zip");
        repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "DefaultModeTesting");
        assertThat(editorPage.getEditorLeftRulesTreeComponent().getViewFilterValue())
                .containsIgnoringCase("By Excel Sheet");

        // 1.5 Verification table nodes
        assertThat(editorPage.getEditorLeftRulesTreeComponent().getCategoriesVisible())
                .contains("Model", "Algorithm", "Test");

        // 1.6 Modify the "Default Order:" value
        adminPage = editorPage.openUserMenu().navigateToMySettings();
        mySettings = adminPage.navigateToMySettingsPage();
        mySettings.setDefaultOrder("By Category Inversed");
        mySettings.saveSettings();
        editorPage = adminPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "DefaultModeTesting");
        assertThat(editorPage.getEditorLeftRulesTreeComponent().getViewFilterValue())
                .containsIgnoringCase("By Category Inversed");

        // 1.7 Verify the table nodes list
        assertThat(editorPage.getEditorLeftRulesTreeComponent().getCategoriesVisible())
                .contains("Model", "Smart", "Spreadsheet", "Test");

        // 1.8 Verification that is not overridden by another mode choosing
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_CATEGORY);

        // 1.9 Verify "By Category" node filter
        assertThat(editorPage.getEditorLeftRulesTreeComponent().getCategoriesVisible())
                .contains("Calculaiton-Spreadsheet", "Calculation-Smart", "Model", "Test");

        // 1.10 Verify ordering mode after open/close the browser
        LocalDriverPool.getPage().context().clearCookies();
        LocalDriverPool.getPage().navigate(LocalDriverPool.getAppUrl());
        editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "DefaultModeTesting");
        assertThat(editorPage.getEditorLeftRulesTreeComponent().getViewFilterValue())
                .containsIgnoringCase("By Category Inversed");
    }

    @Test
    @TestCaseId("IPBQA-32117")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testDefaultOrderForMultiUser() {
        String projectName = StringUtil.generateUniqueName("TestOrderingMode");

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(new UserData("admin", "admin"));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(
                domain.ui.webstudio.components.common.CreateNewProjectComponent.TabName.ZIP_ARCHIVE,
                projectName, "TestOrderingMode.zip");

        // 2.2 Verification of the default value of "Default Order:" for admin
        AdminPage adminPage = editorPage.openUserMenu().navigateToMySettings();
        MySettingsPageComponent mySettings = adminPage.navigateToMySettingsPage();
        assertThat(mySettings.getDefaultOrder()).isEqualTo("By Excel Sheet");

        // 2.3 Change default order for admin to "By Category Detailed"
        mySettings.setDefaultOrder("By Category Detailed");
        mySettings.saveSettings();

        editorPage = adminPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "DefaultModeTesting");
        assertThat(editorPage.getEditorLeftRulesTreeComponent().getViewFilterValue())
                .containsIgnoringCase("By Category Detailed");

        // 2.4 Verify the node list
        assertThat(editorPage.getEditorLeftRulesTreeComponent().getCategoriesVisible())
                .contains("Calculaiton", "Calculation", "Model", "Test");

        // 2.5 Verification that the default "Default Order:" is different for another user (openl_1)
        editorPage.openUserMenu().signOut();
        loginService = new LoginService(LocalDriverPool.getPage());
        editorPage = loginService.login(new UserData("openl_1", "openl_1"));

        // Fill profile for openl_1 (required for first login)
        adminPage = editorPage.openUserMenu().navigateToMyProfile();
        adminPage.navigateToMyProfilePage()
                .setFirstName("First Name")
                .setLastName("Last Name")
                .setEmail("openl_1@mail.com")
                .saveProfile();

        // Check default order for openl_1 — should still be "By Excel Sheet" (default)
        adminPage = editorPage.openUserMenu().navigateToMySettings();
        mySettings = adminPage.navigateToMySettingsPage();
        assertThat(mySettings.getDefaultOrder()).isEqualTo("By Excel Sheet");

        // 2.6 Change default order for openl_1 to "By Type"
        mySettings.setDefaultOrder("By Type");
        mySettings.saveSettings();

        editorPage = adminPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.unlockAllProjects();
        repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "DefaultModeTesting");
        assertThat(editorPage.getEditorLeftRulesTreeComponent().getViewFilterValue())
                .containsIgnoringCase("By Type");

        // 2.7 Verify the node list for "By Type"
        assertThat(editorPage.getEditorLeftRulesTreeComponent().getCategoriesVisible())
                .contains("Decision", "Spreadsheet", "Test", "Datatype", "Vocabulary");
    }
}
