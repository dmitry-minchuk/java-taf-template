package domain.api;

import io.restassured.http.Method;
import org.testcontainers.containers.GenericContainer;

import java.util.List;

public class GetWsServicesMethod extends ApiBaseMethod {

    public GetWsServicesMethod(GenericContainer<?> wsContainer, int port) {
        super("http://localhost:" + wsContainer.getMappedPort(port), "/admin/services");
    }

    public List<String> getServiceNames() {
        io.restassured.response.Response response = callApi(Method.GET, null, true);
        String body = response.getBody().asString();
        if (!body.startsWith("[") && !body.startsWith("{")) {
            LOGGER.warn("GET /admin/services → HTTP {} non-JSON body: {}", response.statusCode(), body);
        } else {
            LOGGER.debug("GET /admin/services → HTTP {} body: {}", response.statusCode(), body);
        }
        return response.jsonPath().getList("name");
    }
}
