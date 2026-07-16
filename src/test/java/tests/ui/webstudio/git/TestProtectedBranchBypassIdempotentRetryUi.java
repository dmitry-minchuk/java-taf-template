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
import static tests.ui.webstudio.git.ProtectedBranchBypassFixture.MERGE_SUCCESS_TOAST;
import static tests.ui.webstudio.git.ProtectedBranchBypassFixture.PROTECTED_TARGET;

/**
 * EPBDS-15960 H.7: a bypass merge is idempotent — after a Manager confirms the
 * bypass and the merge succeeds, re-opening the Sync dialog for the same
 * direction shows the branches as up-to-date (Send disabled), so the same
 * merge cannot be applied twice.
 */
public class TestProtectedBranchBypassIdempotentRetryUi extends BaseTest {

    private static final String PROJECT_NAME = "BypassIdempotentUiProject";
    private static final String MANAGER_LOGIN = "manager_15960_retry";
    private static final String MANAGER_PASSWORD = "manager_15960_retry";

    @AfterMethod(alwaysRun = true)
    public void deleteManagerUser() {
        try {
            new UsersMethod().deleteUser(MANAGER_LOGIN);
        } catch (Exception ignored) {
        }
    }

    @Test
    @TestCaseId("EPBDS-15960")
    @Description("H.7: after a successful bypass merge, re-opening the Sync dialog for the "
            + "same direction shows the branches up-to-date with Send disabled — the merge "
            + "is idempotent and cannot be re-applied.")
    @AppContainerConfig(startParams = AppContainerStartParameters.STUDIO_BYPASS_ENABLED_PARAMS)
    public void testBypassMergeIsIdempotent() {
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
        syncDialog.clickSendYourUpdates();

        BypassConfirmDialogComponent confirmDialog = repositoryPage.getBypassConfirmDialogComponent()
                .waitForDialogToAppear();
        confirmDialog.clickConfirmBypassAndMerge();
        assertThat(confirmDialog.isMergeSuccessNoticeVisible())
                .as("first bypass merge succeeds with a '%s' toast", MERGE_SUCCESS_TOAST)
                .isTrue();

        // After the merge the project may drop from the editor workspace — re-open it if needed, then
        // re-open the Sync dialog from the editor toolbar for the same direction.
        repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        if (repositoryPage.isProjectActionAvailable(PROJECT_NAME, "Open")) {
            repositoryPage.openProject(PROJECT_NAME);
        }
        editor.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editor.getEditorLeftProjectModuleSelectorComponent().selectProject(PROJECT_NAME);
        WaitUtil.waitForCondition(() -> {
            editor.getEditorToolbarPanelComponent().clickSync();
            return syncDialog.isVisible();
        }, 15_000, 1_000, "Re-open the Sync dialog after the merge");
        syncDialog.selectBranch(PROTECTED_TARGET);

        assertThat(syncDialog.isExportButtonEnabled())
                .as("H.7 — Send is disabled after the merge (branches are up-to-date)")
                .isFalse();
    }
}
