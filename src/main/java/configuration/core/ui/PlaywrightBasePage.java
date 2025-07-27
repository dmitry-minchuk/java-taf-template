package configuration.core.ui;

import com.microsoft.playwright.Page;
import configuration.appcontainer.AppContainerPool;
import configuration.driver.PlaywrightDriverPool;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

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

    // Navigate to page URL with Playwright's native wait conditions
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

    public boolean isPageOpened() {
        String urlExpected = Objects.requireNonNullElseGet(absoluteUrl, 
            () -> AppContainerPool.get().getAppHostUrl() + urlAppender);
        String currentUrl = page.url();
        boolean isOpen = Objects.requireNonNull(currentUrl).equalsIgnoreCase(urlExpected);
        
        LOGGER.debug("Page opened check - Expected: {}, Current: {}, Match: {}", 
                    urlExpected, currentUrl, isOpen);
        return isOpen;
    }

    // Refresh page using Playwright's reload with wait conditions
    public void refresh() {
        LOGGER.info("Refreshing page: {}", page.url());
        page.reload(new Page.ReloadOptions()
                .setWaitUntil(com.microsoft.playwright.options.WaitUntilState.DOMCONTENTLOADED)
                .setTimeout(30000));
    }

    public String getTitle() {
        return page.title();
    }

    public String getCurrentUrl() {
        return page.url();
    }

    // Wait for page to be ready using Playwright's native load state waiting
    public void waitForPageReady() {
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE);
    }

    public byte[] takeScreenshot() {
        return page.screenshot(new Page.ScreenshotOptions()
                .setFullPage(true)
                .setType(com.microsoft.playwright.options.ScreenshotType.PNG));
    }

    public Page getPage() {
        return page;
    }

    // Check if element with given text is present using Playwright's text locator
    public boolean hasText(String text) {
        try {
            return page.getByText(text).isVisible();
        } catch (Exception e) {
            return false;
        }
    }

    public void waitForText(String text) {
        waitForText(text, 10000); // 10 seconds default
    }
    public void waitForText(String text, int timeoutMs) {
        page.getByText(text).waitFor(new com.microsoft.playwright.Locator.WaitForOptions()
                .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                .setTimeout(timeoutMs));
    }
}