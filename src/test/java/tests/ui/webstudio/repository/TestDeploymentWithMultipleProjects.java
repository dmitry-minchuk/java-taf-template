package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.RepositoriesPageComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.repositorytabcomponents.DeployConfigurationTabsComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.StringUtil;
import org.testng.Assert;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TestDeploymentWithMultipleProjects extends BaseTest {

    private static final int AMOUNT_OF_PROJECTS = 10;

    @Test
    @TestCaseId("EPBDS-10950")
    @Description("Test deployment configuration with multiple projects - create, add, validate and remove projects from deployment")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testDeploymentWithMultipleProjects() {
        String deployConfigName = StringUtil.generateUniqueName("DeployConfig");
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        List<String> addedProjects = new ArrayList<>();
        generateProjects(editorPage, addedProjects);
        addDeploymentRepository(editorPage);

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createDeployConfiguration(deployConfigName);

        DeployConfigurationTabsComponent deployConfig = repositoryPage.getDeployConfigurationTabsComponent();
        deployConfig.openProjectsToDeployTab();

        validateProjectsListWhileAddingProjects(deployConfig, addedProjects);
        validateProjectsListWhileDeletingProjects(deployConfig, addedProjects);
    }

    private void addDeploymentRepository(EditorPage editorPage) {
        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        RepositoriesPageComponent repositoriesPage = adminPage.navigateToRepositoriesPage();
        repositoriesPage.createH2DeploymentRepository(User.ADMIN);
    }

    private void generateProjects(EditorPage editorPage, List<String> addedProjects) {
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        for (int i = 0; i < AMOUNT_OF_PROJECTS; i++) {
            String projectName = StringUtil.generateUniqueName("TestDeploymentWithMultipleProjects");
            repositoryPage.createProjectFromTemplate(projectName, "Example 1 - Bank Rating");
            addedProjects.add(projectName);
        }
    }

    private void validateProjectsListWhileAddingProjects(DeployConfigurationTabsComponent deployConfig, List<String> addedProjects) {
        for (String projectName : addedProjects) {
            deployConfig.addProjectToDeploy(projectName);
            deployConfig.openProjectsToDeployTab();
            List<String> visibleProjects = deployConfig.getVisibleProjectsInDeployList();
            Assert.assertTrue(visibleProjects.containsAll(List.of(projectName)),
                "Added project should be visible in deployment list: " + projectName);
        }
    }

    private void validateProjectsListWhileDeletingProjects(DeployConfigurationTabsComponent deployConfig, List<String> addedProjects) {
        Iterator<String> iterator = addedProjects.iterator();
        while (iterator.hasNext()) {
            String projectName = iterator.next();
            deployConfig.removeProjectFromDeploy(projectName);
            iterator.remove();
            deployConfig.openProjectsToDeployTab();
            List<String> visibleProjects = deployConfig.getVisibleProjectsInDeployList();
            Assert.assertFalse(visibleProjects.contains(projectName),
                "Removed project should not be visible in deployment list: " + projectName);
        }
    }
}