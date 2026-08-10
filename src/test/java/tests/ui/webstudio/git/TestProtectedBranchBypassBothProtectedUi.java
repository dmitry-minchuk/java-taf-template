package tests.ui.webstudio.git;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.components.common.SyncChangesDialogComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.utils.WaitUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;
import static tests.ui.webstudio.git.ProtectedBranchBypassFixture.DEV_BRANCH;
import static tests.ui.webstudio.git.ProtectedBranchBypassFixture.PROTECTED_TARGET;

public class TestProtectedBranchBypassBothProtectedUi extends BaseTest {

    private static final String PROJECT_NAME = "BypassBothProtectedUiProject";
    private static final String MANAGER_LOGIN = "manager_15960_both";
    private static final String MANAGER_PASSWORD = "manager_15960_both";

    @Test
    @TestCaseId("EPBDS-15960")
    @Description("H.6: merging between two protected branches shows the 'both branches are "
            + "protected' bypass warning naming both the send target and the receive source.")
    @AppContainerConfig(startParams = AppContainerStartParameters.STUDIO_BYPASS_BOTH_PROTECTED_PARAMS)
    public void testBothProtectedBranchesShowBothCopy() {
        ProtectedBranchBypassFixture.provisionProjectAndUser(
                PROJECT_NAME, MANAGER_LOGIN, MANAGER_PASSWORD, "MANAGER");

        LoginService loginService = new LoginService(DriverPool.getPage());
        EditorPage editorPage = loginService.login(new UserData(MANAGER_LOGIN, MANAGER_PASSWORD));

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
                .as("H.6 — bypass warning is shown when both branches are protected")
                .isTrue();
        assertThat(syncDialog.getBypassWarningText())
                .as("H.6 — warning uses the both-protected copy and names both branches")
                .contains("Both branches are protected")
                .contains(PROTECTED_TARGET)
                .contains(DEV_BRANCH);
    }
}
