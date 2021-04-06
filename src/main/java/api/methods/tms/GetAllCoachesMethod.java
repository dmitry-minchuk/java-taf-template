package api.methods.tms;

import api.methods.ApiBaseMethod;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class GetAllCoachesMethod extends ApiBaseMethod {
    private final String baseURL = "https://msqv123.exadel.by:8443/api/user_controller/get_coaches";
    private RequestSpecification requestSpecification;

    public GetAllCoachesMethod(String jwt) {
        super("");
        requestSpecification = new RequestSpecBuilder()
                .addHeader("Authorization", "Bearer " + jwt)
                .build();
    }

    public Response callAPI() {
        return callApi(Method.GET, requestSpecification, baseURL);
    }
}
