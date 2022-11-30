package configuration.appcontainer;

import org.testcontainers.containers.Network;

public class AppContainerPool {
    private static final ThreadLocal<AppContainerData> threadLocalAppContainer = new ThreadLocal<AppContainerData>();

    public static void setAppContainer(String containerName, Network network) {
        if(threadLocalAppContainer.get() == null)
            threadLocalAppContainer.set(AppContainerFactory.createContainer(containerName, network));
    }

    public static void closeAppContainer() {
        threadLocalAppContainer.get().getAppContainer().stop();
        threadLocalAppContainer.remove();
    }

    public static AppContainerData get() {
        return threadLocalAppContainer.get();
    }
}
