package api.methods;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.JSONObject;

public class PostSampleResource extends ApiBaseMethod {
    private RequestSpecification baseRequestSpecification = new RequestSpecBuilder()
            .addCookie("CookieName", "CookieValue")
            .addHeader("HeaderName", "HeaderValue")
            .build();

    public PostSampleResource() {
        super("/post");
    }

    public Response callApi() {
        JSONObject jsonObject = getJsonFromFile("src/test/resources/api/post_sample_resourse/rq.json");
        jsonObject
                .getJSONObject("role")
                .put("base", "driver");

        RequestSpecification rs = new RequestSpecBuilder()
                .addRequestSpecification(baseRequestSpecification)
                .addRequestSpecification(attachRequestJsonFile(jsonObject))
                .build();

        return callApi(Method.POST, rs);
    }
}
