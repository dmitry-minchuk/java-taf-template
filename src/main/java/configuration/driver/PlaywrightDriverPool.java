package configuration.driver;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.ScreenshotType;
import com.microsoft.playwright.options.WaitUntilState;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Playwright-based replacement for DriverPool
 * Manages Playwright Browser and Page instances with thread-safe access
 * Phase 1: Local execution only (no Docker containers)
 */
public class PlaywrightDriverPool {
    
    protected static final Logger LOGGER = LogManager.getLogger(PlaywrightDriverPool.class);
    
    private static final ThreadLocal<PlaywrightContext> threadLocalContext = new ThreadLocal<>();
    
    /**
     * Container for Playwright components per thread
     */
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
    
    /**
     * Initialize Playwright for local execution
     * Phase 1: No Docker containers, direct browser launch
     */
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
    
    /**
     * Get the current Page instance for this thread
     */
    public static Page getPage() {
        PlaywrightContext context = threadLocalContext.get();
        if (context == null) {
            throw new IllegalStateException("Playwright not initialized for current thread. Call setPlaywright() first.");
        }
        return context.getPage();
    }
    
    /**
     * Get the current Browser instance for this thread
     */
    public static Browser getBrowser() {
        PlaywrightContext context = threadLocalContext.get();
        if (context == null) {
            throw new IllegalStateException("Playwright not initialized for current thread. Call setPlaywright() first.");
        }
        return context.getBrowser();
    }
    
    /**
     * Get the current BrowserContext instance for this thread
     */
    public static BrowserContext getBrowserContext() {
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
    
    /**
     * Close Playwright and clean up resources for current thread
     */
    public static void closePlaywright() {
        PlaywrightContext context = threadLocalContext.get();
        if (context != null) {
            LOGGER.info("Closing Playwright for thread: {}", Thread.currentThread().getName());
            context.close();
            threadLocalContext.remove();
        } else {
            LOGGER.debug("No Playwright context to close for thread: {}", Thread.currentThread().getName());
        }
    }
    
    /**
     * Check if Playwright is initialized for current thread
     */
    public static boolean isInitialized() {
        return threadLocalContext.get() != null;
    }
    
    /**
     * Create a new page in the current browser context
     * Useful for tests that need multiple tabs/pages
     */
    public static Page createNewPage() {
        BrowserContext context = getBrowserContext();
        Page newPage = context.newPage();
        LOGGER.debug("Created new page in browser context");
        return newPage;
    }
    
    /**
     * Take screenshot with current page
     * Utility method for debugging and test reporting
     */
    public static byte[] takeScreenshot() {
        Page page = getPage();
        return page.screenshot(new Page.ScreenshotOptions()
                .setFullPage(true)
                .setType(ScreenshotType.PNG));
    }
    
    /**
     * Navigate to URL using current page
     * Convenience method with default options
     */
    public static void navigateTo(String url) {
        Page page = getPage();
        LOGGER.info("Navigating to URL: {}", url);
        page.navigate(url, new Page.NavigateOptions()
                .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                .setTimeout(30000)); // 30 seconds timeout
    }
}