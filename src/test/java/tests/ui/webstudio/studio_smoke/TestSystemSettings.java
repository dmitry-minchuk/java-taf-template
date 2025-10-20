package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.SystemSettingsPageComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
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
        // Step 1 & 2: Create project and test Verify on Edit = true
        String projectNameForVerification = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");
        EditorPage editorPage = new EditorPage();

        SystemSettingsPageComponent systemSettings = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToSystemSettingsPage();
        systemSettings.setVerifyOnEdit(true);
        systemSettings.applySettingsAndRelogin(User.ADMIN);

        editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectNameForVerification, "Main");

        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        editorPage.getTableToolbarPanelComponent().getEditBtn().click();
        editorPage.getCenterTable().editCell(6, 2, "1000", true);
        editorPage.getEditTablePanelComponent().clickSaveChanges();

        // Step 3: Test Verify on Edit = false
        systemSettings = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToSystemSettingsPage();
        systemSettings.setVerifyOnEdit(false);
        systemSettings.applySettingsAndRelogin(User.ADMIN);

        editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectNameForVerification, "Main");

        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "Hello");

        editorPage.getTableToolbarPanelComponent().getEditBtn().click();
        editorPage.getCenterTable().editCell(4, 2, "Integer aaa", true);
        editorPage.getEditTablePanelComponent().clickSaveChanges();

        editorPage.getProblemsPanelComponent().checkNoProblems();
        Assert.assertTrue(editorPage.getTableToolbarPanelComponent().isVerifyButtonPresent(), "Verify button should be present when Verify on Edit is disabled");
        editorPage.getTableToolbarPanelComponent().clickVerify();
        Assert.assertEquals(editorPage.getProblemsPanelComponent().getErrorsCount(), 1, "Should have 1 error after clicking Verify button");

        // Step 4: Test Dispatching Validation = true
        systemSettings = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToSystemSettingsPage();
        systemSettings.setDispatchingValidation(true);
        systemSettings.clickApplyButton();

        String projectNameForDispatch = WorkflowService.loginCreateProjectFromExcelFile(User.ADMIN, "TestSystemSettings.xlsx");

        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectNameForDispatch, "TestSystemSettings");

        editorPage.getTableToolbarPanelComponent()
                .clickTestDropdown()
                .runTests();

        Assert.assertTrue(editorPage.getTestResultValidationComponent().isTestTableFailed(), "Tests should fail with dispatching validation enabled");

        // Step 5: Test Dispatching Validation = false
        systemSettings = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToSystemSettingsPage();
        systemSettings.setDispatchingValidation(false);
        systemSettings.applySettingsAndRelogin(User.ADMIN);

        editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectNameForDispatch, "TestSystemSettings");

        editorPage.getTableToolbarPanelComponent()
                .clickTestDropdown()
                .runTests();

        Assert.assertTrue(editorPage.getTestResultValidationComponent().isTestTablePassed(), "Tests should pass with dispatching validation disabled");

        // Step 6: Test Thread Number validation
        systemSettings = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToSystemSettingsPage();

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
        assertThat(errorMsg).contains(expectedErrorMessage); // BUG: no errors shown
    }
}
