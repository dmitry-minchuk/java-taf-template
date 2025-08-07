package configuration.driver;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.ScreenshotType;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

// Enhanced Playwright driver pool with Docker container support for isolated browser execution
public class PlaywrightDockerDriverPool {

    protected static final Logger LOGGER = LogManager.getLogger(PlaywrightDockerDriverPool.class);

    private static final ThreadLocal<PlaywrightDockerContext> threadLocalContext = new ThreadLocal<>();

    // Playwright Docker image constants  
    private static final String PLAYWRIGHT_DOCKER_IMAGE = "cdp-browser";
    private static final String DEFAULT_PLAYWRIGHT_VERSION = "latest";

    // File system binding configuration - same as DriverFactory for consistency
    private static final String HOST_RESOURCE_PATH = ProjectConfiguration.getProperty(PropertyNameSpace.HOST_RESOURCE_PATH);
    private static final String CONTAINER_RESOURCE_PATH = ProjectConfiguration.getProperty(PropertyNameSpace.CONTAINER_RESOURCE_PATH);

    // Container for Playwright Docker components per thread
    private static class PlaywrightDockerContext {
        private final Network network;
        private final GenericContainer<?> playwrightContainer;
        private final Playwright playwright;
        private final Browser browser;
        private final BrowserContext browserContext;
        private final Page page;

        public PlaywrightDockerContext(Network network, GenericContainer<?> playwrightContainer,
                                       Playwright playwright, Browser browser,
                                       BrowserContext browserContext, Page page) {
            this.network = network;
            this.playwrightContainer = playwrightContainer;
            this.playwright = playwright;
            this.browser = browser;
            this.browserContext = browserContext;
            this.page = page;
        }

        public Network getNetwork() { return network; }
        public GenericContainer<?> getPlaywrightContainer() { return playwrightContainer; }
        public Playwright getPlaywright() { return playwright; }
        public Browser getBrowser() { return browser; }
        public BrowserContext getBrowserContext() { return browserContext; }
        public Page getPage() { return page; }

        public void close() {
            try {
                if (page != null && !page.isClosed()) {
                    page.close();
                    LOGGER.debug("Page closed successfully");
                }
                if (browserContext != null) {
                    browserContext.close();
                    LOGGER.debug("Browser context closed successfully");
                }
                if (browser != null && browser.isConnected()) {
                    browser.close();
                    LOGGER.debug("Browser closed successfully");
                }
                if (playwright != null) {
                    playwright.close();
                    LOGGER.debug("Playwright closed successfully");
                }
                if (playwrightContainer != null && playwrightContainer.isRunning()) {
                    playwrightContainer.stop();
                    LOGGER.debug("Playwright container stopped successfully");
                }
            } catch (Exception e) {
                LOGGER.warn("Error during Playwright Docker cleanup: {}", e.getMessage());
            }
        }
    }

    // Initialize Playwright with Docker container and file system binding
    public static void setPlaywrightDocker(Network network) {
        if (threadLocalContext.get() == null) {
            try {
                String browserName = ProjectConfiguration.getProperty(PropertyNameSpace.BROWSER);
                LOGGER.info("Initializing Playwright Docker container with browser: {}", browserName);

                // Create Playwright Docker container
                GenericContainer<?> playwrightContainer = createPlaywrightContainer(network, browserName);

                // Connect to containerized Playwright
                Playwright playwright = connectToPlaywrightContainer(playwrightContainer);

                // Connect to browser running in the container
                Browser browser = launchContainerizedBrowser(playwright, browserName, playwrightContainer);

                // Create browser context with file system binding
                BrowserContext browserContext = createContainerizedBrowserContext(browser, network);

                // Create new page
                Page page = browserContext.newPage();

                // Store in thread local
                PlaywrightDockerContext context = new PlaywrightDockerContext(
                        network, playwrightContainer, playwright, browser, browserContext, page);
                threadLocalContext.set(context);

                LOGGER.info("Browser container initialized successfully for thread: {}", Thread.currentThread().getName());

            } catch (Exception e) {
                LOGGER.error("Failed to initialize Browser container: {}", e.getMessage(), e);
                throw new RuntimeException("Browser container initialization failed", e);
            }
        } else {
            LOGGER.debug("Browser container already initialized for thread: {}", Thread.currentThread().getName());
        }
    }

    // Create Playwright Docker container with remote server setup
    private static GenericContainer<?> createPlaywrightContainer(Network network, String browserName) {
        String playwrightVersion = ProjectConfiguration.getProperty(PropertyNameSpace.BROWSER_VERSION);
        if (playwrightVersion == null || playwrightVersion.isEmpty() || "latest".equals(playwrightVersion)) {
            playwrightVersion = DEFAULT_PLAYWRIGHT_VERSION;
        }

        DockerImageName dockerImageName = DockerImageName.parse(PLAYWRIGHT_DOCKER_IMAGE + ":" + playwrightVersion);

        GenericContainer<?> container = new GenericContainer<>(dockerImageName)
                .withNetwork(network)
                .withExposedPorts(9222) // CDP port for remote browser connection
                .waitingFor(Wait.forHttp("/json/version").forPort(9222).withStartupTimeout(Duration.ofSeconds(5)))
                .withPrivilegedMode(true) // Security best practice
                .withSharedMemorySize(2147483648L) // 2GB shared memory for browsers
                .withFileSystemBind(HOST_RESOURCE_PATH, CONTAINER_RESOURCE_PATH, BindMode.READ_ONLY);


        LOGGER.info("Creating Browser Docker container with image: {}", dockerImageName);
        LOGGER.info("Volume mapping configured: {} (host) -> {} (container)", HOST_RESOURCE_PATH, CONTAINER_RESOURCE_PATH);
        container.start();
        LOGGER.info(container.getLogs());

        LOGGER.info("Browser Docker container started successfully - Container ID: {}", container.getContainerId());

        return container;
    }

    // Connect to Playwright server running inside the Docker container
    private static Playwright connectToPlaywrightContainer(GenericContainer<?> container) {
        try {
            // Create local Playwright instance for API access
            Playwright playwright = Playwright.create();

            LOGGER.info("Created Browser instance for remote container connection");
            return playwright;

        } catch (Exception e) {
            LOGGER.error("Failed to create Playwright instance: {}", e.getMessage());
            throw new RuntimeException("Browser instance creation failed", e);
        }
    }

    // Get CDP HTTP endpoint URL for container connection
    private static String getContainerCdpEndpoint(GenericContainer<?> container) {
        String playwrightHost = container.getHost();
        int playwrightPort = container.getMappedPort(9222);
        String httpEndpoint = "http://" + playwrightHost + ":" + playwrightPort;

        LOGGER.info("Container CDP endpoint: {}", httpEndpoint);
        return httpEndpoint;
    }

    // Connect to browser running in the containerized environment
    private static Browser launchContainerizedBrowser(Playwright playwright, String browserName, GenericContainer<?> container) {
        try {
            // Get CDP endpoint from container
            String httpEndpoint = getContainerCdpEndpoint(container);

            // For CDP connection, we only support Chromium (since we're running Chrome in container)
            BrowserType browserType = playwright.chromium();
            LOGGER.debug("Connecting to Chrome browser in container via CDP");

            // Connect to remote Chrome browser running in container via CDP
            Browser browser = browserType.connectOverCDP(httpEndpoint);
            LOGGER.info("Connected to Chrome browser in Playwright container via CDP endpoint: {}", httpEndpoint);
            return browser;

        } catch (Exception e) {
            LOGGER.error("Failed to connect to container browser: {}", e.getMessage(), e);
            throw new RuntimeException("Container browser connection failed", e);
        }
    }

    // Create browser context with container-specific settings
    private static BrowserContext createContainerizedBrowserContext(Browser browser, Network network) {
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setViewportSize(1280, 720)
                .setLocale("en-US")
                .setTimezoneId("America/New_York")
                .setAcceptDownloads(true)
                .setIgnoreHTTPSErrors(true);

        // Container-specific user agent
        contextOptions.setUserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Playwright-Container");

        if (network != null) {
            LOGGER.debug("Browser context configured for container network: {}", network.getId());
        }

        return browser.newContext(contextOptions);
    }

    // Get current Page instance for this thread
    public static Page getPage() {
        PlaywrightDockerContext context = threadLocalContext.get();
        if (context == null) {
            throw new IllegalStateException("Playwright Docker not initialized for current thread. Call setPlaywrightDocker() first.");
        }
        return context.getPage();
    }

    // Get current Browser instance for this thread
    public static Browser getBrowser() {
        PlaywrightDockerContext context = threadLocalContext.get();
        if (context == null) {
            throw new IllegalStateException("Playwright Docker not initialized for current thread. Call setPlaywrightDocker() first.");
        }
        return context.getBrowser();
    }

    // Get current BrowserContext instance for this thread
    public static BrowserContext getBrowserContext() {
        PlaywrightDockerContext context = threadLocalContext.get();
        if (context == null) {
            throw new IllegalStateException("Playwright Docker not initialized for current thread. Call setPlaywrightDocker() first.");
        }
        return context.getBrowserContext();
    }

    /**
     * Get the current Network instance for this thread
     */
    public static Network getNetwork() {
        PlaywrightDockerContext context = threadLocalContext.get();
        if (context == null) {
            throw new IllegalStateException("Playwright Docker not initialized for current thread. Call setPlaywrightDocker() first.");
        }
        return context.getNetwork();
    }

    /**
     * Close Playwright Docker and clean up resources for current thread
     */
    public static void closePlaywrightDocker() {
        PlaywrightDockerContext context = threadLocalContext.get();
        if (context != null) {
            LOGGER.info("Closing Playwright Docker for thread: {}", Thread.currentThread().getName());
            context.close();
            threadLocalContext.remove();
        } else {
            LOGGER.debug("No Playwright Docker context to close for thread: {}", Thread.currentThread().getName());
        }
    }

    public static boolean isInitialized() {
        return threadLocalContext.get() != null;
    }

    public static Page createNewPage() {
        BrowserContext context = getBrowserContext();
        Page newPage = context.newPage();
        LOGGER.debug("Created new page in Docker-aware browser context");
        return newPage;
    }

    public static byte[] takeScreenshot() {
        Page page = getPage();
        return page.screenshot(new Page.ScreenshotOptions()
                .setFullPage(true)
                .setType(ScreenshotType.PNG));
    }

    public static void navigateTo(String url) {
        Page page = getPage();

        // Container network URL resolution for Playwright container
        String resolvedUrl = resolveContainerNetworkUrl(url);

        LOGGER.info("Navigating to URL via Docker network: {} -> {}", url, resolvedUrl);
        page.navigate(resolvedUrl, new Page.NavigateOptions()
                .setWaitUntil(com.microsoft.playwright.options.WaitUntilState.DOMCONTENTLOADED)
                .setTimeout(30000));
    }

    private static String resolveContainerNetworkUrl(String url) {
        PlaywrightDockerContext context = threadLocalContext.get();
        if (context == null || context.getNetwork() == null) {
            return url; // No container network, return original URL
        }

        // Convert localhost URLs to container network URLs for direct container communication
        String defaultAppPort = configuration.projectconfig.ProjectConfiguration.getProperty(
                configuration.projectconfig.PropertyNameSpace.DEFAULT_APP_PORT);
        String localhostPattern = "localhost:" + defaultAppPort;
        String loopbackPattern = "127.0.0.1:" + defaultAppPort;

        if (url.contains(localhostPattern) || url.contains(loopbackPattern)) {
            try {
                configuration.appcontainer.AppContainerData appData = configuration.appcontainer.AppContainerPool.get();
                if (appData != null) {
                    // Use container-to-container communication via Docker network
                    String containerNetworkUrl = getContainerNetworkUrl(appData);
                    LOGGER.info("Docker network communication: {} -> {}", url, containerNetworkUrl);
                    return containerNetworkUrl;
                }
            } catch (Exception e) {
                LOGGER.debug("Could not resolve container network URL, using original: {}", e.getMessage());
            }
        }

        return url;
    }

    public static void navigateToApp() {
        try {
            configuration.appcontainer.AppContainerData appData = configuration.appcontainer.AppContainerPool.get();
            if (appData != null) {
                // Use Docker network for container-to-container communication
                String appUrl = getContainerNetworkUrl(appData);
                LOGGER.info("Navigating to application via Docker network: {}", appUrl);
                navigateTo(appUrl);
            } else {
                LOGGER.warn("No application container found for navigation");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to navigate to application container via Docker network: {}", e.getMessage());
            throw new RuntimeException("Application container navigation failed", e);
        }
    }

    private static String getContainerNetworkUrl(configuration.appcontainer.AppContainerData appData) {
        try {
            var appContainer = appData.getAppContainer();
            String containerName = appContainer.getContainerName(); // Docker network hostname

            String defaultAppPort = configuration.projectconfig.ProjectConfiguration.getProperty(
                    configuration.projectconfig.PropertyNameSpace.DEFAULT_APP_PORT);
            String deployedAppPath = configuration.projectconfig.ProjectConfiguration.getProperty(
                    configuration.projectconfig.PropertyNameSpace.DEPLOYED_APP_PATH);

            // Direct container-to-container URL via Docker network
            String containerNetworkUrl = String.format("http://%s:%s%s", containerName, defaultAppPort, deployedAppPath);
            LOGGER.debug("Container network URL for {}: {}", containerName, containerNetworkUrl);
            return containerNetworkUrl;

        } catch (Exception e) {
            LOGGER.warn("Could not get container network URL, falling back to container URL: {}", e.getMessage());
            return appData.getAppHostUrl();
        }
    }

    public static String getDockerInfo() {
        PlaywrightDockerContext context = threadLocalContext.get();
        if (context == null) {
            return "No Playwright Docker context available";
        }

        StringBuilder info = new StringBuilder();
        info.append("Playwright Docker Architecture:\n");
        info.append(String.format("  Network: %s\n",
                context.getNetwork() != null ? context.getNetwork().getId() : "none"));
        info.append(String.format("  Playwright Container: %s\n",
                context.getPlaywrightContainer() != null ? context.getPlaywrightContainer().getContainerId() : "none"));
        info.append(String.format("  Browser: %s\n",
                context.getBrowser() != null ? context.getBrowser().browserType().name() : "none"));
        info.append(String.format("  Current Page: %s\n",
                context.getPage() != null ? context.getPage().url() : "none"));

        // Add application container info if available
        try {
            configuration.appcontainer.AppContainerData appData = configuration.appcontainer.AppContainerPool.get();
            if (appData != null) {
                info.append(String.format("  App Container: %s\n", appData.getAppContainer().getContainerId()));
                info.append(String.format("  Container Network URL: %s", getContainerNetworkUrl(appData)));
            }
        } catch (Exception e) {
            info.append("  App Container: unavailable");
        }

        return info.toString();
    }

    public static GenericContainer<?> getPlaywrightContainer() {
        PlaywrightDockerContext context = threadLocalContext.get();
        if (context == null) {
            throw new IllegalStateException("Playwright Docker not initialized for current thread. Call setPlaywrightDocker() first.");
        }
        return context.getPlaywrightContainer();
    }

    public static java.io.File downloadFile(com.microsoft.playwright.Locator trigger) throws java.io.IOException {
        return helpers.utils.PlaywrightDownloadUtil.downloadFile(trigger);
    }

    public static java.io.File downloadFile(com.microsoft.playwright.Locator trigger, int timeoutMs) throws java.io.IOException {
        return helpers.utils.PlaywrightDownloadUtil.downloadFile(trigger, timeoutMs);
    }
}