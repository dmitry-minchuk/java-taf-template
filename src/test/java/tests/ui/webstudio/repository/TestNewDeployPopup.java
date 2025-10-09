package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.RepositoriesPageComponent;
import domain.ui.webstudio.components.common.MessageComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.repositorytabcomponents.DeployModalComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNewDeployPopup extends BaseTest { // This is not realistic to automate yet because we cannot run compose file

    //@Test
    @TestCaseId("IPBQA-32875")
    @Description("Test new deploy popup with mandatory field validation and deployment configuration creation")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testNewDeployPopup() {
        // Step 1: Login as admin and navigate to Repository
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        // Step 2: Create project "Example 1 - Bank Rating"
        String projectName = "TestProjectForDeploy";
        repositoryPage.createProjectFromTemplate(projectName, "Example 1 - Bank Rating");
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);

        // Step 3: Check Deploy button availability
        assertThat(repositoryPage.getRepositoryContentButtonsPanelComponent().isDeployButtonEnabled())
            .as("Deploy button should be available after project creation")
            .isTrue();

        // Add Deployment Repository (prerequisite for deployment)
        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        RepositoriesPageComponent repositoriesPage = adminPage.navigateToRepositoriesPage();
        repositoriesPage.createH2DeploymentRepository(User.ADMIN);
        repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", projectName);

        // Step 4: Try to Deploy without mandatory fields
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickDeploy();
        DeployModalComponent deployModal = repositoryPage.getDeployModalComponent();

        // Verify modal appeared
        assertThat(deployModal.isModalVisible())
            .as("Deploy modal should appear when clicking Deploy button")
            .isTrue();
        deployModal.deployWithoutMandatoryFields();

        boolean validationWorked = deployModal.isErrorMessageDisplayed() || deployModal.isModalVisible();
        assertThat(validationWorked)
            .as("Deployment should not proceed without mandatory fields - either error shown or modal remains open")
            .isTrue();
        LOGGER.info("Validation: deployment blocked without mandatory fields");

        // Step 5: Fill required fields and Deploy
        String deploymentRepository = "repo"; // This might need adjustment based on actual repo name
        String deploymentName = "Test";
        String deploymentComment = "Test";

        // If modal is not visible (was closed), click Deploy again
        if (!deployModal.isModalVisible()) {
            repositoryPage.getRepositoryContentButtonsPanelComponent().clickDeploy();
            deployModal.waitForModal();
        }

        deployModal.deployWithAllFields(deploymentRepository, deploymentName, deploymentComment);
        LOGGER.info("Deployment form filled and submitted");

        // Step 6: Verify deployment configuration was created
        MessageComponent messageComponent = new MessageComponent();
        boolean messageDisplayed = messageComponent.getMessageText() != null && !messageComponent.getMessageText().isEmpty();
        if (messageDisplayed) {
            LOGGER.info("Success message displayed: {}", messageComponent.getMessageText());
        }

        // Navigate to Deployment Configurations in the tree and verify "Test" deployment exists
        repositoryPage.refresh();
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Deploy Configurations");

        boolean deploymentExists = repositoryPage.getLeftRepositoryTreeComponent()
                .isItemExistsInTree(deploymentName);

        assertThat(deploymentExists)
            .as("Deployment configuration '%s' should be present in Deploy Configurations folder", deploymentName)
            .isTrue();
        LOGGER.info("Deployment configuration '{}' successfully created and visible in tree", deploymentName);

        // Additional verification: Select deployment and verify it opens
        repositoryPage.getLeftRepositoryTreeComponent()
                .selectItemInFolder("Deploy Configurations", deploymentName);
        LOGGER.info("Successfully selected deployment configuration in tree");
    }
}
