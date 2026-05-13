package domain.api;

import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Design-repository operations on WebStudio: upload a ZIP archive as a new project.
 */
public class RepositoryProjectsMethod extends AuthorizedApiMethod {

    public RepositoryProjectsMethod() {
        super("/rest/repos");
    }

    /**
     * Upload a ZIP archive as a new project into the given design repository.
     * Mirrors WebStudio UI "Create Project from ZIP" — server reads the project
     * name (and modules) from the rules.xml at zip root; we pass the same name
     * as a URL path parameter for routing.
     *
     * @param repoName    design repository id (default in DEFAULT_STUDIO_PARAMS is "design")
     * @param projectName project name (typically obtained from rules.xml inside the zip)
     * @param zipFile     ZIP archive
     */
    public Response uploadProject(String repoName, String projectName, File zipFile) {
        String encodedName = URLEncoder.encode(projectName, StandardCharsets.UTF_8).replace("+", "%20");
        String url = fullApiUrl + "/" + repoName + "/projects/" + encodedName;
        RequestSpecification spec = authorizedRequest().multiPart("template", zipFile);
        return callApi(Method.PUT, spec, url, true);
    }
}
