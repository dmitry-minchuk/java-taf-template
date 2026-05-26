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
 * EPBDS-15960 H.3: clicking Cancel on the bypass confirmation modal closes
 * the confirmation, leaves the Sync dialog in its pre-Send state, and does
 * NOT merge — the bypass warning is still shown, and a subsequent click on
 * "Send your updates" still produces the same confirmation modal.
 */
public class TestProtectedBranchBypassCancelMergeUi extends BaseTest {

    private static final String PROJECT_NAME = "BypassCancelUiProject";
    private static final String MANAGER_LOGIN = "manager_15960_cancel";
    private static final String MANAGER_PASSWORD = "manager_15960_cancel";

    @AfterMethod(alwaysRun = true)
    public void deleteManagerUser() {
        try {
            new UsersMethod().deleteUser(MANAGER_LOGIN);
        } catch (Exception ignored) {
        }
    }

    @Test
    @TestCaseId("EPBDS-15960")
    @Description("H.3: Cancel on the bypass confirmation modal aborts the merge — the modal "
            + "closes, no Merge Successful toast appears, and the Sync dialog is still showing "
            + "the bypass warning so the user can retry or close it manually.")
    @AppContainerConfig(startParams = AppContainerStartParameters.STUDIO_BYPASS_ENABLED_PARAMS)
    public void testCancelOnBypassConfirmAbortsTheMerge() {
        ProtectedBranchBypassFixture.provisionProjectAndUser(
                PROJECT_NAME, MANAGER_LOGIN, MANAGER_PASSWORD, "MANAGER");

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(new UserData(MANAGER_LOGIN, MANAGER_PASSWORD));

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.refresh();
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);
        repositoryPage.getRepositoryContentButtonsPanelComponent().openProjectAndWait();

        SyncChangesDialogComponent syncDialog = repositoryPage.getSyncChangesDialogComponent();
        WaitUtil.waitForCondition(() -> {
            repositoryPage.getRepositoryContentButtonsPanelComponent().clickSync();
            return syncDialog.isVisible();
        }, 15_000, 1_000, "Click Sync until the merge dialog appears");
        syncDialog.selectBranch(PROTECTED_TARGET);

        syncDialog.clickSendYourUpdates();
        BypassConfirmDialogComponent confirmDialog = repositoryPage.getBypassConfirmDialogComponent()
                .waitForDialogToAppear();
        confirmDialog.clickCancel();

        assertThat(confirmDialog.waitForDialogToDisappear())
                .as("H.3 — confirmation modal disappears after Cancel")
                .isTrue();
        assertThat(confirmDialog.isMergeSuccessNoticeAbsent())
                .as("H.3 — Cancel must not produce a Merge Successful toast")
                .isTrue();
        assertThat(syncDialog.isVisible())
                .as("H.3 — Sync dialog is still open after Cancel so the user can retry or close")
                .isTrue();
        assertThat(syncDialog.isBypassWarningVisible())
                .as("H.3 — bypass warning is still rendered, target is still protected")
                .isTrue();

        // Re-click Send to confirm the bypass flow is not in a stuck state after Cancel.
        syncDialog.clickSendYourUpdates();
        assertThat(repositoryPage.getBypassConfirmDialogComponent().waitForDialogToAppear().getTitle())
                .as("H.3 — re-click Send after Cancel re-opens the same confirmation modal")
                .isEqualTo("Bypass branch protection?");
    }
}
