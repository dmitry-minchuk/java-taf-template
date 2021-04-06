package api.methods.tms;

import api.methods.ApiBaseMethod;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class GetTrainingByIdMethod extends ApiBaseMethod {
    private final String baseURL = "https://msqv123.exadel.by:8443/api/training/";
    private RequestSpecification requestSpecification;

    public GetTrainingByIdMethod(String jwt) {
        super("");
        requestSpecification = new RequestSpecBuilder()
                .addHeader("Authorization", "Bearer " + jwt)
                .build();
    }

    public Response callAPI(String id) {
        return callApi(Method.GET, requestSpecification, baseURL + id);
    }
}
