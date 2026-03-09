package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.api.GetWsServicesMethod;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.leftmenu.EditorLeftRulesTreeComponent;
import domain.ui.webstudio.components.repositorytabcomponents.DeployModalComponent;
import domain.ui.webstudio.components.repositorytabcomponents.RepositoryContentTabPropertiesComponent;
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
        GetWsServicesMethod wsApi = new GetWsServicesMethod(deployInfra.getWsContainer(), WS_PORT);
        List<String> expectedProjects = List.of(nameProject, nameDependentProject1, nameDependentProject2, nameProjectTutorial2);

        WaitUtil.waitForCondition(
                () -> {
                    List<String> services = wsApi.getServiceNames();
                    return expectedProjects.stream().allMatch(
                            project -> services.stream().anyMatch(s -> s.endsWith("_" + project)));
                },
                45000, 3000, "Waiting for all services to appear in WS");

        List<String> actual = wsApi.getServiceNames();
        LOGGER.info("WS services: {}", actual);
        for (String project : expectedProjects) {
            assertThat(actual).as("WS should contain service for project '%s'", project)
                    .anyMatch(s -> s.endsWith("_" + project));
        }
        LOGGER.info("Step 8: WebService verification completed — all steps done");
    }
}
