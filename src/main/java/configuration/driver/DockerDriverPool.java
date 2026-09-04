package configuration.driver;

import com.microsoft.playwright.*;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import helpers.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.ToStringConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

public class DockerDriverPool {

    protected static final Logger LOGGER = LogManager.getLogger(DockerDriverPool.class);
    private static final int DEFAULT_TIMEOUT_MS = Integer.parseInt(ProjectConfiguration.getProperty(PropertyNameSpace.PLAYWRIGHT_DEFAULT_TIMEOUT));

    private static final ThreadLocal<PlaywrightDockerContext> threadLocalContext = new ThreadLocal<>();

    private static final String PLAYWRIGHT_DOCKER_IMAGE = "mcr.microsoft.com/playwright";
    private static final String PLAYWRIGHT_VERSION = "v1.52.0-noble";
    private static final String PLAYWRIGHT_NPM_PACKAGE = "playwright@1.52.0";
    private static final Object NPM_CACHE_WARM_UP_LOCK = new Object();

    private static final String HOST_RESOURCE_PATH = ProjectConfiguration.getProperty(PropertyNameSpace.HOST_RESOURCE_PATH);
    private static final String CONTAINER_RESOURCE_PATH = ProjectConfiguration.getProperty(PropertyNameSpace.CONTAINER_RESOURCE_PATH);

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
            DriverPool.closeQuietly("page", () -> {
                if (page != null && !page.isClosed()) {
                    page.close();
                }
            });
            DriverPool.closeQuietly("browser context", () -> {
                if (browserContext != null) {
                    browserContext.close();
                }
            });
            DriverPool.closeQuietly("browser", () -> {
                if (browser != null && browser.isConnected()) {
                    browser.close();
                }
            });
            DriverPool.closeQuietly("playwright", () -> {
                if (playwright != null) {
                    playwright.close();
                }
            });
            DriverPool.closeQuietly("playwright container", () -> {
                if (playwrightContainer != null && playwrightContainer.isRunning()) {
                    playwrightContainer.stop();
                }
            });
        }
    }

    public static void setPlaywrightDocker(Network network) {
        if (threadLocalContext.get() == null) {
            try {
                String browserName = ProjectConfiguration.getProperty(PropertyNameSpace.BROWSER);
                LOGGER.info("Initializing Playwright Docker container with browser: {}", browserName);

                GenericContainer<?> playwrightContainer = createPlaywrightContainer(network, browserName);

                Playwright playwright = connectToPlaywrightContainer();

                Browser browser = launchContainerizedBrowser(playwright, browserName, playwrightContainer);

                BrowserContext browserContext = createContainerizedBrowserContext(browser, network);

                Page page = browserContext.newPage();

                page.setDefaultTimeout(DEFAULT_TIMEOUT_MS);

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

    private static GenericContainer<?> createPlaywrightContainer(Network network, String browserName) {
        String playwrightVersion = ProjectConfiguration.getProperty(PropertyNameSpace.BROWSER_VERSION);
        if (playwrightVersion == null || playwrightVersion.isEmpty() || "latest".equals(playwrightVersion)) {
            playwrightVersion = PLAYWRIGHT_VERSION;
        }

        DockerImageName dockerImageName = DockerImageName.parse(PLAYWRIGHT_DOCKER_IMAGE + ":" + playwrightVersion);

        long startupTimeoutSeconds = Long.parseLong(ProjectConfiguration.getProperty(PropertyNameSpace.PLAYWRIGHT_SERVER_STARTUP_TIMEOUT_SECONDS));
        GenericContainer<?> container = new GenericContainer<>(dockerImageName)
                .withNetwork(network)
                .withExposedPorts(3000)
                .withCommand("/bin/sh", "-c", "npx -y " + PLAYWRIGHT_NPM_PACKAGE + " run-server --port 3000 --host 0.0.0.0")
                .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(startupTimeoutSeconds)))
                .withWorkingDirectory("/home/pwuser")
                .withSharedMemorySize(2147483648L)
                .withFileSystemBind(HOST_RESOURCE_PATH, CONTAINER_RESOURCE_PATH, BindMode.READ_ONLY);
        ToStringConsumer containerOutput = new ToStringConsumer();
        container.withLogConsumer(containerOutput);
        String npmCacheDir = ProjectConfiguration.getProperty(PropertyNameSpace.PLAYWRIGHT_NPM_CACHE_DIR);
        Path npmCache = null;
        if (npmCacheDir != null && !npmCacheDir.isBlank()) {
            npmCache = Paths.get(npmCacheDir).toAbsolutePath();
            try {
                Files.createDirectories(npmCache);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot create the npm cache directory " + npmCache, e);
            }
            container.withFileSystemBind(npmCache.toString(), "/root/.npm", BindMode.READ_WRITE);
            LOGGER.info("npm cache of the Playwright Server container is shared from {}", npmCache);
        }

        LOGGER.info("Creating Playwright Server Docker container with image: {}", dockerImageName);
        LOGGER.info("Volume mapping configured: {} (host) -> {} (container)", HOST_RESOURCE_PATH, CONTAINER_RESOURCE_PATH);

        LOGGER.info("Starting container and waiting for Playwright Server (startup timeout {} s)...", startupTimeoutSeconds);
        long startedAt = System.currentTimeMillis();
        try {
            if (npmCache != null && !Files.exists(npmCache.resolve("_npx"))) {
                synchronized (NPM_CACHE_WARM_UP_LOCK) {
                    container.start();
                }
            } else {
                container.start();
            }
        } catch (RuntimeException e) {
            LOGGER.error("Playwright Server container did not start within {} s; container output:\n{}", startupTimeoutSeconds, containerOutput.toUtf8String());
            throw e;
        }
        LOGGER.info("Playwright Server container started in {} ms", System.currentTimeMillis() - startedAt);

        LOGGER.info("Container started - ID: {}", container.getContainerId());
        LOGGER.info("Mapped Playwright Server port: {}:{} -> 3000", container.getHost(), container.getMappedPort(3000));
        LOGGER.info("Container is attached to network for service discovery");
        LOGGER.info("Container startup logs:\n{}", container.getLogs());

        String serverEndpoint = "http://" + container.getHost() + ":" + container.getMappedPort(3000);
        LOGGER.info("Playwright Server ready: {}", serverEndpoint);

        return container;
    }

    private static Playwright connectToPlaywrightContainer() {
        try {
            Playwright playwright = Playwright.create();

            LOGGER.info("Created Playwright instance for server connection");
            return playwright;

        } catch (Exception e) {
            LOGGER.error("Failed to create Playwright instance: {}", e.getMessage());
            throw new RuntimeException("Playwright instance creation failed", e);
        }
    }

    private static Browser launchContainerizedBrowser(Playwright playwright, String browserName, GenericContainer<?> container) {
        String playwrightHost = container.getHost();
        int playwrightPort = container.getMappedPort(3000);
        String wsEndpoint = String.format("ws://%s:%d/", playwrightHost, playwrightPort);
        BrowserType browserType = playwright.chromium();

        int maxAttempts = 5;
        int delayMs = 1000;
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                Browser browser = browserType.connect(wsEndpoint);
                LOGGER.info("Connected to Playwright Server via WebSocket on attempt {}: {}", attempt, wsEndpoint);
                return browser;
            } catch (Exception e) {
                lastException = e;
                LOGGER.warn("WebSocket connection attempt {}/{} failed: {}. Retrying in {}ms...",
                        attempt, maxAttempts, e.getMessage(), delayMs);
                if (attempt < maxAttempts) {
                    WaitUtil.sleep(delayMs, "Waiting for Playwright Server to be ready");
                }
            }
        }

        if (lastException != null && lastException.getMessage() != null
                && lastException.getMessage().contains("ECONNREFUSED")
                && lastException.getMessage().contains("::1")) {
            LOGGER.error("IPv6/IPv4 connectivity issue detected. Add -Djava.net.preferIPv4Stack=true to JVM args.");
        }
        LOGGER.error("Failed to connect to Playwright Server after {} attempts", maxAttempts);
        throw new RuntimeException("Playwright Server browser connection failed", lastException);
    }

    private static BrowserContext createContainerizedBrowserContext(Browser browser, Network network) {
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setViewportSize(1280, 720)
                .setLocale("en-US")
                .setTimezoneId("America/New_York")
                .setAcceptDownloads(true)
                .setIgnoreHTTPSErrors(true);

        boolean videoRecordingEnabled = Boolean.parseBoolean(ProjectConfiguration.getProperty(PropertyNameSpace.ENABLE_VIDEO_RECORDING));

        if (videoRecordingEnabled) {
            Path tempVideoDir = Paths.get("/tmp/playwright-videos");
            contextOptions.setRecordVideoDir(tempVideoDir).setRecordVideoSize(1280, 720);
            LOGGER.info("Video recording enabled for Docker container with temp directory: {}", tempVideoDir);
        }

        if (network != null) {
            LOGGER.debug("Browser context configured for container network: {}", network.getId());
        }

        return browser.newContext(contextOptions);
    }

    public static Page getPage() {
        PlaywrightDockerContext context = threadLocalContext.get();
        if (context == null) {
            throw new IllegalStateException("Playwright Docker not initialized for current thread. Call setPlaywrightDocker() first.");
        }
        return context.getPage();
    }

    public static Browser getBrowser() {
        PlaywrightDockerContext context = threadLocalContext.get();
        if (context == null) {
            throw new IllegalStateException("Playwright Docker not initialized for current thread. Call setPlaywrightDocker() first.");
        }
        return context.getBrowser();
    }

    public static BrowserContext getBrowserContext() {
        PlaywrightDockerContext context = threadLocalContext.get();
        if (context == null) {
            throw new IllegalStateException("Playwright Docker not initialized for current thread. Call setPlaywrightDocker() first.");
        }
        return context.getBrowserContext();
    }

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

    public static void navigateTo(String url) {
        Page page = getPage();
        String resolvedUrl = resolveContainerNetworkUrl(url);

        LOGGER.info("Navigating to URL via Docker network: {} -> {}", url, resolvedUrl);
        page.navigate(resolvedUrl, new Page.NavigateOptions()
                .setWaitUntil(com.microsoft.playwright.options.WaitUntilState.DOMCONTENTLOADED)
                .setTimeout(DEFAULT_TIMEOUT_MS));
    }

    private static String resolveContainerNetworkUrl(String url) {
        PlaywrightDockerContext context = threadLocalContext.get();
        if (context == null || context.getNetwork() == null) {
            return url;
        }

        String defaultAppPort = configuration.projectconfig.ProjectConfiguration.getProperty(
                configuration.projectconfig.PropertyNameSpace.DEFAULT_APP_PORT);
        String localhostPattern = "localhost:" + defaultAppPort;
        String loopbackPattern = "127.0.0.1:" + defaultAppPort;

        if (url.contains(localhostPattern) || url.contains(loopbackPattern)) {
            try {
                configuration.appcontainer.AppContainerData appData = configuration.appcontainer.AppContainerPool.get();
                if (appData != null) {
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
            String containerName = appContainer.getContainerName();

            String defaultAppPort = ProjectConfiguration.getProperty(PropertyNameSpace.DEFAULT_APP_PORT);
            String deployedAppPath = ProjectConfiguration.getProperty(PropertyNameSpace.DEPLOYED_APP_PATH);

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

}
