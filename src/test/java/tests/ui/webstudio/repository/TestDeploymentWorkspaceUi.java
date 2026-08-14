package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.pages.mainpages.DeploymentWorkspacePage;
import domain.ui.webstudio.pages.mainpages.DeploymentsHomePage;
import helpers.service.DeployFixtureService;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDeploymentWorkspaceUi extends BaseTest {

    private static final String UNKNOWN_DEPLOYMENT = DeployFixtureService.PRIMARY_REPOSITORY_ID + ":NeverDeployed";

    private final DeployFixtureService deployFixture = new DeployFixtureService();

    @Override
    protected Map<String, String> additionalContainerFiles() {
        return deployFixture.containerFiles();
    }

    @Override
    protected void startAuxiliaryContainers() {
        deployFixture.start();
    }

    @Override
    protected void stopAuxiliaryContainers() {
        deployFixture.stop();
    }

    @Test
    @TestCaseId("IPBQA-33013")
    @Description("EPBDS-16403: a well-formed deployment id that names no existing deployment must land on the "
            + "application 404 page rather than a broken detail screen, and the Deployments list must keep "
            + "working afterwards.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEPLOY_STUDIO_PARAMS)
    public void testUnknownDeploymentIdShowsNotFoundPage() {
        new LoginService(DriverPool.getPage()).login(UserService.getUser(User.ADMIN));
        DeploymentsHomePage deployments = new DeploymentsHomePage().open();
        assertThat(deployments.isEmptyShown(10000))
                .as("Precondition: the deployment repository must be configured and empty")
                .isTrue();

        String unknownId = Base64.getUrlEncoder()
                .encodeToString(UNKNOWN_DEPLOYMENT.getBytes(StandardCharsets.UTF_8));
        DeploymentWorkspacePage workspace = new DeploymentWorkspacePage().openById(unknownId);

        assertThat(workspace.isNotFoundPageShown(10000))
                .as("An unknown deployment id must render the application 404 page")
                .isTrue();
        assertThat(workspace.isNotFoundHomeButtonShown(5000))
                .as("The 404 page must offer a way back to the application")
                .isTrue();

        deployments = new DeploymentsHomePage().open();
        assertThat(deployments.isEmptyShown(10000))
                .as("The Deployments list must keep working after the 404")
                .isTrue();
    }
}
