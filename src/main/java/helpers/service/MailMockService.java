package helpers.service;

import configuration.network.NetworkPool;
import io.restassured.RestAssured;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

public class MailMockService {

    private static final Logger LOGGER = LogManager.getLogger(MailMockService.class);

    public static final String ALIAS = "mailmock";
    public static final int SMTP_PORT = 1025;
    public static final int API_PORT = 8025;
    public static final String SMTP_URL = "smtp://" + ALIAS + ":" + SMTP_PORT;
    public static final String USERNAME = "studio-mailer";
    public static final String PASSWORD = "studio-mailer-pass";

    private static final String IMAGE = "axllent/mailpit:v1.27";
    private static final Duration STARTUP_TIMEOUT = Duration.ofMinutes(2);

    private GenericContainer<?> mailpit;

    public void start() {
        Network network = NetworkPool.getNetwork();
        if (network == null) {
            network = Network.newNetwork();
            NetworkPool.setNetwork(network);
        }
        mailpit = new GenericContainer<>(DockerImageName.parse(IMAGE))
                .withNetwork(network)
                .withNetworkAliases(ALIAS)
                .withExposedPorts(API_PORT, SMTP_PORT)
                .withEnv("MP_SMTP_AUTH_ACCEPT_ANY", "1")
                .withEnv("MP_SMTP_AUTH_ALLOW_INSECURE", "1")
                .waitingFor(Wait.forHttp("/livez").forPort(API_PORT).forStatusCode(200)
                        .withStartupTimeout(STARTUP_TIMEOUT));
        LOGGER.info("Starting Mailpit ({}) on network alias '{}'", IMAGE, ALIAS);
        mailpit.start();
        LOGGER.info("Mailpit ready. In-network SMTP: {} | host API: {}", SMTP_URL, hostApiUrl());
    }

    public int getReceivedMessagesCount() {
        return RestAssured.given().get(hostApiUrl() + "/api/v1/messages").jsonPath().getInt("total");
    }

    public String getReceivedMessagesJson() {
        return RestAssured.given().get(hostApiUrl() + "/api/v1/messages").asString();
    }

    public void stop() {
        if (mailpit != null) {
            mailpit.stop();
        }
    }

    private String hostApiUrl() {
        return "http://" + mailpit.getHost() + ":" + mailpit.getMappedPort(API_PORT);
    }
}
