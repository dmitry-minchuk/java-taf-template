package tests;

import configuration.appcontainer.AppContainerPool;
import configuration.driver.DriverPool;
import configuration.listeners.TestEventsHandler;
import helpers.utils.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.*;

@Listeners(TestEventsHandler.class)
public abstract class BaseTest {
    protected static final Logger LOGGER = LogManager.getLogger(BaseTest.class);

    @BeforeClass
    public void beforeClass() {
        AppContainerPool.setAppContainer(StringUtil.generateUniqueName());
    }

    @AfterClass
    public void afterClass() {
        AppContainerPool.closeAppContainer();
    }

    @BeforeMethod
    public void beforeMethod() {
        DriverPool.setDriver();
    }

    @AfterMethod
    public void afterMethod() {
        DriverPool.closeDriver();
    }
}
