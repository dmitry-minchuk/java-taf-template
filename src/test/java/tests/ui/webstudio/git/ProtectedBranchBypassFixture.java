package tests.ui.webstudio.git;

import domain.api.AclProjectsMethod;
import domain.api.ProjectBranchesMethod;
import domain.api.ProjectResourcesMethod;
import domain.api.ProjectsMethod;
import domain.api.RepositoryProjectsMethod;
import domain.api.UsersMethod;
import domain.serviceclasses.models.UserData;
import helpers.utils.TestDataUtil;
import io.restassured.response.Response;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Shared REST setup plumbing for EPBDS-15960 Section H tests: upload a project,
 * provision a protected release branch + a dev branch with a divergent commit,
 * create a Manager (or Contributor) user with the requested ACL role, release
 * the workspace lock the admin REST steps leave behind.
 */
public final class ProtectedBranchBypassFixture {

    public static final String DESIGN_REPO = "design";
    public static final String PROTECTED_TARGET = "release-EPBDS-15818";
    public static final String DEV_BRANCH = "EPBDS-15818_dev";
    public static final String PROJECT_ZIP = "RulesEditor.TestSearchOnProjectLevel.Example1BankRating.zip";
    public static final String DIVERGENT_FILE = "rules.xml";
    public static final String MERGE_SUCCESS_TOAST = "Merge Successful";

    private ProtectedBranchBypassFixture() {
    }

    /** End-to-end setup: identity, project, branches, divergent commit, user + ACL. */
    public static String provisionProjectAndUser(String projectName, String userLogin,
                                                 String userPassword, String role) {
        configureAdminCommitIdentity();
        String projectId = uploadProject(projectName);
        provisionBranches(projectId);
        commitDivergentChangeOnDev(projectId);
        provisionUser(projectId, userLogin, userPassword, role);
        new ProjectBranchesMethod()
                .switchBranch(projectId, DEV_BRANCH, new UserData(userLogin, userPassword));
        return projectId;
    }

    public static void configureAdminCommitIdentity() {
        new UsersMethod().setCurrentUserInfo("Test", "Automation",
                "test-automation@openl.local", "Test Automation");
    }

    public static String uploadProject(String projectName) {
        File zip = new File(TestDataUtil.getFilePathFromResources(PROJECT_ZIP));
        Response upload = new RepositoryProjectsMethod().uploadProject(DESIGN_REPO, projectName, zip);
        assertThat(upload.getStatusCode()).as("upload project %s", projectName).isLessThan(300);
        return resolveProjectId(projectName);
    }

    public static void provisionBranches(String projectId) {
        ProjectBranchesMethod branches = new ProjectBranchesMethod();
        assertThat(branches.createBranch(projectId, PROTECTED_TARGET).getStatusCode())
                .as("create protected target branch").isLessThan(300);
        assertThat(branches.createBranch(projectId, DEV_BRANCH).getStatusCode())
                .as("create dev branch").isLessThan(300);
        assertThat(branches.switchBranch(projectId, DEV_BRANCH).getStatusCode())
                .as("switch project to dev branch").isLessThan(300);
    }

    public static void commitDivergentChangeOnDev(String projectId) {
        ProjectResourcesMethod resources = new ProjectResourcesMethod();
        Response get = resources.getResource(projectId, DIVERGENT_FILE);
        assertThat(get.getStatusCode()).as("download %s on dev", DIVERGENT_FILE).isEqualTo(200);
        try {
            Path tmp = Files.createTempFile("bypass-merge-divergence-", ".xml");
            Files.write(tmp, get.asByteArray());
            Files.write(tmp, "\n<!-- EPBDS-15960 divergence marker -->\n".getBytes(),
                    StandardOpenOption.APPEND);
            assertThat(resources.updateResource(projectId, DIVERGENT_FILE, tmp.toFile()).getStatusCode())
                    .as("PUT modified %s commits divergence on dev", DIVERGENT_FILE).isLessThan(300);
        } catch (Exception e) {
            throw new RuntimeException("failed to commit divergent change on dev", e);
        }
        // PUT leaves admin lock + OPENED state; reopen+close releases both.
        ProjectsMethod projects = new ProjectsMethod();
        assertThat(projects.openProject(projectId).getStatusCode())
                .as("release exclusive lock after PUT").isLessThan(300);
        assertThat(projects.closeProject(projectId).getStatusCode())
                .as("close admin's workspace claim").isLessThan(300);
    }

    public static void provisionUser(String projectId, String login, String password, String role) {
        UsersMethod users = new UsersMethod();
        assertThat(users.createUser(login, password).getStatusCode())
                .as("create user %s", login).isLessThan(300);
        assertThat(new AclProjectsMethod().grantRole(projectId, login, true, role).getStatusCode())
                .as("grant %s role on project to %s", role, login).isLessThan(300);
    }

    public static String resolveProjectId(String projectName) {
        Response resp = new ProjectsMethod().getAllProjects(500);
        List<Map<String, Object>> content = resp.jsonPath().getList("content");
        return content.stream()
                .filter(p -> projectName.equals(p.get("name")))
                .map(p -> String.valueOf(p.get("id")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("project " + projectName + " not found"));
    }
}
