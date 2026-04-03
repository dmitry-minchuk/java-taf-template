package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
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

public class TestDeploymentConfigurationRepositoryConnectionMsSql extends BaseTest {

    private static final Map<String, String> additionalContainerFiles = new HashMap<>();

    private DeployInfrastructureService deployInfra;
    private final String projectName = "MsSqlDeployTest";
    private final String deploymentName = "mssql-deploy";

    @Override
    @BeforeMethod
    public void beforeMethod(ITestResult result) {
        additionalContainerFiles.clear();
        deployInfra = DeployInfrastructureService.builder()
                .withMsSql()
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
    @Description("Deployment Configuration Repository — connect via MS SQL JDBC and deploy a project")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testDeploymentConfigurationRepositoryConnectionMsSql() {
        EditorPage editorPage = new LoginService(LocalDriverPool.getPage()).login(UserService.getUser(User.ADMIN));
        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        RepositoriesPageComponent reposPage = adminPage.navigateToRepositoriesPage();

        reposPage.addDeploymentRepository();
        reposPage.setDesignRepositoryType("Database JDBC");
        reposPage.setDesignRepositoryJdbcUrl(deployInfra.getMsSqlJdbcUrl());
        reposPage.setSecureConnection(true);
        reposPage.setDesignRepositoryLogin(deployInfra.getMsSqlContainer().getUsername());
        reposPage.setDesignRepositoryPassword(deployInfra.getMsSqlContainer().getPassword());
        reposPage.applyChangesAndRelogin(User.ADMIN);

        editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        RepositoryPage repositoryPage = new RepositoryPage();
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, projectName, "Example 1 - Bank Rating");

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickDeploy();

        DeployModalComponent deployModal = repositoryPage.getDeployModalComponent();
        deployModal.deployWithAllFields(null, deploymentName, "MS SQL JDBC deploy test");

        assertThat(deployModal.isSuccessNotificationVisible())
                .as("Deploy should complete successfully with a success notification")
                .isTrue();

        verifyMsSqlContainsDeployedData();
    }

    private void verifyMsSqlContainsDeployedData() {
        List<Map<String, String>> rows = DbVerificationUtil.queryRows(
                deployInfra.getMsSqlHostJdbcUrl(),
                deployInfra.getMsSqlContainer().getUsername(),
                deployInfra.getMsSqlContainer().getPassword(),
                "SELECT id, file_name, author, file_comment, DATALENGTH(file_data) as file_size FROM openl_repository ORDER BY id");
        assertThat(rows)
                .as("MS SQL deployment repository should contain deployed project files")
                .isNotEmpty();
    }
}
