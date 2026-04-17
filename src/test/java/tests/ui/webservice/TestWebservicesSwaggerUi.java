package tests.ui.webservice;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.projectconfig.PropertyNameSpace;
import configuration.driver.LocalDriverPool;
import domain.ui.webservice.pages.ServicePage;
import helpers.utils.TestDataUtil;
import helpers.utils.WaitUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class TestWebservicesSwaggerUi extends BaseTest {

    private static final String DEPLOYMENT1 = "deployment1/Sample1";
    private static final String DEPLOYMENT2 = "deployment2/Sample2";
    private static final String DEPLOYMENT3 = "deployment3/Sample3";

    // Reuses TestRuleServicesNewUI test data:
    //   deployment1/Sample1 (KAFKA)        — alphabetical ordering fixture
    //   deployment2/Sample2 (RESTFUL, ok)  — Swagger UI verification target
    //   deployment3/Sample3 (failed)       — alphabetical ordering fixture
    private static final Map<String, String> additionalContainerFiles = new HashMap<>(Map.of(
            TestDataUtil.getDirectoryPathFromResources("TestRuleServicesNewUI"),
            "/opt/openl/shared/"
    ));

    @Test
    @TestCaseId("IPBQA-32142")
    @Description("Verify Swagger UI renders for RESTFUL service and JSON/YAML spec formats are available; "
            + "verify alphabetical ordering of deployments on the services page (EPBDS-13121)")
    @AppContainerConfig(startParams = AppContainerStartParameters.SERVICE_FILE_PARAMS, dockerImageProperty = PropertyNameSpace.WS_DOCKER_IMAGE_NAME)
    public void testWebservicesSwaggerUi() {
        ServicePage servicePage = new ServicePage(LocalDriverPool.getPage());
        servicePage.open();

        assertThat(servicePage.getDeploymentRow(DEPLOYMENT1).isVisible(10000))
                .as(DEPLOYMENT1 + " should be visible once services are loaded").isTrue();
        assertThat(servicePage.getDeploymentRow(DEPLOYMENT2).isVisible(5000))
                .as(DEPLOYMENT2 + " should be visible").isTrue();
        assertThat(servicePage.getDeploymentRow(DEPLOYMENT3).isVisible(5000))
                .as(DEPLOYMENT3 + " should be visible").isTrue();

        // Step 1: Verify alphabetical ordering of deployments (EPBDS-13121)
        List<String> deploymentNames = LocalDriverPool.getPage().locator("h3").allTextContents()
                .stream()
                .map(String::trim)
                .filter(name -> name.startsWith("deployment"))
                .collect(Collectors.toList());
        assertThat(deploymentNames)
                .as("Deployments should be listed in alphabetical order")
                .containsExactly(DEPLOYMENT1, DEPLOYMENT2, DEPLOYMENT3);

        // Step 2: Navigate to Swagger UI for the RESTFUL service
        servicePage.getProjectTitleLink(DEPLOYMENT2).click();

        // Step 3: Verify Swagger UI container renders (JS-rendered, needs extended timeout)
        servicePage = new ServicePage(LocalDriverPool.getPage());
        assertThat(servicePage.getSwaggerUiContainer().isVisible(15000))
                .as("Swagger UI container #swagger-ui should be visible after navigating to swagger-ui.html")
                .isTrue();

        // Step 4: Wait for API selector to be populated with deployed service entries
        // The select#select-api options are populated asynchronously by Swagger UI JavaScript
        assertThat(WaitUtil.waitForCondition(
                () -> !LocalDriverPool.getPage().locator("xpath=//select[@id='select-api']")
                        .locator("option").allTextContents().isEmpty(),
                15000, 1000, "Waiting for API selector options to be populated"))
                .as("API selector (select#select-api) should list at least one deployed service").isTrue();

        // Step 5: Verify JSON/YAML spec download options are visible in Swagger UI
        // OpenL renders button text via CSS pseudo-elements; use Playwright text= selector
        assertThat(WaitUtil.waitForCondition(
                () -> LocalDriverPool.getPage().locator("text=Download OpenAPI spec").isVisible(),
                15000, 1000, "Waiting for 'Download OpenAPI spec' button to appear"))
                .as("Swagger UI should show 'Download OpenAPI spec' button (JSON spec available)").isTrue();
        assertThat(LocalDriverPool.getPage().locator("text=View OpenAPI spec (New Tab)").isVisible())
                .as("Swagger UI should show 'View OpenAPI spec (New Tab)' button (spec viewable in browser)").isTrue();
    }
}
