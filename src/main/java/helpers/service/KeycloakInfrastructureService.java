package helpers.service;

import configuration.network.NetworkPool;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;

/**
 * Ephemeral OIDC identity provider (Keycloak) for SSO UI tests, started for the
 * duration of a single test on a shared Docker network.
 * <p>
 * Networking model (parallel-safe): Keycloak joins the test network under the alias
 * {@value #ALIAS} and its frontend hostname is pinned to {@code http://keycloak:8080}.
 * The Studio container and the containerised Playwright browser both reach it by that
 * exact URL via the isolated network, so the OIDC issuer is identical for the browser,
 * Studio and the token — no host port mapping is involved in the SSO flow. The host-mapped
 * port is used only by the test JVM to mint bearer tokens for REST setup.
 * <p>
 * Must run in {@code execution.mode=PLAYWRIGHT_DOCKER} so the browser shares the network.
 */
public class KeycloakInfrastructureService {

    private static final Logger LOGGER = LogManager.getLogger(KeycloakInfrastructureService.class);

    public static final String ALIAS = "keycloak";
    public static final int PORT = 8080;
    public static final String REALM = "openlstudio";
    public static final String CLIENT_ID = "openlstudio";
    public static final String CLIENT_SECRET = "openlstudiosecret";
    /** Issuer as seen on the shared network — identical for browser, Studio and token. */
    public static final String ISSUER_URI = "http://" + ALIAS + ":" + PORT + "/realms/" + REALM;

    private static final String IMAGE = "quay.io/keycloak/keycloak:26.0";
    private static final String REALM_CLASSPATH_RESOURCE = "keycloak/openl-bypass-realm.json";
    private static final String REALM_IMPORT_PATH = "/opt/keycloak/data/import/realm.json";

    private Network network;
    private GenericContainer<?> keycloak;

    /** Creates the shared network (registered in {@link NetworkPool}) and starts Keycloak on it. */
    public void start() {
        network = Network.newNetwork();
        NetworkPool.setNetwork(network);

        keycloak = new GenericContainer<>(DockerImageName.parse(IMAGE))
                .withNetwork(network)
                .withNetworkAliases(ALIAS)
                .withExposedPorts(PORT)
                .withEnv("KEYCLOAK_ADMIN", "admin")
                .withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
                .withEnv("KC_HOSTNAME", "http://" + ALIAS + ":" + PORT)
                .withEnv("KC_HOSTNAME_STRICT", "false")
                .withEnv("KC_HTTP_ENABLED", "true")
                .withCopyFileToContainer(
                        MountableFile.forClasspathResource(REALM_CLASSPATH_RESOURCE), REALM_IMPORT_PATH)
                .withCommand("start-dev", "--import-realm")
                .waitingFor(Wait.forHttp("/realms/" + REALM + "/.well-known/openid-configuration")
                        .forPort(PORT)
                        .forStatusCode(200)
                        .withStartupTimeout(Duration.ofMinutes(3)));

        LOGGER.info("Starting Keycloak ({}) on network alias '{}'", IMAGE, ALIAS);
        keycloak.start();
        LOGGER.info("Keycloak ready. In-network issuer: {} | host token endpoint: {}",
                ISSUER_URI, hostBaseUrl());
    }

    /** Resource-owner-password-grant access token, minted by the test JVM via the host port. */
    public String getAccessToken(String username, String password) {
        Response resp = RestAssured.given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "password")
                .formParam("client_id", CLIENT_ID)
                .formParam("client_secret", CLIENT_SECRET)
                .formParam("username", username)
                .formParam("password", password)
                .formParam("scope", "openid")
                .post(hostBaseUrl() + "/realms/" + REALM + "/protocol/openid-connect/token");
        if (resp.getStatusCode() != 200) {
            throw new IllegalStateException("Failed to obtain Keycloak token for " + username
                    + ": HTTP " + resp.getStatusCode() + " " + resp.asString());
        }
        return resp.jsonPath().getString("access_token");
    }

    public void stop() {
        if (keycloak != null) {
            keycloak.stop();
        }
    }

    private String hostBaseUrl() {
        return "http://" + keycloak.getHost() + ":" + keycloak.getMappedPort(PORT);
    }
}
