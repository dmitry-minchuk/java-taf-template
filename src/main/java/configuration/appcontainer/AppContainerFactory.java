package configuration.appcontainer;

import configuration.ProjectConfiguration;
import configuration.PropertyNameSpace;
import configuration.driver.DockerNetwork;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public class AppContainerFactory {
    protected static final Logger LOGGER = LogManager.getLogger(AppContainerFactory.class);
    private final static String APP_HOST_URL_TEMPLATE = "http://%s:%s%s";
    private final static Integer APP_PORT = Integer.parseInt(ProjectConfiguration.getProperty(PropertyNameSpace.DEFAULT_APP_PORT));
    private final static String DEPLOYED_APP_PATH = ProjectConfiguration.getProperty(PropertyNameSpace.DEPLOYED_APP_PATH);
    private final static String DOCKER_IMAGE_NAME = ProjectConfiguration.getProperty(PropertyNameSpace.DOCKER_IMAGE_NAME);

    public static AppContainerData createContainer(String containerName) {
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse(DOCKER_IMAGE_NAME));
        container.addExposedPort(APP_PORT);
        container.withNetwork(DockerNetwork.getNetwork());
        container.withNetworkAliases(containerName);
        container.start();
        container.waitingFor(Wait.forHttp(DEPLOYED_APP_PATH));
        LOGGER.info(String.format("Application Localhost accessible url for %s: http://localhost:%s%s", containerName, container.getMappedPort(APP_PORT), DEPLOYED_APP_PATH));
        LOGGER.info(String.format("Application Url accessible from the Selenium container: http://%s:%s%s", containerName, APP_PORT, DEPLOYED_APP_PATH));
        return new AppContainerData(container, String.format(
                APP_HOST_URL_TEMPLATE,
                containerName,
                APP_PORT,
                DEPLOYED_APP_PATH));
    }
}
