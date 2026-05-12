package domain.api;

import io.restassured.http.Method;
import io.restassured.response.Response;

public class ProjectTestsMethod extends AuthorizedApiMethod {

    public ProjectTestsMethod() {
        super("/rest/projects");
    }

    public Response runAllTests(String projectId) {
        return callApi(Method.POST, authorizedJsonRequest(""), fullApiUrl + "/" + projectId + "/tests/run", true);
    }

    public Response getTestsSummary(String projectId, boolean failuresOnly, int failuresLimit) {
        return getTestsSummary(projectId, failuresOnly, failuresLimit, true);
    }

    public Response getTestsSummary(String projectId, boolean failuresOnly, int failuresLimit, boolean withLogs) {
        int boundedLimit = Math.max(1, failuresLimit);
        return callApi(Method.GET, authorizedRequest(),
                fullApiUrl + "/" + projectId + "/tests/summary?failuresOnly=" + failuresOnly + "&failures=" + boundedLimit,
                withLogs);
    }
}
