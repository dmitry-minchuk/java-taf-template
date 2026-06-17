package domain.api;

import io.restassured.http.Method;
import io.restassured.response.Response;

/**
 * WebStudio per-project status endpoint (6.1.x+). Returns ProjectStatusViewModel:
 * compileState (idle|compiling|ok|warnings|errors) plus a compilation breakdown
 * (messages/modules/tests). Replaces the removed session-scoped
 * {@code GET /rest/compile/progress/{}/{}} and {@code GET /rest/projects/{id}/modules}.
 */
public class ProjectStatusMethod extends AuthorizedApiMethod {

    public ProjectStatusMethod() {
        super("/rest/projects");
    }

    public Response getStatus(String projectId) {
        return getStatus(projectId, true);
    }

    public Response getStatus(String projectId, boolean withLogs) {
        return callApi(Method.GET, authorizedRequest(), fullApiUrl + "/" + projectId + "/status", withLogs);
    }
}
