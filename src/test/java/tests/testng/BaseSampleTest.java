package tests.testng;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.logevents.SelenideLogger;
import configuration.domain.PropertyNameSpace;
import configuration.listeners.SelenideListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeSuite;
import configuration.ProjectConfiguration;

public abstract class BaseSampleTest {
    protected static final Logger LOGGER = LogManager.getLogger(BaseSampleTest.class);

    @BeforeSuite
    public void setConfiguration() {
        SelenideLogger.addListener("SelenideLogger", new SelenideListener());
        if(System.getProperty(PropertyNameSpace.SELENIDE_REMOTE.getValue()) == null) {
            Configuration.remote = ProjectConfiguration.getProperty(PropertyNameSpace.SELENIUM_HOST);
        }
        Configuration.timeout = Long.parseLong(ProjectConfiguration.getProperty(PropertyNameSpace.IMPLICIT_TIMEOUT));

        LOGGER.info("selenide.remote: " + Configuration.remote);
        LOGGER.info("selenide.baseUrl: " + Configuration.baseUrl);
        LOGGER.info("Environment: " + ProjectConfiguration.getProperty(PropertyNameSpace.ENV));
    }
}
