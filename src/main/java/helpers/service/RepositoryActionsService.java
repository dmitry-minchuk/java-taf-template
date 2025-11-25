package helpers.service;

import configuration.appcontainer.AppContainerPool;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.repositorytabcomponents.LeftRepositoryTreeComponent;
import domain.ui.webstudio.components.repositorytabcomponents.RepositoryContentButtonsPanelComponent;
import domain.ui.webstudio.components.repositorytabcomponents.RepositoryContentTabPropertiesComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.Container;

import java.io.IOException;
import java.util.List;

public class RepositoryActionsService {
    private static final Logger LOGGER = LogManager.getLogger(RepositoryActionsService.class);
    public static final String PROJECT_STANDARD_NAME = "Empty Project";

    public static void eraseAllProjectsFromRemoteGitRepository() {
        try {
            LoginService loginService = new LoginService(LocalDriverPool.getPage());
            EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));
            RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                    .selectTab(domain.ui.webstudio.components.common.TabSwitcherComponent.TabName.REPOSITORY);

            LOGGER.info("Started erasing all projects from remote git repository");

            List<String> visibleProjects = repositoryPage.getAllVisibleProjectsInTable();

            if (!visibleProjects.isEmpty()) {
                for (String projectName : visibleProjects) {
                    LOGGER.info("Processing project: {}", projectName);
                    repositoryPage.getLeftRepositoryTreeComponent().selectProjectInTree(projectName);

                    List<String> branchesToDelete = getBranchesForProject(repositoryPage, projectName);
                    branchesToDelete.remove("master");

                    for (String branchName : branchesToDelete) {
                        LOGGER.info("Attempting to delete branch: {}", branchName);
                        deleteProjectBranch(repositoryPage, projectName, branchName);
                        repositoryPage.getLeftRepositoryTreeComponent().selectProjectInTree(projectName);
                    }

                    deleteProject(repositoryPage, projectName);
                }
            }

            repositoryPage.refresh();
            WaitUtil.sleep(1000, "Waiting for repository to refresh");

            List<String> visibleProjectsRevealed = repositoryPage.getAllVisibleProjectsInTable();

            if (!visibleProjectsRevealed.isEmpty()) {
                for (String projectName : visibleProjectsRevealed) {
                    LOGGER.info("Erasing project: {}", projectName);
                    repositoryPage.getLeftRepositoryTreeComponent().selectProjectInTree(projectName);
                    eraseProject(repositoryPage, projectName);
                }
            }

            LOGGER.info("Successfully erased all projects from remote git repository");
        } catch (Exception e) {
            LOGGER.error("Error while erasing projects from git repository", e);
            throw new RuntimeException("Failed to erase projects", e);
        }
    }

    private static List<String> getBranchesForProject(RepositoryPage repositoryPage, String projectName) {
        try {
            repositoryPage.getLeftRepositoryTreeComponent().selectProjectInTree(projectName);
            RepositoryContentTabPropertiesComponent propertiesComponent = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();

            String currentBranch = propertiesComponent.getProperty(
                    RepositoryContentTabPropertiesComponent.Property.REPOSITORY);
            LOGGER.info("Current branch for project {}: {}", projectName, currentBranch);

            return List.of(currentBranch != null ? currentBranch : "master");
        } catch (Exception e) {
            LOGGER.warn("Error getting branches for project {}: {}", projectName, e.getMessage());
            return List.of("master");
        }
    }

    private static void deleteProjectBranch(RepositoryPage repositoryPage, String projectName, String branchName) {
        try {
            deleteRemoteGitBranchInsideContainer(branchName);
            LOGGER.info("Successfully deleted branch {} for project {}", branchName, projectName);
        } catch (Exception e) {
            LOGGER.warn("Error deleting branch {} via Git: {}", branchName, e.getMessage());
            try {
                deleteLocalGitBranchInsideContainer(branchName);
                LOGGER.info("Fallback: deleted local branch {}", branchName);
            } catch (Exception e2) {
                LOGGER.error("Failed to delete branch {} via both methods", branchName, e2);
            }
        }
    }

    private static void deleteProject(RepositoryPage repositoryPage, String projectName) {
        try {
            RepositoryContentButtonsPanelComponent buttonsPanel = repositoryPage.getRepositoryContentButtonsPanelComponent();
            buttonsPanel.clickDeleteBtn();
            confirmDeleteAction(repositoryPage);
            LOGGER.info("Successfully deleted project: {}", projectName);
        } catch (Exception e) {
            LOGGER.warn("Error deleting project {}: {}", projectName, e.getMessage());
        }
    }

    private static void eraseProject(RepositoryPage repositoryPage, String projectName) {
        try {
            RepositoryContentButtonsPanelComponent buttonsPanel = repositoryPage.getRepositoryContentButtonsPanelComponent();
            buttonsPanel.clickEraseBtn();
            confirmDeleteAction(repositoryPage);
            LOGGER.info("Successfully erased project: {}", projectName);
        } catch (Exception e) {
            LOGGER.warn("Error erasing project {}: {}", projectName, e.getMessage());
        }
    }

    private static void confirmDeleteAction(RepositoryPage repositoryPage) {
        WaitUtil.sleep(500, "Waiting for delete action to complete");
    }

    private static void deleteRemoteGitBranchInsideContainer(String branchName) throws IOException, InterruptedException {
        if (AppContainerPool.get() != null && AppContainerPool.get().getAppContainer() != null) {
            String command = String.format("cd /workspace ; git push origin --delete %s 2>&1 || true", branchName);
            Container.ExecResult result = AppContainerPool.get().getAppContainer()
                    .execInContainer("sh", "-c", command);
            LOGGER.info("Git delete remote branch exit code: {}", result.getExitCode());
        }
    }

    private static void deleteLocalGitBranchInsideContainer(String branchName) throws IOException, InterruptedException {
        if (AppContainerPool.get() != null && AppContainerPool.get().getAppContainer() != null) {
            String command = String.format("cd /workspace ; git branch --delete --force %s 2>&1 || true", branchName);
            Container.ExecResult result = AppContainerPool.get().getAppContainer()
                    .execInContainer("sh", "-c", command);
            LOGGER.info("Git delete local branch exit code: {}", result.getExitCode());
        }
    }
}
