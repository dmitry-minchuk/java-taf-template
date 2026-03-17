package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerPool;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.RepositoriesPageComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.LogsUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSupportedRepositories extends BaseTest {

    private static final List<String> DESIGN_REPOSITORY_TYPES = Arrays.asList(
            "Database JDBC", "Database JNDI", "AWS S3", "Azure Blob Storage", "Git");

    // Deployment repositories have the same types as design (Local was removed)
    private static final List<String> DEPLOYMENT_REPOSITORY_TYPES = Arrays.asList(
            "Database JDBC",
            "Database JNDI",
            "AWS S3",
            "Azure Blob Storage",
            "Git");

    @Test
    @TestCaseId("IPBQA-29276")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testSupportedRepositories() {
        // Login as admin
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        // Navigate to Admin → Repositories
        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        RepositoriesPageComponent repositories = adminPage.navigateToRepositoriesPage();

        // Verify Design Repository types
        // Legacy: assertThat(DesignRepositoryPage.getAllTypes()).isEqualTo(repositoryValues)
        // In legacy this was checked on Installation Wizard, then on Admin Settings
        // Installation Wizard no longer exists — verify via Admin Settings only
        repositories.clickDesignRepositoriesTab();
        List<String> actualDesignTypes = repositories.getAllRepositoryTypes();
        assertThat(actualDesignTypes).isEqualTo(DESIGN_REPOSITORY_TYPES);

        // Verify default Design Repository type is "Git"
        assertThat(repositories.getDesignRepositoryType()).isEqualTo("Git");

        // Verify Deployment Repository types
        // Legacy: assertThat(DeploymentRepositoryPage.getAllTypes()).isEqualTo(deploymentRepositoryValues)
        // NOTE: DeploymentConfigurationRepository was removed (EPBDS-15093) — skipping those assertions
        repositories.clickDeploymentRepositoriesTab();
        repositories.addDeploymentRepository();
        List<String> actualDeploymentTypes = repositories.getAllRepositoryTypes();
        assertThat(actualDeploymentTypes).isEqualTo(DEPLOYMENT_REPOSITORY_TYPES);

        // Verify no errors in application logs
        LogsUtil.inspectLogFile(AppContainerPool.get());
    }
}
