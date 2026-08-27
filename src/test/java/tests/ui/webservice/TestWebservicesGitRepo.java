package tests.ui.webservice;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import domain.api.ServiceHelloMethod;
import domain.ui.webservice.pages.ServicePage;
import helpers.service.GitContainerService;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestWebservicesGitRepo extends BaseTest {

    private static final String GIT_PROJECT_NAME = "SimpleGitProject";
    private static final String BASE_PATH = "deploy/";

    private static final String GIT_CONTAINER_ALIAS = "git-container-git-repo";

    private GitContainerService gitContainer;

    @Override
    protected void startAuxiliaryContainers() {
        gitContainer = new GitContainerService(
                GIT_CONTAINER_ALIAS, "ruleServiceTestData", "main", "/ruleservice_repo");
        gitContainer.start();
    }

    @Override
    protected void stopAuxiliaryContainers() {
        if (gitContainer != null) {
            gitContainer.stop();
            gitContainer = null;
        }
    }

    @Override
    protected Map<String, String> additionalContainerConfig() {
        return Map.of(
                "production-repository.base.path", BASE_PATH,
                "production-repository.uri", gitContainer.getInNetworkUrl()
        );
    }

    @Test
    @TestCaseId("EPBDS-14497")
    @Description("Test that SimpleGitProject from Git repository is deployed and service endpoint works")
    @AppContainerConfig(startParams = AppContainerStartParameters.SERVICE_PARAMS)
    public void testWebservicesGitRepo() {
        ServicePage servicePage = new ServicePage(DriverPool.getPage());
        servicePage.open();

        verifyProjectDeployed(servicePage, GIT_PROJECT_NAME);
        verifyServiceEndpointResponse();
    }

    private void verifyProjectDeployed(ServicePage servicePage, String projectName) {
        assertThat(servicePage.getProjectElement(projectName).isVisible(5000))
                .as("Project '%s' should be visible in services list", projectName)
                .isTrue();

        assertThat(servicePage.getExpandServiceButton(projectName).isVisible(5000))
                .as("Project '%s' should have status-deployed (successfully compiled)", projectName)
                .isTrue();

        assertThat(servicePage.getFailedIcon(projectName).isVisible(1000))
                .as("Project '%s' should not have failed status", projectName)
                .isFalse();
    }

    private void verifyServiceEndpointResponse() {
        ServiceHelloMethod serviceHelloMethod = new ServiceHelloMethod(GIT_PROJECT_NAME);
        Response response = serviceHelloMethod.post("10");

        assertThat(response.getStatusCode())
                .as("Service endpoint should return HTTP 200")
                .isEqualTo(200);

        assertThat(response.getBody().asString())
                .as("Service endpoint should return 'Good Morning'")
                .isEqualTo("Good Morning");
    }
}
