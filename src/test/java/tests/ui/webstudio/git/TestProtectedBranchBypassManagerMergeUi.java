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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;
import static tests.ui.webstudio.git.ProtectedBranchBypassFixture.MERGE_SUCCESS_TOAST;
import static tests.ui.webstudio.git.ProtectedBranchBypassFixture.PROTECTED_TARGET;

/**
 * EPBDS-15960 sections G/H — UI bypass merge flow as a Manager with
 * {@code security.allow-bypass-protected-branches=ON}: in-dialog warning,
 * secondary confirmation modal, "Merge Successful" toast.
 */
public class TestProtectedBranchBypassManagerMergeUi extends BaseTest {

    private static final String PROJECT_NAME = "BypassMergeUiProject";
    private static final String MANAGER_LOGIN = "manager_15960";
    private static final String MANAGER_PASSWORD = "manager_15960";

    @AfterMethod(alwaysRun = true)
    public void deleteManagerUser() {
        try {
            new UsersMethod().deleteUser(MANAGER_LOGIN);
        } catch (Exception ignored) {
        }
    }

    @Test
    @TestCaseId("EPBDS-15960")
    @Description("Sections G/H: Manager merging into a protected branch with bypass=ON sees "
            + "the in-dialog warning, the secondary confirmation modal, and a Merge Successful toast.")
    @AppContainerConfig(startParams = AppContainerStartParameters.STUDIO_BYPASS_ENABLED_PARAMS)
    public void testManagerBypassMergeFlowUi() {
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
        helpers.utils.WaitUtil.waitForCondition(() -> {
            editor.getEditorToolbarPanelComponent().clickSync();
            return syncDialog.isVisible();
        }, 15_000, 1_000, "Click Sync until the merge dialog appears");
        syncDialog.selectBranch(PROTECTED_TARGET);

        assertThat(syncDialog.isBypassWarningVisible())
                .as("G.1 — Sync dialog must show the bypass warning for a protected target")
                .isTrue();
        assertThat(syncDialog.getBypassWarningText())
                .as("G.2 — bypass warning must name the protected branch")
                .contains("Bypass branch protection?")
                .contains(PROTECTED_TARGET);

        syncDialog.clickSendYourUpdates();
        BypassConfirmDialogComponent confirmDialog = repositoryPage.getBypassConfirmDialogComponent()
                .waitForDialogToAppear();
        assertThat(confirmDialog.getTitle())
                .as("H.1 — confirmation modal title")
                .isEqualTo("Bypass branch protection?");

        confirmDialog.clickConfirmBypassAndMerge();
        assertThat(confirmDialog.isMergeSuccessNoticeVisible())
                .as("H.2 — '%s' toast after confirming bypass", MERGE_SUCCESS_TOAST)
                .isTrue();
    }

}
