package helpers.service;

import configuration.network.NetworkPool;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
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
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketVersioningStatus;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetBucketVersioningRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutBucketVersioningRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.VersioningConfiguration;

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
    private static final String S3MOCK_ALIAS = "s3mock";
    private static final int S3MOCK_HTTP_PORT = 9090;
    private static final String S3_ACCESS_KEY = "test";
    private static final String S3_SECRET_KEY = "test";
    private static final String S3_REGION = "us-east-1";

    public enum DbType { NONE, POSTGRES, ORACLE, MSSQL }
    public enum PostgresMode { PRODUCTION_REPO, SECURITY_DB }

    private final DbType dbType;
    private final PostgresMode postgresMode;
    private final boolean withWs;
    private final boolean withS3Mock;

    private Network network;
    private PostgreSQLContainer<?> postgresContainer;
    private OracleContainer oracleContainer;
    private MSSQLServerContainer<?> msSqlContainer;
    private GenericContainer<?> wsContainer;
    private GenericContainer<?> s3MockContainer;
    private S3Client s3Client;
    private String bucketName;

    private DeployInfrastructureService(DbType dbType, PostgresMode postgresMode, boolean withWs, boolean withS3Mock) {
        this.dbType = dbType;
        this.postgresMode = postgresMode;
        this.withWs = withWs;
        this.withS3Mock = withS3Mock;
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

        if (withS3Mock) {
            startS3MockContainer();
            initializeS3Client();
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
        if (s3MockContainer != null && s3MockContainer.isRunning()) {
            LOGGER.info("Stopping S3Mock container...");
            s3MockContainer.stop();
        }
        if (s3Client != null) {
            s3Client.close();
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
        ensureS3MockEnabled();
        bucketName = "openl-test-" + UUID.randomUUID().toString().replace("-", "");
        try {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
            return bucketName;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create S3 bucket '" + bucketName + "'", e);
        }
    }

    public Map<String, String> getS3RuleServiceConfig(String classpathJarMode) {
        ensureBucketCreated();

        Map<String, String> config = new HashMap<>();
        config.put("JAVA_OPTS", "-Xms32m -XX:MaxRAMPercentage=50.0");
        config.put("ruleservice.deployer.enabled", "true");
        config.put("production-repository.factory", "repo-aws-s3");
        config.put("production-repository.service-endpoint", getS3MockInNetworkEndpoint());
        config.put("production-repository.bucket-name", bucketName);
        config.put("production-repository.region-name", S3_REGION);
        config.put("production-repository.access-key", S3_ACCESS_KEY);
        config.put("production-repository.secret-key", S3_SECRET_KEY);
        config.put("production-repository.listener-timer-period", "2");
        config.put("ruleservice.datasource.deploy.classpath.jars", classpathJarMode);
        return config;
    }

    public Map<String, String> getRuleServiceConfig(String classpathJarMode) {
        return getS3RuleServiceConfig(classpathJarMode);
    }

    public boolean isBucketEmpty() {
        return snapshotObjects().isEmpty();
    }

    public boolean bucketExists() {
        ensureBucketCreated();
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            return true;
        } catch (NoSuchBucketException e) {
            return false;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            throw new RuntimeException("Failed to check S3 bucket existence for '" + bucketName + "'", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to check S3 bucket existence for '" + bucketName + "'", e);
        }
    }

    public boolean isBucketVersioningEnabled() {
        ensureBucketCreated();
        try {
            var versioning = s3Client.getBucketVersioning(
                    GetBucketVersioningRequest.builder().bucket(bucketName).build());
            return BucketVersioningStatus.ENABLED.equals(versioning.status());
        } catch (Exception e) {
            throw new RuntimeException("Failed to read S3 bucket versioning for '" + bucketName + "'", e);
        }
    }

    public Map<String, ObjectSnapshot> snapshotObjects() {
        ensureBucketCreated();

        Map<String, ObjectSnapshot> result = new TreeMap<>();
        try {
            var objects = s3Client.listObjectsV2Paginator(
                    ListObjectsV2Request.builder().bucket(bucketName).build()).contents();
            for (var object : objects) {
                String objectName = object.key();
                HeadObjectResponse response = s3Client.headObject(
                        HeadObjectRequest.builder().bucket(bucketName).key(objectName).build());
                result.put(objectName, ObjectSnapshot.from(response));
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to snapshot S3 objects for bucket '" + bucketName + "'", e);
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

    private void startS3MockContainer() {
        LOGGER.info("Starting S3Mock container...");
        s3MockContainer = new GenericContainer<>(DockerImageName.parse(getS3MockDockerImage()))
                .withNetwork(network)
                .withNetworkAliases(S3MOCK_ALIAS)
                .withExposedPorts(S3MOCK_HTTP_PORT)
                .waitingFor(Wait.forHttp("/favicon.ico")
                        .forPort(S3MOCK_HTTP_PORT)
                        .forStatusCode(200)
                        .withStartupTimeout(Duration.ofMinutes(3)));
        s3MockContainer.start();
    }

    private void initializeS3Client() {
        s3Client = S3Client.builder()
                .endpointOverride(java.net.URI.create(getS3MockHostEndpoint()))
                .region(Region.of(S3_REGION))
                .forcePathStyle(true)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(S3_ACCESS_KEY, S3_SECRET_KEY)))
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

    private String getS3MockDockerImage() {
        String configuredImage = ProjectConfiguration.getProperty(PropertyNameSpace.S3MOCK_DOCKER_IMAGE_NAME);
        if (configuredImage == null || configuredImage.isBlank()) {
            throw new IllegalStateException("S3Mock docker image is not configured.");
        }
        return configuredImage;
    }

    private String getS3MockHostEndpoint() {
        ensureS3MockContainerStarted();
        return "http://" + s3MockContainer.getHost() + ":" + s3MockContainer.getMappedPort(S3MOCK_HTTP_PORT);
    }

    private String getS3MockInNetworkEndpoint() {
        ensureS3MockContainerStarted();
        return "http://" + S3MOCK_ALIAS + ":" + S3MOCK_HTTP_PORT;
    }

    private void ensureS3MockEnabled() {
        ensureS3MockContainerStarted();
        if (s3Client == null) {
            throw new IllegalStateException("S3Mock infrastructure is not initialized. Configure builder with withS3Mock() and call start().");
        }
    }

    private void ensureS3MockContainerStarted() {
        if (!withS3Mock || s3MockContainer == null) {
            throw new IllegalStateException("S3Mock infrastructure is not initialized. Configure builder with withS3Mock() and call start().");
        }
    }

    private void ensureBucketCreated() {
        ensureS3MockEnabled();
        if (bucketName == null || bucketName.isBlank()) {
            throw new IllegalStateException("S3 bucket is not initialized. Call createBucket() first.");
        }
    }

    // ==================== Builder ====================

    public record ObjectSnapshot(String versionId, Instant lastModified, long size, String etag) {
        private static ObjectSnapshot from(HeadObjectResponse response) {
            return new ObjectSnapshot(
                    response.versionId(),
                    response.lastModified(),
                    response.contentLength(),
                    response.eTag());
        }
    }

    public static class Builder {
        private DbType dbType = DbType.NONE;
        private PostgresMode postgresMode = PostgresMode.PRODUCTION_REPO;
        private boolean withWs = false;
        private boolean withS3Mock = false;

        public Builder withS3Mock() {
            this.withS3Mock = true;
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
            return new DeployInfrastructureService(dbType, postgresMode, withWs, withS3Mock);
        }
    }
}
