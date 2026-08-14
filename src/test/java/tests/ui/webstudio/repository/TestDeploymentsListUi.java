package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.DeploymentsHomePage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.DeployFixtureService;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDeploymentsListUi extends BaseTest {

    private static final String ALPHA_PROJECT = "AlphaProject";
    private static final String BRAVO_PROJECT = "BravoProject";
    private static final String ALPHA_DEPLOYMENT = "AlphaDeployment";
    private static final String BRAVO_DEPLOYMENT = "BravoDeployment";
    private static final String TEMPLATE = "Sample Project";
    private static final String NO_MATCH_TEXT = "No deployments match your search";

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
    @TestCaseId("EPBDS-16307")
    @Description("EPBDS-16307: the Deployments list filters by its search box, states when nothing matches, "
            + "restores on Clear search, keeps reporting the repository total, pages through ?size, and a "
            + "search made from a later page shows the match instead of an empty page.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEPLOY_STUDIO_PARAMS)
    public void testDeploymentsListSearchAndPaging() {
        EditorPage editorPage = new LoginService(DriverPool.getPage()).login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        deployProject(repositoryPage, ALPHA_PROJECT, ALPHA_DEPLOYMENT);
        deployProject(repositoryPage, BRAVO_PROJECT, BRAVO_DEPLOYMENT);

        DeploymentsHomePage deployments = new DeploymentsHomePage().open();
        assertThat(deployments.getVisibleDeploymentNames())
                .as("Both deployments must be listed before filtering")
                .containsExactlyInAnyOrder(ALPHA_DEPLOYMENT, BRAVO_DEPLOYMENT);
        assertThat(deployments.getSummary())
                .as("The summary must count both deployments")
                .contains("2 deployments");

        deployments.search("bravo");
        assertThat(deployments.getVisibleDeploymentNames())
                .as("A case-insensitive fragment must leave only the matching deployment")
                .containsExactly(BRAVO_DEPLOYMENT);
        assertThat(deployments.getSummary())
                .as("The summary must keep reporting the repository total while the list is filtered")
                .contains("2 deployments");

        deployments.search("no-such-deployment-xyz");
        assertThat(deployments.isNoMatchShown(10000))
                .as("A query matching nothing must show the no-match placeholder")
                .isTrue();
        assertThat(deployments.getNoMatchText())
                .as("The no-match placeholder must explain itself")
                .contains(NO_MATCH_TEXT);

        deployments.clickClearSearch();
        assertThat(deployments.getVisibleDeploymentNames())
                .as("Clear search must restore both deployments")
                .containsExactlyInAnyOrder(ALPHA_DEPLOYMENT, BRAVO_DEPLOYMENT);

        deployments = new DeploymentsHomePage().openWithQuery("?size=1");
        assertThat(deployments.isPaginationShown(10000))
                .as("A page size smaller than the list must render the pagination control")
                .isTrue();
        List<String> firstPage = deployments.getVisibleDeploymentNames();
        assertThat(firstPage)
                .as("The first page must hold exactly one deployment")
                .hasSize(1);

        deployments = new DeploymentsHomePage().openWithQuery("?size=1&page=2");
        List<String> secondPage = deployments.getVisibleDeploymentNames();
        assertThat(secondPage)
                .as("The second page must hold the other deployment")
                .hasSize(1)
                .isNotEqualTo(firstPage);

        deployments.search(firstPage.getFirst());
        assertThat(deployments.getVisibleDeploymentNames())
                .as("Searching from a later page must show the match, not an empty page")
                .containsExactly(firstPage.getFirst());
        assertThat(deployments.isPaginationShown(3000))
                .as("The pagination control must disappear once the filtered set fits a single page")
                .isFalse();
    }

    private void deployProject(RepositoryPage repositoryPage, String projectName, String deploymentName) {
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, projectName, TEMPLATE);
        repositoryPage.openProjectsList();
        repositoryPage.clickDeploy(projectName)
                .deployWithAllFields(DeployFixtureService.PRIMARY_REPOSITORY_NAME, deploymentName,
                        "Deploy " + projectName);
        repositoryPage.closeAllMessages();
        repositoryPage.openProjectsList();
    }
}
