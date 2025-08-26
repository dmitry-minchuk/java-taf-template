package configuration.core.ui;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import helpers.utils.WaitUtil;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Playwright-based element wrapper with native wait strategies
public class PlaywrightWebElement {
    
    protected final static Logger LOGGER = LogManager.getLogger(PlaywrightWebElement.class);
    private static final int DEFAULT_TIMEOUT_MS = Integer.parseInt(ProjectConfiguration.getProperty(PropertyNameSpace.PLAYWRIGHT_DEFAULT_TIMEOUT));
    private final int timeoutInMilliseconds;
    @Getter
    private final Page page;
    @Getter
    private final String selector;
    @Getter
    private final Locator locator;
    private final String elementName;
    @Getter
    private final PlaywrightWebElement parentElement;
    
    public PlaywrightWebElement(Page page, String selector) {
        this.page = page;
        this.selector = selector;
        this.locator = page.locator(selector);
        this.elementName = "Element";
        this.parentElement = null; // Page-level element has no parent
        this.timeoutInMilliseconds = DEFAULT_TIMEOUT_MS;
    }
    
    public PlaywrightWebElement(Page page, String selector, String elementName) {
        this.page = page;
        this.selector = selector;
        this.locator = page.locator(selector);
        this.elementName = elementName != null ? elementName : "Element";
        this.parentElement = null; // Page-level element has no parent
        this.timeoutInMilliseconds = DEFAULT_TIMEOUT_MS;
    }
    
    public PlaywrightWebElement(PlaywrightWebElement parent, String selector) {
        this.page = parent.page;
        this.selector = selector;
        this.locator = parent.locator.locator(selector);
        this.elementName = "Element";
        this.parentElement = parent; // Store parent reference for scoping
        this.timeoutInMilliseconds = parent.timeoutInMilliseconds;
    }
    
    public PlaywrightWebElement(PlaywrightWebElement parent, String selector, String elementName) {
        this.page = parent.page;
        this.selector = selector;
        this.locator = parent.locator.locator(selector);
        this.elementName = elementName != null ? elementName : "Element";
        this.parentElement = parent; // Store parent reference for scoping
        this.timeoutInMilliseconds = parent.timeoutInMilliseconds;
    }
    
    
    // Core Actions

    public void click() {
        isVisible();
        LOGGER.info("Clicking {} ", elementName);
        locator.click();
    }
    
    public void fill(String text) {
        isVisible();
        LOGGER.info("Filling {} with text: '{}'", elementName, text);
        locator.fill(text);
    }
    
    public String getText() {
        isVisible();
        WaitUtil.sleep(500);
        String text = locator.textContent();
        LOGGER.info("Getting text from {}: '{}'", elementName, text);
        return text;
    }
    
    public String getAttribute(String name) {
        isVisible();
        String value = locator.getAttribute(name);
        LOGGER.info("Getting attribute '{}' from {}: '{}'", name, elementName, value);
        return value;
    }
    
    // Visibility and State Checks
    
    public boolean isVisible() {
        try {
            waitForVisible();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean isVisible(int timeoutInSeconds) {
        try {
            waitForVisible(timeoutInSeconds);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    
    public boolean isSelected() {
        isVisible();
        return locator.isChecked();
    }
    
    // Selection methods for dropdowns
    
    public void selectByVisibleText(String text) {
        LOGGER.info("Selecting option '{}' in {}", text, elementName);
        locator.selectOption(text);
    }
    
    @Deprecated
    public void selectByVisibleText(String text, long timeoutInSeconds) {
        LOGGER.info("Selecting option '{}' in {} (deprecated timeout method)", text, elementName);
        locator.selectOption(text);
    }
    
    // Dynamic locator formatting - Builder pattern implementation
    public PlaywrightWebElement format(Object... args) {
        String formattedSelector = String.format(this.selector, args);
        if (this.parentElement != null) {
            return new PlaywrightWebElement(this.parentElement, formattedSelector, this.elementName);
        }
        return new PlaywrightWebElement(this.page, formattedSelector, this.elementName);
    }

    // Utility methods
    
    public void clear() {
        LOGGER.info("Clearing {}", elementName);
        locator.clear();
        WaitUtil.sleep(500);
    }
    
    public void sendKeys(CharSequence... keysToSend) {
        String text = String.join("", keysToSend);
        LOGGER.info("Sending keys '{}' to {}", text, elementName);
        
        // Check if this is a file input element - if so, use setInputFiles instead
        String inputType = locator.getAttribute("type");
        if ("file".equals(inputType)) {
            LOGGER.info("Detected file input, using setInputFiles instead of sendKeys");
            setInputFiles(text);
            return;
        }
        
        clear();
        locator.fill(text);
    }
    
    // File upload method for input[type=file] elements  
    public void setInputFiles(String... filePaths) {
        LOGGER.info("Setting input files {} to {}", java.util.Arrays.toString(filePaths), elementName);
        java.nio.file.Path[] paths = java.util.Arrays.stream(filePaths)
            .map(java.nio.file.Paths::get)
            .toArray(java.nio.file.Path[]::new);
        locator.setInputFiles(paths);
    }
    
    // Explicit wait methods
    
    public void waitForVisible() {
        LOGGER.info("Waiting for {} to be visible", elementName);
        locator.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE));
    }
    
    public void waitForVisible(long timeoutInSeconds) {
        LOGGER.info("Waiting for {} to be visible (timeout: {}s)", elementName, timeoutInSeconds);
        locator.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.VISIBLE)
            .setTimeout((int)(timeoutInSeconds * 1000)));
    }
    
    public void waitForHidden() {
        LOGGER.info("Waiting for {} to be hidden", elementName);
        locator.waitFor(new Locator.WaitForOptions()
            .setState(WaitForSelectorState.HIDDEN));
    }
    
    // Interaction methods
    
    public void hover() {
        LOGGER.info("Hovering over {}", elementName);
        locator.hover();
    }
}