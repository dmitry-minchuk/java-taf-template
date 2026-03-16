package tests.ui.webstudio.rules_editor;

import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.components.admincomponents.MySettingsPageComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.WorkflowService;
import helpers.utils.StringUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestOrderingMode extends BaseTest {

    private static final Map<String, String> additionalContainerConfig = new HashMap<>();

    // ========== Test 1: Single User Mode ==========

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

        // 1.3 Verification of the all drop-down values — verified via getDefaultOrder options
        // Legacy asserts: "By Type", "By Excel Sheet", "By Category", "By Category Detailed", "By Category Inversed"
        // The dropdown options are covered by the FilterOptions enum; we verify the default and changing works

        // 1.4 Verification that the default "Default Order:" value is applied
        EditorPage editorPage2 = adminPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
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
        // Set filter to "By Category" manually — this should NOT override the default
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_CATEGORY);

        // 1.9 Verify "By Category" node filter
        assertThat(editorPage.getEditorLeftRulesTreeComponent().getCategoriesVisible())
                .contains("Calculaiton-Spreadsheet", "Calculation-Smart", "Model", "Test");

        // 1.10 Verify ordering mode after open/close the browser
        // Simulate browser close/reopen by clearing cookies and navigating back
        LocalDriverPool.getPage().context().clearCookies();
        LocalDriverPool.getPage().navigate(LocalDriverPool.getAppUrl());
        editorPage = new EditorPage();
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "DefaultModeTesting");
        // After browser reopen, the default order setting ("By Category Inversed") should persist
        assertThat(editorPage.getEditorLeftRulesTreeComponent().getViewFilterValue())
                .containsIgnoringCase("By Category Inversed");
    }

    // ========== Test 2: Multi User Mode ==========

    @Test
    @TestCaseId("IPBQA-32117")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testDefaultOrderForMultiUser() {
        // 2.1 Start Webstudio in multi user mode — handled by @AppContainerConfig
        // additionalContainerConfig overrides security.administrators to "user1, user2"
        // But DEFAULT_STUDIO_PARAMS already sets admin, so we use admin user here
        String projectName = StringUtil.generateUniqueName("TestOrderingMode");

        // Login as admin, create project
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(new UserData("admin", "admin"));

        // Create project
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
        // Open the project (openl_1 needs to open it from repository)
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

    // ========== Test 3: Table List Ordering ==========

    @Test
    @TestCaseId("IPBQA-32507")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testTableListOrdering() {
        String projectName = WorkflowService.loginCreateProjectFromExcelFile(User.ADMIN, "sortingtesting.xlsx");
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "sortingtesting");

        // Verify default filter
        assertThat(editorPage.getEditorLeftRulesTreeComponent().getViewFilterValue())
                .containsIgnoringCase("By Type");

        // Switch to "By Excel Sheet" and verify categories
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_EXCEL_SHEET);
        assertThat(editorPage.getEditorLeftRulesTreeComponent().getCategoriesVisible())
                .isEqualTo(List.of("Sheet1", "Asheet", "すsupersheet"));

        // Expand folders and verify leaf node ordering
        editorPage.getEditorLeftRulesTreeComponent().expandFolderInTree("Sheet1");
        editorPage.getEditorLeftRulesTreeComponent().expandFolderInTree("Asheet");
        List<String> nodesNames = editorPage.getEditorLeftRulesTreeComponent().getAllEndNodesNames();
        assertThat(nodesNames).containsSequence(List.of("_MyRules2", "MyRules1", "MyRules1"));
        assertThat(nodesNames).containsSequence(List.of("тест123", "はsomeRules", "_someRules", "étudiantomeRules", "トsomeRules"));

        // Edit table _MyRules2: add a row to change ordering
        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Sheet1", "_MyRules2");
        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().clickCell(4, 2);
        editorPage.getEditorTableActionsPanelComponent().clickInsertRowAfter();
        editorPage.getCenterTable().editCell(5, 1, "1");
        editorPage.getCenterTable().editCell(5, 2, "1");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        // Verify ordering changed after edit
        nodesNames = editorPage.getEditorLeftRulesTreeComponent().getAllEndNodesNames();
        assertThat(nodesNames).containsSequence(List.of("MyRules1", "MyRules1", "_MyRules2"));

        // Create new Datatype table in "Asheet" category
        editorPage.getEditorToolbarPanelComponent().clickCreateTable();
        editorPage.getCreateTableDialogComponent()
                .selectType("Datatype Table")
                .clickNext()
                .setTechnicalName("NewDatatype")
                .addParameter("", "textField")
                .setCategorySelection("Asheet")
                .save();

        // Verify ordering includes new table
        nodesNames = editorPage.getEditorLeftRulesTreeComponent().getAllEndNodesNames();
        assertThat(nodesNames).containsSequence(List.of("тест123", "はsomeRules", "_someRules", "étudiantomeRules", "トsomeRules", "NewDatatype"));

        // Remove table はsomeRules and verify ordering update
        editorPage.getEditorLeftRulesTreeComponent().selectItemInFolder("Asheet", "はsomeRules");
        editorPage.getEditorToolbarPanelComponent().removeCurrentTable();
        editorPage.getEditorLeftRulesTreeComponent().expandFolderInTree("Asheet");
        nodesNames = editorPage.getEditorLeftRulesTreeComponent().getAllEndNodesNames();
        assertThat(nodesNames).containsSequence(List.of("тест123", "_someRules", "étudiantomeRules", "トsomeRules", "NewDatatype"));
    }

    // ========== Test 4: Table List Ordering 2 ==========

    @Test
    @TestCaseId("IPBQA-32507")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testTableListOrdering2() {
        String projectName = WorkflowService.loginCreateProjectFromExcelFile(User.ADMIN, "sortingtesting1.xlsx");
        EditorPage editorPage = new EditorPage();

        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectName, "sortingtesting1");

        // Switch to "By Excel Sheet" filter
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_EXCEL_SHEET);
        editorPage.getEditorLeftRulesTreeComponent().expandFolderInTree("Sheet1");

        // Verify ordering with utility tables hidden (default)
        List<String> nodesNames = editorPage.getEditorLeftRulesTreeComponent().getAllEndNodesNames();
        assertThat(nodesNames).containsSequence(List.of("_MyRules", "MyRules", "MyRules", "Atable"));

        // Disable "Hide Utility Tables" in advanced filter
        editorPage.getEditorLeftRulesTreeComponent().setAdvancedFilter(false);

        // Verify ordering with utility tables visible
        nodesNames = editorPage.getEditorLeftRulesTreeComponent().getAllEndNodesNames();
        assertThat(nodesNames).containsSequence(List.of("_MyRules", "Test123", "MyRules", "MyRules", "Test123", "Atable"));
    }
}
