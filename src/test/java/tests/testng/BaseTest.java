package tests.testng;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import configuration.domain.PropertyNameSpace;
import configuration.listeners.SelenideListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import configuration.ProjectConfiguration;

public abstract class BaseTest {
    protected static final Logger LOGGER = LogManager.getLogger(BaseTest.class);
    private static final String LISTENER_NAME = "SelenideLogger";

    @BeforeSuite
    public void setConfiguration() {
        if(System.getProperty(PropertyNameSpace.SELENIDE_REMOTE.getValue()) == null) {
            Configuration.remote = ProjectConfiguration.getProperty(PropertyNameSpace.SELENIUM_HOST);
        }
        Configuration.browser = ProjectConfiguration.getProperty(PropertyNameSpace.BROWSER);
        Configuration.browserVersion = ProjectConfiguration.getProperty(PropertyNameSpace.BROWSER_VERSION);
        Configuration.timeout = Long.parseLong(ProjectConfiguration.getProperty(PropertyNameSpace.IMPLICIT_TIMEOUT));

        LOGGER.info("selenide.remote: " + Configuration.remote);
        LOGGER.info("selenide.baseUrl: " + Configuration.baseUrl);
        LOGGER.info("Environment: " + ProjectConfiguration.getProperty(PropertyNameSpace.ENV));
    }

    @BeforeMethod
    public void methodSpecificConfiguration() {
        //SelenideLogger is setting listener to current thread only
        SelenideLogger.addListener(LISTENER_NAME, new SelenideListener());
    }

    @AfterMethod
    public void methodSpecificConfigurationCleanUp() {
        //SelenideLogger is deleting listener from current thread just to make sure we don't have redundant listener instances
        SelenideLogger.removeListener(LISTENER_NAME);
    }
}
