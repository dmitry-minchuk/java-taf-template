package domain.api;

import domain.serviceclasses.models.UserData;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.LinkedHashMap;
import java.util.Map;

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

    public Response createUser(String username, String password) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", username);
        body.put("displayName", username);
        body.put("firstName", username);
        body.put("lastName", username);
        body.put("email", username + "@example.com");
        Map<String, Object> pwd = new LinkedHashMap<>();
        pwd.put("password", password);
        body.put("internalPassword", pwd);
        return callApi(Method.PUT, authorizedJsonRequest(body), fullApiUrl, true);
    }

    public Response deleteUser(String username) {
        return callApi(Method.DELETE, authorizedRequest(), fullApiUrl + "/" + username, true);
    }

    public Response getProfile(UserData asUser) {
        RequestSpecification spec = RestAssured.given()
                .header("Accept", "application/json")
                .auth().preemptive().basic(asUser.getLogin(), asUser.getPassword());
        return callApi(Method.GET, spec, fullApiUrl + "/profile", true);
    }

    public String getProfileDisplayName(UserData asUser) {
        return getProfile(asUser).jsonPath().getString("displayName");
    }
}
