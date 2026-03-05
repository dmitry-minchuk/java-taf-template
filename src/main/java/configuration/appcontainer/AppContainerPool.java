package configuration.appcontainer;

import helpers.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.Network;

import java.util.Map;

public class AppContainerPool {
    protected static final Logger LOGGER = LogManager.getLogger(AppContainerPool.class);
    private static final ThreadLocal<AppContainerData> threadLocalAppContainer = new ThreadLocal<AppContainerData>();

    public static void setAppContainer(String containerName,
                                       Network network,
                                       Map<String, String> envVars,
                                       Map<String, String> filesToCopy) {
        if(threadLocalAppContainer.get() == null) {
            threadLocalAppContainer.set(AppContainerFactory.createContainer(containerName, network, envVars, filesToCopy));
            if (network != null)
                WaitUtil.sleep(3000, "Wait 3 seconds for DNS propagation in Docker's embedded DNS server");
        }
    }

    public static void closeAppContainer() {
        threadLocalAppContainer.get().getAppContainer().stop();
        threadLocalAppContainer.remove();
    }

    public static AppContainerData get() {
        return threadLocalAppContainer.get();
    }
}
