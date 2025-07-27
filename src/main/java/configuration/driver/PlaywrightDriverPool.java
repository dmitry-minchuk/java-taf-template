package configuration.driver;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.ScreenshotType;
import com.microsoft.playwright.options.WaitUntilState;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.function.Consumer;

// Unified Playwright driver pool with automatic mode detection for local and Docker execution
public class PlaywrightDriverPool {
    
    protected static final Logger LOGGER = LogManager.getLogger(PlaywrightDriverPool.class);
    
    // Execution modes for Playwright framework (moved from BaseTest for independence)
    public enum ExecutionMode {
        SELENIUM,           // Original Selenium with Docker containers  
        PLAYWRIGHT_LOCAL,   // Local Playwright execution
        PLAYWRIGHT_DOCKER   // Docker-aware Playwright execution
    }
    
    private static final ThreadLocal<PlaywrightContext> threadLocalContext = new ThreadLocal<>();
    
    // Get current execution mode from system properties with fallback to local mode
    private static ExecutionMode getExecutionMode() {
        String mode = System.getProperty("execution.mode", "PLAYWRIGHT_LOCAL");
        
        try {
            ExecutionMode execMode = ExecutionMode.valueOf(mode.toUpperCase());
            return execMode;
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Unknown execution mode '{}', defaulting to PLAYWRIGHT_LOCAL", mode);
            return ExecutionMode.PLAYWRIGHT_LOCAL;
        }
    }
    
    // Container for Playwright components per thread
    private static class PlaywrightContext {
        private final Playwright playwright;
        private final Browser browser;
        private final BrowserContext browserContext;
        private final Page page;
        
        public PlaywrightContext(Playwright playwright, Browser browser, BrowserContext browserContext, Page page) {
            this.playwright = playwright;
            this.browser = browser;
            this.browserContext = browserContext;
            this.page = page;
        }
        
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
            } catch (Exception e) {
                LOGGER.warn("Error during Playwright cleanup: {}", e.getMessage());
            }
        }
    }
    
    // Initialize Playwright for local execution with direct browser launch
    public static void setPlaywright() {
        if (threadLocalContext.get() == null) {
            try {
                String browserName = ProjectConfiguration.getProperty(PropertyNameSpace.BROWSER);
                LOGGER.info("Initializing Playwright with browser: {}", browserName);
                
                // Create Playwright instance
                Playwright playwright = Playwright.create();
                
                // Launch browser based on configuration
                Browser browser = launchBrowser(playwright, browserName);
                
                // Create browser context with options
                BrowserContext browserContext = createBrowserContext(browser);
                
                // Create new page
                Page page = browserContext.newPage();
                
                // Store in thread local
                PlaywrightContext context = new PlaywrightContext(playwright, browser, browserContext, page);
                threadLocalContext.set(context);
                
                LOGGER.info("Playwright initialized successfully for thread: {}", Thread.currentThread().getName());
                
            } catch (Exception e) {
                LOGGER.error("Failed to initialize Playwright: {}", e.getMessage(), e);
                throw new RuntimeException("Playwright initialization failed", e);
            }
        } else {
            LOGGER.debug("Playwright already initialized for thread: {}", Thread.currentThread().getName());
        }
    }
    
    private static Browser launchBrowser(Playwright playwright, String browserName) {
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(false) // Phase 1: Run in headed mode for debugging
                .setSlowMo(0) // No slow motion for normal execution
                .setDevtools(false); // Disable devtools by default
        
        // Add browser-specific arguments
        launchOptions.setArgs(java.util.List.of(
                "--disable-blink-features=AutomationControlled",
                "--disable-dev-shm-usage",
                "--no-sandbox"
        ));
        
        BrowserType browserType = switch (browserName.toLowerCase()) {
            case "chrome", "chromium" -> {
                LOGGER.debug("Launching Chromium browser");
                yield playwright.chromium();
            }
            case "firefox" -> {
                LOGGER.debug("Launching Firefox browser");
                yield playwright.firefox();
            }
            case "webkit", "safari" -> {
                LOGGER.debug("Launching WebKit browser");
                yield playwright.webkit();
            }
            default -> {
                LOGGER.warn("Unknown browser '{}', defaulting to Chromium", browserName);
                yield playwright.chromium();
            }
        };
        
        return browserType.launch(launchOptions);
    }
    
    private static BrowserContext createBrowserContext(Browser browser) {
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setViewportSize(1280, 720) // Default viewport size
                .setLocale("en-US")
                .setTimezoneId("America/New_York")
                .setAcceptDownloads(true)
                .setIgnoreHTTPSErrors(true); // Ignore SSL errors for testing
        
        // Add user agent if needed
        contextOptions.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        
        return browser.newContext(contextOptions);
    }
    
    // Initialize Playwright based on execution mode with unified setup
    public static void initializePlaywright(org.testcontainers.containers.Network network) {
        ExecutionMode mode = getExecutionMode();
        switch (mode) {
            case PLAYWRIGHT_LOCAL -> setPlaywright();
            case PLAYWRIGHT_DOCKER -> PlaywrightDockerDriverPool.setPlaywrightDocker(network);
            case SELENIUM -> throw new UnsupportedOperationException(
                "Use DriverPool for Selenium mode, not PlaywrightDriverPool");
        }
        LOGGER.info("Playwright initialized in {} mode", mode);
    }

    // Get current Page instance with automatic mode detection and delegation
    public static Page getPage() {
        ExecutionMode mode = getExecutionMode();
        return switch (mode) {
            case PLAYWRIGHT_LOCAL -> getLocalPage();
            case PLAYWRIGHT_DOCKER -> PlaywrightDockerDriverPool.getPage();
            case SELENIUM -> throw new UnsupportedOperationException(
                "Use DriverPool for Selenium mode, not PlaywrightDriverPool");
        };
    }
    
    // Get Page for LOCAL mode
    private static Page getLocalPage() {
        PlaywrightContext context = threadLocalContext.get();
        if (context == null) {
            throw new IllegalStateException("Playwright not initialized for current thread. Call setPlaywright() first.");
        }
        return context.getPage();
    }
    
    // Get current Browser instance with automatic mode detection
    public static Browser getBrowser() {
        ExecutionMode mode = getExecutionMode();
        return switch (mode) {
            case PLAYWRIGHT_LOCAL -> getLocalBrowser();
            case PLAYWRIGHT_DOCKER -> PlaywrightDockerDriverPool.getBrowser();
            case SELENIUM -> throw new UnsupportedOperationException(
                "Use DriverPool for Selenium mode, not PlaywrightDriverPool");
        };
    }
    
    // Get current BrowserContext instance with automatic mode detection
    public static BrowserContext getBrowserContext() {
        ExecutionMode mode = getExecutionMode();
        return switch (mode) {
            case PLAYWRIGHT_LOCAL -> getLocalBrowserContext();
            case PLAYWRIGHT_DOCKER -> PlaywrightDockerDriverPool.getBrowserContext();
            case SELENIUM -> throw new UnsupportedOperationException(
                "Use DriverPool for Selenium mode, not PlaywrightDriverPool");
        };
    }
    
    // Get Browser for LOCAL mode
    private static Browser getLocalBrowser() {
        PlaywrightContext context = threadLocalContext.get();
        if (context == null) {
            throw new IllegalStateException("Playwright not initialized for current thread. Call setPlaywright() first.");
        }
        return context.getBrowser();
    }
    
    // Get BrowserContext for LOCAL mode
    private static BrowserContext getLocalBrowserContext() {
        PlaywrightContext context = threadLocalContext.get();
        if (context == null) {
            throw new IllegalStateException("Playwright not initialized for current thread. Call setPlaywright() first.");
        }
        return context.getBrowserContext();
    }
    
    /**
     * Get the current Playwright instance for this thread
     */
    public static Playwright getPlaywright() {
        PlaywrightContext context = threadLocalContext.get();
        if (context == null) {
            throw new IllegalStateException("Playwright not initialized for current thread. Call setPlaywright() first.");
        }
        return context.getPlaywright();
    }
    
    // Close Playwright and clean up resources with automatic mode detection
    public static void closePlaywright() {
        ExecutionMode mode = getExecutionMode();
        switch (mode) {
            case PLAYWRIGHT_LOCAL -> closeLocalPlaywright();
            case PLAYWRIGHT_DOCKER -> PlaywrightDockerDriverPool.closePlaywrightDocker();
            case SELENIUM -> throw new UnsupportedOperationException(
                "Use DriverPool for Selenium mode, not PlaywrightDriverPool");
        }
    }
    
    // Close Playwright for LOCAL mode
    private static void closeLocalPlaywright() {
        PlaywrightContext context = threadLocalContext.get();
        if (context != null) {
            LOGGER.info("Closing Playwright for thread: {}", Thread.currentThread().getName());
            context.close();
            threadLocalContext.remove();
        } else {
            LOGGER.debug("No Playwright context to close for thread: {}", Thread.currentThread().getName());
        }
    }
    
    // Check if Playwright is initialized with automatic mode detection
    public static boolean isInitialized() {
        ExecutionMode mode = getExecutionMode();
        return switch (mode) {
            case PLAYWRIGHT_LOCAL -> threadLocalContext.get() != null;
            case PLAYWRIGHT_DOCKER -> PlaywrightDockerDriverPool.isInitialized();
            case SELENIUM -> throw new UnsupportedOperationException(
                "Use DriverPool for Selenium mode, not PlaywrightDriverPool");
        };
    }
    
    // Create a new page in the current browser context for multiple tabs/pages
    public static Page createNewPage() {
        BrowserContext context = getBrowserContext();
        Page newPage = context.newPage();
        LOGGER.debug("Created new page in browser context");
        return newPage;
    }
    
    // Take screenshot with current page for debugging and test reporting
    public static byte[] takeScreenshot() {
        Page page = getPage();
        return page.screenshot(new Page.ScreenshotOptions()
                .setFullPage(true)
                .setType(ScreenshotType.PNG));
    }
    
    // Navigate to URL using current page with default options
    public static void navigateTo(String url) {
        Page page = getPage();
        LOGGER.info("Navigating to URL: {}", url);
        page.navigate(url, new Page.NavigateOptions()
                .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                .setTimeout(30000)); // 30 seconds timeout
    }
    
    // Navigate to application container URL with automatic mode detection
    public static void navigateToApp() {
        ExecutionMode mode = getExecutionMode();
        switch (mode) {
            case PLAYWRIGHT_LOCAL -> {
                // For local mode, use mapped port URL
                try {
                    configuration.appcontainer.AppContainerData appData = configuration.appcontainer.AppContainerPool.get();
                    if (appData != null) {
                        var container = appData.getAppContainer();
                        Integer defaultAppPort = Integer.parseInt(
                            configuration.projectconfig.ProjectConfiguration.getProperty(
                                configuration.projectconfig.PropertyNameSpace.DEFAULT_APP_PORT));
                        Integer mappedPort = container.getMappedPort(defaultAppPort);
                        
                        String deployedAppPath = configuration.projectconfig.ProjectConfiguration.getProperty(
                            configuration.projectconfig.PropertyNameSpace.DEPLOYED_APP_PATH);
                        
                        String hostUrl = String.format("http://localhost:%d%s", mappedPort, deployedAppPath);
                        LOGGER.info("Navigating to application via host URL (LOCAL): {}", hostUrl);
                        navigateTo(hostUrl);
                    } else {
                        LOGGER.warn("No application container found for navigation");
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to navigate to application container: {}", e.getMessage());
                    throw new RuntimeException("Application container navigation failed", e);
                }
            }
            case PLAYWRIGHT_DOCKER -> PlaywrightDockerDriverPool.navigateToApp();
            case SELENIUM -> throw new UnsupportedOperationException(
                "Use DriverPool for Selenium mode, not PlaywrightDriverPool");
        }
    }
    
    /**
     * Download file by triggering download action
     * Unified download method with automatic mode detection
     */
    public static java.io.File downloadFile(com.microsoft.playwright.Locator trigger) throws java.io.IOException {
        return helpers.utils.PlaywrightDownloadUtil.downloadFile(trigger);
    }
    
    /**
     * Download file by triggering download action with custom timeout
     * Unified download method with automatic mode detection
     */
    public static java.io.File downloadFile(com.microsoft.playwright.Locator trigger, int timeoutMs) throws java.io.IOException {
        return helpers.utils.PlaywrightDownloadUtil.downloadFile(trigger, timeoutMs);
    }
    
    /**
     * Get current execution mode for debugging purposes
     */
    public static ExecutionMode getCurrentExecutionMode() {
        return getExecutionMode();
    }
    
    /**
     * Get comprehensive debugging information about current state
     * Includes execution mode, browser state, and file system configuration
     */
    public static String getDebugInfo() {
        ExecutionMode mode = getExecutionMode();
        StringBuilder info = new StringBuilder();
        info.append(String.format("Execution Mode: %s\n", mode));
        
        switch (mode) {
            case PLAYWRIGHT_LOCAL -> {
                PlaywrightContext context = threadLocalContext.get();
                if (context != null) {
                    info.append(String.format("Browser: %s\n", context.getBrowser().browserType().name()));
                    info.append(String.format("Page URL: %s\n", context.getPage().url()));
                } else {
                    info.append("Context: Not initialized\n");
                }
            }
            case PLAYWRIGHT_DOCKER -> {
                info.append(PlaywrightDockerDriverPool.getDockerInfo());
            }
            case SELENIUM -> {
                info.append("Selenium mode - use DriverPool for debugging\n");
            }
        }
        
        // Add basic configuration information
        info.append("\nConfiguration:\n");
        String hostResourcePath = ProjectConfiguration.getProperty(PropertyNameSpace.HOST_RESOURCE_PATH);
        info.append(String.format("Host Resource Path: %s\n", hostResourcePath));
        
        return info.toString();
    }
}