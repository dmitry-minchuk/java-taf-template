package helpers.service;

import configuration.network.NetworkPool;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import io.minio.BucketExistsArgs;
import io.minio.GetBucketVersioningArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.messages.VersioningConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MSSQLServerContainer;
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
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class DeployInfrastructureService {

    private static final Logger LOGGER = LogManager.getLogger(DeployInfrastructureService.class);
    private static final int WS_PORT = 8080;
    private static final int MSSQL_PORT = 1433;
    private static final String POSTGRES_ALIAS = "postgres";
    private static final String POSTGRES_JDBC_URL = "jdbc:postgresql://" + POSTGRES_ALIAS + ":5432/openl?currentSchema=repository";
    private static final String ORACLE_ALIAS = "oracle";
    private static final String MSSQL_ALIAS = "sqlserver";
    private static final String MSSQL_DB_NAME = "openl";
    private static final String MSSQL_PASSWORD = "Openl_Strong_Password_123!";
    private static final String MINIO_ALIAS = "minio";
    private static final int MINIO_PORT = 9000;
    private static final String MINIO_ROOT_USER = "minio";
    private static final String MINIO_ROOT_PASSWORD = "minio123";
    private static final String MINIO_REGION = "us-west-2";

    public enum DbType { NONE, POSTGRES, ORACLE, MSSQL }
    public enum PostgresMode { PRODUCTION_REPO, SECURITY_DB }

    private final DbType dbType;
    private final PostgresMode postgresMode;
    private final boolean withWs;
    private final boolean withMinio;

    private Network network;
    private PostgreSQLContainer<?> postgresContainer;
    private OracleContainer oracleContainer;
    private MSSQLServerContainer<?> msSqlContainer;
    private GenericContainer<?> wsContainer;
    private GenericContainer<?> minioContainer;
    private MinioClient minioClient;
    private String bucketName;

    private DeployInfrastructureService(DbType dbType, PostgresMode postgresMode, boolean withWs, boolean withMinio) {
        this.dbType = dbType;
        this.postgresMode = postgresMode;
        this.withWs = withWs;
        this.withMinio = withMinio;
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
        } else if (dbType == DbType.ORACLE) {
            startOracle();
        } else if (dbType == DbType.MSSQL) {
            startMsSql();
        }

        if (withMinio) {
            startMinioContainer();
            initializeMinioClient();
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
        if (msSqlContainer != null && msSqlContainer.isRunning()) {
            LOGGER.info("Stopping MS SQL container...");
            msSqlContainer.stop();
        }
        if (minioContainer != null && minioContainer.isRunning()) {
            LOGGER.info("Stopping Minio container...");
            minioContainer.stop();
        }
    }

    public Map<String, String> getFilesToCopy() {
        Map<String, String> files = new HashMap<>();
        switch (dbType) {
            case POSTGRES -> {
                files.put(getPostgresJarPath(), "/opt/openl/lib/postgresql.jar");
                if (postgresMode == PostgresMode.PRODUCTION_REPO) {
                    files.put(createProductionRepoProperties().toAbsolutePath().toString(),
                            "/opt/openl/shared/.properties");
                }
            }
            case ORACLE -> files.put(getOracleJarPath(), "/opt/openl/lib/ojdbc11.jar");
            case MSSQL -> files.put(getMsSqlJarPath(), "/opt/openl/lib/mssql-jdbc.jar");
            case NONE -> {
                return files;
            }
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
        return "jdbc:oracle:thin:@" + ORACLE_ALIAS + ":1521/" + oracleContainer.getDatabaseName();
    }

    public PostgreSQLContainer<?> getPostgresContainer() {
        if (postgresContainer == null) throw new IllegalStateException("PostgreSQL not started");
        return postgresContainer;
    }

    public MSSQLServerContainer<?> getMsSqlContainer() {
        if (msSqlContainer == null) throw new IllegalStateException("MS SQL not started");
        return msSqlContainer;
    }

    public String getMsSqlJdbcUrl() {
        if (msSqlContainer == null) throw new IllegalStateException("MS SQL not started");
        return "jdbc:sqlserver://" + MSSQL_ALIAS + ":" + MSSQL_PORT
                + ";databaseName=" + MSSQL_DB_NAME
                + ";encrypt=false;trustServerCertificate=true;";
    }

    public String getMsSqlHostJdbcUrl() {
        if (msSqlContainer == null) throw new IllegalStateException("MS SQL not started");
        return "jdbc:sqlserver://" + msSqlContainer.getHost() + ":" + msSqlContainer.getMappedPort(MSSQL_PORT)
                + ";databaseName=" + MSSQL_DB_NAME
                + ";encrypt=false;trustServerCertificate=true;";
    }

    public Map<String, String> getContainerConfig() {
        Map<String, String> config = new HashMap<>();
        if (dbType == DbType.POSTGRES && postgresMode == PostgresMode.SECURITY_DB) {
            String pgJdbcUrl = "jdbc:postgresql://" + POSTGRES_ALIAS + ":5432/" + postgresContainer.getDatabaseName();
            config.put("db.url", pgJdbcUrl);
            config.put("db.user", postgresContainer.getUsername());
            config.put("db.password", postgresContainer.getPassword());
        } else if (dbType == DbType.MSSQL) {
            config.put("db.url", getMsSqlJdbcUrl());
            config.put("db.user", msSqlContainer.getUsername());
            config.put("db.password", msSqlContainer.getPassword());
        }
        return config;
    }

    public String createBucket() {
        ensureMinioEnabled();
        bucketName = "openl-test-" + UUID.randomUUID().toString().replace("-", "");
        try {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            return bucketName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Minio bucket '" + bucketName + "'", e);
        }
    }

    public Map<String, String> getMinioRuleServiceConfig(String classpathJarMode) {
        ensureBucketCreated();

        Map<String, String> config = new HashMap<>();
        config.put("JAVA_OPTS", "-Xms32m -XX:MaxRAMPercentage=50.0");
        config.put("ruleservice.deployer.enabled", "true");
        config.put("production-repository.factory", "repo-aws-s3");
        config.put("production-repository.service-endpoint", getMinioInNetworkEndpoint());
        config.put("production-repository.bucket-name", bucketName);
        config.put("production-repository.region-name", MINIO_REGION);
        config.put("production-repository.access-key", MINIO_ROOT_USER);
        config.put("production-repository.secret-key", MINIO_ROOT_PASSWORD);
        config.put("production-repository.listener-timer-period", "2");
        config.put("ruleservice.datasource.deploy.classpath.jars", classpathJarMode);
        return config;
    }

    public Map<String, String> getRuleServiceConfig(String classpathJarMode) {
        return getMinioRuleServiceConfig(classpathJarMode);
    }

    public boolean isBucketEmpty() {
        return snapshotObjects().isEmpty();
    }

    public boolean bucketExists() {
        ensureBucketCreated();
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to check Minio bucket existence for '" + bucketName + "'", e);
        }
    }

    public boolean isBucketVersioningEnabled() {
        ensureBucketCreated();
        try {
            VersioningConfiguration versioning = minioClient.getBucketVersioning(
                    GetBucketVersioningArgs.builder().bucket(bucketName).build());
            return versioning != null && VersioningConfiguration.Status.ENABLED.equals(versioning.status());
        } catch (Exception e) {
            throw new RuntimeException("Failed to read Minio bucket versioning for '" + bucketName + "'", e);
        }
    }

    public Map<String, ObjectSnapshot> snapshotObjects() {
        ensureBucketCreated();

        Map<String, ObjectSnapshot> result = new TreeMap<>();
        try {
            var objects = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(bucketName).recursive(true).build()).iterator();
            while (objects.hasNext()) {
                String objectName = objects.next().get().objectName();
                StatObjectResponse response = minioClient.statObject(
                        StatObjectArgs.builder().bucket(bucketName).object(objectName).build());
                result.put(objectName, ObjectSnapshot.from(response));
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to snapshot Minio objects for bucket '" + bucketName + "'", e);
        }
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
                .withNetworkAliases(ORACLE_ALIAS)
                .withStartupTimeout(Duration.ofMinutes(5));
        oracleContainer.start();
        LOGGER.info("Oracle started. In-network JDBC URL: {}", getOracleJdbcUrl());
    }

    private void startMsSql() {
        LOGGER.info("Starting MS SQL container...");
        msSqlContainer = new MSSQLServerContainer<>(
                DockerImageName.parse(ProjectConfiguration.getProperty(PropertyNameSpace.DB_MSSQL_CONTAINER_IMAGE)))
                .acceptLicense()
                .withPassword(MSSQL_PASSWORD)
                .withNetwork(network)
                .withNetworkAliases(MSSQL_ALIAS)
                .withStartupTimeout(Duration.ofMinutes(5));
        msSqlContainer.start();
        createMsSqlDatabase();
        LOGGER.info("MS SQL started. In-network JDBC URL: {}", getMsSqlJdbcUrl());
    }

    private void createMsSqlDatabase() {
        try (var conn = java.sql.DriverManager.getConnection(
                msSqlContainer.getJdbcUrl(),
                msSqlContainer.getUsername(),
                msSqlContainer.getPassword());
             var stmt = conn.createStatement()) {
            stmt.execute("IF DB_ID('" + MSSQL_DB_NAME + "') IS NULL CREATE DATABASE " + MSSQL_DB_NAME);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create MS SQL database '" + MSSQL_DB_NAME + "'", e);
        }
    }

    private void startMinioContainer() {
        LOGGER.info("Starting Minio container...");
        minioContainer = new GenericContainer<>(
                DockerImageName.parse(ProjectConfiguration.getProperty(PropertyNameSpace.MINIO_DOCKER_IMAGE_NAME)))
                .withNetwork(network)
                .withNetworkAliases(MINIO_ALIAS)
                .withExposedPorts(MINIO_PORT)
                .withEnv("MINIO_ROOT_USER", MINIO_ROOT_USER)
                .withEnv("MINIO_ROOT_PASSWORD", MINIO_ROOT_PASSWORD)
                .withEnv("MINIO_REGION_NAME", MINIO_REGION)
                .withCommand("server", "/data", "--console-address", ":9001")
                .waitingFor(Wait.forHttp("/minio/health/ready")
                        .forPort(MINIO_PORT)
                        .forStatusCode(200)
                        .withStartupTimeout(Duration.ofMinutes(3)));
        minioContainer.start();
    }

    private void initializeMinioClient() {
        minioClient = MinioClient.builder()
                .endpoint(getMinioHostEndpoint())
                .credentials(MINIO_ROOT_USER, MINIO_ROOT_PASSWORD)
                .build();
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

    private String getMsSqlJarPath() {
        return System.getProperty("user.home") + "/"
                + ProjectConfiguration.getProperty(PropertyNameSpace.DB_MSSQL_JAR_MAVEN_PATH);
    }

    private String getMinioHostEndpoint() {
        ensureMinioContainerStarted();
        return "http://" + minioContainer.getHost() + ":" + minioContainer.getMappedPort(MINIO_PORT);
    }

    private String getMinioInNetworkEndpoint() {
        ensureMinioContainerStarted();
        return "http://" + MINIO_ALIAS + ":" + MINIO_PORT;
    }

    private void ensureMinioEnabled() {
        ensureMinioContainerStarted();
        if (minioClient == null) {
            throw new IllegalStateException("Minio infrastructure is not initialized. Configure builder with withMinio() and call start().");
        }
    }

    private void ensureMinioContainerStarted() {
        if (!withMinio || minioContainer == null) {
            throw new IllegalStateException("Minio infrastructure is not initialized. Configure builder with withMinio() and call start().");
        }
    }

    private void ensureBucketCreated() {
        ensureMinioEnabled();
        if (bucketName == null || bucketName.isBlank()) {
            throw new IllegalStateException("Minio bucket is not initialized. Call createBucket() first.");
        }
    }

    // ==================== Builder ====================

    public record ObjectSnapshot(String versionId, Instant lastModified, long size, String etag) {
        private static ObjectSnapshot from(StatObjectResponse response) {
            return new ObjectSnapshot(
                    response.versionId(),
                    response.lastModified().toInstant(),
                    response.size(),
                    response.etag());
        }
    }

    public static class Builder {
        private DbType dbType = DbType.NONE;
        private PostgresMode postgresMode = PostgresMode.PRODUCTION_REPO;
        private boolean withWs = false;
        private boolean withMinio = false;

        public Builder withMinio() {
            this.withMinio = true;
            return this;
        }

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

        public Builder withMsSql() {
            this.dbType = DbType.MSSQL;
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
            return new DeployInfrastructureService(dbType, postgresMode, withWs, withMinio);
        }
    }
}
