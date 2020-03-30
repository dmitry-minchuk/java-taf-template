package api.methods.httpbin;

import api.methods.ApiBaseMethod;
import io.restassured.http.Method;
import io.restassured.response.Response;


public class GetSampleResourceMethod extends ApiBaseMethod {

    public GetSampleResourceMethod() {
        super("/get");
    }

    public GetSampleResourceMethod(String path) {
        super(path);
    }

    public Response getAll() {
        return callApi(Method.GET, null);
    }
}
