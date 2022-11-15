package tests;

import configuration.ProjectConfiguration;
import configuration.domain.PropertyNameSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

public abstract class BaseTest {
    protected static final Logger LOGGER = LogManager.getLogger(BaseTest.class);

    @BeforeSuite
    public void setConfiguration() {
        LOGGER.info("Environment: " + ProjectConfiguration.getProperty(PropertyNameSpace.ENV));
    }

    @BeforeMethod
    public void methodSpecificConfiguration() {
    }

    @AfterMethod
    public void methodSpecificConfigurationCleanUp() {
    }
}
