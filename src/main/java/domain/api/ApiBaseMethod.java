package domain.api;

import configuration.appcontainer.AppContainerPool;
import configuration.listeners.RestAssuredFilter;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
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
    private static final String API_BASE_URL = "http://localhost:";
    protected String fullApiUrl;

    public ApiBaseMethod(String path) {
        try {
            int appPort = Integer.parseInt(ProjectConfiguration.getProperty(PropertyNameSpace.DEFAULT_APP_PORT));
            String deployedAppPath = ProjectConfiguration.getProperty(PropertyNameSpace.DEPLOYED_APP_PATH);
            int mappedPort = AppContainerPool.get().getAppContainer().getMappedPort(appPort);
            this.fullApiUrl = API_BASE_URL + mappedPort + deployedAppPath + path;
            LOGGER.debug("API URL constructed: {}", this.fullApiUrl);
        } catch (Exception e) {
            throw new RuntimeException("Failed to construct API URL from path: " + path, e);
        }
    }

    protected ApiBaseMethod(String baseUrl, String path) {
        this.fullApiUrl = baseUrl + path;
        LOGGER.debug("API URL constructed (custom base): {}", this.fullApiUrl);
    }

    public Response callApi(Method method, RequestSpecification requestSpecification, String fullApiUrl, boolean withLogs) {
        RestAssured.useRelaxedHTTPSValidation();
        Response response;
        try {
            RequestSpecification spec = (requestSpecification == null) ? new RequestSpecBuilder().build() : requestSpecification;

            if (withLogs)
                spec = spec.filter(new RestAssuredFilter());

            response = RestAssured
                    .given(spec)
                    .request(method, new URL(fullApiUrl));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e.toString());
        }
        return response;
    }

    public Response callApi(Method method, RequestSpecification requestSpecification, String fullApiUrl) {
        return callApi(method, requestSpecification, fullApiUrl, true);
    }

    public Response callApi(Method method, RequestSpecification requestSpecification, boolean withLogs) {
        return callApi(method, requestSpecification, fullApiUrl, withLogs);
    }

    public Response callApi(Method method, RequestSpecification requestSpecification) {
        return callApi(method, requestSpecification, fullApiUrl, true);
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
