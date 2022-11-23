package configuration.driver;

import configuration.ProjectConfiguration;
import configuration.PropertyNameSpace;
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
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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

    public static ContainerizedDriver getContainerizedDriver() { // create browsers by test-containers (losing logs for driver actions for unknown reason)
        DriverEventLogger driverEventLogger = new DriverEventLogger();
        String driverName = ProjectConfiguration.getProperty(PropertyNameSpace.BROWSER);
        ContainerizedDriver containerizedDriver;
        List<String> seleniumPortBindings = new ArrayList<>();
        seleniumPortBindings.add("4444:4444"); // for debugging purposes
        seleniumPortBindings.add("5900:5900"); // for debugging purposes
        switch (driverName) {
            case "chrome":
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.setLogLevel(ChromeDriverLogLevel.ALL);
                BrowserWebDriverContainer<?> chromeContainer = new BrowserWebDriverContainer<>().withCapabilities(chromeOptions);
                chromeContainer.setPortBindings(seleniumPortBindings);
                chromeContainer.withNetwork(DockerNetwork.getNetwork());
                chromeContainer.start();
                containerizedDriver = new ContainerizedDriver(new EventFiringDecorator(driverEventLogger).decorate(new RemoteWebDriver(chromeContainer.getSeleniumAddress(), new ChromeOptions())), chromeContainer);
                return containerizedDriver;
            case "firefox":
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.setLogLevel(FirefoxDriverLogLevel.INFO);
                BrowserWebDriverContainer<?> firefoxContainer = new BrowserWebDriverContainer<>().withCapabilities(firefoxOptions);
                firefoxContainer.setPortBindings(seleniumPortBindings);
                firefoxContainer.withNetwork(DockerNetwork.getNetwork());
                firefoxContainer.start();
                containerizedDriver = new ContainerizedDriver(new EventFiringDecorator(driverEventLogger).decorate(new RemoteWebDriver(firefoxContainer.getSeleniumAddress(), new FirefoxOptions())), firefoxContainer);
                return containerizedDriver;
        }
        throw new RuntimeException("DriverName unknown: " + driverName);
    }

//    public static ContainerizedDriver getContainerizedDriver() { // create browsers by myself (losing logs for driver actions for unknown reason)
//        DriverEventLogger driverEventLogger = new DriverEventLogger();
//        String driverName = ProjectConfiguration.getProperty(PropertyNameSpace.BROWSER);
//        ContainerizedDriver containerizedDriver;
//        switch (driverName) {
//            case "chrome":
//                ChromeOptions chromeOptions = new ChromeOptions();
//                chromeOptions.setLogLevel(ChromeDriverLogLevel.ALL);
//
//                GenericContainer<?> selenium = new GenericContainer<>(DockerImageName.parse("selenium/standalone-chrome:latest"));
//                selenium.addExposedPorts(4444); // have to set this explicitly since java is starting container and should have access there from inside
//                selenium.start();
//                String seleniumHost = String.format("http://%s:%s/wd/hub", selenium.getHost(), selenium.getMappedPort(4444));
//
//                containerizedDriver = new ContainerizedDriver(new EventFiringDecorator(driverEventLogger).decorate(new RemoteWebDriver(getSeleniumHost(), addCommonBrowserOptions(chromeOptions))), selenium);
//                return containerizedDriver;
//        }
//        throw new RuntimeException("DriverName unknown: " + driverName);
//    }

    private static AbstractDriverOptions<?> addCommonBrowserOptions(AbstractDriverOptions<?> browserOptions) {
        browserOptions.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        return browserOptions;
    }

    private static URL getSeleniumHost(String seleniumHost) {
        try {
            return new URL(seleniumHost);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    private static URL getSeleniumHost() {
        return getSeleniumHost(ProjectConfiguration.getProperty(PropertyNameSpace.SELENIUM_HOST));
    }

}
