package tests;

import configuration.driver.DriverPool;
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

    @BeforeMethod
    public void beforeMethod() {
        DriverPool.setDriver();
    }

    @AfterMethod
    public void afterMethod() {
        DriverPool.closeDriver();
    }

    protected WebDriver getDriver() {
        return DriverPool.getDriver();
    }
}
