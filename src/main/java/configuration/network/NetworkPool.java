package configuration.network;

import configuration.driver.DriverPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.Network;

public class NetworkPool {
    protected static final Logger LOGGER = LogManager.getLogger(DriverPool.class);
    private static final ThreadLocal<Network> threadLocalNetwork = new ThreadLocal<Network>();

    public static void setNetwork(Network network) {
        if(threadLocalNetwork.get() == null)
            threadLocalNetwork.set(network);
    }

    public static void closeNetwork() {
        threadLocalNetwork.get().close();
        threadLocalNetwork.remove();
    }

    public static Network getDriver() {
        return threadLocalNetwork.get();
    }
}
