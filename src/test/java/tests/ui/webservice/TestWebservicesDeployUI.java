package tests.ui.webservice;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerPool;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.ui.webservice.pages.ServicePage;
import helpers.utils.LogsUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestWebservicesDeployUI extends BaseTest {
    private static final String SIMPLE_PROJECT = "SimpleProject";
    private static final String SIMPLE_PROJECT_2 = "SimpleProject2";
    private static final String EXAMPLE_3_PROJECT = "Example 3 - Auto Policy Calculation";
    private static final String MULTIPLE_PROJECT = "multiple-deployment/project1";
    private static final String MULTIPLE_PROJECT_2 = "multiple-deployment/project2";
    private static final String HELLO_RULE = "someDeployment/Hello_Rule";

    private static final Map<String, String> additionalContainerConfig = new HashMap<>(Map.ofEntries(
            Map.entry("production-repository.base.path", "TestWebservicesDeployUI")
            //For local run git.token.ruleservice HERE!!!
            //Map.entry("production-repository.password", "git.token")
    ));

    @Test
    @TestCaseId("IPBQA-28640")
    @Description("Test WebService deployment UI - verify projects are deployed and accessible")
    @AppContainerConfig(startParams = AppContainerStartParameters.SERVICE_PARAMS)
    public void testWebservicesDeployUi() {
        // Initialize ServicePage
        ServicePage servicePage = new ServicePage(LocalDriverPool.getPage());
        servicePage.open();

        // Part 1: Verify SimpleProject is present and accessible
        assertThat(servicePage.getProjectElement(SIMPLE_PROJECT).isVisible(5000))
                .as("SimpleProject should be visible in the services list")
                .isTrue();

        // Part 1.2: Download SimpleProject
        servicePage.downloadProject(SIMPLE_PROJECT);

        // Refresh page to see updated state
        LocalDriverPool.getPage().reload();
        servicePage = new ServicePage(LocalDriverPool.getPage());

        // Part 1.3: Verify SimpleProject2 manifest link is present
        assertThat(servicePage.getManifestLink(SIMPLE_PROJECT_2).isVisible(5000))
                .as("SimpleProject2 manifest link should be visible")
                .isTrue();

        // Part 1.4: Verify Example 3 project is present
        assertThat(servicePage.getProjectElement(EXAMPLE_3_PROJECT).isVisible(5000))
                .as("Example 3 project should be visible")
                .isTrue();

        // Refresh page
        LocalDriverPool.getPage().reload();
        servicePage = new ServicePage(LocalDriverPool.getPage());

        // Part 2: Verify multiple deployments are present
        assertThat(servicePage.getProjectElement(MULTIPLE_PROJECT).isVisible(5000))
                .as("multiple-deployment/project1 should be visible")
                .isTrue();
        assertThat(servicePage.getProjectElement(MULTIPLE_PROJECT_2).isVisible(5000))
                .as("multiple-deployment/project2 should be visible")
                .isTrue();

        // Download multiple deployment projects
        servicePage.downloadProject(MULTIPLE_PROJECT);
        servicePage.downloadProject(MULTIPLE_PROJECT_2);

        // Refresh page
        LocalDriverPool.getPage().reload();
        servicePage = new ServicePage(LocalDriverPool.getPage());

        // Part 3: Verify someDeployment/Hello_Rule is present
        assertThat(servicePage.getProjectElement(HELLO_RULE).isVisible(5000))
                .as("someDeployment/Hello_Rule should be visible")
                .isTrue();

        // Refresh page
        LocalDriverPool.getPage().reload();
        LogsUtil.inspectLogFile(AppContainerPool.get());
    }
}
