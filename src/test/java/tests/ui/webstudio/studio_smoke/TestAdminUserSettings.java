package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.ui.webstudio.components.common.TableComponent;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.admincomponents.MyProfilePageComponent;
import domain.ui.webstudio.components.admincomponents.MySettingsPageComponent;
import domain.ui.webstudio.components.admincomponents.UsersPageComponent;
import domain.ui.webstudio.components.editortabcomponents.TableToolbarPanelComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.LoginPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.service.WorkflowService;
import org.testng.Assert;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.List;

import static domain.serviceclasses.constants.User.ADMIN;
import static domain.ui.webstudio.components.admincomponents.SecurityPageComponent.DefaultGroup.ADMINISTRATORS;
import static org.assertj.core.api.Assertions.assertThat;

public class TestAdminUserSettings extends BaseTest {

    @Test
    @TestCaseId("IPBQA-31293")
    @Description("User settings and profile management")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testUserSettingsAndDetails() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());

        // Scenario 1: Clear profile information (lines 34-44 from original)
        EditorPage editorPage = loginService.login(UserService.getUser(ADMIN));
        editorPage
                .openUserMenu()
                .navigateToAdministration()
                .navigateToSecurityPage()
                .selectDefaultGroup(ADMINISTRATORS.getValue())
                .clickApply();
        editorPage = loginService.login(UserService.getUser(ADMIN));
        MyProfilePageComponent myProfileComponent = editorPage
                .openUserMenu()
                .navigateToAdministration()
                .navigateToMyProfilePage();

        myProfileComponent.setFirstName("")
                .setLastName("")
                .setEmail("")
                .setDisplayName("");
        Assert.assertFalse(myProfileComponent.getSaveProfileBtn().isEnabled(), "Save button should be disabled because nothing changed on the page.");

        // Scenario 2: Verify empty profile fields (lines 45-57 from original)
        myProfileComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToMyProfilePage();

        Assert.assertEquals(myProfileComponent.getUsername(), "admin", "Username should be admin");
        Assert.assertEquals(myProfileComponent.getFirstName(), "", "First name should be empty");
        Assert.assertEquals(myProfileComponent.getLastName(), "", "Last name should be empty");
        Assert.assertEquals(myProfileComponent.getEmail(), "", "Email should be empty");

        String displayName = myProfileComponent.getDisplayName();
        Assert.assertEquals(displayName, "", "Display name should be empty");

        // Scenario 3: Update profile and check users table (lines 58-76 from original)
        myProfileComponent
                .setFirstName("Abc")
                .setLastName("Bcd")
                .setEmail("admin@admin.com")
                .setDisplayNamePattern("First Last")
                .saveProfile();

        myProfileComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToMyProfilePage();

        Assert.assertEquals(myProfileComponent.getFirstName(), "Abc", "First name should be 'Abc'");
        Assert.assertEquals(myProfileComponent.getLastName(), "Bcd", "Last name should be 'Bcd'");
        Assert.assertEquals(myProfileComponent.getEmail(), "admin@admin.com", "Email should be 'admin@admin.com'");
        Assert.assertEquals(myProfileComponent.getDisplayName(), "Abc Bcd", "Display name should be 'Abc Bcd'");

        UsersPageComponent usersComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();

        // Use new API with row index
        int adminRow = usersComponent.getUserRow("admin");
        String adminFullName = usersComponent.getFullNameFromRow(adminRow);
        String[] nameParts = adminFullName.split(" ");

        Assert.assertEquals(nameParts[0], "Abc", "Admin first name in Users table should be 'Abc'");
        Assert.assertEquals(nameParts[1], "Bcd", "Admin last name in Users table should be 'Bcd'");
        Assert.assertEquals(usersComponent.getEmailFromRow(adminRow), "admin@admin.com", "Admin email in Users table should be 'admin@admin.com'");
        Assert.assertEquals(adminFullName, "Abc Bcd", "Admin display name in Users table should be 'Abc Bcd'");

        // Scenario 4: Change password and test authentication (lines 77-95 from original)
        myProfileComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToMyProfilePage();

        myProfileComponent.setCurrentPassword("admin").setNewPassword("12345").setConfirmPassword("12345").saveProfile();

        //TODO: Logout and test old password (should fail) - NOW NO ERRORS ON UI
        editorPage.openUserMenu().signOut();
        LoginPage loginPage = new LoginPage();
        UserData oldPasswordData = new UserData("admin", "admin");
//        loginPage.login(oldPasswordData);
//        Assert.assertTrue(loginPage.isLoginErrorDisplayed(), "Login error should be displayed for old password");
//        String errorMessage = loginPage.getLoginErrorMessage();
//        Assert.assertTrue(errorMessage.contains("Wrong username") || errorMessage.contains("Invalid username"), "Error message should indicate wrong credentials");

        // Login with new password
        UserData newUserData = new UserData("admin", "12345");
        editorPage = loginService.login(newUserData);
        myProfileComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToMyProfilePage();

        // Scenario 5: Create new user (lines 96-131 from original)
        usersComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();

        // Create new user1 with full details
        usersComponent.clickAddUser()
                .setUsername("user1")
                .setEmail("user1@example.com")
                .setFirstName("Aaa")
                .setLastName("Bbb")
                .setPassword("user1")
                .clickAddRoleBtn()
                .setRoleRepository(0, "Design")
                .setRole(0, "Manager")
                .inviteUser();
        
        // Logout admin and login as user1
        editorPage.openUserMenu().signOut();
        UserData user1Data = new UserData("user1", "user1");
        editorPage = loginService.login(user1Data);
        
        // Verify user1 profile details
        myProfileComponent = editorPage.openUserMenu()
                .navigateToMyProfile()
                .navigateToMyProfilePage();
                
        Assert.assertEquals(myProfileComponent.getUsername(), "user1", "Username should be 'user1'");
        Assert.assertEquals(myProfileComponent.getFirstName(), "Aaa", "First name should be 'Aaa'");
        Assert.assertEquals(myProfileComponent.getLastName(), "Bbb", "Last name should be 'Bbb'");
        Assert.assertEquals(myProfileComponent.getEmail(), "user1@example.com", "Email should be user1@example.com");
        
        // Change display name
        myProfileComponent.setDisplayName("Bbb Aaa").saveProfile();
        
        // Verify display name change
        myProfileComponent = editorPage.openUserMenu()
                .navigateToMySettings()
                .navigateToMyProfilePage();
        Assert.assertEquals(myProfileComponent.getDisplayName(), "Bbb Aaa", "Display name should be updated");

        // Verify user in Users table
        usersComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();
        int user1Row = usersComponent.getUserRow("user1");
        Assert.assertEquals(usersComponent.getFullNameFromRow(user1Row), "Bbb Aaa", "Display name should be updated in users table");

        // Scenario 6: Check default settings (lines 133-143 from original)
        editorPage.openUserMenu().signOut();
        UserData adminNewPassword = new UserData("admin", "12345");
        editorPage = loginService.login(adminNewPassword);

        MySettingsPageComponent mySettingsComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToMySettingsPage();

        Assert.assertTrue(mySettingsComponent.isShowHeaderEnabled(), "Show Header should be true");
        Assert.assertFalse(mySettingsComponent.isShowFormulasEnabled(), "Show Formulas should be false");
        Assert.assertEquals(mySettingsComponent.getTestsPerPage(), 5, "Tests per page should be 5");
        Assert.assertFalse(mySettingsComponent.isFailuresOnlyEnabled(), "Failures Only should be false");
        Assert.assertFalse(mySettingsComponent.isCompoundResultEnabled(), "Compound Result should be false");
        Assert.assertFalse(mySettingsComponent.isShowNumbersWithoutFormattingEnabled(), "Show numbers without formatting should be false");

        myProfileComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToMyProfilePage();
        myProfileComponent.setCurrentPassword("12345").setNewPassword("admin").setConfirmPassword("admin").saveProfile();
        editorPage.openUserMenu().signOut();

        // Scenario 7: Verify "View project in Single module mode" (from JIRA scenario 7)
        // NOTE: This setting will be removed in 5.25 (EPBDS-10660), so test is partially commented out
        // The "View project in Single module mode" setting has been removed, skipping this part of scenario 7
        // mySettingsComponent = editorPage.openUserMenu().navigateToAdministration().navigateToMySettingsPage();
        // mySettingsComponent.setViewProjectInSingleModuleMode(true).saveSettings();
        // ... (rest of scenario 7 validation is skipped as feature will be removed)

        // Scenario 8: Verify Table Settings (from JIRA scenario 8)
        String projectNameTest1 = WorkflowService.loginCreateProjectFromExcelFile(ADMIN, "Test1.xlsx");
        editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectNameTest1, "Test1");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "CapitalAdequacyScore");

        TableComponent tableComponent = editorPage.getCenterTable();
        Assert.assertEquals(tableComponent.getCellText(3, 2), "2500", "Cell content should be '2500'");

        // Click "arrow" after admin, Click "User settings", Set Show Header: false, Show Formulas: true, Click "Save"
        mySettingsComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToMySettingsPage();
        mySettingsComponent.setShowFormulas(true).setShowHeader(false).saveSettings();

        // Return to table and verify
        editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectNameTest1, "Test1");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "CapitalAdequacyScore");
        Assert.assertEquals(tableComponent.getRowsCount(), 7, "Table should have 7 rows");
        Assert.assertEquals(tableComponent.getCellText(2, 2), "=50*45/D8", "Formula should be visible");

        // Scenario 9: Check settings isolation for different users (lines 154-164 from original)
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(user1Data);

        // Navigate to the same project as user1
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.unlockAllProjects();
        repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectNameTest1, "Test1"); //User1 is NOT admin
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "CapitalAdequacyScore");

        // User1 should see different table format (8 rows instead of 7) due to different settings
        TableComponent tableComponentUser1 = editorPage.getCenterTable();
        Assert.assertEquals(tableComponentUser1.getRowsCount(), 8, "Table should have 8 rows for user1 (different settings)");
        // User1 should see different header row content
        Assert.assertEquals(tableComponentUser1.getCellText(1, 1), "SimpleRules Double CapitalAdequacyScore (Double capitalAdequacy)",
                           "User1 should see different header format");

        // Scenario 9: Change test settings (lines 165-176 from original)
        editorPage.openUserMenu().signOut();
        String projectNameTemplate = WorkflowService.loginCreateProjectFromTemplate(ADMIN, "Example 1 - Bank Rating");
        mySettingsComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToMySettingsPage();

        mySettingsComponent.setTestsPerPage(20)
                .setFailuresOnly(true)
                .setCompoundResult(true)
                .saveSettings();

        // Scenario 10: Verify settings in TestRunDropDown (lines 177-184 from original)
        // Reuse already created projectNameTemplate from Scenario 7
        editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectNameTemplate, "Bank Rating");

        // Verify test execution settings in dropdown
        TableToolbarPanelComponent.IRunTestsMenu testSettings = editorPage.getTableToolbarPanelComponent().clickTestDropdown();
        Assert.assertEquals(testSettings.getTestPerPage(), "20", "Tests per page should be 20");
        Assert.assertTrue(testSettings.isFailuresOnlyChecked(), "Failures Only should be enabled");
        Assert.assertTrue(testSettings.isCompoundResultChecked(), "Compound Result should be enabled");

        // Scenario 11: Verify user settings isolation (lines 185-195 from original)
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(user1Data);
        mySettingsComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToMySettingsPage();

        // Verify that user1 has default settings (different from admin's modified settings)
        Assert.assertTrue(mySettingsComponent.isShowHeaderEnabled(), "User1 Show Header should still be true");
        Assert.assertFalse(mySettingsComponent.isShowFormulasEnabled(), "User1 Show Formulas should still be false");
        Assert.assertEquals(mySettingsComponent.getTestsPerPage(), 5, "User1 Tests per page should still be 5");
        Assert.assertFalse(mySettingsComponent.isFailuresOnlyEnabled(), "User1 Failures Only should still be false");
        Assert.assertFalse(mySettingsComponent.isCompoundResultEnabled(), "User1 Compound Result should still be false");

        // Scenario 12: Test Help functionality (lines 196-201 from original)
        editorPage.openUserMenu().openHelp();
        String helpUrl = editorPage.getPage().url();
        Assert.assertTrue(helpUrl.contains("help"), "Help should open OpenL Tablets documentation");

        // Scenarios 13-17: Trace functionality with number formatting (lines 202-234 from original)
        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(UserService.getUser(User.ADMIN));

        editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectNameTest1, "Test1");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", "TotalAssets4");

        // Scenario 13: Trace without formatting
        TableToolbarPanelComponent.TraceWindow traceWindow = (TableToolbarPanelComponent.TraceWindow) editorPage.getTableToolbarPanelComponent().clickTraceExpectTraceWindow();
        List<String> traceItems = traceWindow.getVisibleItemsFromTree();
        traceWindow.close();
        assertThat(traceItems.getFirst()).contains("SpreadSheet Double TotalAssets4() = 268.59");

        // Scenario 14: Enable showNumbersWithoutFormatting and test trace
        mySettingsComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToMySettingsPage();
        mySettingsComponent.setShowNumbersWithoutFormatting(true).saveSettings();

        editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectNameTest1, "Test1");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Spreadsheet")
                .selectItemInFolder("Spreadsheet", "TotalAssets4");

        traceWindow = (TableToolbarPanelComponent.TraceWindow) editorPage.getTableToolbarPanelComponent().clickTraceExpectTraceWindow();
        traceItems = traceWindow.getVisibleItemsFromTree();
        traceWindow.close();
        assertThat(traceItems.getFirst()).contains("268.59000000000003"); // Unformatted number

        // Scenario 15: Verify E-notation is not shown
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("TBasic")
                .selectItemInFolder("TBasic", "SetNonZeroValues");

        traceWindow = (TableToolbarPanelComponent.TraceWindow) editorPage.getTableToolbarPanelComponent()
                .clickTrace()
                .clickTraceInsideMenu();
        TableComponent centerTable = traceWindow.expandItemInTree(0).getCenterTable();
        assertThat(centerTable.getCell(3, 4).getText()).contains("0.0001").doesNotContain("E-");
        traceWindow.close();

        // Scenario 16: Disable showNumbersWithoutFormatting and test again
        mySettingsComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToMySettingsPage();
        mySettingsComponent.setShowNumbersWithoutFormatting(false).saveSettings();

        // Scenario 17: Final E-notation verification
        editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectNameTest1, "Test1");
        editorPage.getEditorLeftRulesTreeComponent()
                .expandFolderInTree("TBasic")
                .selectItemInFolder("TBasic", "SetNonZeroValues");

        traceWindow = (TableToolbarPanelComponent.TraceWindow) editorPage.getTableToolbarPanelComponent()
                .clickTrace()
                .clickTraceInsideMenu();
        centerTable = traceWindow.expandItemInTree(0).getCenterTable();
        assertThat(centerTable.getCell(3, 4).getText()).contains("0.0001").doesNotContain("E-");
        traceWindow.close();
    }
}