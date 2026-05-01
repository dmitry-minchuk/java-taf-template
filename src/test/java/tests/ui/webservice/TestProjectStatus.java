package tests.ui.webservice;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import configuration.projectconfig.PropertyNameSpace;
import domain.ui.webservice.pages.ServicePage;
import helpers.utils.TestDataUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestProjectStatus extends BaseTest {

    private static final String FAILED_PROJECT = "Example 3 - Auto Policy Calculation-failure";
    private static final String SUCCESS_PROJECT = "Example 3 - Auto Policy Calculation-success";

    private static final Map<String, String> additionalContainerConfig = new HashMap<>(Map.of(
            "production-repository.factory", "repo-zip"
    ));

    private static final Map<String, String> additionalContainerFiles = new HashMap<>(Map.of(
            TestDataUtil.getDirectoryPathFromResources("TestProjectStatus"),
            "/opt/openl/shared/"
    ));

    @Test
    @TestCaseId("IPBQA-29488")
    @Description("Project Status on the Web Services page: failed deployment shows compilation errors, successful deployment shows success status")
    @AppContainerConfig(startParams = AppContainerStartParameters.SERVICE_FILE_PARAMS, dockerImageProperty = PropertyNameSpace.WS_DOCKER_IMAGE_NAME)
    public void testProjectStatus() {
        ServicePage servicePage = new ServicePage(LocalDriverPool.getPage());
        servicePage.open();

        assertThat(servicePage.getDeploymentFailedIcon(FAILED_PROJECT).isVisible(10000))
                .as("Failed status icon should be visible for '%s'", FAILED_PROJECT)
                .isTrue();

        servicePage.getDeploymentFailedIcon(FAILED_PROJECT).click();
        String failedErrorText = servicePage.getDeploymentErrorText(FAILED_PROJECT).getText();
        assertThat(failedErrorText)
                .as("Compilation error messages for '%s' should reference the missing 'price' identifier and the invalid 'AirbagType' domain value", FAILED_PROJECT)
                .contains("Identifier 'price' is not found.")
                .contains("Field 'price' is not found in type 'Vehicle'.")
                .contains("AirbagType");

        assertThat(servicePage.getDeploymentSuccessIcon(SUCCESS_PROJECT).isVisible(10000))
                .as("Success status icon should be visible for '%s'", SUCCESS_PROJECT)
                .isTrue();
    }
}
