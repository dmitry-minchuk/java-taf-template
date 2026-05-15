package domain.api;

import io.restassured.http.Method;
import io.restassured.response.Response;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Per-user profile updates. Useful in fresh WebStudio instances where the
 * admin account has no name/email yet — JGit commits inside local design
 * repos fail with "Name of PersonIdent must not be null" until first/last
 * name and email are set.
 */
public class UsersMethod extends AuthorizedApiMethod {

    public UsersMethod() {
        super("/rest/users");
    }

    public Response setCurrentUserInfo(String firstName, String lastName, String email, String displayName) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("firstName", firstName);
        body.put("lastName", lastName);
        body.put("email", email);
        body.put("displayName", displayName);
        return callApi(Method.PUT, authorizedJsonRequest(body), fullApiUrl + "/info", true);
    }
}
