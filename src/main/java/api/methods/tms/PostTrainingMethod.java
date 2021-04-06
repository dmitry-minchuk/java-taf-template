package api.methods.tms;

import api.methods.ApiBaseMethod;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class PostTrainingMethod extends ApiBaseMethod {
    private final String baseURL = "https://msqv123.exadel.by:8443/api/training/create";
    private RequestSpecification requestSpecification;

    public PostTrainingMethod(String jwt) {
        super("");
        requestSpecification = new RequestSpecBuilder()
                .addHeader("Authorization", "Bearer " + jwt)
                .addHeader("Content-Type", "application/json")
                .setBody(getStringFromFile("post-training/create-training-rq.json"))
                .build();
    }

    public Response callAPI() {
        return callApi(Method.POST, requestSpecification, baseURL);
    }
}
