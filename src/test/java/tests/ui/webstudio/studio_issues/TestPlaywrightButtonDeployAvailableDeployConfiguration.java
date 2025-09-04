package tests.ui.webstudio.studio_issues;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.PlaywrightDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import domain.ui.webstudio.components.PlaywrightTabSwitcherComponent;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestPlaywrightButtonDeployAvailableDeployConfiguration extends BaseTest {

    @Test
    @TestCaseId("EPBDS-8289")
    @Description("Deploy button should be available after adding a project to a deploy configuration and saving it.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testPlaywrightButtonDeployAvailableDeployConfiguration() {
        LoginService loginService = new LoginService(PlaywrightDriverPool.getPage());
        loginService.login(UserService.getUser(User.ADMIN));

        RepositoryPage repositoryPage = new RepositoryPage().getTabSwitcherComponent().selectTab(PlaywrightTabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProjectFromTemplate("1", "Example 1 - Bank Rating");
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", "1");
        String revision1 = repositoryPage.getRepositoryContentTabPropertiesComponent().getRevision();
        LOGGER.info("revision1: {}", revision1);

        repositoryPage.createProjectFromTemplate("2", "Example 2 - Corporate Rating");
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", "2");
        String revision2 = repositoryPage.getRepositoryContentTabPropertiesComponent().getRevision();
        LOGGER.info("revision2: {}", revision2);

        repositoryPage.refresh();
        String deployConfigName = "deployConfiguration";
        repositoryPage.createDeployConfiguration(deployConfigName);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Deploy Configurations")
                .selectItemInFolder("Deploy Configurations", deployConfigName);

        repositoryPage.getDeployConfigurationTabsComponent()
                .openProjectsToDeployTab()
                .addProject("1", revision1);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Deploy Configurations")
                .selectItemInFolder("Deploy Configurations", deployConfigName);
        repositoryPage.getDeployConfigurationTabsComponent()
                .openProjectsToDeployTab()
                .addProject("2", revision2);
        repositoryPage.getRepositoryContentButtonsPanelComponent().saveDeploy();
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Deploy Configurations")
                .selectItemInFolder("Deploy Configurations", deployConfigName);
        
        assertThat(repositoryPage.getRepositoryContentButtonsPanelComponent().isDeployButtonEnabled())
            .as("Deploy button should be enabled after saving the configuration")
            .isTrue();

        repositoryPage.refresh();
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Deploy Configurations")
                .selectItemInFolder("Deploy Configurations", deployConfigName);
    }
}