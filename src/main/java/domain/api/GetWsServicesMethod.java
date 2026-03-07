package domain.api;

import io.restassured.http.Method;
import org.testcontainers.containers.GenericContainer;

import java.util.List;

public class GetWsServicesMethod extends ApiBaseMethod {

    public GetWsServicesMethod(GenericContainer<?> wsContainer, int port) {
        super("http://localhost:" + wsContainer.getMappedPort(port), "/admin/services");
    }

    public List<String> getServiceNames() {
        return callApi(Method.GET, null, true)
                .jsonPath()
                .getList("name");
    }
}
