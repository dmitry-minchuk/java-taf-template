package api.methods.tms;

import api.methods.ApiBaseMethod;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class PostCommentMethod extends ApiBaseMethod {
    private final String baseURL = "https://msqv123.exadel.by:8443/api/training/%s/add_comment";
    private RequestSpecification requestSpecification;

    public PostCommentMethod(String jwt) {
        super("");
        requestSpecification = new RequestSpecBuilder()
                .addHeader("Authorization", "Bearer " + jwt)
                .addHeader("Content-Type", "application/json")
                .setBody(getStringFromFile("post-comment/create-comment.json"))
                .build();
    }

    public Response callAPI(String trainingId) {
        return callApi(Method.POST, requestSpecification, String.format(baseURL, trainingId));
    }
}
