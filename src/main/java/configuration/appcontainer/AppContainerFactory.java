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
    private static final Duration DEFAULT_STARTUP_TIMEOUT = Duration.ofMinutes(
            Long.parseLong(ProjectConfiguration.getProperty(PropertyNameSpace.APP_CONTAINER_STARTUP_TIMEOUT_MINUTES)));

    public static AppContainerData createContainer(String containerName,
                                                   Network network,
                                                   Map<String, String> envVars,
                                                   Map<String, String> filesToCopy,
                                                   String dockerImageName) {
        return createContainer(containerName, network, envVars, filesToCopy, dockerImageName, DEFAULT_STARTUP_TIMEOUT);
    }

    public static AppContainerData createContainer(String containerName,
                                                   Network network,
                                                   Map<String, String> envVars,
                                                   Map<String, String> filesToCopy,
                                                   String dockerImageName,
                                                   Duration startupTimeout) {
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse(dockerImageName));
        container.addExposedPort(APP_PORT);
        container.withNetwork(network);
        container.withNetworkAliases(containerName);
        if(envVars != null)
            container.withEnv(envVars);
        if(filesToCopy != null)
            filesToCopy.forEach((hostPath, containerPath) ->
                    container.withCopyFileToContainer(getMountableFile(hostPath), containerPath));
        container.waitingFor(buildHttpWaitStrategy(envVars, startupTimeout));
        LOGGER.info("Starting app container: {} (startup timeout: {})", containerName, startupTimeout);
        container.start();

        LOGGER.info(String.format("App Localhost accessible url for %s: http://localhost:%s%s", containerName, container.getMappedPort(APP_PORT), DEPLOYED_APP_PATH));
        LOGGER.info(String.format("App Url accessible from the Browser container: http://%s:%s%s", containerName, APP_PORT, DEPLOYED_APP_PATH));
        return new AppContainerData(container, String.format("http://%s:%s%s", containerName, APP_PORT, DEPLOYED_APP_PATH));
    }

    private static org.testcontainers.containers.wait.strategy.WaitStrategy buildHttpWaitStrategy(
            Map<String, String> envVars, Duration startupTimeout) {
        // In oauth2/saml SSO mode the root path 302-redirects to the external IdP (e.g. keycloak:8080),
        // a network alias the host running the wait check cannot resolve — HttpURLConnection would
        // follow the redirect and throw UnknownHostException. Wait on a static, permit-all resource
        // that responds without redirecting instead, accepting any non-server-error status.
        String userMode = envVars == null ? null : envVars.get("user.mode");
        boolean externalAuth = "oauth2".equals(userMode) || "saml".equals(userMode);
        if (externalAuth) {
            return Wait.forHttp("/favicon.ico")
                    .forStatusCodeMatching(code -> code >= 200 && code < 500)
                    .withReadTimeout(Duration.ofSeconds(20))
                    .withStartupTimeout(startupTimeout);
        }
        return Wait.forHttp(DEPLOYED_APP_PATH)
                .forStatusCode(200)
                // withReadTimeout prevents indefinite hang when files are copied to a running container:
                // TestContainers copies files after container starts, OpenL detects them and recompiles
                // rules, which blocks the HTTP server. Without a read timeout, the TCP connection
                // succeeds but HttpURLConnection.getResponseCode() hangs forever on parseHTTPHeader.
                // A read timeout causes SocketTimeoutException -> IOException -> TestContainers retries
                // the health check until OpenL finishes recompiling and the server responds again.
                .withReadTimeout(Duration.ofSeconds(20))
                .withStartupTimeout(startupTimeout);
    }

    private static MountableFile getMountableFile(String resourcePath) {
        File file = new File(resourcePath);
        Path path = file.toPath();
        if (file.isDirectory()) {
            // Directories need execute bit to be traversable (0755).
            // MountableFile.forHostPath applies fileMode to ALL TAR entries, including
            // subdirectory entries inside the archive. Using 0666 on directories removes
            // the execute bit, making them inaccessible inside the container.
            return MountableFile.forHostPath(path, 0755);
        }
        // Files use 0666 (owner rw, group rw, others rw) so that container users
        // (e.g. openl uid=1000) can read AND write them.
        // WebStudio's Migrator writes back to .properties after migration.
        return MountableFile.forHostPath(path, 0666);
    }
}
