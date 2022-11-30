package tests;

import configuration.appcontainer.AppContainerPool;
import configuration.driver.DriverPool;
import helpers.utils.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.Network;
import org.testng.annotations.*;

public abstract class BaseTest {
    protected static final Logger LOGGER = LogManager.getLogger(BaseTest.class);

    @BeforeMethod
    public void beforeMethod() {
        Network network = Network.newNetwork();
        AppContainerPool.setAppContainer(StringUtil.generateUniqueName(), network);
        DriverPool.setDriver(network);
    }

    @AfterMethod
    public void afterMethod() {
        DriverPool.closeDriver();
        AppContainerPool.closeAppContainer();
    }
}
