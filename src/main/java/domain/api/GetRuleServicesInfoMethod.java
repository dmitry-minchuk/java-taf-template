package domain.api;

import io.restassured.http.Method;
import io.restassured.response.Response;

public class GetRuleServicesInfoMethod extends ApiBaseMethod {

    public GetRuleServicesInfoMethod() {
        super("/admin/ui/info");
    }

    public Response getApplicationInfo() {
        LOGGER.debug("Retrieving Rule Services info from: {}", fullApiUrl);
        return callApi(Method.GET, null, true);
    }
}
