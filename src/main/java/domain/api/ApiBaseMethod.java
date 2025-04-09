package domain.api;

import configuration.appcontainer.AppContainerPool;
import configuration.listeners.RestAssuredFilter;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

public abstract class ApiBaseMethod {
    protected static final Logger LOGGER = LogManager.getLogger(ApiBaseMethod.class);
    private static final String TEMPLATE_PATH = "src/test/resources/api/";
    private String fullApiUrl;

    public ApiBaseMethod(String path) {
        fullApiUrl = AppContainerPool.get().getAppHostUrl();
    }

    public Response callApi(Method method, RequestSpecification requestSpecification, String fullApiUrl) {
        LOGGER.info(String.format("%1$s%1$s%2$s%3$sHTTP REQUEST%3$s%2$s", System.lineSeparator(), "-", "="));
        RestAssured.useRelaxedHTTPSValidation();
        Response response;

        try {
            response = RestAssured
                    .given(((requestSpecification == null) ? new RequestSpecBuilder().build() : requestSpecification))
                    .filter(new RestAssuredFilter())
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

    public Response callApi(Method method, RequestSpecification requestSpecification) {
        return callApi(method, requestSpecification, fullApiUrl);
    }

    protected JSONObject getJsonFromFile(String path) {
        return new JSONObject(getStringFromFile(path));
    }

    protected String getStringFromFile(String path) {
        String fileContents;
        try {
            FileInputStream inputStream = new FileInputStream(TEMPLATE_PATH + path);
            fileContents = IOUtils.toString(inputStream, Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage() + e.getCause());
        }
        return fileContents;
    }

    protected RequestSpecification attachRequestJsonFile(JSONObject jsonObject) {
        return RestAssured.given().body(jsonObject.toString()).with().contentType(ContentType.JSON);
    }
}
