package configuration.core.ui;

import com.microsoft.playwright.Page;
import configuration.appcontainer.AppContainerPool;
import configuration.driver.PlaywrightDriverPool;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * Playwright-based replacement for BasePage
 * Handles page navigation and initialization using Playwright
 */
public abstract class PlaywrightBasePage {
    protected static final Logger LOGGER = LogManager.getLogger(PlaywrightBasePage.class);
    protected String absoluteUrl = null;
    protected String urlAppender = "";
    protected Page page;

    @Getter
    protected ConfirmationPopupComponent confirmationPopup;

    public PlaywrightBasePage() {
        this.page = PlaywrightDriverPool.getPage();
        LOGGER.info("{} was opened.", this.getClass().getName());
        
        // Initialize confirmation popup
        confirmationPopup = new ConfirmationPopupComponent();
        confirmationPopup.initPlaywright(page, ""); // Empty selector for global popup
    }

    public PlaywrightBasePage(String urlAppender) {
        this.urlAppender = urlAppender;
        this.page = PlaywrightDriverPool.getPage();
        LOGGER.info("{} was opened with URL appender: {}", this.getClass().getName(), urlAppender);
        
        // Initialize confirmation popup
        confirmationPopup = new ConfirmationPopupComponent();
        confirmationPopup.initPlaywright(page, ""); // Empty selector for global popup
    }

    /**
     * Navigate to the page URL
     * Uses Playwright's native navigation with wait conditions
     */
    public void open() {
        String url = AppContainerPool.get().getAppHostUrl() + urlAppender;
        LOGGER.info("Opening page: {}", url);
        
        // Navigate with Playwright's built-in wait conditions
        page.navigate(url, new Page.NavigateOptions()
                .setWaitUntil(com.microsoft.playwright.options.WaitUntilState.DOMCONTENTLOADED)
                .setTimeout(30000)); // 30 seconds timeout
        
        // Set viewport to maximize equivalent
        page.setViewportSize(1920, 1080);
    }

    /**
     * Check if the current page URL matches expected URL
     */
    public boolean isPageOpened() {
        String urlExpected = Objects.requireNonNullElseGet(absoluteUrl, 
            () -> AppContainerPool.get().getAppHostUrl() + urlAppender);
        String currentUrl = page.url();
        boolean isOpen = Objects.requireNonNull(currentUrl).equalsIgnoreCase(urlExpected);
        
        LOGGER.debug("Page opened check - Expected: {}, Current: {}, Match: {}", 
                    urlExpected, currentUrl, isOpen);
        return isOpen;
    }

    /**
     * Refresh the current page
     * Uses Playwright's reload with wait conditions
     */
    public void refresh() {
        LOGGER.info("Refreshing page: {}", page.url());
        page.reload(new Page.ReloadOptions()
                .setWaitUntil(com.microsoft.playwright.options.WaitUntilState.DOMCONTENTLOADED)
                .setTimeout(30000));
    }

    /**
     * Get the current page title
     */
    public String getTitle() {
        return page.title();
    }

    /**
     * Get the current page URL
     */
    public String getCurrentUrl() {
        return page.url();
    }

    /**
     * Wait for page to be ready
     * Uses Playwright's native load state waiting
     */
    public void waitForPageReady() {
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
    }

    /**
     * Take screenshot of current page
     */
    public byte[] takeScreenshot() {
        return page.screenshot(new Page.ScreenshotOptions()
                .setFullPage(true)
                .setType(com.microsoft.playwright.options.ScreenshotType.PNG));
    }

    /**
     * Get the Playwright Page object for advanced operations
     */
    public Page getPage() {
        return page;
    }

    /**
     * Check if an element with given text is present on page
     * Uses Playwright's text locator capabilities
     */
    public boolean hasText(String text) {
        try {
            return page.getByText(text).isVisible();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Wait for text to appear on page
     */
    public void waitForText(String text) {
        waitForText(text, 10000); // 10 seconds default
    }

    /**
     * Wait for text to appear on page with timeout
     */
    public void waitForText(String text, int timeoutMs) {
        page.getByText(text).waitFor(new com.microsoft.playwright.Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(timeoutMs));
    }
}