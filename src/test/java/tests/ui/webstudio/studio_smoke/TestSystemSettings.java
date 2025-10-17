package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.SystemSettingsPageComponent;
import domain.ui.webstudio.components.common.TableComponent;
import domain.ui.webstudio.components.editortabcomponents.EditTablePanelComponent;
import domain.ui.webstudio.components.editortabcomponents.TableToolbarPanelComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftProjectModuleSelectorComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.service.WorkflowService;
import org.testng.Assert;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSystemSettings extends BaseTest {

    private static final String validationDescription = "Number of threads must be positive integer";
    private static final String[][] INVALID_THREAD_COUNT_DATA = {
        {"aaa", validationDescription},
        {"#%", validationDescription},
        {"1.1", validationDescription},
        {"-5", validationDescription}
    };

    @Test
    @TestCaseId("IPBQA-30651")
    @Description("System Settings - Test Dispatching Validation, Verify on Edit, and Thread Number validation")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testSystemSettings() {
        String projectNameForVerification = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        EditorPage editorPage = new EditorPage();
        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        SystemSettingsPageComponent systemSettings = adminPage.navigateToSystemSettingsPage();
        systemSettings.setVerifyOnEdit(true);
        systemSettings.applySettingsAndRelogin(User.ADMIN);

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectNameForVerification, "Main");

        EditorLeftRulesTreeComponent rulesTree = editorPage.getEditorLeftRulesTreeComponent();
        rulesTree.setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE);
        rulesTree.expandFolderInTree("Decision");
        rulesTree.selectItemInFolder("Decision", "Hello");

        // Edit table and verify Save button appears
        TableToolbarPanelComponent tableToolbar = editorPage.getTableToolbarPanelComponent();
        tableToolbar.getEditBtn().click();

        TableComponent table = editorPage.getCenterTable();
        table.editCell(6, 2, "1000", false);

        EditTablePanelComponent editTablePanel = editorPage.getEditTablePanelComponent();
        Assert.assertNotNull(editTablePanel, "Edit table panel should be present when editing");
        editTablePanel.clickSaveChanges();

        // Step 3: Test Verify on Edit = false
        adminPage = editorPage.openUserMenu().navigateToAdministration();
        systemSettings = adminPage.navigateToSystemSettingsPage();

        systemSettings.setVerifyOnEdit(false);
        systemSettings.applySettingsAndRelogin(User.ADMIN);

        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectNameForVerification, "Main");

        rulesTree = editorPage.getEditorLeftRulesTreeComponent();
        rulesTree.setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE);
        rulesTree.expandFolderInTree("Decision");
        rulesTree.selectItemInFolder("Decision", "Hello");

        // Edit table with invalid data
        tableToolbar = editorPage.getTableToolbarPanelComponent();
        tableToolbar.getEditBtn().click();

        table = editorPage.getCenterTable();
        table.editCell(4, 2, "Integer aaa", false);

        // Save changes and check for compilation errors in ProblemsPanel
        editTablePanel = editorPage.getEditTablePanelComponent();
        editTablePanel.clickSaveChanges();

        // Verify that problems panel shows errors
        Assert.assertTrue(editorPage.getProblemsPanelComponent().hasErrors(),
            "Problems panel should show errors when invalid data is saved with Verify on Edit disabled");

        // Step 4: Test Dispatching Validation = true
        adminPage = editorPage.openUserMenu().navigateToAdministration();
        systemSettings = adminPage.navigateToSystemSettingsPage();

        systemSettings.setDispatchingValidation(true);
        systemSettings.applySettingsAndRelogin(User.ADMIN);

        // Create project from Excel file with dispatching validation error
        String projectNameForDispatch = WorkflowService.loginCreateProjectFromExcelFile(User.ADMIN,
            "TestSystemSettings/SmokeStudio.TestSystemSettings.xlsx");

        editorPage = new EditorPage();
        projectSelector = editorPage.getEditorLeftProjectModuleSelectorComponent();
        projectSelector.selectModule(projectNameForDispatch, projectNameForDispatch);

        tableToolbar = editorPage.getTableToolbarPanelComponent();
        tableToolbar.clickTestDropdown().runTests();

        // Verify dispatching validation error message
        Assert.assertTrue(editorPage.getTestResultValidationComponent().isTestTableFailed(),
            "Tests should fail with dispatching validation enabled");

        // Step 5: Test Dispatching Validation = false
        adminPage = editorPage.openUserMenu().navigateToAdministration();
        systemSettings = adminPage.navigateToSystemSettingsPage();

        systemSettings.setDispatchingValidation(false);
        systemSettings.applySettingsAndRelogin(User.ADMIN);

        editorPage = new EditorPage();
        projectSelector = editorPage.getEditorLeftProjectModuleSelectorComponent();
        projectSelector.selectModule(projectNameForDispatch, projectNameForDispatch);

        tableToolbar = editorPage.getTableToolbarPanelComponent();
        tableToolbar.clickTestDropdown().runTests();

        // Verify tests pass now
        Assert.assertTrue(editorPage.getTestResultValidationComponent().isTestTablePassed(),
            "Tests should pass with dispatching validation disabled");

        // Step 6: Test Thread Number validation
        adminPage = editorPage.openUserMenu().navigateToAdministration();
        systemSettings = adminPage.navigateToSystemSettingsPage();

        // Test all invalid thread count values
        for (String[] testData : INVALID_THREAD_COUNT_DATA) {
            validateThreadCountError(systemSettings, testData[0], testData[1]);
        }
    }

    private void validateThreadCountError(SystemSettingsPageComponent systemSettings,
                                         String invalidValue,
                                         String expectedErrorMessage) {
        systemSettings.setTestThreadCount(invalidValue);
        systemSettings.clickApplyButton();
        String errorMsg = systemSettings.getErrorMessage();
        assertThat(errorMsg).contains(expectedErrorMessage);
    }
}
