package api.methods.tms;

import api.methods.ApiBaseMethod;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class GetAllTrainingsMethod extends ApiBaseMethod {
    private final String baseURL = "https://msqv123.exadel.by:8443/api/training/all_archive";
    private RequestSpecification requestSpecification;

    public GetAllTrainingsMethod(String jwt) {
        super("");
        requestSpecification = new RequestSpecBuilder()
                .addHeader("Authorization", "Bearer " + jwt)
                .build();
    }

    public Response callAPI() {
        return callApi(Method.GET, requestSpecification, baseURL);
    }
}
