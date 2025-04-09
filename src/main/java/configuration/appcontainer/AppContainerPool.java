package configuration.appcontainer;

import org.testcontainers.containers.Network;

import java.util.Map;

public class AppContainerPool {
    private static final ThreadLocal<AppContainerData> threadLocalAppContainer = new ThreadLocal<AppContainerData>();

    public static void setAppContainer(String containerName,
                                       Network network,
                                       Map<String, String> envVars,
                                       String copyFileFromPath,
                                       String copyFileToContainerPath) {
        if(threadLocalAppContainer.get() == null)
            threadLocalAppContainer.set(AppContainerFactory.createContainer(containerName, network, envVars, copyFileFromPath, copyFileToContainerPath));
    }

    public static void closeAppContainer() {
        threadLocalAppContainer.get().getAppContainer().stop();
        threadLocalAppContainer.remove();
    }

    public static AppContainerData get() {
        return threadLocalAppContainer.get();
    }
}
