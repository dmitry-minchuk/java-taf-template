package helpers.service;

import configuration.network.NetworkPool;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.oracle.OracleContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class DeployInfrastructureService {

    private static final Logger LOGGER = LogManager.getLogger(DeployInfrastructureService.class);
    private static final int WS_PORT = 8080;
    private static final String POSTGRES_ALIAS = "postgres";
    private static final String POSTGRES_JDBC_URL = "jdbc:postgresql://" + POSTGRES_ALIAS + ":5432/openl?currentSchema=repository";

    public enum DbType { POSTGRES, ORACLE }
    public enum PostgresMode { PRODUCTION_REPO, SECURITY_DB }

    private final DbType dbType;
    private final PostgresMode postgresMode;
    private final boolean withWs;

    private Network network;
    private PostgreSQLContainer<?> postgresContainer;
    private OracleContainer oracleContainer;
    private GenericContainer<?> wsContainer;

    private DeployInfrastructureService(DbType dbType, PostgresMode postgresMode, boolean withWs) {
        this.dbType = dbType;
        this.postgresMode = postgresMode;
        this.withWs = withWs;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void start() {
        network = Network.newNetwork();
        NetworkPool.setNetwork(network);

        if (dbType == DbType.POSTGRES) {
            if (postgresMode == PostgresMode.SECURITY_DB) {
                startPostgresAsSecurityDb();
            } else {
                startPostgres();
            }
        } else {
            startOracle();
        }

        if (withWs) {
            startWsContainer();
        }
    }

    public void cleanup() {
        if (wsContainer != null && wsContainer.isRunning()) {
            LOGGER.info("Stopping WebService container...");
            wsContainer.stop();
        }
        if (postgresContainer != null && postgresContainer.isRunning()) {
            LOGGER.info("Stopping PostgreSQL container...");
            postgresContainer.stop();
        }
        if (oracleContainer != null && oracleContainer.isRunning()) {
            LOGGER.info("Stopping Oracle container...");
            oracleContainer.stop();
        }
    }

    public Map<String, String> getFilesToCopy() {
        Map<String, String> files = new HashMap<>();
        if (dbType == DbType.POSTGRES) {
            files.put(getPostgresJarPath(), "/opt/openl/lib/postgresql.jar");
            if (postgresMode == PostgresMode.PRODUCTION_REPO) {
                files.put(createProductionRepoProperties().toAbsolutePath().toString(),
                        "/opt/openl/shared/.properties");
            }
        } else {
            files.put(getOracleJarPath(), "/opt/openl/lib/ojdbc11.jar");
        }
        return files;
    }

    public GenericContainer<?> getWsContainer() {
        return wsContainer;
    }

    public OracleContainer getOracleContainer() {
        return oracleContainer;
    }

    public String getOracleJdbcUrl() {
        if (oracleContainer == null) throw new IllegalStateException("Oracle not started");
        return "jdbc:oracle:thin:@oracle:1521/" + oracleContainer.getDatabaseName();
    }

    public PostgreSQLContainer<?> getPostgresContainer() {
        if (postgresContainer == null) throw new IllegalStateException("PostgreSQL not started");
        return postgresContainer;
    }

    public Map<String, String> getContainerConfig() {
        Map<String, String> config = new HashMap<>();
        if (dbType == DbType.POSTGRES && postgresMode == PostgresMode.SECURITY_DB) {
            String pgJdbcUrl = "jdbc:postgresql://" + POSTGRES_ALIAS + ":5432/" + postgresContainer.getDatabaseName();
            config.put("db.url", pgJdbcUrl);
            config.put("db.user", postgresContainer.getUsername());
            config.put("db.password", postgresContainer.getPassword());
        }
        return config;
    }

    // ==================== Private ====================

    private void startPostgres() {
        LOGGER.info("Starting PostgreSQL container...");
        postgresContainer = new PostgreSQLContainer<>(
                ProjectConfiguration.getProperty(PropertyNameSpace.DB_POSTGRES_CONTAINER_IMAGE))
                .withDatabaseName("openl")
                .withUsername("openl")
                .withPassword("openl")
                .withNetwork(network)
                .withNetworkAliases(POSTGRES_ALIAS);
        postgresContainer.start();
        createRepositorySchema();
        LOGGER.info("PostgreSQL started. In-network URL: {}", POSTGRES_JDBC_URL);
    }

    private void startPostgresAsSecurityDb() {
        LOGGER.info("Starting PostgreSQL container as security DB...");
        postgresContainer = new PostgreSQLContainer<>(
                ProjectConfiguration.getProperty(PropertyNameSpace.DB_POSTGRES_CONTAINER_IMAGE))
                .withNetwork(network)
                .withNetworkAliases(POSTGRES_ALIAS);
        postgresContainer.start();
        String pgJdbcUrl = "jdbc:postgresql://" + POSTGRES_ALIAS + ":5432/" + postgresContainer.getDatabaseName();
        LOGGER.info("PostgreSQL (security DB) started. In-network URL: {}", pgJdbcUrl);
    }

    private void createRepositorySchema() {
        try (var conn = java.sql.DriverManager.getConnection(
                postgresContainer.getJdbcUrl(),
                postgresContainer.getUsername(),
                postgresContainer.getPassword());
             var stmt = conn.createStatement()) {
            stmt.execute("CREATE SCHEMA IF NOT EXISTS repository");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create 'repository' schema", e);
        }
    }

    private void startOracle() {
        LOGGER.info("Starting Oracle container...");
        oracleContainer = new OracleContainer(
                ProjectConfiguration.getProperty(PropertyNameSpace.DB_ORACLE_CONTAINER_IMAGE))
                .withNetwork(network)
                .withNetworkAliases("oracle")
                .withStartupTimeout(Duration.ofMinutes(5));
        oracleContainer.start();
        LOGGER.info("Oracle started. In-network JDBC URL: {}", getOracleJdbcUrl());
    }

    private void startWsContainer() {
        LOGGER.info("Starting WebService container...");
        String wsImage = ProjectConfiguration.getProperty(PropertyNameSpace.WS_DOCKER_IMAGE_NAME);
        wsContainer = new GenericContainer<>(DockerImageName.parse(wsImage))
                .withNetwork(network)
                .withNetworkAliases("wscontainer")
                .withExposedPorts(WS_PORT)
                .withEnv("JAVA_OPTS", "-Xms32m -XX:MaxRAMPercentage=50.0")
                .withEnv("PRODUCTION-REPOSITORY__REF_", "repo-jdbc")
                .withEnv("PRODUCTION-REPOSITORY_URI", POSTGRES_JDBC_URL)
                .withEnv("PRODUCTION-REPOSITORY_LOGIN", "openl")
                .withEnv("PRODUCTION-REPOSITORY_PASSWORD", "openl")
                .withEnv("RULESERVICE_DEPLOYER_ENABLED", "true")
                .withEnv("ruleservice.datasource.deploy.classpath.jars", "true")
                .withEnv("ruleservice.deployer.delay", "2")
                .withCopyFileToContainer(
                        MountableFile.forHostPath(Path.of(getPostgresJarPath())),
                        "/opt/openl/lib/postgresql.jar")
                .waitingFor(Wait.forHttp("/admin/healthcheck/startup")
                        .forStatusCode(200)
                        .withStartupTimeout(Duration.ofMinutes(5)));
        wsContainer.start();
        LOGGER.info("WebService started. Host port: {}", wsContainer.getMappedPort(WS_PORT));
    }

    private Path createProductionRepoProperties() {
        try {
            Path propsFile = Files.createTempFile("openl-deploy-", ".properties");
            Files.writeString(propsFile, String.join("\n",
                    "production-repository-configs = production",
                    "repository.production.name = Deployment",
                    "repository.production.$$ref = repo-jdbc",
                    "repository.production.uri = " + POSTGRES_JDBC_URL,
                    "repository.production.login = openl",
                    "repository.production.password = openl",
                    ""));
            propsFile.toFile().setReadable(true, false);
            return propsFile;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create production repo .properties", e);
        }
    }

    private String getPostgresJarPath() {
        return System.getProperty("user.home") + "/"
                + ProjectConfiguration.getProperty(PropertyNameSpace.DB_POSTGRES_JAR_MAVEN_PATH);
    }

    private String getOracleJarPath() {
        return System.getProperty("user.home") + "/"
                + ProjectConfiguration.getProperty(PropertyNameSpace.DB_ORACLE_JAR_MAVEN_PATH);
    }

    // ==================== Builder ====================

    public static class Builder {
        private DbType dbType = DbType.POSTGRES;
        private PostgresMode postgresMode = PostgresMode.PRODUCTION_REPO;
        private boolean withWs = false;

        public Builder withPostgres() {
            this.dbType = DbType.POSTGRES;
            this.postgresMode = PostgresMode.PRODUCTION_REPO;
            return this;
        }

        public Builder withPostgresAsSecurityDb() {
            this.dbType = DbType.POSTGRES;
            this.postgresMode = PostgresMode.SECURITY_DB;
            return this;
        }

        public Builder withOracle() {
            this.dbType = DbType.ORACLE;
            return this;
        }

        public Builder withWsContainer() {
            this.withWs = true;
            return this;
        }

        public DeployInfrastructureService build() {
            if (withWs && dbType != DbType.POSTGRES) {
                throw new IllegalStateException("WS container requires PostgreSQL");
            }
            if (withWs && postgresMode == PostgresMode.SECURITY_DB) {
                throw new IllegalStateException("WS container requires PostgreSQL in PRODUCTION_REPO mode");
            }
            return new DeployInfrastructureService(dbType, postgresMode, withWs);
        }
    }
}
