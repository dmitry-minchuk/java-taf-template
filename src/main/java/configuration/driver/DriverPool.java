package configuration.driver;

import org.openqa.selenium.WebDriver;

public class DriverPool {
    private static final ThreadLocal<WebDriver> threadLocalDriver = new ThreadLocal<WebDriver>();

    public static void setDriver() {
        if(threadLocalDriver.get() == null)
            threadLocalDriver.set(DriverFactory.getDriver());
    }

    public static void closeDriver() {
        threadLocalDriver.get().quit();
        threadLocalDriver.remove();
    }

    public static WebDriver getDriver() {
        return threadLocalDriver.get();
    }

}
