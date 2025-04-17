package configuration.appcontainer;

import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import helpers.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;
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
                                                   String copyFileFromPath,
                                                   String copyFileToContainerPath) {
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse(DOCKER_IMAGE_NAME));
        container.addExposedPort(APP_PORT);
        container.withNetwork(network);
        container.withNetworkAliases(containerName);
        if(envVars != null)
            container.withEnv(envVars);
        if(copyFileFromPath != null && copyFileToContainerPath != null)
            container.withCopyFileToContainer(getMountableFile(copyFileFromPath), copyFileToContainerPath);
        container.start();
        container.waitingFor(Wait.forHttp(DEPLOYED_APP_PATH));
        WaitUtil.sleep(30000);
        LOGGER.info(String.format("App Localhost accessible url for %s: http://localhost:%s%s", containerName, container.getMappedPort(APP_PORT), DEPLOYED_APP_PATH));
        LOGGER.info(String.format("App Url accessible from the Selenium container: http://%s:%s%s", containerName, APP_PORT, DEPLOYED_APP_PATH));
        return new AppContainerData(container, String.format("http://%s:%s%s", containerName, APP_PORT, DEPLOYED_APP_PATH));
    }

    private static MountableFile getMountableFile(String resourcePath) {
        File file = new File(resourcePath);
        Path path = file.toPath();
        return MountableFile.forHostPath(path);
    }
}
