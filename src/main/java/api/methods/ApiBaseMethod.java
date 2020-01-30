package api.methods;

import domain.PropertyNameSpace;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import utils.ProjectConfiguration;

import java.net.MalformedURLException;
import java.net.URL;

public abstract class ApiBaseMethod {
    protected static final Logger LOGGER = LogManager.getLogger(ApiBaseMethod.class);
    private String fullApiUrl;

    public ApiBaseMethod(String path) {
        fullApiUrl = ProjectConfiguration.getPropertyByEnv(PropertyNameSpace.BASE_API_URL) + path;
    }

    public Response callApi(Method method, RequestSpecification requestSpecification) {
        LOGGER.info(String.format("%1$s%1$s%2$s%3$sHTTP REQUEST%3$s%2$s", System.lineSeparator(), "-", "="));
        Response response;

        try {
            response = RestAssured
                    .given(((requestSpecification == null) ? new RequestSpecBuilder().build() : requestSpecification))
                    .log()
                    .all()
                    .request(method, new URL(fullApiUrl));
        } catch (MalformedURLException e) {
            LOGGER.info("Api method is trying to call following URL: " + fullApiUrl);
            throw new RuntimeException(e.toString());
        }

        LOGGER.info(String.format("%1$s%1$s%2$s%3$sHTTP RESPONSE%3$s%2$s", System.lineSeparator(), "-", "="));
        response
                .then()
                .log()
                .all();

        return response;
    }

}
