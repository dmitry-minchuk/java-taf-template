package configuration.driver;

import configuration.ProjectConfiguration;
import configuration.PropertyNameSpace;
import configuration.listeners.DriverEventLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.chrome.ChromeDriverLogLevel;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.AbstractDriverOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringDecorator;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.Network;

public class DriverFactory {
    protected static final Logger LOGGER = LogManager.getLogger(DriverFactory.class);
    private final static Integer VNC_PORT = 5900;

    public static ContainerizedDriver getContainerizedDriver(Network network) {
        DriverEventLogger driverEventLogger = new DriverEventLogger();
        String driverName = ProjectConfiguration.getProperty(PropertyNameSpace.BROWSER);
        ContainerizedDriver containerizedDriver;
        switch (driverName) {
            case "chrome":
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.setLogLevel(ChromeDriverLogLevel.ALL);
                BrowserWebDriverContainer<?> chromeContainer = buildContainer(chromeOptions, network);
                containerizedDriver = new ContainerizedDriver(new EventFiringDecorator(driverEventLogger).decorate(new RemoteWebDriver(chromeContainer.getSeleniumAddress(), new ChromeOptions())), chromeContainer);
                return containerizedDriver;
            case "firefox":
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.setLogLevel(FirefoxDriverLogLevel.INFO);
                BrowserWebDriverContainer<?> firefoxContainer = buildContainer(firefoxOptions, network);
                containerizedDriver = new ContainerizedDriver(new EventFiringDecorator(driverEventLogger).decorate(new RemoteWebDriver(firefoxContainer.getSeleniumAddress(), new FirefoxOptions())), firefoxContainer);
                return containerizedDriver;
        }
        throw new RuntimeException("DriverName unknown: " + driverName);
    }

    private static AbstractDriverOptions<?> addCommonBrowserOptions(AbstractDriverOptions<?> browserOptions) {
        browserOptions.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        return browserOptions;
    }

    private static BrowserWebDriverContainer<?> buildContainer(AbstractDriverOptions<?> browserOptions, Network network) {
        BrowserWebDriverContainer<?> browserContainer = new BrowserWebDriverContainer<>().withCapabilities(addCommonBrowserOptions(browserOptions));
        browserContainer.withNetwork(network);
        browserContainer.addExposedPort(VNC_PORT);
        browserContainer.start();
        LOGGER.info(String.format("Localhost accessible url for selenium VNC: http://localhost:%s", browserContainer.getMappedPort(VNC_PORT)));
        LOGGER.info(String.format("Localhost accessible url for selenium HUB: %s", browserContainer.getSeleniumAddress()));
        return browserContainer;
    }
}
