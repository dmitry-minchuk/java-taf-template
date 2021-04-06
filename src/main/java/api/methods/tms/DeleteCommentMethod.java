package api.methods.tms;

import api.methods.ApiBaseMethod;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class DeleteCommentMethod extends ApiBaseMethod {
    private final String baseURL = "https://msqv123.exadel.by:8443/api/training/remove_comment/%s";
    private RequestSpecification requestSpecification;

    public DeleteCommentMethod(String jwt) {
        super("");
        requestSpecification = new RequestSpecBuilder()
                .addHeader("Authorization", "Bearer " + jwt)
                .build();
    }

    public Response callAPI(String commentId) {
        return callApi(Method.DELETE, requestSpecification, String.format(baseURL, commentId));
    }
}
