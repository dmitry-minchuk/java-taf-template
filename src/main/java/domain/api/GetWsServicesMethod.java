package domain.api;

import io.restassured.http.Method;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.testcontainers.containers.GenericContainer;

import java.util.List;

public class GetWsServicesMethod extends ApiBaseMethod {

    public GetWsServicesMethod(GenericContainer<?> wsContainer, int port) {
        super("http://localhost:" + wsContainer.getMappedPort(port), "/admin/services");
    }

    public List<String> getServiceNames() {
        RequestSpecification request = io.restassured.RestAssured.given()
                .redirects().follow(false)
                .accept(ContentType.JSON);
        io.restassured.response.Response response = callApi(Method.GET, request, true);
        String body = response.getBody().asString();
        String trimmedBody = body.stripLeading();
        if (response.statusCode() != 200 || (!trimmedBody.startsWith("[") && !trimmedBody.startsWith("{"))) {
            String preview = body.length() > 500 ? body.substring(0, 500) + "..." : body;
            throw new IllegalStateException(String.format(
                    "GET /admin/services expected JSON from Rule Services but got HTTP %s, content-type '%s', body: %s",
                    response.statusCode(), response.contentType(), preview));
        }
        LOGGER.debug("GET /admin/services → HTTP {} body: {}", response.statusCode(), body);
        return response.jsonPath().getList("name");
    }
}
