package configuration.driver;

import configuration.ProjectConfiguration;
import configuration.domain.PropertyNameSpace;
import configuration.listeners.DriverEventLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverLogLevel;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.AbstractDriverOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringDecorator;

import java.net.MalformedURLException;
import java.net.URL;

public class DriverFactory {
    protected static final Logger LOGGER = LogManager.getLogger(DriverFactory.class);

    public static WebDriver getDriver() {
        DriverEventLogger driverEventLogger = new DriverEventLogger();
        String driverName = ProjectConfiguration.getProperty(PropertyNameSpace.BROWSER);
        switch (driverName) {
            case "chrome":
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.setLogLevel(ChromeDriverLogLevel.ALL);
                return new EventFiringDecorator(driverEventLogger).decorate(new RemoteWebDriver(getSeleniumHost(), addCommonBrowserOptions(chromeOptions)));
            case "firefox":
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.setLogLevel(FirefoxDriverLogLevel.INFO);
                return new EventFiringDecorator(driverEventLogger).decorate(new RemoteWebDriver(getSeleniumHost(), addCommonBrowserOptions(firefoxOptions)));
        }
        throw new RuntimeException("DriverName unknown: " + driverName);
    }

    private static AbstractDriverOptions<?> addCommonBrowserOptions(AbstractDriverOptions<?> browserOptions) {
        browserOptions.setPageLoadStrategy(PageLoadStrategy.NORMAL);
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
