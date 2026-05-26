package domain.api;

import domain.serviceclasses.constants.User;
import domain.serviceclasses.models.UserData;
import helpers.service.UserService;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin-only PATCH for /web/admin/settings/authentication.
 *
 * EPBDS-15818 introduced the global flag `allowBypassProtectedBranches`. Tests that need a
 * deterministic value for this flag (e.g. asserting the legacy "hard 403" behavior) PATCH
 * it via this helper.
 *
 * Implementation notes (probed via curl against a live container):
 * - Endpoint authenticates via either Basic auth or session JSESSIONID — both work.
 * - PATCH body MUST be complete (`userMode` + `allowProjectCreateDelete` +
 *   `allowBypassProtectedBranches` + `administrators`). A partial body is rejected with
 *   HTTP 401 instead of 400 — a Studio quirk. We send all four fields.
 * - A successful PATCH triggers an application restart and invalidates the browser session
 *   the test acquired earlier. Callers must re-login afterwards.
 */
public class AuthenticationSettingsMethod extends AuthorizedApiMethod {

    private static final String MERGE_PATCH_CONTENT_TYPE = "application/merge-patch+json";

    public AuthenticationSettingsMethod() {
        super("/web/admin/settings/authentication");
    }

    public Response setAllowBypassProtectedBranches(boolean enabled) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("userMode", "multi");
        body.put("allowProjectCreateDelete", true);
        body.put("allowBypassProtectedBranches", enabled);
        body.put("administrators", List.of("admin"));
        UserData admin = UserService.getUser(User.ADMIN);
        RequestSpecification spec = RestAssured.given()
                .header("Accept", "application/json")
                .auth().preemptive().basic(admin.getLogin(), admin.getPassword())
                .contentType(MERGE_PATCH_CONTENT_TYPE)
                .body(body);
        return callApi(Method.PATCH, spec, fullApiUrl, true);
    }
}
