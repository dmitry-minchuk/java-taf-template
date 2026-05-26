package domain.api;

import domain.serviceclasses.models.UserData;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ProjectsMethod extends AuthorizedApiMethod {

    public ProjectsMethod() {
        super("/rest/projects");
    }

    public Response getProjects(String projectName) {
        String encodedProjectName = URLEncoder.encode(projectName, StandardCharsets.UTF_8);
        return callApi(Method.GET, authorizedRequest(), fullApiUrl + "?name=" + encodedProjectName, true);
    }

    public Response getAllProjects(int size) {
        return callApi(Method.GET, authorizedRequest(), fullApiUrl + "?size=" + size, true);
    }

    public Response openProject(String projectId) {
        return callApi(Method.PATCH,
                authorizedJsonRequest(Map.of("status", "OPENED")),
                fullApiUrl + "/" + projectId,
                true);
    }

    /** Closes the project for the current user (releases the workspace claim). */
    public Response closeProject(String projectId) {
        return callApi(Method.PATCH,
                authorizedJsonRequest(Map.of("status", "CLOSED")),
                fullApiUrl + "/" + projectId,
                true);
    }

    /** Same as {@link #openProject(String)} but as a specific user (per-user workspace). */
    public Response openProject(String projectId, UserData asUser) {
        RequestSpecification spec = RestAssured.given()
                .header("Accept", "application/json")
                .auth().preemptive().basic(asUser.getLogin(), asUser.getPassword())
                .contentType(ContentType.JSON)
                .body(Map.of("status", "OPENED"));
        return callApi(Method.PATCH, spec, fullApiUrl + "/" + projectId, true);
    }

    public Response getProjectTables(String projectId, String tableName) {
        String encodedTableName = URLEncoder.encode(tableName, StandardCharsets.UTF_8);
        return callApi(Method.GET,
                authorizedRequest(),
                fullApiUrl + "/" + projectId + "/tables?name=" + encodedTableName,
                true);
    }

    public Response createTable(String projectId, Object request) {
        return callApi(Method.POST,
                authorizedJsonRequest(request),
                fullApiUrl + "/" + projectId + "/tables",
                true);
    }
}
