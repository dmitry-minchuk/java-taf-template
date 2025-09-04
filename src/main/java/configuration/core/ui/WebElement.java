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

public class WebElement {
    
    protected final static Logger LOGGER = LogManager.getLogger(WebElement.class);
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
    private final WebElement parentElement;
    
    public WebElement(Page page, String selector) {
        this.page = page;
        this.selector = selector;
        this.locator = page.locator(selector);
        this.elementName = "Element";
        this.parentElement = null; // Page-level element has no parent
        this.timeoutInMilliseconds = DEFAULT_TIMEOUT_MS;
    }
    
    public WebElement(Page page, String selector, String elementName) {
        this.page = page;
        this.selector = selector;
        this.locator = page.locator(selector);
        this.elementName = elementName != null ? elementName : "Element";
        this.parentElement = null; // Page-level element has no parent
        this.timeoutInMilliseconds = DEFAULT_TIMEOUT_MS;
    }
    
    public WebElement(WebElement parent, String selector) {
        this.page = parent.page;
        this.selector = selector;
        this.locator = parent.locator.locator(selector);
        this.elementName = "Element";
        this.parentElement = parent; // Store parent reference for scoping
        this.timeoutInMilliseconds = parent.timeoutInMilliseconds;
    }
    
    public WebElement(WebElement parent, String selector, String elementName) {
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

    public void click(int timeoutInMillis) {
        isVisible(timeoutInMillis);
        LOGGER.info("Clicking with increased timeout {} ", elementName);
        locator.click();
    }

    public void doubleClick() {
        isVisible();
        LOGGER.info("Double clicking {} ", elementName);
        locator.dblclick();
    }

    public void press(String key) {
        isVisible();
        LOGGER.info("Pressing {} on {}", key, elementName);
        locator.press(key);
    }
    
    public void fill(String text) {
        isVisible();
        LOGGER.info("Filling {} with text: '{}'", elementName, text);
        locator.fill(text);
    }

    public void fillSequentially(String text) {
        isVisible();
        LOGGER.info("Filling Sequentially {} with text: '{}'", elementName, text);
        locator.pressSequentially(text);
    }
    
    public String getText() {
        isVisible();
        String text = locator.textContent();
        LOGGER.info("Getting text from {}: '{}'", elementName, text);
        return text;
    }

    public String getText(int timeoutInMillis) {
        WaitUtil.sleep(timeoutInMillis);
        return getText();
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
            waitForVisible(100);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean isVisible(int timeoutInMillis) {
        try {
            waitForVisible(timeoutInMillis);
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
    public WebElement format(Object... args) {
        String formattedSelector = String.format(this.selector, args);
        if (this.parentElement != null) {
            return new WebElement(this.parentElement, formattedSelector, this.elementName);
        }
        return new WebElement(this.page, formattedSelector, this.elementName);
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
    
    public WebElement waitForVisible() {
        LOGGER.info("Waiting for {} to be visible", elementName);
        locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE));
        return this;
    }
    
    public WebElement waitForVisible(long timeoutInMillis) {
        LOGGER.info("Waiting for {} to be visible (timeout: {}s)", elementName, timeoutInMillis);
        locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.VISIBLE)
                .setTimeout((int)(timeoutInMillis)));
        return this;
    }
    
    public WebElement waitForHidden(long timeoutInMillis) {
        LOGGER.info("Waiting for {} to be hidden", elementName);
        locator.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN)
                .setTimeout((int)(timeoutInMillis)));
        return this;
    }
    
    // Interaction methods
    
    public WebElement hover() {
        LOGGER.info("Hovering over {}", elementName);
        locator.hover();
        return this;
    }
    
    // CSS properties
    
    public String getCssValue(String propertyName) {
        LOGGER.info("Getting CSS property '{}' from {}", propertyName, elementName);
        return locator.evaluate("el => window.getComputedStyle(el)." + propertyName).toString();
    }

    public boolean isEnabled() {
        LOGGER.info("Checking if {} is enabled", elementName);
        return locator.isEnabled();
    }

    public void selectOption(String value) {
        LOGGER.info("Selecting option '{}' in {}", value, elementName);
        locator.selectOption(value);
    }
}