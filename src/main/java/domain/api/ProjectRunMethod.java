package domain.api;

import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.Response;

public class ProjectRunMethod extends AuthorizedApiMethod {

    public static final String XLSX_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    public ProjectRunMethod(String projectId) {
        super("/rest/projects/" + projectId + "/run");
    }

    public Response startRun(String tableId, String inputJson) {
        var request = authorizedRequest().contentType(ContentType.JSON);
        if (inputJson != null) {
            request.body(inputJson);
        }
        return callApi(Method.POST, request, fullApiUrl + "?tableId=" + tableId, true);
    }

    public Response startRun(String tableId) {
        return startRun(tableId, null);
    }

    public Response startRunWithoutTableId(String inputJson) {
        var request = authorizedRequest().contentType(ContentType.JSON);
        if (inputJson != null) {
            request.body(inputJson);
        }
        return callApi(Method.POST, request, fullApiUrl, true);
    }

    public Response getJsonResult() {
        return getResult(ContentType.JSON.toString());
    }

    public Response getXlsxResult() {
        return getResult(XLSX_CONTENT_TYPE);
    }

    public Response getResult(String acceptMediaType) {
        return callApi(Method.GET,
                authorizedRequest().header("Accept", acceptMediaType),
                fullApiUrl + "/result",
                true);
    }

    public Response cancelRun() {
        return callApi(Method.DELETE, authorizedRequest(), true);
    }
}
