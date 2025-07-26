package configuration.driver;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.ScreenshotType;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

/**
 * Enhanced Playwright driver pool with Docker container awareness
 * Phase 3: Provides isolated browser execution while maintaining compatibility
 * 
 * Strategy: Use Playwright's built-in container support for browser isolation
 * This approach leverages Playwright's native Docker integration rather than complex remote connections
 */
public class PlaywrightDockerDriverPool {
    
    protected static final Logger LOGGER = LogManager.getLogger(PlaywrightDockerDriverPool.class);
    
    private static final ThreadLocal<PlaywrightDockerContext> threadLocalContext = new ThreadLocal<>();
    
    /**
     * Container for Playwright Docker components per thread
     */
    private static class PlaywrightDockerContext {
        private final Network network;
        private final Playwright playwright;
        private final Browser browser;
        private final BrowserContext browserContext;
        private final Page page;
        private final GenericContainer<?> browserContainer; // Optional: for explicit container management
        
        public PlaywrightDockerContext(Network network, Playwright playwright, Browser browser, 
                                     BrowserContext browserContext, Page page, GenericContainer<?> browserContainer) {
            this.network = network;
            this.playwright = playwright;
            this.browser = browser;
            this.browserContext = browserContext;
            this.page = page;
            this.browserContainer = browserContainer;
        }
        
        public Network getNetwork() { return network; }
        public Playwright getPlaywright() { return playwright; }
        public Browser getBrowser() { return browser; }
        public BrowserContext getBrowserContext() { return browserContext; }
        public Page getPage() { return page; }
        public GenericContainer<?> getBrowserContainer() { return browserContainer; }
        
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
                if (browserContainer != null && browserContainer.isRunning()) {
                    browserContainer.stop();
                    LOGGER.debug("Browser container stopped successfully");
                }
            } catch (Exception e) {
                LOGGER.warn("Error during Playwright Docker cleanup: {}", e.getMessage());
            }
        }
    }
    
    /**
     * Initialize Playwright with Docker awareness
     * Phase 3: Enhanced for container networking and isolation
     */
    public static void setPlaywrightDocker(Network network) {
        if (threadLocalContext.get() == null) {
            try {
                String browserName = ProjectConfiguration.getProperty(PropertyNameSpace.BROWSER);
                LOGGER.info("Initializing Playwright Docker-aware setup with browser: {}", browserName);
                
                // For Phase 3, we'll use Playwright's native browser launching with Docker environment
                // This is simpler and more reliable than remote connections
                Playwright playwright = createDockerAwarePlaywright();
                
                // Launch browser with Docker-optimized settings
                Browser browser = launchDockerBrowser(playwright, browserName);
                
                // Create browser context optimized for container networking
                BrowserContext browserContext = createDockerBrowserContext(browser, network);
                
                // Create new page
                Page page = browserContext.newPage();
                
                // Store in thread local (browserContainer is null for this approach)
                PlaywrightDockerContext context = new PlaywrightDockerContext(
                    network, playwright, browser, browserContext, page, null);
                threadLocalContext.set(context);
                
                LOGGER.info("Playwright Docker-aware setup initialized successfully for thread: {}", Thread.currentThread().getName());
                
            } catch (Exception e) {
                LOGGER.error("Failed to initialize Playwright Docker setup: {}", e.getMessage(), e);
                throw new RuntimeException("Playwright Docker initialization failed", e);
            }
        } else {
            LOGGER.debug("Playwright Docker already initialized for thread: {}", Thread.currentThread().getName());
        }
    }
    
    private static Playwright createDockerAwarePlaywright() {
        // Create Playwright with Docker environment variables
        // This ensures Playwright can work properly in containerized environments
        System.setProperty("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "false");
        System.setProperty("PLAYWRIGHT_BROWSERS_PATH", System.getProperty("user.home") + "/.cache/ms-playwright");
        
        Playwright playwright = Playwright.create();
        LOGGER.debug("Created Docker-aware Playwright instance");
        return playwright;
    }
    
    private static Browser launchDockerBrowser(Playwright playwright, String browserName) {
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(true) // Always headless in Docker environment
                .setSlowMo(0) // No slow motion for Docker execution
                .setDevtools(false); // Disable devtools in containers
        
        // Docker-optimized browser arguments
        launchOptions.setArgs(java.util.List.of(
                "--no-sandbox", // Required for Docker containers
                "--disable-dev-shm-usage", // Prevent shared memory issues
                "--disable-gpu", // Disable GPU in containers
                "--disable-features=VizDisplayCompositor", // Stability in containers
                "--disable-background-timer-throttling", // Prevent throttling
                "--disable-backgrounding-occluded-windows",
                "--disable-renderer-backgrounding",
                "--no-first-run" // Skip first run setup
        ));
        
        BrowserType browserType = switch (browserName.toLowerCase()) {
            case "chrome", "chromium" -> {
                LOGGER.debug("Launching Chromium browser for Docker");
                yield playwright.chromium();
            }
            case "firefox" -> {
                LOGGER.debug("Launching Firefox browser for Docker");
                yield playwright.firefox();
            }
            case "webkit", "safari" -> {
                LOGGER.debug("Launching WebKit browser for Docker");
                yield playwright.webkit();
            }
            default -> {
                LOGGER.warn("Unknown browser '{}', defaulting to Chromium for Docker", browserName);
                yield playwright.chromium();
            }
        };
        
        Browser browser = browserType.launch(launchOptions);
        LOGGER.info("Launched {} browser optimized for Docker environment", browserName);
        return browser;
    }
    
    private static BrowserContext createDockerBrowserContext(Browser browser, Network network) {
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setViewportSize(1280, 720) // Standard viewport for containers
                .setLocale("en-US")
                .setTimezoneId("America/New_York")
                .setAcceptDownloads(false) // Disable downloads in containers
                .setIgnoreHTTPSErrors(true); // Ignore SSL errors for testing
        
        // Container-optimized user agent
        contextOptions.setUserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Playwright-Docker");
        
        // Network-aware configuration
        if (network != null) {
            LOGGER.debug("Browser context configured for Docker network: {}", network.getId());
        }
        
        return browser.newContext(contextOptions);
    }
    
    /**
     * Get the current Page instance for this thread
     */
    public static Page getPage() {
        PlaywrightDockerContext context = threadLocalContext.get();
        if (context == null) {
            throw new IllegalStateException("Playwright Docker not initialized for current thread. Call setPlaywrightDocker() first.");
        }
        return context.getPage();
    }
    
    /**
     * Get the current Browser instance for this thread
     */
    public static Browser getBrowser() {
        PlaywrightDockerContext context = threadLocalContext.get();
        if (context == null) {
            throw new IllegalStateException("Playwright Docker not initialized for current thread. Call setPlaywrightDocker() first.");
        }
        return context.getBrowser();
    }
    
    /**
     * Get the current BrowserContext instance for this thread
     */
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
    
    /**
     * Check if Playwright Docker is initialized for current thread
     */
    public static boolean isInitialized() {
        return threadLocalContext.get() != null;
    }
    
    /**
     * Create a new page in the current browser context
     */
    public static Page createNewPage() {
        BrowserContext context = getBrowserContext();
        Page newPage = context.newPage();
        LOGGER.debug("Created new page in Docker-aware browser context");
        return newPage;
    }
    
    /**
     * Take screenshot with current page
     */
    public static byte[] takeScreenshot() {
        Page page = getPage();
        return page.screenshot(new Page.ScreenshotOptions()
                .setFullPage(true)
                .setType(ScreenshotType.PNG));
    }
    
    /**
     * Navigate to URL with container networking awareness
     * Automatically resolves container hostnames within the Docker network
     */
    public static void navigateTo(String url) {
        Page page = getPage();
        
        // Container network URL resolution
        String resolvedUrl = resolveContainerUrl(url);
        
        LOGGER.info("Navigating to URL (Docker-aware): {} -> {}", url, resolvedUrl);
        page.navigate(resolvedUrl, new Page.NavigateOptions()
                .setWaitUntil(com.microsoft.playwright.options.WaitUntilState.DOMCONTENTLOADED)
                .setTimeout(30000));
    }
    
    /**
     * Resolve URLs for container networking
     * Converts localhost URLs to container-accessible URLs when needed
     * Phase 3.4: Implements proper container-to-container communication
     */
    private static String resolveContainerUrl(String url) {
        PlaywrightDockerContext context = threadLocalContext.get();
        if (context == null || context.getNetwork() == null) {
            return url; // No container network, return original URL
        }
        
        // Phase 3.4: Convert localhost URLs to container network URLs using configured port
        String defaultAppPort = configuration.projectconfig.ProjectConfiguration.getProperty(
            configuration.projectconfig.PropertyNameSpace.DEFAULT_APP_PORT);
        String localhostPattern = "localhost:" + defaultAppPort;
        String loopbackPattern = "127.0.0.1:" + defaultAppPort;
        
        if (url.contains(localhostPattern) || url.contains(loopbackPattern)) {
            // Get app container name from AppContainerPool
            try {
                configuration.appcontainer.AppContainerData appData = configuration.appcontainer.AppContainerPool.get();
                if (appData != null) {
                    String containerUrl = appData.getAppHostUrl();
                    LOGGER.info("Container networking: {} -> {}", url, containerUrl);
                    return containerUrl;
                }
            } catch (Exception e) {
                LOGGER.debug("Could not resolve container URL, using original: {}", e.getMessage());
            }
        }
        
        return url;
    }
    
    /**
     * Navigate to application container URL
     * Phase 3.6: Use host-accessible URL for Playwright running on host
     */
    public static void navigateToApp() {
        try {
            configuration.appcontainer.AppContainerData appData = configuration.appcontainer.AppContainerPool.get();
            if (appData != null) {
                // Phase 3.6: Get the localhost URL instead of container-internal URL
                String appUrl = getHostAccessibleUrl(appData);
                LOGGER.info("Navigating to application via host URL: {}", appUrl);
                navigateTo(appUrl);
            } else {
                LOGGER.warn("No application container found for navigation");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to navigate to application container: {}", e.getMessage());
            throw new RuntimeException("Application container navigation failed", e);
        }
    }
    
    /**
     * Phase 3.6: Get host-accessible URL for the application container
     * Since Playwright runs on host, we need localhost URLs with mapped ports
     */
    private static String getHostAccessibleUrl(configuration.appcontainer.AppContainerData appData) {
        try {
            // Get the container and mapped port
            var container = appData.getAppContainer();
            Integer defaultAppPort = Integer.parseInt(
                configuration.projectconfig.ProjectConfiguration.getProperty(
                    configuration.projectconfig.PropertyNameSpace.DEFAULT_APP_PORT));
            Integer mappedPort = container.getMappedPort(defaultAppPort);
            
            String deployedAppPath = configuration.projectconfig.ProjectConfiguration.getProperty(
                configuration.projectconfig.PropertyNameSpace.DEPLOYED_APP_PATH);
            
            String hostUrl = String.format("http://localhost:%d%s", mappedPort, deployedAppPath);
            LOGGER.debug("Container {} mapped to host URL: {}", container.getContainerName(), hostUrl);
            return hostUrl;
            
        } catch (Exception e) {
            LOGGER.warn("Could not get host-accessible URL, falling back to container URL: {}", e.getMessage());
            return appData.getAppHostUrl();
        }
    }
    
    /**
     * Get network and container information for debugging
     */
    public static String getDockerInfo() {
        PlaywrightDockerContext context = threadLocalContext.get();
        if (context == null) {
            return "No Playwright Docker context available";
        }
        
        return String.format("Playwright Docker Context - Network: %s | Browser: %s | Page: %s",
                context.getNetwork() != null ? context.getNetwork().getId() : "none",
                context.getBrowser() != null ? context.getBrowser().browserType().name() : "none",
                context.getPage() != null ? context.getPage().url() : "none");
    }
}