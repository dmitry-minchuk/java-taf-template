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
    private final int timeoutInMilliseconds;
    @Getter
    private final Page page;
    @Getter
    private final String selector;
    @Getter
    private final Locator locator;
    private String elementName;
    
    public PlaywrightWebElement(Page page, String selector) {
        this.page = page;
        this.selector = selector;
        this.locator = page.locator(selector);
        this.elementName = "Element";
        this.timeoutInMilliseconds = Integer.parseInt(
            ProjectConfiguration.getProperty(PropertyNameSpace.WEB_ELEMENT_EXPLICIT_WAIT)
        ) * 1000;
    }
    
    public PlaywrightWebElement(Page page, String selector, String elementName) {
        this.page = page;
        this.selector = selector;
        this.locator = page.locator(selector);
        this.elementName = elementName != null ? elementName : "Element";
        this.timeoutInMilliseconds = Integer.parseInt(
            ProjectConfiguration.getProperty(PropertyNameSpace.WEB_ELEMENT_EXPLICIT_WAIT)
        ) * 1000;
    }
    
    public PlaywrightWebElement(PlaywrightWebElement parent, String selector) {
        this.page = parent.page;
        this.selector = selector;
        this.locator = parent.locator.locator(selector);
        this.elementName = "Element";
        this.timeoutInMilliseconds = parent.timeoutInMilliseconds;
    }
    
    public PlaywrightWebElement(PlaywrightWebElement parent, String selector, String elementName) {
        this.page = parent.page;
        this.selector = selector;
        this.locator = parent.locator.locator(selector);
        this.elementName = elementName != null ? elementName : "Element";
        this.timeoutInMilliseconds = parent.timeoutInMilliseconds;
    }
    
    
    // Core Actions
    
    public void click() {
        click(750);
    }
    
    @Deprecated
    public void click(int timeoutInMs) {
        WaitUtil.sleep(timeoutInMs);
        LOGGER.info("Clicking {} ", elementName);
        locator.click();
    }
    
    public void fill(String text) {
        LOGGER.info("Filling {} with text: '{}'", elementName, text);
        locator.fill(text);
    }
    
    @Deprecated
    public void fill(String text, long timeoutInSeconds) {
        LOGGER.info("Filling {} with text: '{}' (deprecated timeout method)", elementName, text);
        locator.fill(text);
    }
    
    public String getText() {
        WaitUtil.sleep(500);
        String text = locator.textContent();
        LOGGER.info("Getting text from {}: '{}'", elementName, text);
        return text;
    }
    
    public String getAttribute(String name) {
        String value = locator.getAttribute(name);
        LOGGER.info("Getting attribute '{}' from {}: '{}'", name, elementName, value);
        return value;
    }
    
    // Visibility and State Checks
    
    public boolean isVisible() {
        try {
            boolean visible = locator.isVisible();
            LOGGER.info("Checking visibility of {}: {}", elementName, visible);
            return visible;
        } catch (Exception e) {
            LOGGER.info("Checking visibility of {}: false (exception)", elementName);
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
            boolean enabled = locator.isEnabled();
            LOGGER.info("Checking if {} is enabled: {}", elementName, enabled);
            return enabled;
        } catch (Exception e) {
            LOGGER.info("Checking if {} is enabled: false (exception)", elementName);
            return false;
        }
    }
    
    public boolean isSelected() {
        try {
            boolean selected = locator.isChecked();
            LOGGER.info("Checking if {} is selected: {}", elementName, selected);
            return selected;
        } catch (Exception e) {
            LOGGER.info("Checking if {} is selected: false (exception)", elementName);
            return false;
        }
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
    
    // Format functionality removed - use String.format() directly with selectors
    
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
        locator.type(text);
    }
    
    @Deprecated
    public void sendKeys(long timeoutInSeconds, CharSequence... keysToSend) {
        String text = String.join("", keysToSend);
        LOGGER.info("Sending keys '{}' to {} (deprecated timeout method)", text, elementName);
        clear();
        locator.type(text);
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
}