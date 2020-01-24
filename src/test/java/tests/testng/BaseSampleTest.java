package tests.testng;

import com.codeborne.selenide.Configuration;
import domain.PropertyNameSpace;
import org.testng.annotations.BeforeSuite;
import utils.ProjectConfiguration;

public abstract class BaseSampleTest {

    @BeforeSuite
    public void setConfiguration() {
        Configuration.remote = ProjectConfiguration.getProperty(PropertyNameSpace.SELENIUM_HOST);
        Configuration.timeout = Long.parseLong(ProjectConfiguration.getProperty(PropertyNameSpace.IMPLICIT_TIMEOUT));
    }
}
