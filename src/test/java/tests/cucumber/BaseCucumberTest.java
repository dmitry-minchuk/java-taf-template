package tests.cucumber;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import domain.PropertyNameSpace;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.qameta.allure.selenide.AllureSelenide;
import io.qameta.allure.selenide.LogType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.BeforeSuite;
import utils.ProjectConfiguration;

import java.util.logging.Level;

public class BaseCucumberTest extends AbstractTestNGCucumberTests {
    protected static final Logger LOGGER = LogManager.getLogger(BaseCucumberTest.class);

    @BeforeSuite
    public void setConfiguration() {
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide().enableLogs(LogType.DRIVER, Level.ALL));
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide().screenshots(true).savePageSource(false));
        if(System.getProperty(PropertyNameSpace.SELENIDE_REMOTE.getValue()) == null) {
            Configuration.remote = ProjectConfiguration.getProperty(PropertyNameSpace.SELENIUM_HOST);
        }
        Configuration.timeout = Long.parseLong(ProjectConfiguration.getProperty(PropertyNameSpace.IMPLICIT_TIMEOUT));

        LOGGER.info("selenide.remote: " + Configuration.remote);
        LOGGER.info("selenide.baseUrl: " + Configuration.baseUrl);
    }
}
