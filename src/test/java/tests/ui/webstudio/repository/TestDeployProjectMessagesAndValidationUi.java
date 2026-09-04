package tests.ui.webstudio.repository;

import configuration.annotations.KnownIssue;
import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.repositorytabcomponents.DeployModalComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.DeployInfrastructureService;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.StringUtil;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDeployProjectMessagesAndValidationUi extends BaseTest {

    private static final String TEMPLATE_NAME = "Example 1 - Bank Rating";
    private static final String REMOVED_FEATURE_NAME = "Deploy Configuration";
    private static final String INVALID_DEPLOYMENT_NAME = "bad:name?*";

    private static final Map<String, String> additionalContainerFiles = new HashMap<>();

    @Override
    protected Map<String, String> additionalContainerFiles() {
        return additionalContainerFiles;
    }
    private DeployInfrastructureService deployInfra;

    @Override
    @BeforeMethod
    public void beforeMethod(ITestResult result) {
        additionalContainerFiles.clear();
        deployInfra = DeployInfrastructureService.builder().withPostgres().build();
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
    @TestCaseId("EPBDS-16273")
    @Description("A successful deploy must report that the project was deployed. KNOWN-FAILING: the toast still "
            + "names the removed \"Deploy Configuration\" feature."
            + " Known bug: EPBDS-16273.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEPLOY_STUDIO_PARAMS)
    @KnownIssue("EPBDS-16273")
    public void testDeploySuccessMessageNamesTheProject() {
        RepositoryPage repositoryPage = openRepositoryWithProject("DeployMsg");
        String projectName = lastCreatedProject;

        DeployModalComponent deployModal = repositoryPage.clickDeploy(projectName);
        deployModal.deployWithAllFields(null, StringUtil.generateUniqueName("Deploy"), "Deploy for message check");

        String notification = deployModal.getSuccessNotificationText();
        assertThat(notification)
                .as("The deploy notification must not name the removed \"%s\" feature", REMOVED_FEATURE_NAME)
                .doesNotContain(REMOVED_FEATURE_NAME);
        assertThat(notification.toLowerCase())
                .as("The deploy notification should say the project was deployed")
                .contains("deployed");
    }

    @Test
    @TestCaseId("EPBDS-16271")
    @Description("The Deployment Name field must reject forbidden characters, as the REST deploy API does. "
            + "KNOWN-FAILING: the field takes any value and the invalid name becomes the deployment folder."
            + " Known bug: EPBDS-16271.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEPLOY_STUDIO_PARAMS)
    @KnownIssue("EPBDS-16271")
    public void testDeploymentNameRejectsForbiddenCharacters() {
        RepositoryPage repositoryPage = openRepositoryWithProject("DeployName");
        String projectName = lastCreatedProject;

        DeployModalComponent deployModal = repositoryPage.clickDeploy(projectName);
        deployModal.waitForModal();
        deployModal.fillDeploymentName(INVALID_DEPLOYMENT_NAME);
        deployModal.fillComment("Deploy with a forbidden deployment name");
        deployModal.clickDeploy();

        assertThat(deployModal.isErrorMessageDisplayed() || !deployModal.isSuccessNotificationVisible())
                .as("A deployment name carrying forbidden characters (%s) must be refused", INVALID_DEPLOYMENT_NAME)
                .isTrue();
        assertThat(deployModal.getErrorMessage())
                .as("The refusal should name the problem with the deployment name")
                .isNotEmpty();
    }

    private String lastCreatedProject;

    private RepositoryPage openRepositoryWithProject(String prefix) {
        lastCreatedProject = StringUtil.generateUniqueName(prefix);
        EditorPage editorPage = new LoginService(DriverPool.getPage())
                .login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, lastCreatedProject, TEMPLATE_NAME);
        repositoryPage.openProjectsList();
        return repositoryPage;
    }
}
