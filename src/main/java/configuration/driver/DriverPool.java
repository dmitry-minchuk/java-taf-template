package configuration.driver;

import configuration.ProjectConfiguration;
import configuration.appcontainer.AppContainerPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

public class DriverPool {
    protected static final Logger LOGGER = LogManager.getLogger(DriverPool.class);
    private static final ThreadLocal<ContainerizedDriver> threadLocalDriver = new ThreadLocal<ContainerizedDriver>();

    public static void setDriver() {
        if(threadLocalDriver.get() == null)
            threadLocalDriver.set(DriverFactory.getContainerizedDriver());
    }

    public static void closeDriver() {
        threadLocalDriver.get().getDriver().quit();
        threadLocalDriver.get().getDriverContainer().stop();
        threadLocalDriver.remove();
    }

    public static WebDriver getDriver() {
        return threadLocalDriver.get().getDriver();
    }

}
