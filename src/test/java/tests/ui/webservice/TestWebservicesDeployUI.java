package tests.ui.webservice;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerPool;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import configuration.projectconfig.PropertyNameSpace;
import domain.ui.webservice.pages.ServicePage;
import helpers.service.GitContainerService;
import helpers.utils.LogsUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestWebservicesDeployUI extends BaseTest {
    private static final String SIMPLE_PROJECT = "SimpleProject";
    private static final String SIMPLE_PROJECT_2 = "SimpleProject2";
    private static final String EXAMPLE_3_PROJECT = "Example 3 - Auto Policy Calculation";
    private static final String MULTIPLE_PROJECT = "multiple-deployment/project1";
    private static final String MULTIPLE_PROJECT_2 = "multiple-deployment/project2";
    private static final String HELLO_RULE = "someDeployment/Hello_Rule";

    private static final String BASE_PATH = "TestWebservicesDeployUI";

    private static final String GIT_CONTAINER_ALIAS = "git-container-deploy-ui";

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
    @TestCaseId("IPBQA-28640")
    @Description("Test WebService deployment UI - verify projects are deployed and accessible")
    @AppContainerConfig(startParams = AppContainerStartParameters.SERVICE_PARAMS, dockerImageProperty = PropertyNameSpace.WS_DOCKER_IMAGE_NAME)
    public void testWebservicesDeployUi() {
        ServicePage servicePage = new ServicePage(DriverPool.getPage());
        servicePage.open();

        assertThat(servicePage.getProjectElement(SIMPLE_PROJECT).isVisible(5000))
                .as("SimpleProject should be visible in the services list")
                .isTrue();

        servicePage.downloadProject(SIMPLE_PROJECT);

        DriverPool.getPage().reload();
        servicePage = new ServicePage(DriverPool.getPage());

        assertThat(servicePage.getManifestLink(SIMPLE_PROJECT_2).isVisible(5000))
                .as("SimpleProject2 manifest link should be visible")
                .isTrue();

        assertThat(servicePage.getProjectElement(EXAMPLE_3_PROJECT).isVisible(5000))
                .as("Example 3 project should be visible")
                .isTrue();

        DriverPool.getPage().reload();
        servicePage = new ServicePage(DriverPool.getPage());

        assertThat(servicePage.getProjectElement(MULTIPLE_PROJECT).isVisible(5000))
                .as("multiple-deployment/project1 should be visible")
                .isTrue();
        assertThat(servicePage.getProjectElement(MULTIPLE_PROJECT_2).isVisible(5000))
                .as("multiple-deployment/project2 should be visible")
                .isTrue();

        servicePage.downloadProject(MULTIPLE_PROJECT);
        servicePage.downloadProject(MULTIPLE_PROJECT_2);

        DriverPool.getPage().reload();
        servicePage = new ServicePage(DriverPool.getPage());

        assertThat(servicePage.getProjectElement(HELLO_RULE).isVisible(5000))
                .as("someDeployment/Hello_Rule should be visible")
                .isTrue();

        DriverPool.getPage().reload();
        LogsUtil.inspectLogFile(AppContainerPool.get());
    }
}
