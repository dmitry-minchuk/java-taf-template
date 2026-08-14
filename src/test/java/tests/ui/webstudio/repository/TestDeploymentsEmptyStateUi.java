package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.DeploymentsHomePage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.WorkflowService;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDeploymentsEmptyStateUi extends BaseTest {

    @Test
    @TestCaseId("EPBDS-16307")
    @Description("Negative: without a deployment repository the Deployments screen states that none is "
            + "configured, the header does not offer the Deployments tab, and a project row offers no Deploy "
            + "action while other row actions are still listed.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testDeploymentsAreClosedOffWithoutDeployRepository() {
        String projectName = WorkflowService.loginCreateProjectFromTemplate(User.ADMIN, "Sample Project");

        DeploymentsHomePage deployments = new DeploymentsHomePage().open();
        assertThat(deployments.isEmptyRepositoriesShown(10000))
                .as("Without a deploy repository the content pane must state that none is configured")
                .isTrue();
        assertThat(deployments.isNoRepositoriesRailShown(10000))
                .as("The repository rail must state that there are no deployment repositories")
                .isTrue();

        TabSwitcherComponent tabSwitcher = new EditorPage().getTabSwitcherComponent();
        assertThat(tabSwitcher.isTabOfferedWithin("Projects", 10000))
                .as("Sanity: the header must still offer Projects")
                .isTrue();
        assertThat(tabSwitcher.isTabOfferedWithin("Deployments", 5000))
                .as("The header must not offer the Deployments tab without a deployment repository")
                .isFalse();

        RepositoryPage repositoryPage = new EditorPage().getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        List<String> rowActions = repositoryPage.getProjectActionLabels(projectName);
        assertThat(rowActions)
                .as("Sanity: the project row actions must be readable")
                .contains("Export");
        assertThat(rowActions)
                .as("A project must offer no Deploy action while no deployment repository exists")
                .doesNotContain("Deploy");
    }
}
