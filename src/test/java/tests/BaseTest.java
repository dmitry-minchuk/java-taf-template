package tests;

import configuration.driver.DriverFactory;
import configuration.listeners.TestEventsHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

@Listeners(TestEventsHandler.class)
public abstract class BaseTest {
    protected static final Logger LOGGER = LogManager.getLogger(BaseTest.class);
    private final ThreadLocal<WebDriver> threadLocalDriver = new ThreadLocal<WebDriver>();

    @BeforeMethod
    public void beforeMethod() {
        if(threadLocalDriver.get() == null)
            threadLocalDriver.set(DriverFactory.getDriver());
    }

    @AfterMethod
    public void afterMethod() {
        threadLocalDriver.get().quit();
        threadLocalDriver.remove();
    }

    protected WebDriver getDriver() {
        return threadLocalDriver.get();
    }
}
