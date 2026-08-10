package tests.ui.webstudio.git;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.components.common.BypassConfirmDialogComponent;
import domain.ui.webstudio.components.common.SyncChangesDialogComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.utils.WaitUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;
import static tests.ui.webstudio.git.ProtectedBranchBypassFixture.PROTECTED_TARGET;

public class TestProtectedBranchBypassContributorBlockedUi extends BaseTest {

    private static final String PROJECT_NAME = "BypassContributorUiProject";
    private static final String CONTRIBUTOR_LOGIN = "contributor_15960";
    private static final String CONTRIBUTOR_PASSWORD = "contributor_15960";

    @Test
    @TestCaseId("EPBDS-15960")
    @Description("H.4: A Contributor (not bypass-eligible) attempting to merge into a "
            + "protected branch is told the branch is protected in the Sync dialog and NOT "
            + "the 'Bypass branch protection?' confirmation modal.")
    @AppContainerConfig(startParams = AppContainerStartParameters.STUDIO_BYPASS_ENABLED_PARAMS)
    public void testContributorSeesErrorInsteadOfBypassConfirm() {
        ProtectedBranchBypassFixture.provisionProjectAndUser(
                PROJECT_NAME, CONTRIBUTOR_LOGIN, CONTRIBUTOR_PASSWORD, "CONTRIBUTOR");

        LoginService loginService = new LoginService(DriverPool.getPage());
        EditorPage editorPage = loginService.login(
                new UserData(CONTRIBUTOR_LOGIN, CONTRIBUTOR_PASSWORD));

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
                .as("H.4 — bypass warning must NOT be shown for a Contributor on a protected target")
                .isFalse();

        assertThat(syncDialog.hasBlockedMessageContaining("protected"))
                .as("H.4 — Contributor must be told the target branch is protected")
                .isTrue();

        BypassConfirmDialogComponent confirmDialog = repositoryPage.getBypassConfirmDialogComponent();
        assertThat(confirmDialog.waitForDialogToDisappear())
                .as("H.4 — bypass confirmation modal must NOT open for a Contributor")
                .isTrue();
    }
}
