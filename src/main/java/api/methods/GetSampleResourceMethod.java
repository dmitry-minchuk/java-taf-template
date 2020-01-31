package api.methods;

import io.restassured.http.Method;
import io.restassured.response.Response;


public class GetSampleResourceMethod extends ApiBaseMethod {

    public GetSampleResourceMethod() {
        super("/get");
    }

    public Response callApi() {
        return callApi(Method.GET, null);
    }
}
