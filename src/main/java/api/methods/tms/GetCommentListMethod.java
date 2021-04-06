package api.methods.tms;

import api.methods.ApiBaseMethod;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class GetCommentListMethod extends ApiBaseMethod {
    private final String baseURL = "https://msqv123.exadel.by:8443/api/training/%s/comment_list";
    private RequestSpecification requestSpecification;

    public GetCommentListMethod(String jwt) {
        super("");
        requestSpecification = new RequestSpecBuilder()
                .addHeader("Authorization", "Bearer " + jwt)
                .build();
    }

    public Response callAPI(String trainingId) {
        return callApi(Method.GET, requestSpecification, String.format(baseURL, trainingId));
    }
}
