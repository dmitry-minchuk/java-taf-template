package configuration.driver;

import org.openqa.selenium.WebDriver;

public class ContainerizedDriverPool {
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
