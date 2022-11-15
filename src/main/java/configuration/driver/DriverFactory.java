package configuration.driver;

import configuration.ProjectConfiguration;
import configuration.domain.PropertyNameSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.AbstractDriverOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

public class DriverFactory {
    protected static final Logger LOGGER = LogManager.getLogger(DriverFactory.class);

    public static WebDriver getDriver() {
        String driverName = ProjectConfiguration.getProperty(PropertyNameSpace.BROWSER);
        switch (driverName) {
            case "chrome":
                return new RemoteWebDriver(getSeleniumHost(), buildBrowserOptions(new ChromeOptions()));
            case "firefox":
                return new RemoteWebDriver(getSeleniumHost(), buildBrowserOptions(new FirefoxOptions()));
        }
        throw new RuntimeException("DriverName unknown: " + driverName);
    }

    private static AbstractDriverOptions<?> buildBrowserOptions(AbstractDriverOptions<?> browserOptions) {
        browserOptions.setPageLoadStrategy(PageLoadStrategy.NONE);
        return browserOptions;
    }

    private static URL getSeleniumHost() {
        try {
            return new URL(ProjectConfiguration.getProperty(PropertyNameSpace.SELENIUM_HOST));
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
