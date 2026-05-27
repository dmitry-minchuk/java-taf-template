package tests.ui.webstudio.sso;

import configuration.driver.LocalDriverPool;
import domain.api.AuthorizedApiMethod;
import domain.api.RepositoryProjectsMethod;
import domain.ui.webstudio.components.admincomponents.UsersPageComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.repositorytabcomponents.RepositoryContentButtonsPanelComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.KeycloakLoginPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.KeycloakInfrastructureService;
import helpers.utils.TestDataUtil;
import io.restassured.response.Response;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import tests.BaseTest;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PLAYWRIGHT_DOCKER base for the Admin 'Users' view tests under an external IdP (Keycloak).
 * Shared flow (IPBQA-32788/32789): an external user is synced via SSO login, the admin assigns
 * a per-project role (Manager → Viewer) through the Admin 'Users' view, and after the user
 * re-logs in via SSO the repository access matches the role. Subclasses only differ by the
 * Studio auth-mode container ({@code @AppContainerConfig}); the IdP is the same Keycloak.
 */
public abstract class AbstractUsersViewRolesSsoTest extends BaseTest {

    protected static final String DESIGN_REPO = "design";
    protected static final String PROJECT_NAME = "SsoUsersViewProject";
    protected static final String PROJECT_ZIP = "RulesEditor.TestSearchOnProjectLevel.Example1BankRating.zip";
    protected static final String EXTERNAL_USER = "studiouser";

    protected final KeycloakInfrastructureService keycloak = new KeycloakInfrastructureService();

    @Override
    @BeforeMethod
    public void beforeMethod(ITestResult result) {
        if (!"PLAYWRIGHT_DOCKER".equalsIgnoreCase(System.getProperty("execution.mode", "PLAYWRIGHT_LOCAL"))) {
            throw new SkipException("External-auth Users-view test requires -Dexecution.mode=PLAYWRIGHT_DOCKER "
                    + "(browser must share the Docker network with Keycloak).");
        }
        keycloak.start();
        super.beforeMethod(result);
    }

    @Override
    @AfterMethod
    public void afterMethod(ITestResult result) {
        try {
            super.afterMethod(result);
        } finally {
            keycloak.stop();
        }
    }

    /** Manager→Viewer role-assignment-and-access flow, identical across auth modes. */
    protected void runUsersViewRoleFlow() {
        uploadProjectAsAdmin();

        // 1. External user logs in once via SSO so Studio syncs them into its user table.
        ssoLogin(EXTERNAL_USER, EXTERNAL_USER);
        assertThat(repositoryProjects()).as("external user has no project access before any role").doesNotContain(PROJECT_NAME);

        // 2. Admin assigns the Manager role on the project through the Admin 'Users' view.
        assignProjectRole("Manager", true);

        // 3. External user now sees Manager affordances on the project.
        ssoLogin(EXTERNAL_USER, EXTERNAL_USER);
        RepositoryContentButtonsPanelComponent managerToolbar = selectProjectToolbar();
        assertThat(managerToolbar.isCopyBtnVisible())
                .as("group/role Manager via external auth sees the Copy action").isTrue();

        // 4. Admin downgrades the role to Viewer.
        assignProjectRole("Viewer", false);

        // 5. External user now sees only read-only (Viewer) affordances.
        ssoLogin(EXTERNAL_USER, EXTERNAL_USER);
        RepositoryContentButtonsPanelComponent viewerToolbar = selectProjectToolbar();
        assertThat(viewerToolbar.isCopyBtnVisible()).as("Viewer does NOT see the Copy action").isFalse();
        assertThat(viewerToolbar.isExportBtnVisible()).as("Viewer still sees the Export action").isTrue();
    }

    private void uploadProjectAsAdmin() {
        AuthorizedApiMethod.setBearerToken(keycloak.getAccessToken("admin", "admin"));
        try {
            File zip = new File(TestDataUtil.getFilePathFromResources(PROJECT_ZIP));
            Response upload = new RepositoryProjectsMethod().uploadProject(DESIGN_REPO, PROJECT_NAME, zip);
            assertThat(upload.getStatusCode()).as("upload project %s", PROJECT_NAME).isLessThan(300);
        } finally {
            AuthorizedApiMethod.clearBearerToken();
        }
    }

    private void assignProjectRole(String role, boolean addNewRow) {
        EditorPage editorPage = ssoLogin("admin", "admin");
        UsersPageComponent users = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();
        assertThat(users.isUserInList(EXTERNAL_USER))
                .as("synced external user %s is listed in the Admin Users view", EXTERNAL_USER).isTrue();
        users.clickEditUser(EXTERNAL_USER).clickProjectsTab();
        if (addNewRow) {
            users.clickAddRoleBtn().setProject(0, PROJECT_NAME);
        }
        users.setProjectRole(0, role).saveUser();
    }

    /** Clears the browser session so the next SSO login starts fresh (switches IdP user). */
    protected EditorPage ssoLogin(String username, String password) {
        LocalDriverPool.getBrowserContext().clearCookies();
        LocalDriverPool.getPage().navigate(LocalDriverPool.getAppUrl());
        return new KeycloakLoginPage().login(username, password);
    }

    private RepositoryPage goToRepository() {
        RepositoryPage repositoryPage = new EditorPage().getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.refresh();
        return repositoryPage;
    }

    private java.util.List<String> repositoryProjects() {
        return goToRepository().getAllVisibleProjectsInTable();
    }

    private RepositoryContentButtonsPanelComponent selectProjectToolbar() {
        RepositoryPage repositoryPage = goToRepository();
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);
        return repositoryPage.getRepositoryContentButtonsPanelComponent();
    }
}
