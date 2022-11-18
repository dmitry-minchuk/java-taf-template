package configuration.listeners;

import configuration.ProjectConfiguration;
import configuration.PropertyNameSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;

public class TestEventsHandler implements ITestListener, ISuiteListener {
    protected static final Logger LOGGER = LogManager.getLogger(TestEventsHandler.class);

    @Override
    public void onStart(ISuite suite) {
        LOGGER.info("Environment: " + ProjectConfiguration.getProperty(PropertyNameSpace.ENV));
    }
}
