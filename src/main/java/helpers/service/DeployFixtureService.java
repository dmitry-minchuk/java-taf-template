package helpers.service;

import java.util.HashMap;
import java.util.Map;

public class DeployFixtureService {

    public static final String PRIMARY_REPOSITORY_ID = "production";
    public static final String PRIMARY_REPOSITORY_NAME = "Deployment";
    public static final String SECOND_REPOSITORY_ID = "production-second";
    public static final String SECOND_REPOSITORY_NAME = "Deployment Two";

    private final Map<String, String> containerFiles = new HashMap<>();
    private DeployInfrastructureService deployInfra;

    public Map<String, String> containerFiles() {
        return Map.copyOf(containerFiles);
    }

    public void start() {
        containerFiles.clear();
        deployInfra = DeployInfrastructureService.builder()
                .withPostgres()
                .withSecondProductionRepository()
                .build();
        deployInfra.start();
        containerFiles.putAll(deployInfra.getFilesToCopy());
    }

    public void stop() {
        if (deployInfra != null) {
            deployInfra.cleanup();
            deployInfra = null;
        }
    }
}
