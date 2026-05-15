package domain.api;

import io.restassured.http.Method;
import io.restassured.response.Response;

/**
 * WebStudio compile-status endpoints. All endpoints are session-scoped:
 * they read the "current project" from the HTTP session, so the caller must
 * have just loaded a project (e.g. via PATCH /rest/projects/{id} status=OPENED)
 * on the same session (SessionFilter / JSESSIONID).
 */
public class CompileMethod extends AuthorizedApiMethod {

    public CompileMethod() {
        super("/rest/compile");
    }

    /**
     * Fetch project compile messages. Use (-1, -1) to get ALL messages; otherwise
     * the controller returns only messages after the given index (incremental).
     */
    public Response getCompileProgress(long messageId, int messageIndex) {
        return getCompileProgress(messageId, messageIndex, true);
    }

    public Response getCompileProgress(long messageId, int messageIndex, boolean withLogs) {
        return callApi(Method.GET, authorizedRequest(),
                fullApiUrl + "/progress/" + messageId + "/" + messageIndex, withLogs);
    }
}
