package tests.ui.webstudio.sso;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.api.AclProjectsMethod;
import domain.api.AuthorizedApiMethod;
import domain.api.GroupsMethod;
import domain.api.ProjectBranchesMethod;
import tests.ui.webstudio.git.ProtectedBranchBypassFixture;
import domain.ui.webstudio.components.common.BypassConfirmDialogComponent;
import domain.ui.webstudio.components.common.SyncChangesDialogComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.KeycloakLoginPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.utils.WaitUtil;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static tests.ui.webstudio.git.ProtectedBranchBypassFixture.DEV_BRANCH;
import static tests.ui.webstudio.git.ProtectedBranchBypassFixture.PROTECTED_TARGET;

/**
 * PLAYWRIGHT_DOCKER test — run with {@code -Dexecution.mode=PLAYWRIGHT_DOCKER}.
 * <p>
 * EPBDS-15960 Z.6 — Manager role granted via GROUP membership (not a direct user grant).
 * An ephemeral Keycloak (OIDC) IdP issues a {@code groups} claim; the Studio group
 * {@code bypass-managers} is granted MANAGER on the project, and the user {@code groupmgr}
 * — whose only project role comes from that group — must be bypass-eligible and able to
 * merge into a protected branch through the UI.
 * <p>
 * The browser, Studio and Keycloak share one Docker network so the OIDC issuer
 * {@code http://keycloak:8080/realms/openlstudio} is identical for all three (parallel-safe,
 * no host port in the SSO path). Hence the browser must run inside the network — PLAYWRIGHT_DOCKER.
 */
public class TestProtectedBranchBypassGroupMembershipUi extends AbstractSsoUiTest {

    private static final String PROJECT_NAME = "BypassGroupUiProject";
    private static final String BYPASS_GROUP = "bypass-managers";
    private static final String GROUP_USER = "groupmgr";
    private static final String GROUP_USER_PASSWORD = "groupmgr";

    @Test
    @TestCaseId("EPBDS-15960")
    @Description("Z.6: a user whose Manager role comes only from Keycloak group membership "
            + "(group bypass-managers granted MANAGER on the project) is bypass-eligible and can "
            + "merge into a protected branch through the UI bypass-confirm flow.")
    @AppContainerConfig(startParams = AppContainerStartParameters.STUDIO_OIDC_BYPASS_PARAMS)
    public void testGroupDerivedManagerCanBypassMerge() {
        // ===== REST setup as admin (bearer token) =====
        AuthorizedApiMethod.setBearerToken(keycloak.getAccessToken("admin", "admin"));
        String projectId;
        try {
            projectId = ProtectedBranchBypassFixture.uploadProject(PROJECT_NAME);
            ProtectedBranchBypassFixture.provisionBranches(projectId);
            ProtectedBranchBypassFixture.commitDivergentChangeOnDev(projectId);
            // Manager role is derived purely from group membership: register the Studio group
            // and grant it MANAGER on the project (non-principal SID), no direct user grant.
            assertThat(new GroupsMethod().createGroup(BYPASS_GROUP).getStatusCode())
                    .as("create Studio group %s", BYPASS_GROUP).isLessThan(300);
            assertThat(new AclProjectsMethod().grantRole(projectId, BYPASS_GROUP, false, "MANAGER").getStatusCode())
                    .as("grant MANAGER on the project to the group (principal=false)").isLessThan(300);
        } finally {
            AuthorizedApiMethod.clearBearerToken();
        }

        // groupmgr's per-user workspace must be on the dev branch before the UI merge.
        AuthorizedApiMethod.setBearerToken(keycloak.getAccessToken(GROUP_USER, GROUP_USER_PASSWORD));
        try {
            assertThat(new ProjectBranchesMethod().switchBranch(projectId, DEV_BRANCH).getStatusCode())
                    .as("groupmgr switches workspace to dev").isLessThan(300);
        } finally {
            AuthorizedApiMethod.clearBearerToken();
        }

        // ===== UI flow as groupmgr via Keycloak SSO =====
        LocalDriverPool.getPage().navigate(LocalDriverPool.getAppUrl());
        EditorPage editorPage = new KeycloakLoginPage().login(GROUP_USER, GROUP_USER_PASSWORD);

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
                .as("Z.6 — group-derived Manager sees the bypass warning on a protected target")
                .isTrue();

        syncDialog.clickSendYourUpdates();
        BypassConfirmDialogComponent confirmDialog = repositoryPage.getBypassConfirmDialogComponent()
                .waitForDialogToAppear();
        confirmDialog.clickConfirmBypassAndMerge();

        assertThat(confirmDialog.isMergeSuccessNoticeVisible())
                .as("Z.6 — confirming bypass merges successfully for a group-derived Manager")
                .isTrue();
    }
}
