package configuration.core.ui;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;

// Playwright-based element wrapper with native wait strategies
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
        this.timeoutInMilliseconds = Integer.parseInt(
            ProjectConfiguration.getProperty(PropertyNameSpace.WEB_ELEMENT_EXPLICIT_WAIT)
        ) * 1000;
    }
    
    // Constructor for child locators
    public PlaywrightWebElement(PlaywrightWebElement parent, String selector) {
        this.page = parent.page;
        this.selector = selector;
        this.locator = parent.locator.locator(selector);
        this.timeoutInMilliseconds = parent.timeoutInMilliseconds;
    }
    
    // Core Actions
    
    public void click() {
        LOGGER.debug("Clicking element with selector: {}", selector);
        locator.click();
    }
    
    @Deprecated
    public void click(long timeoutInSeconds) {
        LOGGER.debug("Clicking element with selector: {} (deprecated timeout method)", selector);
        locator.click();
    }
    
    public void fill(String text) {
        LOGGER.debug("Filling element with selector: {} with text: {}", selector, text);
        locator.fill(text);
    }
    
    @Deprecated
    public void fill(String text, long timeoutInSeconds) {
        LOGGER.debug("Filling element with selector: {} with text: {} (deprecated timeout method)", selector, text);
        locator.fill(text);
    }
    
    public String getText() {
        LOGGER.debug("Getting text from element with selector: {}", selector);
        return locator.textContent();
    }
    
    public String getAttribute(String name) {
        LOGGER.debug("Getting attribute '{}' from element with selector: {}", name, selector);
        return locator.getAttribute(name);
    }
    
    // Visibility and State Checks
    
    public boolean isVisible() {
        try {
            LOGGER.debug("Checking visibility of element with selector: {}", selector);
            return locator.isVisible();
        } catch (Exception e) {
            LOGGER.debug("Element not visible: {}", selector);
            return false;
        }
    }
    
    @Deprecated
    public boolean isVisible(int timeoutInSeconds) {
        return isVisible();
    }
    
    public boolean isDisplayed() {
        return isVisible();
    }
    
    public boolean isDisplayed(int timeoutInSeconds) {
        return isVisible(timeoutInSeconds);
    }
    
    public boolean isEnabled() {
        try {
            return locator.isEnabled();
        } catch (Exception e) {
            LOGGER.debug("Element enabled check failed for selector: {}", selector);
            return false;
        }
    }
    
    public boolean isSelected() {
        try {
            return locator.isChecked();
        } catch (Exception e) {
            LOGGER.debug("Element selected check failed for selector: {}", selector);
            return false;
        }
    }
    
    // Selection methods for dropdowns
    
    public void selectByVisibleText(String text) {
        LOGGER.debug("Selecting option '{}' in element with selector: {}", text, selector);
        locator.selectOption(text);
    }
    
    @Deprecated
    public void selectByVisibleText(String text, long timeoutInSeconds) {
        LOGGER.debug("Selecting option '{}' in element with selector: {} (deprecated timeout method)", text, selector);
        locator.selectOption(text);
    }
    
    // Format functionality
    
    public PlaywrightWebElement format(Object... args) {
        String formattedSelector = String.format(this.selector, args);
        LOGGER.debug("Formatting selector from '{}' to '{}'", this.selector, formattedSelector);
        return new PlaywrightWebElement(this.page, formattedSelector);
    }
    
    // Utility methods
    
    public void clear() {
        LOGGER.debug("Clearing element with selector: {}", selector);
        locator.clear();
    }
    
    public void sendKeys(CharSequence... keysToSend) {
        String text = String.join("", keysToSend);
        LOGGER.debug("Sending keys '{}' to element with selector: {}", text, selector);
        clear();
        locator.type(text);
    }
    
    @Deprecated
    public void sendKeys(long timeoutInSeconds, CharSequence... keysToSend) {
        String text = String.join("", keysToSend);
        LOGGER.debug("Sending keys '{}' to element with selector: {} (deprecated timeout method)", text, selector);
        clear();
        locator.type(text);
    }
    
    // Explicit wait methods
    
    public void waitForVisible() {
        LOGGER.debug("Explicitly waiting for element to be visible: {}", selector);
        locator.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE));
    }
    
    public void waitForVisible(long timeoutInSeconds) {
        LOGGER.debug("Explicitly waiting for element to be visible: {} (timeout: {}s)", selector, timeoutInSeconds);
        locator.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout((int)(timeoutInSeconds * 1000)));
    }
    
    public void waitForHidden() {
        LOGGER.debug("Explicitly waiting for element to be hidden: {}", selector);
        locator.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.HIDDEN));
    }
    
    // Getter methods
    
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