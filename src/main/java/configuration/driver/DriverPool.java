package configuration.driver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testcontainers.containers.Network;

public class DriverPool {
    protected static final Logger LOGGER = LogManager.getLogger(DriverPool.class);
    private static final ThreadLocal<ContainerizedDriver> threadLocalDriver = new ThreadLocal<ContainerizedDriver>();

    public static void setDriver(Network network) {
        if(threadLocalDriver.get() == null)
            threadLocalDriver.set(DriverFactory.getContainerizedDriver(network));
    }

    public static void closeDriver() {
        threadLocalDriver.get().getDriver().quit();
        threadLocalDriver.get().getDriverContainer().stop();
        threadLocalDriver.remove();
    }

    public static WebDriver getDriver() {
        return threadLocalDriver.get().getDriver();
    }

    public static ContainerizedDriver getDriverContainer() {
        return threadLocalDriver.get();
    }

}
