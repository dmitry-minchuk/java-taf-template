package configuration.appcontainer;

import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;

public class AppContainerFactory {
    protected static final Logger LOGGER = LogManager.getLogger(AppContainerFactory.class);
    private final static Integer APP_PORT = Integer.parseInt(ProjectConfiguration.getProperty(PropertyNameSpace.DEFAULT_APP_PORT));
    private final static String DEPLOYED_APP_PATH = ProjectConfiguration.getProperty(PropertyNameSpace.DEPLOYED_APP_PATH);
    private final static String DOCKER_IMAGE_NAME = ProjectConfiguration.getProperty(PropertyNameSpace.DOCKER_IMAGE_NAME);

    public static AppContainerData createContainer(String containerName,
                                                   Network network,
                                                   Map<String, String> envVars,
                                                   Map<String, String> filesToCopy) {
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse(DOCKER_IMAGE_NAME));
        container.addExposedPort(APP_PORT);
        container.withNetwork(network);
        container.withNetworkAliases(containerName);
        if(envVars != null)
            container.withEnv(envVars);
        if(filesToCopy != null)
            filesToCopy.forEach((hostPath, containerPath) ->
                    container.withCopyFileToContainer(getMountableFile(hostPath), containerPath));
        container.waitingFor(Wait.forHttp(DEPLOYED_APP_PATH)
                .forStatusCode(200)
                .withStartupTimeout(Duration.ofMinutes(5)));
        LOGGER.info("Starting app container: {}", containerName);
        container.start();

        LOGGER.info(String.format("App Localhost accessible url for %s: http://localhost:%s%s", containerName, container.getMappedPort(APP_PORT), DEPLOYED_APP_PATH));
        LOGGER.info(String.format("App Url accessible from the Browser container: http://%s:%s%s", containerName, APP_PORT, DEPLOYED_APP_PATH));
        return new AppContainerData(container, String.format("http://%s:%s%s", containerName, APP_PORT, DEPLOYED_APP_PATH));
    }

    private static MountableFile getMountableFile(String resourcePath) {
        File file = new File(resourcePath);
        Path path = file.toPath();
        // Use file mode 0666 (owner rw, group rw, others rw) to ensure
        // container users (e.g. openl uid=1000) can read AND write copied files.
        // WebStudio's Migrator writes back to .properties after migration.
        return MountableFile.forHostPath(path, 0666);
    }
}
