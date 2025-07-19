package domain.api;

import configuration.appcontainer.AppContainerPool;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import io.restassured.http.Method;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

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
        return callApi(Method.GET, null, fullUrl, false);
    }

    public String getApplicationInfoOneLiner() {
        try {
            Response response = getApplicationInfo();
            if (response.getStatusCode() != 200) {
                throw new RuntimeException("Failed to retrieve application info. Status: " + response.getStatusCode());
            }
            
            // Get response body once and parse with JsonPath to avoid consumption issue
            String responseBody = response.asString();
            JsonPath jsonPath = JsonPath.from(responseBody);
            
            // Extract values using JsonPath - fields with dots need quotes for proper parsing
            String version = jsonPath.getString("'openl.version'");
            String buildDate = jsonPath.getString("'openl.build.date'");
            String buildNumber = jsonPath.getString("'openl.build.number'");
            
            return String.format("Application started: version=%s, build=%s, commit=%s", 
                version != null ? version : "unknown",
                buildDate != null ? buildDate : "unknown",
                buildNumber != null ? buildNumber : "unknown");
        } catch (Exception e) {
            return String.format("Application info unavailable: %s", e.getMessage());
        }
    }
}