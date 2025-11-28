package helpers.service;

import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GitActionsService {
    private static final Logger LOGGER = LogManager.getLogger(GitActionsService.class);

    public static Git cloneRemoteRepository() throws Exception {
        LOGGER.info("Cloning remote repository");
        String gitUrl = ProjectConfiguration.getProperty(PropertyNameSpace.GIT_URL);
        String login = ProjectConfiguration.getProperty(PropertyNameSpace.GIT_LOGIN);
        String password = ProjectConfiguration.getProperty(PropertyNameSpace.GIT_PASSWORD);

        File tempDir = File.createTempFile("jgit-", ".git");
        tempDir.delete();
        tempDir.mkdir();

        Git git = Git.cloneRepository()
                .setURI(gitUrl)
                .setDirectory(tempDir)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(login, password))
                .call();

        LOGGER.info("Repository cloned to: {}", tempDir.getAbsolutePath());
        return git;
    }

    public static void deleteLocalBranch(Git git, String branchName) throws Exception {
        LOGGER.info("Deleting local branch: {}", branchName);
        git.branchDelete()
                .setBranchNames(branchName)
                .call();
    }

    public static void deleteRemoteBranch(Git git, String branchName) throws Exception {
        LOGGER.info("Deleting remote branch: {}", branchName);
        RefSpec refSpec = new RefSpec()
                .setSource(null)
                .setDestination("refs/heads/" + branchName);

        git.push()
                .setRefSpecs(refSpec)
                .setRemote("origin")
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(
                        ProjectConfiguration.getProperty(PropertyNameSpace.GIT_LOGIN),
                        ProjectConfiguration.getProperty(PropertyNameSpace.GIT_PASSWORD)
                ))
                .call();
    }

    public static void deleteRemoteBranchDirect(String branchName) {
        try {
            LOGGER.info("Deleting remote branch directly: {}", branchName);
            String gitUrl = ProjectConfiguration.getProperty(PropertyNameSpace.GIT_URL);
            String login = ProjectConfiguration.getProperty(PropertyNameSpace.GIT_LOGIN);
            String password = ProjectConfiguration.getProperty(PropertyNameSpace.GIT_PASSWORD);

            try (Repository repository = new InMemoryRepository(new DfsRepositoryDescription("temp"));
                 Git git = new Git(repository)) {

                RefSpec refSpec = new RefSpec()
                        .setSource(null)
                        .setDestination("refs/heads/" + branchName);

                git.push()
                        .setRemote(gitUrl)
                        .setRefSpecs(refSpec)
                        .setCredentialsProvider(new UsernamePasswordCredentialsProvider(login, password))
                        .call();

                LOGGER.info("Remote branch {} deleted successfully", branchName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete remote branch: " + branchName, e);
        }
    }

    public static void deleteLocalRepository(Git git) throws Exception {
        LOGGER.info("Deleting local repository");
        String path = git.getRepository().getDirectory().getAbsolutePath().replace("/.git", "");
        File repository = new File(path);

        int counter = 10;
        while (repository.exists() && counter > 0) {
            counter--;
            try {
                LOGGER.info("Deleting folder: {}", repository.getAbsolutePath());
                org.apache.commons.io.FileUtils.forceDelete(repository);
            } catch (Exception e) {
                LOGGER.warn("Failed to delete folder on attempt {}, retrying...", 11 - counter);
                Thread.sleep(1000);
            }
        }

        if (repository.exists()) {
            LOGGER.warn("Failed to delete repository folder after {} attempts", 10);
        }
    }

    public static void deleteAllRemoteBranchesExceptMaster() {
        try {
            LOGGER.info("Deleting all remote branches except master via JGit");
            String gitUrl = ProjectConfiguration.getProperty(PropertyNameSpace.GIT_URL);
            String login = ProjectConfiguration.getProperty(PropertyNameSpace.GIT_LOGIN);
            String password = ProjectConfiguration.getProperty(PropertyNameSpace.GIT_PASSWORD);

            List<String> branchesToDelete = getAllRemoteBranches(gitUrl, login, password);
            branchesToDelete.remove("master");

            LOGGER.info("Found {} branches to delete (excluding master)", branchesToDelete.size());

            for (String branchName : branchesToDelete) {
                try {
                    LOGGER.info("Deleting remote branch: {}", branchName);
                    deleteRemoteBranchDirect(branchName);
                } catch (RuntimeException e) {
                    LOGGER.warn("Failed to delete branch {}: {}", branchName, e.getMessage());
                }
            }

            LOGGER.info("Successfully deleted all remote branches except master");
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete remote branches", e);
        }
    }

    private static List<String> getAllRemoteBranches(String gitUrl, String login, String password) throws Exception {
        List<String> branches = new ArrayList<>();

        try (Repository repository = new InMemoryRepository(new DfsRepositoryDescription("temp"));
             Git tempGit = new Git(repository)) {

            tempGit.lsRemote()
                    .setRemote(gitUrl)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(login, password))
                    .call()
                    .forEach(ref -> {
                        String refName = ref.getName();
                        if (refName.startsWith("refs/heads/")) {
                            String branchName = refName.replace("refs/heads/", "");
                            branches.add(branchName);
                            LOGGER.debug("Found remote branch: {}", branchName);
                        }
                    });
        }

        return branches;
    }
}
