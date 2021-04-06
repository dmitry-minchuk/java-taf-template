package api.methods.tms;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

public class GetAuthMethod {
    private final RequestSpecification requestSpecification = new RequestSpecBuilder()
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("Authorization", "Basic secret_key")
            .addFormParam("grant_type", "password")
            .addFormParam("username", "dminchuk")
            .addFormParam("password", "password")
            .addFormParam("scope", "openid")
            .build();
    private final String AuthURL = "https://keycloak-dev2.exadel.by/auth/realms/exadel-dev/protocol/openid-connect/token";

    public String getJWT() {
        Response r;
        try {
            r = RestAssured
                    .given(requestSpecification)
                    .log()
                    .all()
                    .request(Method.POST, new URL(AuthURL));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e.toString());
        }
        return new JSONObject(r.body().asString()).getString("access_token");
    }
}
