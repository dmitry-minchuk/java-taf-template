package tests.cucumber;

import com.codeborne.selenide.Configuration;
import domain.PropertyNameSpace;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import org.testng.annotations.BeforeSuite;
import utils.ProjectConfiguration;

public class BaseCucumberTest extends AbstractTestNGCucumberTests {

    @BeforeSuite
    public void setConfiguration() {
        Configuration.remote = ProjectConfiguration.getProperty(PropertyNameSpace.SELENIUM_HOST);
    }
}
