package helpers.service;

import configuration.appcontainer.AppContainerData;
import configuration.appcontainer.AppContainerPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.MountableFile;

public class AppContainerFileService {

    private static final Logger LOGGER = LogManager.getLogger(AppContainerFileService.class);

    // Default WebStudio image stores user workspaces under /opt/openl/shared/user-workspace/<user>/<projectName>/.
    public static final String USER_WORKSPACE_BASE = "/opt/openl/shared/user-workspace";

    public static String userWorkspaceProjectPath(String userName, String projectName) {
        return USER_WORKSPACE_BASE + "/" + userName + "/" + projectName;
    }

    public static void copyFileToProjectWorkspace(String userName, String projectName, String hostFilePath, String fileNameInProject) {
        String containerPath = userWorkspaceProjectPath(userName, projectName) + "/" + fileNameInProject;
        copyFileToContainer(hostFilePath, containerPath);
    }

    public static void copyFileToContainer(String hostFilePath, String containerPath) {
        AppContainerData data = AppContainerPool.get();
        if (data == null) {
            throw new IllegalStateException("App container is not initialized — cannot copy file to container");
        }
        GenericContainer<?> container = data.getAppContainer();
        LOGGER.info("Copying host file '{}' into container at '{}'", hostFilePath, containerPath);
        container.copyFileToContainer(MountableFile.forHostPath(hostFilePath, 0666), containerPath);
    }
}
