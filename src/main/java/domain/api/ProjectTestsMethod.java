package domain.api;

import io.restassured.http.Method;
import io.restassured.response.Response;

public class ProjectTestsMethod extends AuthorizedApiMethod {

    public ProjectTestsMethod() {
        super("/rest/projects");
    }

    public Response runAllTests(String projectId) {
        return runAllTests(projectId, null);
    }

    /**
     * If {@code fromModule} is non-null the server scopes execution to that opened
     * module (matches what WebStudio's UI Run-Tests button does — it always runs
     * from the currently-selected module). Scoping makes compile + run noticeably
     * faster for multi-module projects.
     */
    public Response runAllTests(String projectId, String fromModule) {
        String url = fullApiUrl + "/" + projectId + "/tests/run";
        if (fromModule != null && !fromModule.isEmpty()) {
            String encoded = java.net.URLEncoder.encode(fromModule, java.nio.charset.StandardCharsets.UTF_8)
                    .replace("+", "%20");
            url = url + "?fromModule=" + encoded;
        }
        return callApi(Method.POST, authorizedJsonRequest(""), url, true);
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
