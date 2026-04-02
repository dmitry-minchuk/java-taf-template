package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.RepositoriesPageComponent;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.repositorytabcomponents.DeployModalComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.DeployInfrastructureService;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.DbVerificationUtil;
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
 * Migrated from: Repository/TestDeploymentConfigurationRepositoryConnectionOracle.java
 * Ticket: IPBQA-27365
 *
 * Only the JDBC method is migrated — it verifies Oracle JDBC integration as a
 * Deployment Configuration Repository. The Git method is not migrated because
 * Git connectivity is already covered by other tests.
 *
 * Adaptation: Instead of connecting to a pre-deployed Oracle 12c instance,
 * each test run spins up an Oracle Free container via Testcontainers.
 * The ojdbc11 JAR is copied into the WebStudio container's /opt/openl/lib/.
 *
 * All containers share a Docker network (created here and registered in NetworkPool).
 * Oracle is accessible via Docker DNS alias "oracle" — no host.docker.internal dependency.
 */
public class TestDeploymentConfigurationRepositoryConnectionOracle extends BaseTest {

    private static final Map<String, String> additionalContainerFiles = new HashMap<>();

    private DeployInfrastructureService deployInfra;
    private final String projectName = "OracleDeployTest";
    private final String deploymentName = "oracle-deploy";

    @Override
    @BeforeMethod
    public void beforeMethod(ITestResult result) {
        additionalContainerFiles.clear();
        deployInfra = DeployInfrastructureService.builder()
                .withOracle()
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
    @TestCaseId("IPBQA-27365")
    @Description("Deployment Configuration Repository — connect via Oracle JDBC and deploy a project")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testDeploymentConfigurationRepositoryConnectionOracleJDBC() {
        // Step 1: Login and navigate to Admin → Repositories
        EditorPage editorPage = new LoginService(LocalDriverPool.getPage()).login(UserService.getUser(User.ADMIN));
        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        RepositoriesPageComponent reposPage = adminPage.navigateToRepositoriesPage();

        // Step 2: Add Deployment Repository with type "Database JDBC" pointing to Oracle
        reposPage.addDeploymentRepository();
        reposPage.setDesignRepositoryType("Database JDBC");
        reposPage.setDesignRepositoryJdbcUrl(deployInfra.getOracleJdbcUrl());
        reposPage.setSecureConnection(true);
        reposPage.setDesignRepositoryLogin(deployInfra.getOracleContainer().getUsername());
        reposPage.setDesignRepositoryPassword(deployInfra.getOracleContainer().getPassword());

        // Step 3: Apply and relogin
        reposPage.applyChangesAndRelogin(User.ADMIN);

        // Step 4: Go to Repository tab, create a project to deploy
        editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        RepositoryPage repositoryPage = new RepositoryPage();
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, projectName, "Example 1 - Bank Rating");

        // Step 5: Select the project and deploy it to the Oracle deployment repository
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickDeploy();

        // Step 6: Fill deploy modal and deploy
        DeployModalComponent deployModal = repositoryPage.getDeployModalComponent();
        deployModal.deployWithAllFields(null, deploymentName, "Oracle JDBC deploy test");

        // Step 7: Wait for deploy to complete (success notification appears)
        assertThat(deployModal.isSuccessNotificationVisible())
                .as("Deploy should complete successfully with a success notification")
                .isTrue();

        // Step 8: Verify that data was actually written to the Oracle database
        verifyOracleContainsDeployedData();
    }

    private void verifyOracleContainsDeployedData() {
        List<Map<String, String>> rows = DbVerificationUtil.queryRows(
                deployInfra.getOracleContainer().getJdbcUrl(),
                deployInfra.getOracleContainer().getUsername(),
                deployInfra.getOracleContainer().getPassword(),
                "SELECT id, file_name, author, file_comment, modified_at, dbms_lob.getlength(file_data) as file_size FROM openl_repository ORDER BY id");
        assertThat(rows)
                .as("Oracle deployment repository should contain deployed project files")
                .isNotEmpty();
    }
}
