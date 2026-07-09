package domain.api;

import io.restassured.RestAssured;
import io.restassured.http.Method;

public class PersonalAccessTokenApiMethod extends ApiBaseMethod {

    public PersonalAccessTokenApiMethod() {
        super("/rest/users/profile");
    }

    public int getProfileStatusWithToken(String personalAccessToken) {
        return callApi(Method.GET,
                RestAssured.given().header("Authorization", "Token " + personalAccessToken), false)
                .getStatusCode();
    }

    public int getProfileStatusWithoutAuthorization() {
        return callApi(Method.GET, RestAssured.given(), false).getStatusCode();
    }
}
