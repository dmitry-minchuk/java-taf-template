package tests;

import configuration.driver.ContainerizedDriverPool;
import configuration.listeners.TestEventsHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

@Listeners(TestEventsHandler.class)
public abstract class ContainerizedBaseTest {
    protected static final Logger LOGGER = LogManager.getLogger(ContainerizedBaseTest.class);

    @BeforeMethod
    public void beforeMethod() {
        ContainerizedDriverPool.setDriver();
    }

    @AfterMethod
    public void afterMethod() {
        ContainerizedDriverPool.closeDriver();
    }

    protected WebDriver getDriver() {
        return ContainerizedDriverPool.getDriver();
    }
}
