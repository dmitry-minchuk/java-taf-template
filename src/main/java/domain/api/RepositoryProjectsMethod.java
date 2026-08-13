package domain.api;

import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class RepositoryProjectsMethod extends AuthorizedApiMethod {

    public RepositoryProjectsMethod() {
        super("/rest/repos");
    }

    public Response uploadProject(String repoName, String projectName, File zipFile) {
        String url = fullApiUrl + "/" + repoName + "/projects/" + encodePathSegment(projectName);
        RequestSpecification spec = authorizedRequest()
                .urlEncodingEnabled(false)
                .multiPart("template", zipFile);
        return callApi(Method.PUT, spec, url, true);
    }

    public Response createProjectFromTemplate(String repoName, String projectName, String templateName, String branch) {
        String url = fullApiUrl + "/" + repoName + "/projects/" + encodePathSegment(projectName)
                + "?status=OPENED&branch=" + URLEncoder.encode(branch, StandardCharsets.UTF_8);
        RequestSpecification spec = authorizedRequest()
                .urlEncodingEnabled(false)
                .multiPart("templateType", "templates")
                .multiPart("templateCategory", "templates")
                .multiPart("templateName", templateName);
        return callApi(Method.PUT, spec, url, true);
    }

    private static String encodePathSegment(String projectName) {
        return URLEncoder.encode(projectName, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
