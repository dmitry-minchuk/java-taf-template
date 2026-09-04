package tests.ui.webstudio.studio_smoke;

import configuration.annotations.KnownIssue;
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
import helpers.utils.WaitUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestAdminSystemSettings extends BaseTest {

    private static final String validationDescription = "Number of threads must be positive integer";
    private static final String[][] INVALID_THREAD_COUNT_DATA = {
        {"aaa", validationDescription},
        {"#%", validationDescription},
        {"1.1", validationDescription},
        {"-5", validationDescription}
    };

    @Test
    @TestCaseId("IPBQA-30651")
    @Description("System Settings - Test Dispatching Validation, Verify on Edit, and Thread Number validation."
            + " Known bug: EPBDS-15704.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    @KnownIssue("EPBDS-15704")
    public void testSystemSettings() {
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

        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(6, 2, "1000", true);
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

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

        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(4, 2, "Integer aaa", true);
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();

        editorPage.getProblemsPanelComponent().checkNoProblems();
        assertThat(editorPage.getEditorToolbarPanelComponent().isVerifyButtonPresent()).as("Verify button should be present when Verify on Edit is disabled").isTrue();
        editorPage.getEditorToolbarPanelComponent().clickVerify();
        WaitUtil.waitForCondition(() -> editorPage.getProblemsPanelComponent().getErrorsCount() == 1, 5000, 100, "Waiting for errors ti be listed...");
        assertThat(editorPage.getProblemsPanelComponent().getErrorsCount()).as("Should have 1 error after clicking Verify button").isEqualTo(1);

        systemSettings = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToSystemSettingsPage();
        systemSettings.setDispatchingValidation(true);
        systemSettings.clickApplyButton();

        String projectNameForDispatch = WorkflowService.loginCreateProjectFromExcelFile(User.ADMIN, "TestSystemSettings.xlsx");

        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectNameForDispatch, "TestSystemSettings");

        editorPage.getEditorToolbarPanelComponent()
                .clickTestDropdown()
                .runTests();

        assertThat(editorPage.getTestResultValidationComponent().isTestTableFailed()).as("Tests should fail with dispatching validation enabled").isTrue();

        systemSettings = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToSystemSettingsPage();
        systemSettings.setDispatchingValidation(false);
        systemSettings.applySettingsAndRelogin(User.ADMIN);

        editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(projectNameForDispatch, "TestSystemSettings");

        editorPage.getEditorToolbarPanelComponent()
                .clickTestDropdown()
                .runTests();

        assertThat(editorPage.getTestResultValidationComponent().isTestTablePassed()).as("Tests should pass with dispatching validation disabled").isTrue();

        systemSettings = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToSystemSettingsPage();

        for (String[] testData : INVALID_THREAD_COUNT_DATA) {
            validateThreadCountError(systemSettings, testData[0], testData[1]);
        }

        systemSettings = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToSystemSettingsPage();

        String originalDateFormat = systemSettings.getDateFormat();
        String originalTimeFormat = systemSettings.getTimeFormat();

        systemSettings.setDateFormat("yyyy-MM-dd");
        systemSettings.setTimeFormat("HH:mm:ss");
        systemSettings.applySettingsAndRelogin(User.ADMIN);

        systemSettings = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToSystemSettingsPage();
        assertThat(systemSettings.getDateFormat()).isEqualTo("yyyy-MM-dd");
        assertThat(systemSettings.getTimeFormat()).isEqualTo("HH:mm:ss");

        systemSettings.setDateFormat(originalDateFormat);
        systemSettings.setTimeFormat(originalTimeFormat);
        systemSettings.applySettingsAndRelogin(User.ADMIN);

        systemSettings = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToSystemSettingsPage();
        systemSettings.setDateFormat("abc");
        systemSettings.clickApplyButton();
        assertThat(systemSettings.getAllMessages()).contains("Error: Invalid date pattern");

        systemSettings.setDateFormat(originalDateFormat);
        systemSettings.setTimeFormat("xyz");
        systemSettings.clickApplyButton();
        assertThat(systemSettings.getAllMessages()).contains("Error: Invalid time pattern");

        systemSettings.setTimeFormat(originalTimeFormat);
        systemSettings.setDateFormat("");
        systemSettings.clickApplyButton();
        assertThat(systemSettings.getAllMessages()).contains("Error: Cannot be empty.");

        systemSettings.closeAllMessages();
        systemSettings.setDateFormat(originalDateFormat);
        systemSettings.setTimeFormat("");
        systemSettings.clickApplyButton();
        assertThat(systemSettings.getAllMessages()).contains("Error: Cannot be empty.");
    }

    private void validateThreadCountError(SystemSettingsPageComponent systemSettings,
                                         String invalidValue,
                                         String expectedErrorMessage) {
        systemSettings.setTestThreadCount(invalidValue);
        systemSettings.clickApplyButton();
        assertThat(systemSettings.getAllMessages()).contains(expectedErrorMessage);
    }
}
