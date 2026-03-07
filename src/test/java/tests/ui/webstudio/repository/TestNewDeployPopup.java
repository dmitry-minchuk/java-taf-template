package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import configuration.network.NetworkPool;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.components.repositorytabcomponents.DeployModalComponent;
import domain.ui.webstudio.components.repositorytabcomponents.RepositoryContentTabPropertiesComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.StringUtil;
import helpers.utils.WaitUtil;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Migrated from: SmokeStudio/TestDeployConfCreateDeployRedeploy.java
 * Ticket: IPBQA-30049
 *
 * Deploy lifecycle: deploy project to production repository (PostgreSQL via JDBC),
 * edit and redeploy, deploy dependent projects, and verify that deployed rules
 * are accessible via WebService REST endpoint.
 *
 * NOTE: The legacy "Deploy Configuration" entity was removed from WebStudio
 * (EPBDS-15093). Deployment now works directly from a project via DeployModal.
 * Dependencies are resolved automatically by the backend.
 *
 * Infrastructure (3 containers in shared Docker network):
 * - PostgreSQL (alias "postgres") — production repository storage
 * - WebStudio (app container) — deploys rules to PostgreSQL via Docker DNS
 * - WebService (alias "wscontainer") — picks up deployed rules from PostgreSQL,
 *   exposes REST endpoints
 *
 * All containers communicate via Docker DNS aliases (no host.docker.internal).
 */
public class TestNewDeployPopup extends BaseTest {

    private static final int WS_PORT = 8080;
    private static final String POSTGRES_ALIAS = "postgres";
    private static final String POSTGRES_JDBC_URL = "jdbc:postgresql://" + POSTGRES_ALIAS + ":5432/openl?currentSchema=repository";

    private static final Map<String, String> additionalContainerFiles = new HashMap<>();

    private PostgreSQLContainer<?> postgresContainer;
    private GenericContainer<?> wsContainer;
    private Network deployNetwork;

    @Override
    @BeforeMethod
    public void beforeMethod(ITestResult result) {
        additionalContainerFiles.clear();

        // 1. Create shared Docker network and register BEFORE super.beforeMethod()
        // so BaseTest places the app container into the same network.
        deployNetwork = Network.newNetwork();
        NetworkPool.setNetwork(deployNetwork);

        // 2. Start PostgreSQL
        LOGGER.info("Starting PostgreSQL container in shared Docker network...");
        postgresContainer = new PostgreSQLContainer<>(
                ProjectConfiguration.getProperty(PropertyNameSpace.DB_POSTGRES_CONTAINER_IMAGE))
                .withDatabaseName("openl")
                .withUsername("openl")
                .withPassword("openl")
                .withNetwork(deployNetwork)
                .withNetworkAliases(POSTGRES_ALIAS);
        postgresContainer.start();
        LOGGER.info("PostgreSQL started. In-network URL: {}", POSTGRES_JDBC_URL);

        // Create 'repository' schema required by production-repository JDBC config
        try (var conn = java.sql.DriverManager.getConnection(
                postgresContainer.getJdbcUrl(),
                postgresContainer.getUsername(),
                postgresContainer.getPassword());
             var stmt = conn.createStatement()) {
            stmt.execute("CREATE SCHEMA IF NOT EXISTS repository");
            LOGGER.info("Schema 'repository' created in PostgreSQL");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create 'repository' schema", e);
        }

        // 3. PostgreSQL JDBC driver JAR — copied into both app and ws containers
        String pgJarPath = System.getProperty("user.home") + "/"
                + ProjectConfiguration.getProperty(PropertyNameSpace.DB_POSTGRES_JAR_MAVEN_PATH);

        // 3a. Create .properties for production repository ($$ref works only via file)
        Path propsFile;
        try {
            propsFile = Files.createTempFile("openl-deploy-", ".properties");
            String propsContent = String.join("\n",
                    "production-repository-configs = production",
                    "repository.production.name = Deployment",
                    "repository.production.$$ref = repo-jdbc",
                    "repository.production.uri = " + POSTGRES_JDBC_URL,
                    "repository.production.login = openl",
                    "repository.production.password = openl",
                    ""
            );
            Files.writeString(propsFile, propsContent);
            propsFile.toFile().setReadable(true, false);
            LOGGER.info("Created .properties for production repository: {}", propsFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create .properties file", e);
        }

        // Set additional files for app container (picked up by AppContainerFactory)
        additionalContainerFiles.put(pgJarPath, "/opt/openl/lib/postgresql.jar");
        additionalContainerFiles.put(propsFile.toAbsolutePath().toString(), "/opt/openl/shared/.properties");

        // 4. Start WebService container
        LOGGER.info("Starting WebService container in shared Docker network...");
        String wsImageName = ProjectConfiguration.getProperty(PropertyNameSpace.WS_DOCKER_IMAGE_NAME);
        wsContainer = new GenericContainer<>(DockerImageName.parse(wsImageName))
                .withNetwork(deployNetwork)
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
                        MountableFile.forHostPath(Path.of(pgJarPath)),
                        "/opt/openl/lib/postgresql.jar")
                .waitingFor(Wait.forHttp("/admin/healthcheck/startup")
                        .forStatusCode(200)
                        .withStartupTimeout(Duration.ofMinutes(5)));
        wsContainer.start();
        LOGGER.info("WebService started. Host URL: http://localhost:{}", wsContainer.getMappedPort(WS_PORT));

        // 5. Start app container + Playwright via BaseTest
        super.beforeMethod(result);
    }

    @Override
    @AfterMethod
    public void afterMethod(ITestResult result) {
        super.afterMethod(result);
        if (wsContainer != null && wsContainer.isRunning()) {
            LOGGER.info("Stopping WebService container...");
            wsContainer.stop();
        }
        if (postgresContainer != null && postgresContainer.isRunning()) {
            LOGGER.info("Stopping PostgreSQL container...");
            postgresContainer.stop();
        }
    }

    @Test
    @TestCaseId("IPBQA-30049")
    @Description("Deploy lifecycle: deploy to production PostgreSQL, edit, redeploy, "
            + "deploy dependent projects, verify via WS REST")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEPLOY_STUDIO_PARAMS)
    public void testNewDeployPopup() {
        String nameProject = StringUtil.generateUniqueName("DeployTest");
        String deploymentName = StringUtil.generateUniqueName("Deploy");

        // =========================================================================
        // STEP 1: Login, create project from template, get initial revision
        // Legacy steps: 1
        // =========================================================================
        EditorPage editorPage = new LoginService(LocalDriverPool.getPage())
                .login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE,
                nameProject, "Example 1 - Bank Rating");

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", nameProject);

        RepositoryContentTabPropertiesComponent propsTab =
                repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        String projectInitialRevision = propsTab.getRevision();
        LOGGER.info("Step 1: Project '{}' created, initial revision: {}", nameProject, projectInitialRevision);

        // =========================================================================
        // STEP 2: Deploy project to production via DeployModal
        // Legacy steps: 11 (adapted — no Deploy Configuration, direct deploy)
        // =========================================================================
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickDeploy();
        DeployModalComponent deployModal = repositoryPage.getDeployModalComponent();
        deployModal.deployWithAllFields(null, deploymentName, "First deploy to production");
        assertThat(deployModal.isSuccessNotificationVisible())
                .as("Deploy should succeed with success notification")
                .isTrue();
        repositoryPage.closeAllMessages();
        LOGGER.info("Step 2: Project '{}' deployed to production as '{}'", nameProject, deploymentName);

        // =========================================================================
        // STEP 3: Create dependent projects from zip and deploy them
        // Legacy steps: 12 (adapted — deploy each project directly,
        // dependencies resolved automatically by backend)
        // =========================================================================
        String nameDependentProject1 = "Tutorial 3 - More Advanced Decision and Data Tables";
        String nameDependentProject2 = "Tutorial 6 - Introduction to Spreadsheet Tables";
        String zipFile1 = "Tutorial 3 - More Advanced Decision and Data Tables.zip";
        String zipFile2 = "Tutorial 6 - Introduction to Spreadsheet Tables.zip";
        String deploymentNameComplex = StringUtil.generateUniqueName("ComplexDeploy");

        // Create Tutorial 3
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE,
                nameDependentProject1, zipFile1);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", nameDependentProject1);

        // Deploy Tutorial 3 — backend auto-resolves dependency on Tutorial 6
        // (Tutorial 6 doesn't exist yet, so we create Tutorial 6 first)
        // Actually, create both projects first, then deploy
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE,
                nameDependentProject2, zipFile2);

        // Deploy Tutorial 3 (has dependency on Tutorial 6)
        repositoryPage.getLeftRepositoryTreeComponent()
                .selectItemInFolder("Projects", nameDependentProject1);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickDeploy();
        deployModal = repositoryPage.getDeployModalComponent();
        deployModal.deployWithAllFields(null, deploymentNameComplex, "Deploy dependent project");
        assertThat(deployModal.isSuccessNotificationVisible())
                .as("Deploy of dependent project should succeed")
                .isTrue();
        repositoryPage.closeAllMessages();
        LOGGER.info("Step 3: Dependent projects deployed as '{}'", deploymentNameComplex);

        // =========================================================================
        // STEP 4: Edit project table, save — new revision
        // Legacy steps: 13
        // =========================================================================
        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(nameProject, "Bank Rating");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "CapitalDynamicScore");

        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(6, 2, "1000");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();
        WaitUtil.sleep(1000, "Wait for table save");

        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", nameProject);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickSaveBtn();
        repositoryPage.getSaveChangesComponent().getSaveBtn().click();
        WaitUtil.sleep(1000, "Wait for project save");

        propsTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        String projectUpdatedRevision = propsTab.getRevision();
        assertThat(projectUpdatedRevision)
                .as("Revision should change after edit")
                .isNotEqualTo(projectInitialRevision);
        LOGGER.info("Step 4: Project edited, new revision: {}", projectUpdatedRevision);

        // =========================================================================
        // STEP 5: Redeploy project with updated revision
        // Legacy steps: 14 (adapted — just deploy again, same deployment name)
        // =========================================================================
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickDeploy();
        deployModal = repositoryPage.getDeployModalComponent();
        deployModal.deployWithAllFields(null, deploymentName, "Redeploy with updated revision");
        assertThat(deployModal.isSuccessNotificationVisible())
                .as("Redeploy should succeed")
                .isTrue();
        repositoryPage.closeAllMessages();
        LOGGER.info("Step 5: Project redeployed with updated revision");

        // =========================================================================
        // STEP 6: Edit again, resolve conflict if it occurs, deploy
        // Legacy steps: 15 (conflict arose from DC save changing repo state;
        // in new flow conflict may not occur — we handle both cases)
        // =========================================================================
        editorPage = repositoryPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent()
                .selectModule(nameProject, "Bank Rating");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "CapitalDynamicScore");

        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(6, 2, "2000");
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();
        WaitUtil.sleep(1000, "Wait for table save");

        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", nameProject);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickSaveBtn();
        repositoryPage.getSaveChangesComponent().getSaveBtn().click();
        WaitUtil.sleep(1000, "Wait for save");

        // Resolve conflict if it appears (legacy behavior from DC save)
        if (repositoryPage.getResolveConflictsDialogComponent().isDialogVisible()) {
            repositoryPage.getResolveConflictsDialogComponent().resolveConflictUseYours();
            LOGGER.info("Step 6: Conflict resolved using 'Use Yours'");
        } else {
            LOGGER.info("Step 6: No conflict occurred (expected in new deploy flow)");
        }

        propsTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        String projectSecondUpdatedRevision = propsTab.getRevision();
        assertThat(projectSecondUpdatedRevision)
                .as("Revision should change after second edit")
                .isNotEqualTo(projectUpdatedRevision);
        LOGGER.info("Step 6: Second edit done, revision: {}", projectSecondUpdatedRevision);

        // Deploy after edit
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickDeploy();
        deployModal = repositoryPage.getDeployModalComponent();
        deployModal.deployWithAllFields(null, deploymentName, "Deploy after second edit");
        assertThat(deployModal.isSuccessNotificationVisible())
                .as("Deploy after second edit should succeed")
                .isTrue();
        repositoryPage.closeAllMessages();
        LOGGER.info("Step 6: Deployed after second edit");

        // =========================================================================
        // STEP 7: Create and deploy another project (Tutorial 2)
        // Legacy steps: 16 (adapted — deploy directly, not via DC)
        // =========================================================================
        String nameProjectTutorial2 = "Tutorial 2 - Introduction to Data Tables";
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE,
                nameProjectTutorial2, nameProjectTutorial2);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", nameProjectTutorial2);

        repositoryPage.getRepositoryContentButtonsPanelComponent().clickDeploy();
        deployModal = repositoryPage.getDeployModalComponent();
        deployModal.deployWithAllFields(null, nameProjectTutorial2, "Deploy Tutorial 2");
        assertThat(deployModal.isSuccessNotificationVisible())
                .as("Deploy of Tutorial 2 should succeed")
                .isTrue();
        repositoryPage.closeAllMessages();
        LOGGER.info("Step 7: Tutorial 2 deployed");

        // =========================================================================
        // STEP 8: Verify deployed services visible in WebService
        // Legacy steps: 18
        // =========================================================================
        verifyDeployedServicesVisible(nameProject, nameDependentProject1,
                nameDependentProject2, nameProjectTutorial2);
        LOGGER.info("Step 8: WebService verification completed — all steps done");
    }

    private void verifyDeployedServicesVisible(String... expectedProjects) {
        String serviceListUrl = String.format("http://localhost:%d/admin/services", wsContainer.getMappedPort(WS_PORT));
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

        String body = "";
        for (int attempt = 1; attempt <= 15; attempt++) {
            try {
                HttpResponse<String> response = httpClient.send(
                        HttpRequest.newBuilder().uri(URI.create(serviceListUrl)).GET().build(),
                        HttpResponse.BodyHandlers.ofString());
                body = response.body();
                if (response.statusCode() == 200
                        && java.util.Arrays.stream(expectedProjects).allMatch(body::contains)) {
                    LOGGER.info("All {} services found in WS (attempt {})", expectedProjects.length, attempt);
                    break;
                }
            } catch (Exception e) {
                LOGGER.info("WS not ready (attempt {}/15): {}", attempt, e.getMessage());
            }
            WaitUtil.sleep(3000, "Waiting for WS to pick up deployed rules");
        }

        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String pretty = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(mapper.readTree(body));
            LOGGER.info("WS service list:\n{}", pretty);
        } catch (Exception e) {
            LOGGER.info("WS service list (raw): {}", body);
        }
        String serviceList = body;
        for (String project : expectedProjects) {
            assertThat(serviceList).as("Should contain '%s'", project).contains(project);
        }
    }
}
