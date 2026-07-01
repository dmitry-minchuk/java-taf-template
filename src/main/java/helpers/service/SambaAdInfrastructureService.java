package helpers.service;

import com.github.dockerjava.api.command.CreateContainerCmd;
import configuration.network.NetworkPool;
import helpers.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.time.Duration;

/**
 * Ephemeral Active Directory domain controller (Samba 4 AD DC) for AD (LDAP) auth UI tests,
 * started for the duration of a single test on the shared Docker network.
 * <p>
 * Samba in AD DC mode is a real, AD-compatible controller (LDAP + AD schema: {@code sAMAccountName},
 * {@code userPrincipalName}, {@code memberOf}), so Spring's {@code ActiveDirectoryLdapAuthenticationProvider}
 * (UPN bind + {@code userPrincipalName} search) works against it exactly as against Microsoft AD —
 * no real Microsoft infrastructure required.
 * <p>
 * Networking: the DC joins the test network under alias {@value #ALIAS}; WebStudio ({@code user.mode=ad})
 * reaches it at {@value #LDAP_URL} over the LDAP backchannel. AD login is form-based, so the browser
 * never talks to the DC — only the Studio container needs the network. {@code INSECURELDAP=true} enables
 * simple bind over plain 389 (no TLS) and {@code NOCOMPLEXITY=true} allows the fixed test password.
 */
public class SambaAdInfrastructureService {

    private static final Logger LOGGER = LogManager.getLogger(SambaAdInfrastructureService.class);

    public static final String ALIAS = "samba";
    public static final int LDAP_PORT = 389;
    public static final String DOMAIN = "openl.local";
    public static final String LDAP_URL = "ldap://" + ALIAS + ":" + LDAP_PORT;

    /** AD user listed in {@code security.administrators} → Studio admin. */
    public static final String ADMIN_USER = "studioadmin";
    /** AD user with no admin/role grant → plain authenticated user. */
    public static final String REGULAR_USER = "studiouser";
    public static final String USER_PASSWORD = "OpenLPass123!";

    private static final String IMAGE = "nowsci/samba-domain:latest";

    private Network network;
    private GenericContainer<?> samba;

    /** Creates the shared network (registered in {@link NetworkPool}), provisions the AD domain and users. */
    public void start() {
        network = Network.newNetwork();
        NetworkPool.setNetwork(network);

        samba = new GenericContainer<>(DockerImageName.parse(IMAGE))
                .withNetwork(network)
                .withNetworkAliases(ALIAS)
                .withPrivilegedMode(true)
                .withExposedPorts(LDAP_PORT)
                .withEnv("DOMAIN", DOMAIN.toUpperCase())
                .withEnv("DOMAINPASS", USER_PASSWORD)
                .withEnv("NOCOMPLEXITY", "true")
                .withEnv("INSECURELDAP", "true")
                // Short NetBIOS-safe hostname; the default random container name breaks DC provisioning.
                .withCreateContainerCmdModifier((CreateContainerCmd cmd) -> cmd.withHostName("dc1"))
                .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(5)));

        LOGGER.info("Starting Samba AD DC ({}) on network alias '{}', domain {}", IMAGE, ALIAS, DOMAIN);
        samba.start();
        waitForDomainReady();
        createUser(ADMIN_USER, "Studio", "Admin");
        createUser(REGULAR_USER, "Studio", "User");
        LOGGER.info("Samba AD DC ready. In-network LDAP: {}", LDAP_URL);
    }

    /** The DC provisions the domain during entrypoint; poll until {@code samba-tool} answers. */
    private void waitForDomainReady() {
        WaitUtil.waitForCondition(() -> {
            try {
                return samba.execInContainer("samba-tool", "user", "list").getExitCode() == 0;
            } catch (Exception e) {
                return false;
            }
        }, 180_000, 3_000, "Waiting for Samba AD domain to finish provisioning");
    }

    private void createUser(String username, String givenName, String surname) {
        try {
            Container.ExecResult result = samba.execInContainer(
                    "samba-tool", "user", "create", username, USER_PASSWORD,
                    "--given-name=" + givenName,
                    "--surname=" + surname,
                    "--mail-address=" + username + "@" + DOMAIN);
            if (result.getExitCode() != 0) {
                throw new IllegalStateException("Failed to create AD user " + username + ": " + result.getStderr());
            }
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("Error creating AD user " + username, e);
        }
    }

    public void stop() {
        if (samba != null) {
            samba.stop();
        }
    }
}
