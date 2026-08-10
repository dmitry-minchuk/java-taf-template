package configuration.driver;

import com.microsoft.playwright.*;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Playwright lifecycle for {@link ExecutionMode#PLAYWRIGHT_LOCAL}: the browser is launched
 * directly on the host machine. Mode-agnostic callers should go through {@link DriverPool}.
 */
public class LocalDriverPool {

    protected static final Logger LOGGER = LogManager.getLogger(LocalDriverPool.class);
    private static final int DEFAULT_TIMEOUT_MS = Integer.parseInt(ProjectConfiguration.getProperty(PropertyNameSpace.PLAYWRIGHT_DEFAULT_TIMEOUT));

    private static final ThreadLocal<PlaywrightContext> threadLocalContext = new ThreadLocal<>();

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

        // Each step is closed independently: a failure on one (e.g. a flaky page.close())
        // must not leak the browser or the Playwright process behind it.
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
        }
    }

    // Initialize Playwright for local execution with direct browser launch
    public static void setPlaywright() {
        if (threadLocalContext.get() == null) {
            try {
                String browserName = ProjectConfiguration.getProperty(PropertyNameSpace.BROWSER);
                LOGGER.info("Initializing Playwright with browser: {}", browserName);

                Playwright playwright = Playwright.create();
                Browser browser = launchBrowser(playwright, browserName);
                BrowserContext browserContext = createBrowserContext(browser);
                Page page = browserContext.newPage();
                page.setDefaultTimeout(DEFAULT_TIMEOUT_MS);

                threadLocalContext.set(new PlaywrightContext(playwright, browser, browserContext, page));

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
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "false"));
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(headless) // -Dheadless=true for CI / unattended local runs
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
        // The default user agent of the launched browser is kept on purpose:
        // faking a Chrome UA under Firefox/WebKit would test a configuration no real user has.
        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setViewportSize(1280, 720) // Default viewport size
                .setLocale("en-US")
                .setTimezoneId("America/New_York")
                .setAcceptDownloads(true)
                .setIgnoreHTTPSErrors(true); // Ignore SSL errors for testing

        return browser.newContext(contextOptions);
    }

    // Get Page for LOCAL mode
    public static Page getPage() {
        return requireContext().getPage();
    }

    // Get BrowserContext for LOCAL mode
    public static BrowserContext getBrowserContext() {
        return requireContext().getBrowserContext();
    }

    public static Playwright getPlaywright() {
        return requireContext().getPlaywright();
    }

    private static PlaywrightContext requireContext() {
        PlaywrightContext context = threadLocalContext.get();
        if (context == null) {
            throw new IllegalStateException("Playwright not initialized for current thread. Call setPlaywright() first.");
        }
        return context;
    }

    // Close Playwright for LOCAL mode
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

    public static boolean isInitialized() {
        return threadLocalContext.get() != null;
    }

    static String getLocalDebugInfo() {
        PlaywrightContext context = threadLocalContext.get();
        if (context == null) {
            return "Context: Not initialized\n";
        }
        return String.format("Browser: %s%n", context.getBrowser().browserType().name())
                + String.format("Page URL: %s%n", context.getPage().url());
    }
}
