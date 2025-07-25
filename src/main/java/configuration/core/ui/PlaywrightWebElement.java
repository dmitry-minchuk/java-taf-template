package configuration.core.ui;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;

/**
 * Playwright-based replacement for SmartWebElement
 * Uses Playwright's native wait strategies instead of custom waits
 */
public class PlaywrightWebElement {
    
    protected final static Logger LOGGER = LogManager.getLogger(PlaywrightWebElement.class);
    private final int timeoutInMilliseconds;
    private final Page page;
    private final String selector;
    private final Locator locator;
    
    public PlaywrightWebElement(Page page, String selector) {
        this.page = page;
        this.selector = selector;
        this.locator = page.locator(selector);
        // Convert seconds to milliseconds for Playwright
        this.timeoutInMilliseconds = Integer.parseInt(
            ProjectConfiguration.getProperty(PropertyNameSpace.WEB_ELEMENT_EXPLICIT_WAIT)
        ) * 1000;
    }
    
    /**
     * Constructor for child locators (similar to SmartWebElement's parent functionality)
     */
    public PlaywrightWebElement(PlaywrightWebElement parent, String selector) {
        this.page = parent.page;
        this.selector = selector;
        this.locator = parent.locator.locator(selector);
        this.timeoutInMilliseconds = parent.timeoutInMilliseconds;
    }
    
    // Core Actions - Using Playwright's native wait strategies
    
    public void click() {
        LOGGER.debug("Clicking element with selector: {}", selector);
        locator.click(new Locator.ClickOptions().setTimeout(timeoutInMilliseconds));
    }
    
    public void click(long timeoutInSeconds) {
        LOGGER.debug("Clicking element with selector: {} (timeout: {}s)", selector, timeoutInSeconds);
        locator.click(new Locator.ClickOptions().setTimeout((int)(timeoutInSeconds * 1000)));
    }
    
    public void fill(String text) {
        LOGGER.debug("Filling element with selector: {} with text: {}", selector, text);
        // Playwright's fill automatically clears first, then types
        locator.fill(text, new Locator.FillOptions().setTimeout(timeoutInMilliseconds));
    }
    
    public void fill(String text, long timeoutInSeconds) {
        LOGGER.debug("Filling element with selector: {} with text: {} (timeout: {}s)", selector, text, timeoutInSeconds);
        locator.fill(text, new Locator.FillOptions().setTimeout((int)(timeoutInSeconds * 1000)));
    }
    
    public String getText() {
        LOGGER.debug("Getting text from element with selector: {}", selector);
        return locator.textContent(new Locator.TextContentOptions().setTimeout(timeoutInMilliseconds));
    }
    
    public String getAttribute(String name) {
        LOGGER.debug("Getting attribute '{}' from element with selector: {}", name, selector);
        return locator.getAttribute(name, new Locator.GetAttributeOptions().setTimeout(timeoutInMilliseconds));
    }
    
    // Visibility and State Checks - Using Playwright's expect patterns
    
    public boolean isVisible() {
        return isVisible(timeoutInMilliseconds / 1000);
    }
    
    public boolean isVisible(int timeoutInSeconds) {
        try {
            locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout(timeoutInSeconds * 1000));
            return true;
        } catch (Exception e) {
            LOGGER.debug("Element not visible within timeout: {}", selector);
            return false;
        }
    }
    
    public boolean isDisplayed() {
        return isVisible();
    }
    
    public boolean isDisplayed(int timeoutInSeconds) {
        return isVisible(timeoutInSeconds);
    }
    
    public boolean isEnabled() {
        try {
            return locator.isEnabled(new Locator.IsEnabledOptions().setTimeout(timeoutInMilliseconds));
        } catch (Exception e) {
            LOGGER.debug("Element enabled check failed for selector: {}", selector);
            return false;
        }
    }
    
    public boolean isSelected() {
        try {
            return locator.isChecked(new Locator.IsCheckedOptions().setTimeout(timeoutInMilliseconds));
        } catch (Exception e) {
            LOGGER.debug("Element selected check failed for selector: {}", selector);
            return false;
        }
    }
    
    // Selection methods for dropdowns
    
    public void selectByVisibleText(String text) {
        LOGGER.debug("Selecting option '{}' in element with selector: {}", text, selector);
        locator.selectOption(text, new Locator.SelectOptionOptions().setTimeout(timeoutInMilliseconds));
    }
    
    public void selectByVisibleText(String text, long timeoutInSeconds) {
        LOGGER.debug("Selecting option '{}' in element with selector: {} (timeout: {}s)", text, selector, timeoutInSeconds);
        locator.selectOption(text, new Locator.SelectOptionOptions().setTimeout((int)(timeoutInSeconds * 1000)));
    }
    
    // Format functionality (similar to SmartWebElement.format())
    
    public PlaywrightWebElement format(Object... args) {
        String formattedSelector = String.format(this.selector, args);
        LOGGER.debug("Formatting selector from '{}' to '{}'", this.selector, formattedSelector);
        return new PlaywrightWebElement(this.page, formattedSelector);
    }
    
    // Utility methods for compatibility
    
    public void clear() {
        LOGGER.debug("Clearing element with selector: {}", selector);
        locator.clear(new Locator.ClearOptions().setTimeout(timeoutInMilliseconds));
    }
    
    public void sendKeys(CharSequence... keysToSend) {
        String text = String.join("", keysToSend);
        LOGGER.debug("Sending keys '{}' to element with selector: {}", text, selector);
        // Clear first, then type (similar to SmartWebElement behavior)
        clear();
        locator.type(text, new Locator.TypeOptions().setTimeout(timeoutInMilliseconds));
    }
    
    public void sendKeys(long timeoutInSeconds, CharSequence... keysToSend) {
        String text = String.join("", keysToSend);
        LOGGER.debug("Sending keys '{}' to element with selector: {} (timeout: {}s)", text, selector, timeoutInSeconds);
        clear();
        locator.type(text, new Locator.TypeOptions().setTimeout((int)(timeoutInSeconds * 1000)));
    }
    
    // Wait methods using Playwright's native waiting
    
    public void waitForVisible() {
        LOGGER.debug("Waiting for element to be visible: {}", selector);
        locator.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout(timeoutInMilliseconds));
    }
    
    public void waitForVisible(long timeoutInSeconds) {
        LOGGER.debug("Waiting for element to be visible: {} (timeout: {}s)", selector, timeoutInSeconds);
        locator.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout((int)(timeoutInSeconds * 1000)));
    }
    
    public void waitForHidden() {
        LOGGER.debug("Waiting for element to be hidden: {}", selector);
        locator.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.HIDDEN)
            .setTimeout(timeoutInMilliseconds));
    }
    
    // Getter methods for debugging and advanced usage
    
    public String getSelector() {
        return selector;
    }
    
    public Locator getLocator() {
        return locator;
    }
    
    public Page getPage() {
        return page;
    }
}