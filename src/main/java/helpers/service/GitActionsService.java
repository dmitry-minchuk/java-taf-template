package helpers.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.RefSpec;

import java.util.ArrayList;
import java.util.List;

public class GitActionsService {
    private static final Logger LOGGER = LogManager.getLogger(GitActionsService.class);

    public static void deleteRemoteBranchDirect(GitRemote remote, String branchName) {
        try {
            LOGGER.info("Deleting remote branch directly: {} on {}", branchName, remote.url());

            try (Repository repository = new InMemoryRepository(new DfsRepositoryDescription("temp"));
                 Git git = new Git(repository)) {

                RefSpec refSpec = new RefSpec()
                        .setSource(null)
                        .setDestination("refs/heads/" + branchName);

                git.push()
                        .setRemote(remote.url())
                        .setRefSpecs(refSpec)
                        .setCredentialsProvider(remote.credentials())
                        .call();

                LOGGER.info("Remote branch {} deleted successfully", branchName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete remote branch: " + branchName, e);
        }
    }

    public static void deleteAllRemoteBranchesExceptMaster(GitRemote remote) {
        try {
            LOGGER.info("Deleting all remote branches except master via JGit on {}", remote.url());

            List<String> branchesToDelete = getAllRemoteBranches(remote);
            branchesToDelete.remove("master");

            LOGGER.info("Found {} branches to delete (excluding master)", branchesToDelete.size());

            for (String branchName : branchesToDelete) {
                try {
                    LOGGER.info("Deleting remote branch: {}", branchName);
                    deleteRemoteBranchDirect(remote, branchName);
                } catch (RuntimeException e) {
                    LOGGER.warn("Failed to delete branch {}: {}", branchName, e.getMessage());
                }
            }

            LOGGER.info("Successfully deleted all remote branches except master");
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete remote branches", e);
        }
    }

    private static List<String> getAllRemoteBranches(GitRemote remote) throws Exception {
        List<String> branches = new ArrayList<>();

        try (Repository repository = new InMemoryRepository(new DfsRepositoryDescription("temp"));
             Git tempGit = new Git(repository)) {

            tempGit.lsRemote()
                    .setRemote(remote.url())
                    .setCredentialsProvider(remote.credentials())
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
