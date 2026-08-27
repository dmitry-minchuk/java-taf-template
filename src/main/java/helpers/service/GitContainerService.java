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

public class GitContainerService {

    private static final Logger LOGGER = LogManager.getLogger(GitContainerService.class);

    private static final int GIT_DAEMON_PORT = 9418;
    private static final String REPO_NAME = "design";
    private static final String FIXTURE_RESOURCE = "/git_daemon_repo";
    private static final String BRANCH = "master";

    private static final String IMAGE = "alpine/git:2.49.1";
    private static final Duration STARTUP_TIMEOUT = Duration.ofMinutes(3);

    private final String alias;
    private final String repoName;
    private final String branch;
    private final Path fixtureDir;
    private GenericContainer<?> container;

    public GitContainerService(String alias) {
        this(alias, REPO_NAME, BRANCH, FIXTURE_RESOURCE);
    }

    public GitContainerService(String alias, String repoName, String branch, String fixtureResource) {
        this.alias = alias;
        this.repoName = repoName;
        this.branch = branch;
        this.fixtureDir = resolveFixtureDir(fixtureResource);
    }

    public void start() {
        Network network = NetworkPool.getNetwork();
        if (network == null) {
            network = Network.newNetwork();
            NetworkPool.setNetwork(network);
        }
        container = new GenericContainer<>(DockerImageName.parse(IMAGE))
                .withNetwork(network)
                .withNetworkAliases(alias)
                .withExposedPorts(GIT_DAEMON_PORT)
                .withCopyFileToContainer(MountableFile.forHostPath(fixtureDir, 0755), "/tmp/fixture")
                .withCreateContainerCmdModifier(cmd -> cmd.withEntrypoint("sh"))
                .withCommand("-c", setupScript())
                .waitingFor(Wait.forListeningPort().withStartupTimeout(STARTUP_TIMEOUT));
        LOGGER.info("Starting git daemon ({}) on network alias '{}'", IMAGE, alias);
        container.start();
        LOGGER.info("git daemon ready. Host URL: {} | in-network URL: {}", getHostUrl(), getInNetworkUrl());
    }

    public String getHostUrl() {
        return "git://" + container.getHost() + ":" + container.getMappedPort(GIT_DAEMON_PORT) + "/" + repoName;
    }

    public String getInNetworkUrl() {
        return "git://" + alias + ":" + GIT_DAEMON_PORT + "/" + repoName;
    }

    public GitRemote asRemote() {
        return GitRemote.anonymous(getHostUrl());
    }

    public void stop() {
        if (container != null) {
            container.stop();
        }
    }

    private String setupScript() {
        return """
                set -e
                apk add --no-cache git-daemon
                mkdir -p /work /srv/git
                cp -a /tmp/fixture/. /work/
                cd /work
                git config --global --add safe.directory /work
                git config --global --add safe.directory /srv/git/%s.git
                git init -b %s
                git config user.email "test@example.com"
                git config user.name "Test"
                git add -A
                git commit -m "Initial commit"
                git clone --bare /work /srv/git/%s.git
                exec git daemon --reuseaddr --enable=receive-pack --base-path=/srv/git --export-all --port=%d
                """.formatted(repoName, branch, repoName, GIT_DAEMON_PORT);
    }

    private static Path resolveFixtureDir(String fixtureResource) {
        URL url = GitContainerService.class.getResource(fixtureResource);
        if (url == null) {
            throw new IllegalStateException("Fixture resource not found on classpath: " + fixtureResource);
        }
        try {
            return Paths.get(url.toURI());
        } catch (Exception e) {
            throw new IllegalStateException("Cannot resolve fixture directory for " + fixtureResource, e);
        }
    }
}
