package configuration.driver;

import configuration.ProjectConfiguration;
import configuration.PropertyNameSpace;
import configuration.listeners.DriverEventLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.AbstractDriverOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringDecorator;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

import java.util.logging.Level;

public class DriverFactory {
    protected static final Logger LOGGER = LogManager.getLogger(DriverFactory.class);
    private final static Integer VNC_PORT = 7900;

    public static ContainerizedDriver getContainerizedDriver(Network network) {
        DriverEventLogger driverEventLogger = new DriverEventLogger();
        String driverName = ProjectConfiguration.getProperty(PropertyNameSpace.BROWSER);
        ContainerizedDriver containerizedDriver;
        switch (driverName) {
            case "chrome":
                ChromeOptions chromeOptions = new ChromeOptions();
                LoggingPreferences logs = new LoggingPreferences();
                logs.enable(LogType.BROWSER, Level.ALL);
                logs.enable(LogType.PERFORMANCE, Level.INFO);
                chromeOptions.setCapability("goog:loggingPrefs", logs);
                BrowserWebDriverContainer<?> chromeContainer = buildContainer("selenium/standalone-chromium:latest", chromeOptions, network);
                containerizedDriver = new ContainerizedDriver(new EventFiringDecorator(driverEventLogger).decorate(new RemoteWebDriver(chromeContainer.getSeleniumAddress(), new ChromeOptions())), chromeContainer);
                return containerizedDriver;
            case "firefox":
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.setLogLevel(FirefoxDriverLogLevel.INFO);
                BrowserWebDriverContainer<?> firefoxContainer = buildContainer("selenium/standalone-firefox:latest", firefoxOptions, network);
                containerizedDriver = new ContainerizedDriver(new EventFiringDecorator(driverEventLogger).decorate(new RemoteWebDriver(firefoxContainer.getSeleniumAddress(), new FirefoxOptions())), firefoxContainer);
                return containerizedDriver;
        }
        throw new RuntimeException("DriverName unknown: " + driverName);
    }

    private static AbstractDriverOptions<?> addCommonBrowserOptions(AbstractDriverOptions<?> browserOptions) {
        browserOptions.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        return browserOptions;
    }

    private static BrowserWebDriverContainer<?> buildContainer(String browserImageName, AbstractDriverOptions<?> browserOptions, Network network) {
        DockerImageName dockerImageName = DockerImageName.parse(browserImageName)
                .asCompatibleSubstituteFor("selenium/standalone-chrome");
        BrowserWebDriverContainer<?> browserContainer = new BrowserWebDriverContainer<>(dockerImageName)
                .withCapabilities(addCommonBrowserOptions(browserOptions));
        browserContainer.withNetwork(network);
        browserContainer.addExposedPort(VNC_PORT);
        browserContainer.withEnv("SE_VNC_NO_PASSWORD", "1");
        browserContainer.start();
        LOGGER.info(String.format("Localhost accessible url for selenium VNC: http://localhost:%s", browserContainer.getMappedPort(VNC_PORT)));
        LOGGER.info(String.format("Localhost accessible url for selenium HUB: %s", browserContainer.getSeleniumAddress()));
        return browserContainer;
    }
}
