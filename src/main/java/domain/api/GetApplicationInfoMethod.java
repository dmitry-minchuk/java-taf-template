package domain.api;

import configuration.appcontainer.AppContainerPool;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import io.restassured.http.Method;
import io.restassured.response.Response;
import org.json.JSONObject;

public class GetApplicationInfoMethod extends ApiBaseMethod {
    
    private static final String INFO_ENDPOINT = "/web/public/info/openl.json";

    public GetApplicationInfoMethod() {
        super(INFO_ENDPOINT);
    }

    public Response getApplicationInfo() {
        // Use localhost with mapped port instead of container internal URL
        int appPort = Integer.parseInt(ProjectConfiguration.getProperty(PropertyNameSpace.DEFAULT_APP_PORT));
        String deployedAppPath = ProjectConfiguration.getProperty(PropertyNameSpace.DEPLOYED_APP_PATH);
        int mappedPort = AppContainerPool.get().getAppContainer().getMappedPort(appPort);
        String fullUrl = "http://localhost:" + mappedPort + deployedAppPath + INFO_ENDPOINT;
        LOGGER.debug("Retrieving application info from: {}", fullUrl);
        return callApi(Method.GET, null, fullUrl, true);
    }

    public JSONObject getApplicationInfoAsJson() {
        Response response = getApplicationInfo();
        if (response.getStatusCode() != 200) {
            LOGGER.error("Failed to retrieve application info. Status: {}, Body: {}", 
                response.getStatusCode(), response.asString());
            throw new RuntimeException("Failed to retrieve application info. Status: " + response.getStatusCode());
        }
        return new JSONObject(response.asString());
    }

    public String getApplicationInfoOneLiner() {
        try {
            JSONObject json = getApplicationInfoAsJson();
            return String.format("Application started: version=%s, build=%s, commit=%s", 
                json.optString("openl.version", "unknown"),
                json.optString("openl.build.date", "unknown"),
                json.optString("openl.build.number", "unknown"));
        } catch (Exception e) {
            return String.format("Application info unavailable: %s", e.getMessage());
        }
    }
}