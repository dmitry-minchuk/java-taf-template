package api.methods.httpbin;

import api.methods.ApiBaseMethod;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.Method;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.JSONObject;

public class PostSampleResourceMethod extends ApiBaseMethod {
    private RequestSpecification baseRequestSpecification = new RequestSpecBuilder()
            .addCookie("CookieName", "CookieValue")
            .addHeader("HeaderName", "HeaderValue")
            .build();
    private Response response;

    public PostSampleResourceMethod() {
        super("/post");
    }

    public Response changeRole() {
        JSONObject jsonObject = getJsonFromFile("post-sample-resource/rq.json");
        jsonObject
                .getJSONObject("role")
                .put("base", "driver");

        RequestSpecification r = new RequestSpecBuilder()
                .addRequestSpecification(baseRequestSpecification)
                .addRequestSpecification(attachRequestJsonFile(jsonObject))
                .build();

        response = callApi(Method.POST, r);
        return response;
    }

    public void validateResponseAgainstSchema() {
        response
                .then()
                .assertThat()
                .body(JsonSchemaValidator.matchesJsonSchema(getStringFromFile("post-sample-resource/rs-schema.json")));
    }
}
