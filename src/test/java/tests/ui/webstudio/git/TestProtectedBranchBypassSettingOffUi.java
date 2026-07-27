package tests.ui.webstudio.git;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.api.UsersMethod;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.components.common.BypassConfirmDialogComponent;
import domain.ui.webstudio.components.common.SyncChangesDialogComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.utils.WaitUtil;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;
import static tests.ui.webstudio.git.ProtectedBranchBypassFixture.PROTECTED_TARGET;

/**
 * EPBDS-15960 H.8 — with the global bypass setting OFF, an eligible Manager merging into a
 * protected branch sees the same blocked path as a non-eligible user: a protected-branch note in
 * the Sync dialog, no bypass warning and no "Bypass branch protection?" confirmation modal.
 */
public class TestProtectedBranchBypassSettingOffUi extends BaseTest {

    private static final String PROJECT_NAME = "BypassOffUiProject";
    private static final String MANAGER_LOGIN = "manager_15960_off";
    private static final String MANAGER_PASSWORD = "manager_15960_off";

    @AfterMethod(alwaysRun = true)
    public void deleteManagerUser() {
        try {
            new UsersMethod().deleteUser(MANAGER_LOGIN);
        } catch (Exception ignored) {
        }
    }

    @Test
    @TestCaseId("EPBDS-15960")
    @Description("H.8: setting OFF hides the bypass flow — an eligible Manager merging into a "
            + "protected branch is told the branch is protected, with no bypass warning and no confirm modal.")
    @AppContainerConfig(startParams = AppContainerStartParameters.STUDIO_PROTECTED_NO_BYPASS_PARAMS)
    public void testManagerBlockedWhenBypassSettingOff() {
        ProtectedBranchBypassFixture.provisionProjectAndUser(
                PROJECT_NAME, MANAGER_LOGIN, MANAGER_PASSWORD, "MANAGER");

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(new UserData(MANAGER_LOGIN, MANAGER_PASSWORD));

        // React nav: open the project from the /projects list, then open the Sync dialog from the editor
        // toolbar (the Sync dialog + bypass confirm are already React components).
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.openProject(PROJECT_NAME);
        EditorPage editor = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editor.getEditorLeftProjectModuleSelectorComponent().selectProject(PROJECT_NAME);

        SyncChangesDialogComponent syncDialog = repositoryPage.getSyncChangesDialogComponent();
        WaitUtil.waitForCondition(() -> {
            editor.getEditorToolbarPanelComponent().clickSync();
            return syncDialog.isVisible();
        }, 15_000, 1_000, "Click Sync until the merge dialog appears");
        syncDialog.selectBranch(PROTECTED_TARGET);

        assertThat(syncDialog.isBypassWarningVisible())
                .as("H.8 — no bypass warning when the global setting is OFF")
                .isFalse();
        // With the setting OFF a Manager is blocked the same way anyone else is: 6.4.0 says the target
        // branch is protected and disables the merge buttons.
        assertThat(syncDialog.hasBlockedMessageContaining("protected"))
                .as("H.8 — Manager is told the branch is protected when the setting is OFF")
                .isTrue();

        BypassConfirmDialogComponent confirmDialog = repositoryPage.getBypassConfirmDialogComponent();
        assertThat(confirmDialog.waitForDialogToDisappear())
                .as("H.8 — no bypass confirmation modal when the setting is OFF")
                .isTrue();
    }
}
