package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.components.repositorytabcomponents.DeployModalComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.DeployInfrastructureService;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.StringUtil;
import helpers.utils.WaitUtil;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.HashMap;
import java.util.List;
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
    private static final Map<String, String> additionalContainerFiles = new HashMap<>();

    private DeployInfrastructureService deployInfra;

    @Override
    @BeforeMethod
    public void beforeMethod(ITestResult result) {
        additionalContainerFiles.clear();
        deployInfra = DeployInfrastructureService.builder()
                .withPostgres()
                .withWsContainer()
                .build();
        deployInfra.start();
        additionalContainerFiles.putAll(deployInfra.getFilesToCopy());
        super.beforeMethod(result);
    }

    @Override
    @AfterMethod
    public void afterMethod(ITestResult result) {
        super.afterMethod(result);
        deployInfra.cleanup();
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

        String projectInitialRevision = repositoryPage.openProjectDetail(nameProject).getLatestRevisionId();
        repositoryPage.openProjectsList();
        LOGGER.info("Step 1: Project '{}' created, initial revision: {}", nameProject, projectInitialRevision);

        // STEP 2: Deploy project to production via the React DeployModal (project-row Deploy action)
        DeployModalComponent deployModal = repositoryPage.clickDeploy(nameProject);
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

        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE,
                nameDependentProject1, zipFile1);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE,
                nameDependentProject2, zipFile2);

        // Deploy Tutorial 3 (backend auto-resolves the dependency on Tutorial 6)
        deployModal = repositoryPage.clickDeploy(nameDependentProject1);
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
        editProjectCell(editorPage, nameProject, "1000");

        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.saveProject(nameProject, "Edit CapitalDynamicScore to 1000");

        String projectUpdatedRevision = repositoryPage.openProjectDetail(nameProject).getLatestRevisionId();
        repositoryPage.openProjectsList();
        assertThat(projectUpdatedRevision)
                .as("Revision should change after edit")
                .isNotEqualTo(projectInitialRevision);
        LOGGER.info("Step 4: Project edited, new revision: {}", projectUpdatedRevision);

        // STEP 5: Redeploy with the updated revision
        deployModal = repositoryPage.clickDeploy(nameProject);
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
        editProjectCell(editorPage, nameProject, "2000");

        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.saveProject(nameProject, "Second edit to 2000");

        // Resolve conflict if it appears (deploy targets the production repo, so usually none occurs)
        if (repositoryPage.getResolveConflictsDialogComponent().isDialogVisible()) {
            repositoryPage.getResolveConflictsDialogComponent().resolveConflictUseYours();
            LOGGER.info("Step 6: Conflict resolved using 'Use Yours'");
        } else {
            LOGGER.info("Step 6: No conflict occurred (expected in new deploy flow)");
        }

        String projectSecondUpdatedRevision = repositoryPage.openProjectDetail(nameProject).getLatestRevisionId();
        repositoryPage.openProjectsList();
        assertThat(projectSecondUpdatedRevision)
                .as("Revision should change after second edit")
                .isNotEqualTo(projectUpdatedRevision);
        LOGGER.info("Step 6: Second edit done, revision: {}", projectSecondUpdatedRevision);

        // Deploy after edit
        deployModal = repositoryPage.clickDeploy(nameProject);
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

        deployModal = repositoryPage.clickDeploy(nameProjectTutorial2);
        deployModal.deployWithAllFields(null, nameProjectTutorial2, "Deploy Tutorial 2");
        assertThat(deployModal.isSuccessNotificationVisible())
                .as("Deploy of Tutorial 2 should succeed")
                .isTrue();
        repositoryPage.closeAllMessages();
        LOGGER.info("Step 7: Tutorial 2 deployed");

        // =========================================================================
        // STEP 8: Verify deployed services visible in WebService via browser
        // Legacy steps: 18
        // =========================================================================
        boolean isDockerMode = LocalDriverPool.getCurrentExecutionMode() == LocalDriverPool.ExecutionMode.PLAYWRIGHT_DOCKER;
        String wsBaseUrl = isDockerMode
                ? "http://wscontainer:" + WS_PORT
                : "http://localhost:" + deployInfra.getWsContainer().getMappedPort(WS_PORT);
        List<String> expectedProjects = List.of(nameProject, nameDependentProject1, nameDependentProject2, nameProjectTutorial2);
        final long wsServicesTimeoutMs = 30000;
        final long wsPollingIntervalMs = 3000;

        boolean allServicesAppeared = WaitUtil.waitForCondition(
                () -> {
                    try {
                        LocalDriverPool.getPage().navigate(wsBaseUrl);
                        // Block until at least one service row (h3) appears in the DOM;
                        // throws TimeoutError (caught below) if none within the default timeout
                        LocalDriverPool.getPage().waitForSelector("xpath=//h3");
                        String pageContent = LocalDriverPool.getPage().content();
                        List<String> missingProjects = expectedProjects.stream()
                                .filter(project -> !pageContent.contains(project))
                                .toList();
                        if (!missingProjects.isEmpty()) {
                            LOGGER.info("WS services not ready yet. Missing projects: {}", missingProjects);
                            return false;
                        }
                        return true;
                    } catch (Exception e) {
                        LOGGER.warn("Transient error polling WS admin page, will retry: {}", e.getMessage());
                        return false;
                    }
                },
                wsServicesTimeoutMs, wsPollingIntervalMs, "Waiting for all services to appear in WS");
        assertThat(allServicesAppeared)
                .as("All expected WS services should appear within %sms", wsServicesTimeoutMs)
                .isTrue();

        String finalPageContent = LocalDriverPool.getPage().content();
        for (String project : expectedProjects) {
            assertThat(finalPageContent)
                    .as("WS admin UI should show service for project '%s'", project)
                    .contains(project);
        }
        LOGGER.info("Step 8: WebService verification completed — all services found in WS admin UI");
    }

    private void editProjectCell(EditorPage editorPage, String projectName, String value) {
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(projectName, "Bank Rating");
        editorPage.getEditorLeftRulesTreeComponent()
                .setViewFilter(EditorLeftRulesTreeComponent.FilterOptions.BY_TYPE)
                .expandFolderInTree("Decision")
                .selectItemInFolder("Decision", "CapitalDynamicScore");
        editorPage.getEditorToolbarPanelComponent().getEditTableBtn().click();
        editorPage.getCenterTable().editCell(6, 2, value);
        editorPage.getEditorTableActionsPanelComponent().clickSaveChanges();
        WaitUtil.sleep(1000, "Wait for table save");
    }
}
