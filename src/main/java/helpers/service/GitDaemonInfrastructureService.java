package helpers.service;

import configuration.network.NetworkPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

/**
 * Starts an {@code alpine/git} container that serves a local design repository over the
 * {@code git://} protocol (git daemon), replacing the real remote GitHub repository that the
 * STUDIO_GIT tests used to point at.
 *
 * <p>The image does not ship the git daemon binary, so the separate {@code git-daemon} package is
 * installed inside the container before the daemon is started. The fixture repository (a copy of
 * the old {@code forTest4} design repository) is copied into the container, initialised as a bare
 * repository on the {@code master} branch and exported read/write by the daemon.
 */
public class GitDaemonInfrastructureService {

    private static final Logger LOGGER = LogManager.getLogger(GitDaemonInfrastructureService.class);

    public static final String ALIAS = "git-daemon";
    public static final int GIT_DAEMON_PORT = 9418;
    public static final String REPO_NAME = "design";
    public static final String FIXTURE_RESOURCE = "/git_daemon_repo";

    private static final String IMAGE = "alpine/git:2.49.1";
    private static final Duration STARTUP_TIMEOUT = Duration.ofMinutes(3);

    /**
     * Runs inside the container: installs the git-daemon package, builds a working repository from
     * the copied fixture, exports it as a bare repository and starts the daemon (which becomes PID 1).
     */
    private static final String SETUP_SCRIPT = """
            set -e
            apk add --no-cache git-daemon
            mkdir -p /work /srv/git
            cp -a /tmp/fixture/. /work/
            cd /work
            git config --global --add safe.directory /work
            git config --global --add safe.directory /srv/git/design.git
            git init -b master
            git config user.email "test@example.com"
            git config user.name "Test"
            git add -A
            git commit -m "Initial commit"
            git clone --bare /work /srv/git/design.git
            exec git daemon --reuseaddr --enable=receive-pack --base-path=/srv/git --export-all --port=9418
            """;

    private final Path fixtureDir;
    private GenericContainer<?> container;

    public GitDaemonInfrastructureService() {
        this.fixtureDir = resolveFixtureDir();
    }

    public void start() {
        Network network = NetworkPool.getNetwork();
        if (network == null) {
            network = Network.newNetwork();
            NetworkPool.setNetwork(network);
        }
        container = new GenericContainer<>(DockerImageName.parse(IMAGE))
                .withNetwork(network)
                .withNetworkAliases(ALIAS)
                .withExposedPorts(GIT_DAEMON_PORT)
                .withCopyFileToContainer(MountableFile.forHostPath(fixtureDir, 0755), "/tmp/fixture")
                // The alpine/git image sets ENTRYPOINT to ["git"]; override it so we can run the setup script.
                .withCreateContainerCmdModifier(cmd -> cmd.withEntrypoint("sh"))
                .withCommand("-c", SETUP_SCRIPT)
                .waitingFor(Wait.forListeningPort().withStartupTimeout(STARTUP_TIMEOUT));
        LOGGER.info("Starting git daemon ({}) on network alias '{}'", IMAGE, ALIAS);
        container.start();
        LOGGER.info("git daemon ready. Host URL: {} | in-network URL: {}", getHostUrl(), getInNetworkUrl());
    }

    /** Repository URL reachable from the test JVM (host side), used by JGit. */
    public String getHostUrl() {
        return "git://" + container.getHost() + ":" + container.getMappedPort(GIT_DAEMON_PORT) + "/" + REPO_NAME;
    }

    /** Repository URL reachable from the app container over the shared Docker network. */
    public String getInNetworkUrl() {
        return "git://" + ALIAS + ":" + GIT_DAEMON_PORT + "/" + REPO_NAME;
    }

    public void stop() {
        if (container != null) {
            container.stop();
        }
    }

    /**
     * Resolves the fixture directory on the classpath. Assumes the test resources are on the
     * classpath as a plain directory (as in IDE/Maven test runs), not packaged inside a JAR.
     */
    private static Path resolveFixtureDir() {
        URL url = GitDaemonInfrastructureService.class.getResource(FIXTURE_RESOURCE);
        if (url == null) {
            throw new IllegalStateException("Fixture resource not found on classpath: " + FIXTURE_RESOURCE);
        }
        try {
            return Paths.get(url.toURI());
        } catch (Exception e) {
            throw new IllegalStateException("Cannot resolve fixture directory for " + FIXTURE_RESOURCE, e);
        }
    }
}
