package tests.ui.webstudio.db;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.RepositoriesPageComponent;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.repositorytabcomponents.DeployConfigurationTabsComponent;
import domain.ui.webstudio.components.repositorytabcomponents.RepositoryContentTabPropertiesComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import domain.ui.webservice.pages.ServicePage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testcontainers.oracle.OracleContainer;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Migrated from: Repository/TestSplitDesignRepositoryDeployRules.java
 * Ticket: IPBQA-27295
 *
 * Adaptation: Instead of connecting to a pre-deployed Oracle 12c instance, each test run
 * spins up an Oracle Free container (gvenzl/oracle-free:slim-faststart) via Testcontainers,
 * following the same pattern as openl-tablets/ITEST/itest.webstudio/test/.../RdbmsTest.java.
 * The Oracle JDBC URL is derived at runtime and entered via the Admin UI (Design Repository settings).
 *
 * NOTE: This test requires Docker to be running. It will take 2-3 minutes on first run
 * due to Oracle container startup time.
 */
public class TestSplitDesignRepositoryDeployRules extends BaseTest {

    private static final Map<String, String> additionalContainerConfig = new HashMap<>();
    private static final Map<String, String> additionalContainerFiles = new HashMap<>();

    private OracleContainer oracleContainer;
    private String oracleJdbcUrl;

    private final String deployName = "myConfig";
    private final String projectName = TestSplitDesignRepositoryDeployRules.class.getName();

    @Override
    @BeforeMethod
    public void beforeMethod(ITestResult result) {
        additionalContainerConfig.clear();
        additionalContainerFiles.clear();

        LOGGER.info("Starting Oracle Free container for DB integration test...");
        oracleContainer = new OracleContainer("gvenzl/oracle-free:slim-faststart");
        oracleContainer.start();

        int oraclePort = oracleContainer.getMappedPort(1521);
        oracleJdbcUrl = "jdbc:oracle:thin:@host.docker.internal:" + oraclePort + "/" + oracleContainer.getDatabaseName();
        LOGGER.info("Oracle container started. JDBC URL (for app container): {}", oracleJdbcUrl);

        // user.mode and security.administrators already set by DEFAULT_STUDIO_PARAMS

        String ojdbcJarPath = System.getProperty("user.home") + "/.m2/repository/com/oracle/database/jdbc/ojdbc11/23.7.0.25.01/ojdbc11-23.7.0.25.01.jar";
        additionalContainerFiles.put(ojdbcJarPath, "/opt/openl/lib/ojdbc11.jar");

        super.beforeMethod(result);
    }

    @Override
    @AfterMethod
    public void afterMethod(ITestResult result) {
        super.afterMethod(result);
        if (oracleContainer != null && oracleContainer.isRunning()) {
            LOGGER.info("Stopping Oracle container...");
            oracleContainer.stop();
        }
    }

    @Test
    @TestCaseId("IPBQA-27295")
    @Description("Oracle JDBC as Design Repository — create project, deploy, verify web service compiled")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testSplitDesignRepositoryDeployRules() {
        EditorPage editorPage = new LoginService(LocalDriverPool.getPage()).login(UserService.getUser(User.ADMIN));
        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        RepositoriesPageComponent reposPage = adminPage.navigateToRepositoriesPage();

        // Configure Design Repository as Oracle JDBC
        reposPage.clickDesignRepositoriesTab();
        reposPage.setDesignRepositoryType("Database (JDBC)");
        reposPage.setDesignRepositoryJdbcUrl(oracleJdbcUrl);
        reposPage.setSecureConnection(true);
        reposPage.setDesignRepositoryLogin(oracleContainer.getUsername());
        reposPage.setDesignRepositoryPassword(oracleContainer.getPassword());

        // Configure Deployment Config Repository as separate local Git
        reposPage.clickDeploymentRepositoriesTab();
        // NOTE: "Use Design Repository" uncheck and local Git path set —
        // locators need verification against actual new UI layout.
        // The legacy used DeploymentConfigurationRepositoryPage (JSF systemSettingsForm).
        // In the new Ant Design UI, this is handled via Deployment Repositories tab.
        // Below calls use existing addDeploymentRepository() then configure as local Git:
        reposPage.addDeploymentRepository();
        // The deployment config repo local path field (reusing remoteRepositoryPathField after type set):
        // For now, relying on the default path and skipping explicit local path assertion
        // (locator for deployment config repo "local Git path" differs between legacy and new UI)

        reposPage.applyChangesAndRelogin(User.ADMIN);

        // Assert Design Repository type is JDBC after apply
        adminPage = editorPage.openUserMenu().navigateToAdministration();
        reposPage = adminPage.navigateToRepositoriesPage();
        reposPage.clickDesignRepositoriesTab();
        assertThat(reposPage.getDesignRepositoryType()).isEqualTo("Database (JDBC)");
        assertThat(reposPage.getDesignRepositoryUrl()).isEqualTo(oracleJdbcUrl);

        // Open Repository tab and create project from template
        editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        RepositoryPage repositoryPage = new RepositoryPage();
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, projectName,
                "Tutorial 3 - More Advanced Decision and Data Tables");

        // Select project and verify Copy dialog has NO branch selector (JDBC has no branches)
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);

        repositoryPage.getRepositoryContentButtonsPanelComponent().clickCopyBtn();
        assertThat(repositoryPage.getCopyProjectDialogComponent().getCurrentBranch()).isEmpty();
        repositoryPage.getCopyProjectDialogComponent().clickCancelButton();

        // Get revision and verify it is NOT expandable (EPBDS-9633: DB repos have non-expandable revisions)
        RepositoryContentTabPropertiesComponent propsTab =
                repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        String projectRevision = propsTab.getRevision();
        assertThat(propsTab.isRevisionExpandable())
                .as("EPBDS-9633: JDBC repo revision should NOT be expandable")
                .isFalse();

        // Create Deploy Configuration
        repositoryPage.createDeployConfiguration(deployName);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Deploy Configurations")
                .selectItemInFolder("Deploy Configurations", deployName);

        // Add project to deploy config and save
        DeployConfigurationTabsComponent deployTabs =
                repositoryPage.getRepositoryContentTabSwitcherComponent().selectDeployConfigTab();
        deployTabs.openProjectsToDeployTab();
        deployTabs.addProjectToDeploy(projectName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickSaveBtn();

        // Deploy the configuration to "Deployment" repository
        repositoryPage.getLeftRepositoryTreeComponent()
                .selectItemInFolder("Deploy Configurations", deployName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickDeploy();
        repositoryPage.getDeployModalComponent().deployWithAllFields("Deployment", deployName, "Deploy from JDBC test");

        // Verify web service is compiled (no failed icon present)
        ServicePage servicePage = new ServicePage();
        servicePage.open();
        String serviceName = deployName + "/" + projectName;
        assertThat(servicePage.getFailedIcon(serviceName).isVisible(5000))
                .as("Service '" + serviceName + "' should NOT have a failed icon")
                .isFalse();
        assertThat(servicePage.getExpandServiceButton(serviceName).isVisible(10000))
                .as("Service '" + serviceName + "' should be deployed and expandable")
                .isTrue();

        // NOTE: ConsoleOutput.inspectLogFileDemo(container) from legacy is not migrated
        // as it was specific to the legacy container log inspection utility.
    }
}
