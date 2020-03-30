package api.methods.httpbin;

import api.methods.ApiBaseMethod;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class GetImageMethod extends ApiBaseMethod {
    private RequestSpecification requestSpecification = new RequestSpecBuilder().setAccept(ContentType.BINARY).build();

    public GetImageMethod() {
        super("/image/png");
    }

    public Response getImage() {
        return callApi(Method.GET, requestSpecification);
    }
}
