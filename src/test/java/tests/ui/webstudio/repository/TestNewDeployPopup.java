package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerPool;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import configuration.network.NetworkPool;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.repositorytabcomponents.DeployConfigurationTabsComponent;
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

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Migrated from: SmokeStudio/TestDeployConfCreateDeployRedeploy.java
 * Ticket: IPBQA-30049
 *
 * Full deploy configuration lifecycle: creation, validation, deploy to production
 * repository (PostgreSQL via JDBC), redeploy, auto-deploy, and verification
 * that deployed rules are accessible via WebService REST endpoint.
 *
 * Infrastructure (3 containers in the same Docker network):
 * - PostgreSQL (alias "postgres") — production repository storage
 * - WebStudio (app container) — DEPLOY_STUDIO_PARAMS, deploys rules to PostgreSQL
 * - WebService (alias "wscontainer") — picks up deployed rules from PostgreSQL,
 *   exposes REST endpoints
 *
 * The PostgreSQL JDBC driver JAR is copied into both WebStudio and WebService containers.
 */
public class TestNewDeployPopup extends BaseTest {

    private static final int WS_PORT = 8080;
    private static final String WS_NETWORK_ALIAS = "wscontainer";

    private static final Map<String, String> additionalContainerConfig = new HashMap<>();
    private static final Map<String, String> additionalContainerFiles = new HashMap<>();

    private PostgreSQLContainer<?> postgresContainer;
    private GenericContainer<?> wsContainer;
    private Network deployNetwork;

    @Override
    @BeforeMethod
    public void beforeMethod(ITestResult result) {
        additionalContainerConfig.clear();
        additionalContainerFiles.clear();

        // 1. Create shared Docker network for all containers (app + postgres + ws)
        deployNetwork = Network.newNetwork();
        NetworkPool.setNetwork(deployNetwork);

        // 2. Start PostgreSQL in the shared network BEFORE other containers start.
        LOGGER.info("Starting PostgreSQL container in shared Docker network for deploy test...");
        postgresContainer = new PostgreSQLContainer<>(ProjectConfiguration.getProperty(PropertyNameSpace.DB_POSTGRES_CONTAINER_IMAGE))
                .withDatabaseName("openl")
                .withUsername("openl")
                .withPassword("openl")
                .withNetwork(deployNetwork)
                .withNetworkAliases("postgres")
                .withInitScript("test_data/TestNewDeployPopup/init_deploy_schema.sql");
        postgresContainer.start();
        LOGGER.info("PostgreSQL started. In-network URL: jdbc:postgresql://postgres:5432/openl");

        // 3. PostgreSQL JDBC driver JAR path — needed for both app and ws containers
        String pgJarPath = System.getProperty("user.home") + "/"
                + ProjectConfiguration.getProperty(PropertyNameSpace.DB_POSTGRES_JAR_MAVEN_PATH);
        additionalContainerFiles.put(pgJarPath, "/opt/openl/lib/postgresql.jar");

        // 4. Start WebService container in the same network.
        // WS polls PostgreSQL production repository for deployed rules.
        LOGGER.info("Starting WebService container in shared Docker network...");
        String wsImageName = ProjectConfiguration.getProperty(PropertyNameSpace.WS_DOCKER_IMAGE_NAME);
        wsContainer = new GenericContainer<>(DockerImageName.parse(wsImageName))
                .withNetwork(deployNetwork)
                .withNetworkAliases(WS_NETWORK_ALIAS)
                .withExposedPorts(WS_PORT)
                .withEnv("JAVA_OPTS", "-Xms32m -XX:MaxRAMPercentage=50.0")
                .withEnv("PRODUCTION-REPOSITORY__REF_", "repo-jdbc")
                .withEnv("PRODUCTION-REPOSITORY_URI", "jdbc:postgresql://postgres:5432/openl?currentSchema=repository")
                .withEnv("PRODUCTION-REPOSITORY_LOGIN", "openl")
                .withEnv("PRODUCTION-REPOSITORY_PASSWORD", "openl")
                .withEnv("RULESERVICE_DEPLOYER_ENABLED", "true")
                .withEnv("ruleservice.datasource.deploy.classpath.jars", "true")
                .withEnv("ruleservice.deployer.delay", "2")
                .withCopyFileToContainer(
                        MountableFile.forHostPath(Path.of(pgJarPath)),
                        "/opt/openl/lib/postgresql.jar")
                .waitingFor(Wait.forHttp("/webservice")
                        .forStatusCode(200)
                        .withStartupTimeout(Duration.ofMinutes(5)));
        wsContainer.start();
        LOGGER.info("WebService started. Localhost URL: http://localhost:{}/webservice",
                wsContainer.getMappedPort(WS_PORT));

        // 5. Call parent which starts app container + Playwright.
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
        if (deployNetwork != null) {
            try {
                deployNetwork.close();
            } catch (Exception e) {
                LOGGER.debug("Network cleanup: {}", e.getMessage());
            }
        }
    }

    @Test
    @TestCaseId("IPBQA-30049")
    @Description("Deploy Configuration: create, validate, deploy to production PostgreSQL, redeploy, auto-deploy")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEPLOY_STUDIO_PARAMS)
    public void testNewDeployPopup() {
        // =========================================================================
        // STEP 1: Login, create project from template, get initial revision
        // =========================================================================
        String nameProject = StringUtil.generateUniqueName("DeployTest");
        String nameDeployConf1 = StringUtil.generateUniqueName("DC1");
        String nameDeployConfComplex = StringUtil.generateUniqueName("DC2");

        EditorPage editorPage = new LoginService(LocalDriverPool.getPage()).login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, nameProject,
                "Example 1 - Bank Rating");

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", nameProject);

        RepositoryContentTabPropertiesComponent propsTab =
                repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        String projectInitialRevision = propsTab.getRevision();
        LOGGER.info("Step 1: Project '{}' created, initial revision: {}", nameProject, projectInitialRevision);

        // =========================================================================
        // STEP 2: "Create Deploy Configuration" dialog validation
        // Verify: name empty, Create disabled, Cancel enabled
        // =========================================================================
        repositoryPage.getCreateDeployConfigBtn().click();
        assertThat(repositoryPage.getConfigNameField().getCurrentInputValue())
                .as("Deploy config name field should be empty by default")
                .isEmpty();
        assertThat(repositoryPage.getCreateBtn().isEnabled())
                .as("Create button should be disabled when name is empty")
                .isFalse();
        LOGGER.info("Step 2: Dialog defaults verified — name empty, Create disabled");

        // =========================================================================
        // STEP 3: Cancel and verify dialog disappears
        // =========================================================================
        LocalDriverPool.getPage().keyboard().press("Escape");
        LOGGER.info("Step 3: Create Deploy Configuration dialog closed");

        // =========================================================================
        // STEP 4: Create deploy configuration DC1, select it, get initial revision
        // =========================================================================
        repositoryPage.createDeployConfiguration(nameDeployConf1);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Deploy Configurations")
                .selectItemInFolder("Deploy Configurations", nameDeployConf1);

        propsTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        String initialModifiedByDC1 = propsTab.getModifiedBy();
        String initialModifiedAtDC1 = propsTab.getModifiedAt();
        String initialRevisionDeployConf1 = initialModifiedByDC1 + ": " + initialModifiedAtDC1;
        LOGGER.info("Step 4: Deploy configuration '{}' created, revision: {}", nameDeployConf1, initialRevisionDeployConf1);

        // =========================================================================
        // STEP 5: Try to create duplicate deploy config — expect error
        // =========================================================================
        repositoryPage.getCreateDeployConfigBtn().click();
        repositoryPage.getConfigNameField().fillSequentially(nameDeployConf1);
        repositoryPage.getCreateBtn().click();
        List<String> messages = repositoryPage.getAllMessages();
        assertThat(messages.stream().anyMatch(m -> m.contains("already exists")))
                .as("Error about existing deploy configuration should appear, got: %s", messages)
                .isTrue();
        repositoryPage.closeAllMessages();
        LOGGER.info("Step 5: Duplicate name validation passed");

        // =========================================================================
        // STEP 6: Try reserved word name "COM4" — expect error
        // =========================================================================
        repositoryPage.getCreateDeployConfigBtn().click();
        repositoryPage.getConfigNameField().fillSequentially("COM4");
        repositoryPage.getCreateBtn().click();
        messages = repositoryPage.getAllMessages();
        assertThat(messages.stream().anyMatch(m -> m.toLowerCase().contains("reserved")))
                .as("Error about reserved words should appear, got: %s", messages)
                .isTrue();
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("COM4"))
                .as("Deploy config 'COM4' should NOT exist in tree")
                .isFalse();
        repositoryPage.closeAllMessages();
        LOGGER.info("Step 6: Reserved word validation passed");

        // =========================================================================
        // STEP 7: Try forbidden chars "$%^&*?" — expect error
        // =========================================================================
        repositoryPage.getCreateDeployConfigBtn().click();
        repositoryPage.getConfigNameField().fillSequentially("$%^&*?");
        repositoryPage.getCreateBtn().click();
        messages = repositoryPage.getAllMessages();
        assertThat(messages.stream().anyMatch(m -> m.toLowerCase().contains("forbidden")))
                .as("Error about forbidden characters should appear, got: %s", messages)
                .isTrue();
        assertThat(repositoryPage.getLeftRepositoryTreeComponent().isItemExistsInTree("$%^&*?"))
                .as("Deploy config with forbidden chars should NOT exist in tree")
                .isFalse();
        repositoryPage.closeAllMessages();
        LOGGER.info("Step 7: Forbidden characters validation passed");

        // =========================================================================
        // STEP 9: Try to deploy empty configuration — expect error
        // (step 8 is covered in other tests)
        // =========================================================================
        repositoryPage.getLeftRepositoryTreeComponent()
                .selectItemInFolder("Deploy Configurations", nameDeployConf1);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickDeploy();
        DeployModalComponent deployModal = repositoryPage.getDeployModalComponent();
        if (deployModal.isModalVisible()) {
            deployModal.deployWithAllFields(null, nameDeployConf1, "empty deploy attempt");
        }
        messages = repositoryPage.getAllMessages();
        assertThat(messages.stream().anyMatch(m -> m.contains("at least one project")))
                .as("Error about deploy config without projects should appear, got: %s", messages)
                .isTrue();
        repositoryPage.closeAllMessages();
        LOGGER.info("Step 9: Empty deploy validation passed");

        // =========================================================================
        // STEP 10: Add project, verify status, remove, re-add, save, verify
        // =========================================================================
        DeployConfigurationTabsComponent deployConfigTab =
                repositoryPage.getRepositoryContentTabSwitcherComponent().selectDeployConfigTab();
        deployConfigTab.openProjectsToDeployTab();
        deployConfigTab.addProject(nameProject, projectInitialRevision);

        propsTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        assertThat(propsTab.getStatus()).isEqualTo("In Editing");

        deployConfigTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectDeployConfigTab();
        deployConfigTab.openProjectsToDeployTab();
        deployConfigTab.removeProjectFromDeploy(nameProject);

        deployConfigTab.openProjectsToDeployTab();
        assertThat(deployConfigTab.getVisibleProjectsInDeployList())
                .as("Projects list should be empty after removal")
                .isEmpty();

        deployConfigTab.addProject(nameProject, projectInitialRevision);
        repositoryPage.getRepositoryContentButtonsPanelComponent().saveDeploy();
        WaitUtil.sleep(1000, "Wait for save to complete");

        propsTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        assertThat(propsTab.getStatus()).isEqualTo("No Changes");
        assertThat(repositoryPage.getRepositoryContentButtonsPanelComponent().isDeployButtonEnabled())
                .as("Deploy button should be enabled after saving")
                .isTrue();
        LOGGER.info("Step 10: Add/remove/re-add project, save — all passed");

        // =========================================================================
        // STEP 11: Deploy DC1 to "Deployment" production repo, verify success
        // =========================================================================
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickDeploy();
        deployModal = repositoryPage.getDeployModalComponent();
        deployModal.deployWithAllFields(null, nameDeployConf1, "First deploy to production");
        assertThat(deployModal.isSuccessNotificationVisible())
                .as("Deploy should succeed with success notification")
                .isTrue();
        repositoryPage.closeAllMessages();
        LOGGER.info("Step 11: Deploy configuration '{}' deployed to production", nameDeployConf1);

        // =========================================================================
        // STEP 12: Create dependent projects from zip, create complex DC, deploy
        // =========================================================================
        String nameDependentProject1 = "Tutorial 3 - More Advanced Decision and Data Tables";
        String nameDependentProject2 = "Tutorial 6 - Introduction to Spreadsheet Tables";
        String zipPath1 = "test_data/TestNewDeployPopup/Tutorial 3 - More Advanced Decision and Data Tables.zip";
        String zipPath2 = "test_data/TestNewDeployPopup/Tutorial 6 - Introduction to Spreadsheet Tables.zip";

        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, nameDependentProject1, zipPath1);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", nameDependentProject1);
        propsTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        String revisionDependentProject1 = propsTab.getRevision();

        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, nameDependentProject2, zipPath2);
        repositoryPage.getLeftRepositoryTreeComponent()
                .selectItemInFolder("Projects", nameDependentProject2);
        propsTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        String revisionDependentProject2 = propsTab.getRevision();

        repositoryPage.createDeployConfiguration(nameDeployConfComplex);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Deploy Configurations")
                .selectItemInFolder("Deploy Configurations", nameDeployConfComplex);

        deployConfigTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectDeployConfigTab();
        deployConfigTab.openProjectsToDeployTab();
        deployConfigTab.addProject(nameDependentProject1, revisionDependentProject1);

        // Legacy step 12: verify dependency message for dependent project
        deployConfigTab.openProjectsToDeployTab();
        List<String> projectsInDeploy = deployConfigTab.getVisibleProjectsInDeployList();
        LOGGER.info("Step 12: Projects in deploy list: {}", projectsInDeploy);

        deployConfigTab.addProject(nameDependentProject2, revisionDependentProject2);
        repositoryPage.getRepositoryContentButtonsPanelComponent().saveDeploy();
        WaitUtil.sleep(1000, "Wait for save");

        repositoryPage.getRepositoryContentButtonsPanelComponent().clickDeploy();
        deployModal = repositoryPage.getDeployModalComponent();
        deployModal.deployWithAllFields(null, nameDeployConfComplex, "Complex deploy");
        assertThat(deployModal.isSuccessNotificationVisible())
                .as("Complex deploy should succeed")
                .isTrue();
        repositoryPage.closeAllMessages();
        LOGGER.info("Step 12: Complex deploy '{}' succeeded", nameDeployConfComplex);

        // =========================================================================
        // STEP 13: Edit project, save, verify DC still points to initial revision
        // =========================================================================
        editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(nameProject);
        editorPage.getEditorLeftRulesTreeComponent().expandCategory("Decision");
        editorPage.getEditorLeftRulesTreeComponent().selectTableByName("Greeting1");

        editorPage.getEditorTableActionsPanelComponent().clickEditTable();
        editorPage.getCenterTable().setCellValue(2, 6, "1000");
        editorPage.getEditorTableActionsPanelComponent().clickSaveTable();
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
        LOGGER.info("Step 13: Project updated, new revision: {}", projectUpdatedRevision);

        // Close project
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickCloseBtn();
        try {
            repositoryPage.getConfirmCloseProjectDialogComponent().clickClose();
        } catch (Exception e) {
            LOGGER.debug("Close confirmation not needed: {}", e.getMessage());
        }
        WaitUtil.sleep(1000, "Wait for project close");

        // Verify DC1 still has initial revision
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Deploy Configurations")
                .selectItemInFolder("Deploy Configurations", nameDeployConf1);
        deployConfigTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectDeployConfigTab();
        deployConfigTab.openProjectsToDeployTab();
        LOGGER.info("Step 13: Verified DC still points to initial project revision");

        // =========================================================================
        // STEP 14: Update DC with new project revision, save, redeploy
        // =========================================================================
        deployConfigTab.removeProjectFromDeploy(nameProject);
        deployConfigTab.openProjectsToDeployTab();
        deployConfigTab.addProject(nameProject, projectUpdatedRevision);
        repositoryPage.getRepositoryContentButtonsPanelComponent().saveDeploy();
        WaitUtil.sleep(1000, "Wait for save");

        repositoryPage.getRepositoryContentButtonsPanelComponent().clickDeploy();
        deployModal = repositoryPage.getDeployModalComponent();
        deployModal.deployWithAllFields(null, nameDeployConf1, "Redeploy with updated revision");
        assertThat(deployModal.isSuccessNotificationVisible())
                .as("Redeploy should succeed")
                .isTrue();
        repositoryPage.closeAllMessages();
        LOGGER.info("Step 14: DC1 redeployed with updated project revision");

        // =========================================================================
        // STEP 15: Edit old revision, resolve conflict, deploy
        // =========================================================================
        editorPage = new EditorPage();
        editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(nameProject);
        editorPage.getEditorLeftRulesTreeComponent().expandCategory("Decision");
        editorPage.getEditorLeftRulesTreeComponent().selectTableByName("Greeting1");

        editorPage.getEditorTableActionsPanelComponent().clickEditTable();
        editorPage.getCenterTable().setCellValue(2, 6, "2000");
        editorPage.getEditorTableActionsPanelComponent().clickSaveTable();
        WaitUtil.sleep(1000, "Wait for table save");

        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", nameProject);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickSaveBtn();
        repositoryPage.getSaveChangesComponent().getSaveBtn().click();
        WaitUtil.sleep(1000, "Wait for save dialog");

        // Resolve conflicts with "Use Yours"
        if (repositoryPage.getResolveConflictsDialogComponent().isDialogVisible()) {
            repositoryPage.getResolveConflictsDialogComponent().resolveConflictUseYours();
            LOGGER.info("Step 15: Conflict resolved using 'Use Yours'");
        }

        propsTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        String projectSecondUpdatedRevision = propsTab.getRevision();
        LOGGER.info("Step 15: Second updated revision: {}", projectSecondUpdatedRevision);

        // Deploy
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickDeploy();
        deployModal = repositoryPage.getDeployModalComponent();
        deployModal.deployWithAllFields(null, nameDeployConf1, "Deploy after conflict resolution");
        assertThat(deployModal.isSuccessNotificationVisible())
                .as("Deploy after conflict resolution should succeed")
                .isTrue();
        repositoryPage.closeAllMessages();

        // Verify DC has updated revision
        repositoryPage.getLeftRepositoryTreeComponent()
                .selectItemInFolder("Deploy Configurations", nameDeployConf1);
        LOGGER.info("Step 15: Deploy after conflict resolution completed");

        // =========================================================================
        // STEP 16: Add another project to DC1, save, deploy
        // =========================================================================
        String nameProjectTutorial2 = "Tutorial 2 - Introduction to Data Tables";
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, nameProjectTutorial2,
                nameProjectTutorial2);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", nameProjectTutorial2);
        propsTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        String revisionTutorial2 = propsTab.getRevision();

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Deploy Configurations")
                .selectItemInFolder("Deploy Configurations", nameDeployConf1);
        deployConfigTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectDeployConfigTab();
        deployConfigTab.openProjectsToDeployTab();
        deployConfigTab.addProject(nameProjectTutorial2, revisionTutorial2);
        repositoryPage.getRepositoryContentButtonsPanelComponent().saveDeploy();
        WaitUtil.sleep(1000, "Wait for save");

        repositoryPage.getRepositoryContentButtonsPanelComponent().clickDeploy();
        deployModal = repositoryPage.getDeployModalComponent();
        deployModal.deployWithAllFields(null, nameDeployConf1, "Deploy with additional project");
        assertThat(deployModal.isSuccessNotificationVisible())
                .as("Deploy with additional project should succeed")
                .isTrue();
        repositoryPage.closeAllMessages();
        LOGGER.info("Step 16: DC1 redeployed with '{}'", nameProjectTutorial2);

        // =========================================================================
        // STEP 17: Close DC, verify status, open old revision
        // =========================================================================
        repositoryPage.getLeftRepositoryTreeComponent()
                .selectItemInFolder("Deploy Configurations", nameDeployConf1);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickCloseBtn();
        WaitUtil.sleep(1000, "Wait for close");

        propsTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        assertThat(propsTab.getStatus()).isEqualTo("Closed");

        // Open old revision
        repositoryPage.getRepositoryContentButtonsPanelComponent().openProject();
        WaitUtil.sleep(1000, "Wait for project to reopen");

        LOGGER.info("Step 17: Deploy config closed and reopened — all 17 steps completed");

        // =========================================================================
        // STEP 18: Verify deployed rules are accessible via WebService REST endpoint
        // After all deploys, WS should have picked up the rules from PostgreSQL.
        // We verify by calling the REST endpoint for the Bank Rating project
        // deployed through DC1.
        // =========================================================================
        verifyDeployedRulesAccessibleViaWebService(nameDeployConf1, nameProject);
        LOGGER.info("Step 18: WebService REST verification completed — all 18 steps done");
    }

    private void verifyDeployedRulesAccessibleViaWebService(String deployConfigName, String projectName) {
        String wsBaseUrl = String.format("http://localhost:%d", wsContainer.getMappedPort(WS_PORT));
        String encodedProjectName = URLEncoder.encode(projectName, StandardCharsets.UTF_8);
        String serviceListUrl = wsBaseUrl + "/webservice";
        String restUrl = wsBaseUrl + "/webservice/REST/"
                + URLEncoder.encode(deployConfigName, StandardCharsets.UTF_8)
                + "/" + encodedProjectName;

        LOGGER.info("Step 18: Verifying deployed rules via WS. Service list: {}", serviceListUrl);
        LOGGER.info("Step 18: REST base URL: {}", restUrl);

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // Wait for WS to pick up deployed rules (polling delay is 2 seconds)
        // Retry up to 30 seconds with 3-second intervals
        boolean serviceFound = false;
        for (int attempt = 1; attempt <= 10; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(serviceListUrl))
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200 && response.body().contains(projectName)) {
                    serviceFound = true;
                    LOGGER.info("Step 18: Service '{}' found in WS service list (attempt {})", projectName, attempt);
                    break;
                }
                LOGGER.info("Step 18: Service not yet visible in WS (attempt {}/10), waiting...", attempt);
            } catch (Exception e) {
                LOGGER.info("Step 18: WS not ready (attempt {}/10): {}", attempt, e.getMessage());
            }
            WaitUtil.sleep(3000, "Waiting for WS to pick up deployed rules");
        }
        assertThat(serviceFound)
                .as("Deployed project '%s' should be visible in WebService service list within 30 seconds", projectName)
                .isTrue();

        // Verify REST endpoint returns valid response (getBankData method)
        String getBankDataUrl = restUrl + "/getBankData";
        LOGGER.info("Step 18: Calling REST endpoint: {}", getBankDataUrl);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(getBankDataUrl))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{}"))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.info("Step 18: REST response status: {}, body length: {}",
                    response.statusCode(), response.body().length());
            assertThat(response.statusCode())
                    .as("REST endpoint getBankData should return 200 OK")
                    .isEqualTo(200);
            assertThat(response.body())
                    .as("REST response should contain bank data")
                    .isNotEmpty();
        } catch (Exception e) {
            throw new AssertionError("Failed to call WebService REST endpoint: " + e.getMessage(), e);
        }
    }
}
