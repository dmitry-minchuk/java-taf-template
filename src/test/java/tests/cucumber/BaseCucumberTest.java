package tests.cucumber;

import com.codeborne.selenide.Configuration;
import domain.PropertyNameSpace;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import org.apache.log4j.PropertyConfigurator;
import org.testng.annotations.BeforeSuite;
import utils.ProjectConfiguration;

public class BaseCucumberTest extends AbstractTestNGCucumberTests {

    @BeforeSuite
    public void setConfiguration() {
        PropertyConfigurator.configure("src/main/resources/log4j.properties");
        Configuration.remote = ProjectConfiguration.getProperty(PropertyNameSpace.SELENIUM_HOST);
        Configuration.timeout = Long.parseLong(ProjectConfiguration.getProperty(PropertyNameSpace.IMPLICIT_TIMEOUT));
    }
}
