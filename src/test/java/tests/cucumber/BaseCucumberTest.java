package tests.cucumber;

import com.codeborne.selenide.Configuration;
import domain.PropertyName;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import utils.PropertyHandler;

public class BaseCucumberTest extends AbstractTestNGCucumberTests {
    {
        Configuration.remote = PropertyHandler.getProperty(PropertyName.SELENIUM_HOST);
    }
}
