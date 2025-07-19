package domain.api;

import configuration.appcontainer.AppContainerPool;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.testcontainers.containers.Container;

public class ApplicationInfoApi {
    
    private static final Logger LOGGER = LogManager.getLogger(ApplicationInfoApi.class);
    private static final String INFO_ENDPOINT = "/web/public/info/openl.json";
    private static final String APP_PORT = ProjectConfiguration.getProperty(PropertyNameSpace.DEFAULT_APP_PORT);
    private static final String DEPLOYED_APP_PATH = ProjectConfiguration.getProperty(PropertyNameSpace.DEPLOYED_APP_PATH);

    public Container.ExecResult getApplicationInfo() {
        try {
            String internalUrl = "http://localhost:" + APP_PORT + DEPLOYED_APP_PATH + INFO_ENDPOINT;
            LOGGER.info("Retrieving application info from container using: {}", internalUrl);
            
            Container.ExecResult result = AppContainerPool.get().getAppContainer()
                .execInContainer("wget", "-q", "-O", "-", internalUrl);
                
            LOGGER.debug("Wget execution - Exit code: {}, Stdout: {}, Stderr: {}", 
                result.getExitCode(), result.getStdout(), result.getStderr());
            
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute wget command in container", e);
        }
    }

    public JSONObject getApplicationInfoAsJson() {
        Container.ExecResult result = getApplicationInfo();
        if (result.getExitCode() != 0) {
            LOGGER.error("Failed to retrieve application info. Exit code: {}, Stderr: {}", 
                result.getExitCode(), result.getStderr());
            throw new RuntimeException("Failed to retrieve application info. Exit code: " + result.getExitCode());
        }
        return new JSONObject(result.getStdout());
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