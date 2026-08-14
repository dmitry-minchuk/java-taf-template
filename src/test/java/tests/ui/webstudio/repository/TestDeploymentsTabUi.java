package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import com.microsoft.playwright.Response;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.repositorytabcomponents.DeployModalComponent;
import domain.ui.webstudio.pages.mainpages.DeploymentWorkspacePage;
import domain.ui.webstudio.pages.mainpages.DeploymentsHomePage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.DeployFixtureService;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.EntityIdUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDeploymentsTabUi extends BaseTest {

    private static final String PROJECT = "Тест-оПА Deploy";
    private static final String DEPLOYMENT = "QaJourneyАо";
    private static final String TEMPLATE = "Sample Project";
    private static final String EMPTY_CELL = "—";

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
    @TestCaseId("IPBQA-33010")
    @Description("EPBDS-16307 with the EPBDS-16403 guard: the Deployments tab starts empty, shows a deployed "
            + "project, isolates repositories, opens a deployment by its URL-safe id, survives a deep-link "
            + "reload, and a redeploy selected from the dropdown updates the same configuration through "
            + "POST /web/deployments/{id} instead of creating a second one.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEPLOY_STUDIO_PARAMS)
    public void testDeploymentsTabJourney() {
        EditorPage editorPage = new LoginService(DriverPool.getPage()).login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, PROJECT, TEMPLATE);
        repositoryPage.openProjectsList();

        DeploymentsHomePage deployments = new DeploymentsHomePage().open();
        assertThat(deployments.isRepositoryListed(DeployFixtureService.PRIMARY_REPOSITORY_ID))
                .as("Both configured deployment repositories must be listed in the rail")
                .isTrue();
        assertThat(deployments.isRepositoryListed(DeployFixtureService.SECOND_REPOSITORY_ID))
                .as("The second deployment repository must be listed in the rail")
                .isTrue();
        assertThat(deployments.isEmptyShown(10000))
                .as("A configured but empty deployment repository must show its own empty state")
                .isTrue();
        assertThat(deployments.getSummary())
                .as("The summary must report an empty repository as zero deployments")
                .isEqualTo("0 deployments");

        repositoryPage.openProjectsList();
        repositoryPage.clickDeploy(PROJECT)
                .deployWithAllFields(DeployFixtureService.PRIMARY_REPOSITORY_NAME, DEPLOYMENT, "First deploy");
        repositoryPage.closeAllMessages();

        deployments = new DeploymentsHomePage().open();
        assertThat(deployments.getVisibleDeploymentNames())
                .as("The deployed configuration must be the only row of its repository")
                .containsExactly(DEPLOYMENT);
        assertThat(deployments.getSummary())
                .as("The summary must count the deployment")
                .isEqualTo("1 deployment");

        String encodedPayload = DeployFixtureService.PRIMARY_REPOSITORY_ID + ":" + DEPLOYMENT;
        assertThat(Base64.getEncoder().encodeToString(encodedPayload.getBytes(StandardCharsets.UTF_8)))
                .as("Test data guard: this deployment name must force characters the URL-safe alphabet replaces")
                .containsAnyOf("+", "/");

        DeploymentWorkspacePage workspace = deployments.openDeployment(DEPLOYMENT);
        String deepLink = DriverPool.getPage().url();
        String idSegment = EntityIdUtil.lastUrlSegment(deepLink);
        assertThat(idSegment)
                .as("The deployment id must use only the URL-safe Base64 alphabet")
                .matches(EntityIdUtil.URL_SAFE_BASE64_PATTERN)
                .doesNotContain("+", "/");
        assertThat(idSegment)
                .as("The deployment id must actually carry the URL-safe substitution, not plain Base64")
                .containsAnyOf("-", "_");
        assertThat(EntityIdUtil.decodeUrlSafeId(idSegment))
                .as("The deployment id must decode to its repository and name")
                .isEqualTo(encodedPayload);

        assertThat(workspace.getTitle())
                .as("The detail screen must be titled with the deployment name")
                .isEqualTo(DEPLOYMENT);
        assertThat(workspace.getTabNames())
                .as("The detail screen must offer the single Projects tab")
                .containsExactly("Projects");
        assertThat(workspace.isProjectPresent(PROJECT))
                .as("The deployed project must be listed on the detail screen")
                .isTrue();
        List<String> projectCells = workspace.getProjectRowCells(PROJECT);
        assertThat(projectCells.get(1))
                .as("The deployed project must carry a revision, not the empty-value placeholder")
                .isNotBlank()
                .isNotEqualTo(EMPTY_CELL);
        assertThat(projectCells.get(2))
                .as("The deployed project must name the user who deployed it")
                .isNotEqualTo(EMPTY_CELL)
                .containsIgnoringCase("admin");
        String revisionAfterFirstDeploy = projectCells.get(1);

        DriverPool.getPage().navigate(deepLink);
        workspace = new DeploymentWorkspacePage().waitForLoaded();
        assertThat(workspace.getTitle())
                .as("A deep link to the deployment must reopen it after a reload (EPBDS-16403)")
                .isEqualTo(DEPLOYMENT);

        repositoryPage.openProjectsList();
        DeployModalComponent redeployModal = repositoryPage.clickDeploy(PROJECT);
        Response redeployResponse = DriverPool.getPage().waitForResponse(
                response -> "POST".equals(response.request().method())
                        && response.url().endsWith("/deployments/" + idSegment),
                () -> redeployModal.deployToExistingDeployment(
                        DeployFixtureService.PRIMARY_REPOSITORY_NAME, DEPLOYMENT, "Redeploy"));
        assertThat(redeployResponse.status())
                .as("Redeploying by id must succeed; the endpoint returns no content by design")
                .isEqualTo(204);
        repositoryPage.closeAllMessages();

        deployments = new DeploymentsHomePage().open();
        assertThat(deployments.getVisibleDeploymentNames())
                .as("Redeploying must update the same configuration, not duplicate it")
                .containsExactly(DEPLOYMENT);
        workspace = deployments.openDeployment(DEPLOYMENT);
        assertThat(workspace.getProjectRowCells(PROJECT).get(1))
                .as("Redeploying must publish a new revision of the project")
                .isNotEqualTo(revisionAfterFirstDeploy);
    }
}
