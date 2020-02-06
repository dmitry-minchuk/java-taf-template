package tests.testng;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import domain.PropertyNameSpace;
import io.qameta.allure.Allure;
import io.qameta.allure.selenide.AllureSelenide;
import io.qameta.allure.selenide.LogType;
import io.qameta.allure.util.PropertiesUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.maven.plugin.lifecycle.LifecycleConfiguration;
import org.testng.annotations.BeforeSuite;
import utils.ProjectConfiguration;

import java.util.logging.Level;

public abstract class BaseSampleTest {
    protected static final Logger LOGGER = LogManager.getLogger(BaseSampleTest.class);

    @BeforeSuite
    public void setConfiguration() {
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide().enableLogs(LogType.DRIVER, Level.ALL));
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide().screenshots(true).savePageSource(false));
        PropertyConfigurator.configure("src/main/resources/log4j.properties");
        if(System.getProperty(PropertyNameSpace.SELENIDE_REMOTE.getValue()) == null) {
            Configuration.remote = ProjectConfiguration.getProperty(PropertyNameSpace.SELENIUM_HOST);
        }
        Configuration.timeout = Long.parseLong(ProjectConfiguration.getProperty(PropertyNameSpace.IMPLICIT_TIMEOUT));
        
        LOGGER.info("allure.results.directory: " + PropertiesUtils.loadAllureProperties().getProperty("allure.results.directory"));
        LOGGER.info("selenide.remote: " + Configuration.remote);
        LOGGER.info("selenide.url: " + System.getProperty("selenide.url"));
        LOGGER.info("selenide.baseUrl: " + Configuration.baseUrl);
    }
}
