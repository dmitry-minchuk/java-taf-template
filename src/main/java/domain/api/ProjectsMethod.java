package domain.api;

import io.restassured.http.Method;
import io.restassured.response.Response;

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

    public Response openProject(String projectId) {
        return callApi(Method.PATCH,
                authorizedJsonRequest(Map.of("status", "OPENED")),
                fullApiUrl + "/" + projectId,
                true);
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
