package domain.api;

import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** GET / PUT on {@code /rest/projects/{projectId}/files/{path}}. */
public class ProjectResourcesMethod extends AuthorizedApiMethod {

    public ProjectResourcesMethod() {
        super("/rest/projects");
    }

    public Response getResource(String projectId, String path) {
        return callApi(Method.GET, authorizedRequest(), resourceUrl(projectId, path), true);
    }

    public Response updateResource(String projectId, String path, File file) {
        RequestSpecification spec = authorizedRequest().multiPart("file", file);
        return callApi(Method.PUT, spec, resourceUrl(projectId, path), true);
    }

    private String resourceUrl(String projectId, String path) {
        StringBuilder encoded = new StringBuilder();
        for (String segment : path.split("/")) {
            if (segment.isEmpty()) continue;
            if (encoded.length() > 0) encoded.append('/');
            encoded.append(URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20"));
        }
        return fullApiUrl + "/" + projectId + "/files/" + encoded;
    }
}
