package domain.api;

import io.restassured.http.Method;
import io.restassured.response.Response;

import java.util.Map;

/**
 * PUT /rest/acls/projects/{project-id}?sid=...&principal=true — grants an ACL role
 * (MANAGER / CONTRIBUTOR / VIEWER) on a project to a user or group.
 */
public class AclProjectsMethod extends AuthorizedApiMethod {

    public AclProjectsMethod() {
        super("/rest/acls/projects");
    }

    public Response grantRole(String projectId, String sid, boolean principal, String role) {
        String url = fullApiUrl + "/" + projectId + "?sid=" + sid + "&principal=" + principal;
        return callApi(Method.PUT, authorizedJsonRequest(Map.of("role", role)), url, true);
    }
}
