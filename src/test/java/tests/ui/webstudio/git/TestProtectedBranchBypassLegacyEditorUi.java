package tests.ui.webstudio.git;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.api.AclProjectsMethod;
import domain.api.ProjectBranchesMethod;
import domain.api.ProjectsMethod;
import domain.api.RepositoryProjectsMethod;
import domain.api.UsersMethod;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.repositorytabcomponents.RepositoryContentButtonsPanelComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.utils.TestDataUtil;
import io.restassured.response.Response;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static tests.ui.webstudio.git.ProtectedBranchBypassFixture.DESIGN_REPO;
import static tests.ui.webstudio.git.ProtectedBranchBypassFixture.PROJECT_ZIP;
import static tests.ui.webstudio.git.ProtectedBranchBypassFixture.PROTECTED_TARGET;

/**
 * EPBDS-15960 Section I — legacy (RichFaces/JSF) UI implicit bypass. On a protected branch the
 * legacy editor gates mutating actions through {@code isCurrentBranchProtected()} →
 * {@code ProtectedBranchBypassService.isProtectionEnforced(...)}. An eligible Manager with the
 * global setting ON sees the branch as not protected (edit affordances rendered); a Contributor
 * does not. The Upload File / Add Folder toolbar buttons are driven by {@code RepositoryTreeState.canAppend},
 * which is bypass-aware, so they are the observable affordance for this contract.
 */
public class TestProtectedBranchBypassLegacyEditorUi extends BaseTest {

    private static final String PROJECT_NAME = "BypassLegacyUiProject";
    private static final String MANAGER_LOGIN = "manager_15960_legacy";
    private static final String CONTRIBUTOR_LOGIN = "contributor_15960_legacy";

    @AfterMethod(alwaysRun = true)
    public void deleteUsers() {
        UsersMethod users = new UsersMethod();
        try {
            users.deleteUser(MANAGER_LOGIN);
        } catch (Exception ignored) {
        }
        try {
            users.deleteUser(CONTRIBUTOR_LOGIN);
        } catch (Exception ignored) {
        }
    }

    @Test
    @TestCaseId("EPBDS-15960")
    @Description("I.1: an eligible Manager with bypass ON, viewing a project on a protected branch "
            + "in the legacy UI, sees the mutating toolbar actions (Add Folder / Upload File) rendered.")
    @AppContainerConfig(startParams = AppContainerStartParameters.STUDIO_BYPASS_ENABLED_PARAMS)
    public void testEligibleManagerCanEditOnProtectedBranchLegacy() {
        String projectId = provisionProjectOnProtectedBranch(MANAGER_LOGIN, "MANAGER");

        RepositoryContentButtonsPanelComponent toolbar = openProjectAs(MANAGER_LOGIN);

        assertThat(toolbar.isUploadFileBtnVisible())
                .as("I.1 — Manager + bypass ON: Upload File is rendered on the protected branch")
                .isTrue();
        assertThat(toolbar.isAddFolderBtnVisible())
                .as("I.1 — Manager + bypass ON: Add Folder is rendered on the protected branch")
                .isTrue();
    }

    @Test
    @TestCaseId("EPBDS-15960")
    @Description("I.3: a Contributor (never bypass-eligible) viewing a project on a protected branch "
            + "in the legacy UI does NOT see the mutating toolbar actions — the branch stays protected.")
    @AppContainerConfig(startParams = AppContainerStartParameters.STUDIO_BYPASS_ENABLED_PARAMS)
    public void testContributorCannotEditOnProtectedBranchLegacy() {
        String projectId = provisionProjectOnProtectedBranch(CONTRIBUTOR_LOGIN, "CONTRIBUTOR");

        RepositoryContentButtonsPanelComponent toolbar = openProjectAs(CONTRIBUTOR_LOGIN);

        assertThat(toolbar.isUploadFileBtnVisible())
                .as("I.3 — Contributor: Upload File is NOT rendered on the protected branch")
                .isFalse();
        assertThat(toolbar.isAddFolderBtnVisible())
                .as("I.3 — Contributor: Add Folder is NOT rendered on the protected branch")
                .isFalse();
    }

    private String provisionProjectOnProtectedBranch(String login, String role) {
        ProtectedBranchBypassFixture.configureAdminCommitIdentity();
        File zip = new File(TestDataUtil.getFilePathFromResources(PROJECT_ZIP));
        assertThat(new RepositoryProjectsMethod().uploadProject(DESIGN_REPO, PROJECT_NAME, zip).getStatusCode())
                .as("upload project").isLessThan(300);
        String projectId = resolveProjectId();
        assertThat(new ProjectBranchesMethod().createBranch(projectId, PROTECTED_TARGET).getStatusCode())
                .as("create protected branch %s", PROTECTED_TARGET).isLessThan(300);

        UsersMethod users = new UsersMethod();
        assertThat(users.createUser(login, login).getStatusCode()).as("create user %s", login).isLessThan(300);
        assertThat(new AclProjectsMethod().grantRole(projectId, login, true, role).getStatusCode())
                .as("grant %s to %s", role, login).isLessThan(300);

        // Put the user's workspace onto the protected branch so the legacy toolbar reflects it.
        assertThat(new ProjectBranchesMethod()
                .switchBranch(projectId, PROTECTED_TARGET, new UserData(login, login)).getStatusCode())
                .as("%s switches workspace to the protected branch", login).isLessThan(300);
        return projectId;
    }

    private RepositoryContentButtonsPanelComponent openProjectAs(String login) {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(new UserData(login, login));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.refresh();
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);
        repositoryPage.getRepositoryContentButtonsPanelComponent().openProjectAndWait();
        return repositoryPage.getRepositoryContentButtonsPanelComponent();
    }

    private String resolveProjectId() {
        Response resp = new ProjectsMethod().getAllProjects(500);
        List<Map<String, Object>> content = resp.jsonPath().getList("content");
        return content.stream()
                .filter(p -> PROJECT_NAME.equals(p.get("name")))
                .map(p -> String.valueOf(p.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("project " + PROJECT_NAME + " not found"));
    }
}
