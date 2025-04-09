package configuration.driver;

import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import configuration.listeners.DriverEventLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.AbstractDriverOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringDecorator;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.function.Function;

public class DriverFactory {
    protected static final Logger LOGGER = LogManager.getLogger(DriverFactory.class);
    private static final Integer VNC_PORT = 7900;

    private static final Map<String, Function<Network, ContainerizedDriver>> DRIVER_MAP = Map.of(
            "chrome", network -> createContainerizedDriver(network, new ChromeOptions(), "selenium/standalone-chromium"),
            "firefox", network -> createContainerizedDriver(network, new FirefoxOptions().setLogLevel(FirefoxDriverLogLevel.INFO), "selenium/standalone-firefox")
    );

    public static ContainerizedDriver getContainerizedDriver(Network network) {
        String driverName = ProjectConfiguration.getProperty(PropertyNameSpace.BROWSER);
        return DRIVER_MAP.getOrDefault(driverName, n -> {
            throw new RuntimeException("Unknown DriverName: " + driverName);
        }).apply(network);
    }

    private static AbstractDriverOptions<?> addCommonBrowserOptions(AbstractDriverOptions<?> browserOptions) {
        browserOptions.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        return browserOptions;
    }

    private static ContainerizedDriver createContainerizedDriver(Network network, AbstractDriverOptions<?> options, String baseImage) {
        DriverEventLogger driverEventLogger = new DriverEventLogger();
        String imageVersion = ProjectConfiguration.getProperty(PropertyNameSpace.BROWSER_VERSION);
        DockerImageName dockerImageName = DockerImageName.parse(baseImage + ":" + imageVersion)
                .asCompatibleSubstituteFor(baseImage.contains("chromium") ? "selenium/standalone-chrome" : baseImage);

        BrowserWebDriverContainer<?> container = new BrowserWebDriverContainer<>(dockerImageName);
        container.withCapabilities(addCommonBrowserOptions(options));
        container.withNetwork(network);
        container.addExposedPort(VNC_PORT);

        container.start();
        LOGGER.info("Localhost accessible URL for selenium VNC: http://localhost:{}", container.getMappedPort(VNC_PORT));
        LOGGER.info("Localhost accessible URL for selenium HUB: {}", container.getSeleniumAddress());

        EventFiringDecorator<RemoteWebDriver> decoratedDriver = new EventFiringDecorator<>(driverEventLogger);
        return new ContainerizedDriver(decoratedDriver.decorate(new RemoteWebDriver(container.getSeleniumAddress(), options)), container);
    }
}
