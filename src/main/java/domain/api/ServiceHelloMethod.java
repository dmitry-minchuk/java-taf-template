package domain.api;

import configuration.appcontainer.AppContainerPool;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.RestAssured;

public class ServiceHelloMethod extends ApiBaseMethod {

    private String endpoint;

    public ServiceHelloMethod(String serviceName) {
        super("/" + serviceName + "/Hello");
        this.endpoint = String.format("/%s/%s", serviceName, "Hello");
    }

    public Response post(String body) {
        RequestSpecification spec = RestAssured.given()
                .body(body)
                .contentType(ContentType.TEXT);

        // Use localhost with mapped port instead of container internal URL
        int appPort = Integer.parseInt(ProjectConfiguration.getProperty(PropertyNameSpace.DEFAULT_APP_PORT));
        String deployedAppPath = ProjectConfiguration.getProperty(PropertyNameSpace.DEPLOYED_APP_PATH);
        int mappedPort = AppContainerPool.get().getAppContainer().getMappedPort(appPort);
        String localUrl = String.format("http://localhost:%s%s%s", mappedPort, deployedAppPath, endpoint);

        return callApi(Method.POST, spec, localUrl, true);
    }
}
