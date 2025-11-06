package domain.api;

import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.RestAssured;

public class ServiceHelloMethod extends ApiBaseMethod {

    public ServiceHelloMethod(String serviceName) {
        super(String.format("/%s/%s", serviceName, "Hello"));
    }

    public Response post(String body) {
        RequestSpecification spec = RestAssured.given()
                .body(body)
                .contentType(ContentType.TEXT);

        return callApi(Method.POST, spec);
    }
}
