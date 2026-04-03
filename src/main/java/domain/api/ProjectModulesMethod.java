package domain.api;

import io.restassured.http.Method;
import io.restassured.response.Response;

public class ProjectModulesMethod extends AuthorizedApiMethod {

    public ProjectModulesMethod(String projectId) {
        super("/rest/projects/" + projectId + "/modules");
    }

    public Response listModules() {
        return callApi(Method.GET, authorizedRequest(), true);
    }

    public Response addModule(Object request) {
        return callApi(Method.POST, authorizedJsonRequest(request), true);
    }

    public Response editModule(String moduleName, Object request) {
        return callApi(Method.PUT, authorizedJsonRequest(request), fullApiUrl + "/" + moduleName, true);
    }

    public Response copyModule(String moduleName, Object request) {
        return copyModule(moduleName, request, false);
    }

    public Response copyModule(String moduleName, Object request, boolean force) {
        return callApi(Method.POST,
                authorizedJsonRequest(request),
                fullApiUrl + "/" + moduleName + "/copy?force=" + force,
                true);
    }

    public Response removeModule(String moduleName, boolean keepFile) {
        return callApi(Method.DELETE, authorizedRequest(), fullApiUrl + "/" + moduleName + "?keepFile=" + keepFile, true);
    }
}
