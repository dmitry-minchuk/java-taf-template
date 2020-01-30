package api.methods;

import io.restassured.http.Method;
import io.restassured.response.Response;


public class GetSampleResource extends ApiBaseMethod {

    public GetSampleResource() {
        super("/get");
    }

    public Response callApi() {
        return callApi(Method.GET, null);
    }
}
