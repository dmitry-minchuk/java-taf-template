package api.methods.httpbin;

import api.methods.ApiBaseMethod;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class PutAnythingMethod extends ApiBaseMethod {
    private RequestSpecification requestSpecification = new RequestSpecBuilder().build();

    public PutAnythingMethod(String path) {
        super("/anything/" + path);
    }

    public Response putValue() {
        return callApi(Method.PUT, requestSpecification);
    }
}
