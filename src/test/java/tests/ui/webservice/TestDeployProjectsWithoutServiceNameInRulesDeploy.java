package tests.ui.webservice;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import configuration.projectconfig.PropertyNameSpace;
import domain.ui.webservice.pages.ServicePage;
import helpers.service.GitContainerService;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDeployProjectsWithoutServiceNameInRulesDeploy extends BaseTest {

    private static final String RUN_TABLE_PROJECT = "Run_table";
    private static final String INTRODUCTION_PROJECT = "Introduction to Decision Tables";
    private static final String ADVANCED_DECISION_PROJECT = "Advanced Decision";

    private static final String BASE_PATH = "TestDeployProjectsWithoutServiceNameInRulesDeploy";

    private static final String GIT_CONTAINER_ALIAS = "git-container-no-service-name";

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
    @TestCaseId("EPBDS-9881")
    @Description("Test that projects are compiled and deployed as services without service name configuration in rules deploy")
    @AppContainerConfig(startParams = AppContainerStartParameters.SERVICE_PARAMS, dockerImageProperty = PropertyNameSpace.WS_DOCKER_IMAGE_NAME)
    public void testDeployProjectsWithoutServiceNameInRulesDeploy() {
        ServicePage servicePage = new ServicePage(DriverPool.getPage());
        servicePage.open();

        verifyProjectDeployed(servicePage, RUN_TABLE_PROJECT);
        verifyProjectDeployed(servicePage, INTRODUCTION_PROJECT);
        verifyProjectDeployed(servicePage, ADVANCED_DECISION_PROJECT);
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
}
