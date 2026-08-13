package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.pages.mainpages.DeploymentsHomePage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDeploymentsTabUi extends BaseTest {

    @Test
    @TestCaseId("EPBDS-16307")
    @Description("Negative: without a configured deployment repository the React Deployments tab must render "
            + "its empty placeholder, not an error page.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testDeploymentsTabShowsEmptyStateWithoutDeployRepository() {
        new LoginService(DriverPool.getPage()).login(UserService.getUser(User.ADMIN));

        DeploymentsHomePage deployments = new DeploymentsHomePage().open();
        assertThat(deployments.isHomeShown(10000))
                .as("The Deployments tab must render its own screen, not an error page")
                .isTrue();
        assertThat(deployments.isNoRepositoriesShown(10000))
                .as("Without a deploy repository the Deployments tab must show the empty placeholder")
                .isTrue();
    }
}
