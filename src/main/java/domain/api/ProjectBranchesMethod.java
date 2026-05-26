package domain.api;

import io.restassured.http.Method;
import io.restassured.response.Response;

import java.util.Map;

/**
 * Branch management for a project:
 * - POST /rest/projects/{projectId}/branches — create a branch.
 * - GET  /rest/projects/{projectId}/branches — list branches.
 * - PATCH /rest/projects/{projectId} body {"branch":"name"} — switch the project to a branch.
 */
public class ProjectBranchesMethod extends AuthorizedApiMethod {

    public ProjectBranchesMethod() {
        super("/rest/projects");
    }

    public Response createBranch(String projectId, String branchName) {
        String url = fullApiUrl + "/" + projectId + "/branches";
        return callApi(Method.POST, authorizedJsonRequest(Map.of("branch", branchName)), url, true);
    }

    public Response listBranches(String projectId) {
        String url = fullApiUrl + "/" + projectId + "/branches";
        return callApi(Method.GET, authorizedRequest(), url, true);
    }

    public Response switchBranch(String projectId, String branchName) {
        return callApi(Method.PATCH, authorizedJsonRequest(Map.of("branch", branchName)),
                fullApiUrl + "/" + projectId, true);
    }

    /**
     * PATCH /rest/projects/{id} body {"comment":"..."} — when the project has pending modifications
     * (e.g. after createTable / updateTable), this triggers the equivalent of "Save" in the UI:
     * stages the workspace changes as a git commit on the project's current branch.
     */
    public Response commit(String projectId, String comment) {
        return callApi(Method.PATCH, authorizedJsonRequest(Map.of("comment", comment)),
                fullApiUrl + "/" + projectId, true);
    }
}
