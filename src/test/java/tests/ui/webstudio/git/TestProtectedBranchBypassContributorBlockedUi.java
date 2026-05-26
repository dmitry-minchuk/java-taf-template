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
 * EPBDS-15960 H.4: a Contributor (not bypass-eligible) trying to merge a dev
 * branch into a protected release branch sees the plain "privileges" error
 * in the Sync dialog, not the bypass confirmation modal.
 */
public class TestProtectedBranchBypassContributorBlockedUi extends BaseTest {

    private static final String PROJECT_NAME = "BypassContributorUiProject";
    private static final String CONTRIBUTOR_LOGIN = "contributor_15960";
    private static final String CONTRIBUTOR_PASSWORD = "contributor_15960";

    @AfterMethod(alwaysRun = true)
    public void deleteUser() {
        try {
            new UsersMethod().deleteUser(CONTRIBUTOR_LOGIN);
        } catch (Exception ignored) {
        }
    }

    @Test
    @TestCaseId("EPBDS-15960")
    @Description("H.4: A Contributor (not bypass-eligible) attempting to merge into a "
            + "protected branch sees a plain privileges error in the Sync dialog and NOT "
            + "the 'Bypass branch protection?' confirmation modal.")
    @AppContainerConfig(startParams = AppContainerStartParameters.STUDIO_BYPASS_ENABLED_PARAMS)
    public void testContributorSeesErrorInsteadOfBypassConfirm() {
        ProtectedBranchBypassFixture.provisionProjectAndUser(
                PROJECT_NAME, CONTRIBUTOR_LOGIN, CONTRIBUTOR_PASSWORD, "CONTRIBUTOR");

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(
                new UserData(CONTRIBUTOR_LOGIN, CONTRIBUTOR_PASSWORD));

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

        assertThat(syncDialog.isBypassWarningVisible())
                .as("H.4 — bypass warning must NOT be shown for a Contributor on a protected target")
                .isFalse();

        assertThat(syncDialog.hasErrorMessageContaining("privileges"))
                .as("H.4 — Contributor must see the 'privileges' error in the Sync dialog")
                .isTrue();

        BypassConfirmDialogComponent confirmDialog = repositoryPage.getBypassConfirmDialogComponent();
        assertThat(confirmDialog.waitForDialogToDisappear())
                .as("H.4 — bypass confirmation modal must NOT open for a Contributor")
                .isTrue();
    }
}
