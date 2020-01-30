package tests.testng;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import domain.PropertyNameSpace;
import io.qameta.allure.selenide.AllureSelenide;
import io.qameta.allure.selenide.LogType;
import org.apache.log4j.PropertyConfigurator;
import org.testng.annotations.BeforeSuite;
import utils.ProjectConfiguration;

import java.util.logging.Level;

public abstract class BaseSampleTest {

    @BeforeSuite
    public void setConfiguration() {
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide().enableLogs(LogType.DRIVER, Level.ALL));
        SelenideLogger.addListener("AllureSelenide", new AllureSelenide().screenshots(true).savePageSource(false));
        PropertyConfigurator.configure("src/main/resources/log4j.properties");
        if(System.getProperty(PropertyNameSpace.SELENIDE_REMOTE.getValue()).isEmpty()) {
            Configuration.remote = ProjectConfiguration.getProperty(PropertyNameSpace.SELENIUM_HOST);
        }
        Configuration.timeout = Long.parseLong(ProjectConfiguration.getProperty(PropertyNameSpace.IMPLICIT_TIMEOUT));
    }
}
