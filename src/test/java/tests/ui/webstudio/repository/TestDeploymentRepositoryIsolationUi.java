package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDeploymentRepositoryIsolationUi extends BaseTest {

    private static final String PROJECT = "IsolationProject";
    private static final String SHARED_NAME = "SharedAcrossRepositories";
    private static final String TEMPLATE = "Sample Project";

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
    @TestCaseId("IPBQA-33011")
    @Description("Two deployment repositories holding a deployment of the very same name must stay isolated: "
            + "each list shows its own single row, the rail switch is reflected in the URL, and the URL-safe "
            + "id of each deployment decodes to its own repository, not to the other one (EPBDS-16403).")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEPLOY_STUDIO_PARAMS)
    public void testSameDeploymentNameStaysIsolatedPerRepository() {
        EditorPage editorPage = new LoginService(DriverPool.getPage()).login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, PROJECT, TEMPLATE);

        repositoryPage.openProjectsList();
        DeployModalComponent firstDeploy = repositoryPage.clickDeploy(PROJECT);
        firstDeploy.deployWithAllFields(DeployFixtureService.PRIMARY_REPOSITORY_NAME, SHARED_NAME, "Into the first");
        assertThat(firstDeploy.getSuccessNotificationText().toLowerCase())
                .as("Deploying into the first repository must report success")
                .contains("successfully");
        repositoryPage.closeAllMessages();

        repositoryPage.openProjectsList();
        DeployModalComponent secondDeploy = repositoryPage.clickDeploy(PROJECT);
        secondDeploy.deployWithAllFields(DeployFixtureService.SECOND_REPOSITORY_NAME, SHARED_NAME, "Into the second");
        assertThat(secondDeploy.getSuccessNotificationText().toLowerCase())
                .as("Deploying into the second repository must report success, not fail silently")
                .contains("successfully");
        repositoryPage.closeAllMessages();

        DeploymentsHomePage deployments = new DeploymentsHomePage().open();
        assertThat(deployments.getVisibleDeploymentNames())
                .as("The first repository must list its own single deployment")
                .containsExactly(SHARED_NAME);

        DeploymentWorkspacePage workspace = deployments.openDeployment(SHARED_NAME);
        assertThat(EntityIdUtil.decodeUrlSafeId(EntityIdUtil.lastUrlSegment(DriverPool.getPage().url())))
                .as("The id opened from the first repository must decode to that repository")
                .isEqualTo(DeployFixtureService.PRIMARY_REPOSITORY_ID + ":" + SHARED_NAME);
        assertThat(workspace.isProjectPresent(PROJECT))
                .as("The deployment of the first repository must hold the deployed project")
                .isTrue();

        deployments = new DeploymentsHomePage().open();
        deployments.selectRepository(DeployFixtureService.SECOND_REPOSITORY_ID);
        assertThat(DriverPool.getPage().url())
                .as("Switching the repository in the rail must be reflected in the URL")
                .contains("repo=" + DeployFixtureService.SECOND_REPOSITORY_ID);
        assertThat(deployments.getVisibleDeploymentNames())
                .as("The second repository must list its own deployment, not the rows of the first one")
                .containsExactly(SHARED_NAME);

        workspace = deployments.openDeployment(SHARED_NAME);
        assertThat(EntityIdUtil.decodeUrlSafeId(EntityIdUtil.lastUrlSegment(DriverPool.getPage().url())))
                .as("The id opened from the second repository must decode to the second repository")
                .isEqualTo(DeployFixtureService.SECOND_REPOSITORY_ID + ":" + SHARED_NAME);
        assertThat(workspace.getTitle())
                .as("The detail screen of the second repository must be titled with the deployment name")
                .isEqualTo(SHARED_NAME);
        assertThat(workspace.isProjectPresent(PROJECT))
                .as("The second deployment repository must really store the deployed project")
                .isTrue();
    }
}
