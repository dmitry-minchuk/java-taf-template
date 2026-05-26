package domain.api;

import domain.serviceclasses.models.UserData;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * POST /rest/projects/{projectId}/merge/check and POST /rest/projects/{projectId}/merge
 * (with optional ?force=true bypass). EPBDS-15818 introduced the `force` query parameter.
 *
 * All methods accept the acting user explicitly because Section B-D probe role-dependent
 * outcomes (admin / manager / contributor).
 */
public class ProjectMergeMethod extends AuthorizedApiMethod {

    public ProjectMergeMethod() {
        super("/rest/projects");
    }

    public Response checkMerge(String projectId, String mode, String otherBranch, boolean force, UserData asUser) {
        return callMergeEndpoint("/check", projectId, mode, otherBranch, force, asUser);
    }

    public Response merge(String projectId, String mode, String otherBranch, boolean force, UserData asUser) {
        return callMergeEndpoint("", projectId, mode, otherBranch, force, asUser);
    }

    private Response callMergeEndpoint(String subpath, String projectId, String mode, String otherBranch,
                                       boolean force, UserData asUser) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mode", mode);
        body.put("otherBranch", otherBranch);
        String url = fullApiUrl + "/" + projectId + "/merge" + subpath + (force ? "?force=true" : "");
        RequestSpecification spec = RestAssured.given()
                .header("Accept", "application/json")
                .auth().preemptive().basic(asUser.getLogin(), asUser.getPassword())
                .contentType(ContentType.JSON)
                .body(body);
        return callApi(Method.POST, spec, url, true);
    }
}
