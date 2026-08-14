package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.DeploymentWorkspacePage;
import domain.ui.webstudio.pages.mainpages.DeploymentsHomePage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.DeployFixtureService;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDeploymentReplacesContentUi extends BaseTest {

    private static final String FIRST_PROJECT = "ReplacedProject";
    private static final String SECOND_PROJECT = "ReplacingProject";
    private static final String DEPLOYMENT = "SharedDeployment";
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
    @TestCaseId("IPBQA-33014")
    @Description("EPBDS-16307 Deploying another project into an existing deployment REPLACES its content: the deployment "
            + "holds the newly deployed project alone and the previously deployed one is gone. This is "
            + "destructive behaviour, so the test pins it against a silent change.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEPLOY_STUDIO_PARAMS)
    public void testDeployingAnotherProjectReplacesDeploymentContent() {
        EditorPage editorPage = new LoginService(DriverPool.getPage()).login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, FIRST_PROJECT, TEMPLATE);
        repositoryPage.openProjectsList();
        repositoryPage.clickDeploy(FIRST_PROJECT)
                .deployWithAllFields(DeployFixtureService.PRIMARY_REPOSITORY_NAME, DEPLOYMENT, "First project");
        repositoryPage.closeAllMessages();

        DeploymentWorkspacePage workspace = new DeploymentsHomePage().open().openDeployment(DEPLOYMENT);
        assertThat(workspace.isProjectPresent(FIRST_PROJECT))
                .as("Precondition: the deployment must hold the first project")
                .isTrue();

        repositoryPage.openProjectsList();
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, SECOND_PROJECT, TEMPLATE);
        repositoryPage.openProjectsList();
        repositoryPage.clickDeploy(SECOND_PROJECT)
                .deployToExistingDeployment(DeployFixtureService.PRIMARY_REPOSITORY_NAME, DEPLOYMENT,
                        "Second project into the same deployment");
        repositoryPage.closeAllMessages();

        workspace = new DeploymentsHomePage().open().openDeployment(DEPLOYMENT);
        assertThat(workspace.getProjectRowsCount())
                .as("The deployment must hold exactly one project after the replacement")
                .isEqualTo(1);
        assertThat(workspace.isProjectPresent(SECOND_PROJECT))
                .as("The deployment must hold the newly deployed project")
                .isTrue();
        assertThat(workspace.isProjectPresent(FIRST_PROJECT))
                .as("The previously deployed project must be gone from the deployment")
                .isFalse();
    }
}
