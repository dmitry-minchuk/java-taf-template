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
    private String elementName;
    
    public PlaywrightWebElement(Page page, String selector) {
        this.page = page;
        this.selector = selector;
        this.locator = page.locator(selector);
        this.elementName = generateElementName(selector);
        this.timeoutInMilliseconds = Integer.parseInt(
            ProjectConfiguration.getProperty(PropertyNameSpace.WEB_ELEMENT_EXPLICIT_WAIT)
        ) * 1000;
    }
    
    public PlaywrightWebElement(Page page, String selector, String elementName) {
        this.page = page;
        this.selector = selector;
        this.locator = page.locator(selector);
        this.elementName = elementName;
        this.timeoutInMilliseconds = Integer.parseInt(
            ProjectConfiguration.getProperty(PropertyNameSpace.WEB_ELEMENT_EXPLICIT_WAIT)
        ) * 1000;
    }
    
    // Constructor for child locators
    public PlaywrightWebElement(PlaywrightWebElement parent, String selector) {
        this.page = parent.page;
        this.selector = selector;
        this.locator = parent.locator.locator(selector);
        this.elementName = generateElementName(selector);
        this.timeoutInMilliseconds = parent.timeoutInMilliseconds;
    }
    
    public PlaywrightWebElement(PlaywrightWebElement parent, String selector, String elementName) {
        this.page = parent.page;
        this.selector = selector;
        this.locator = parent.locator.locator(selector);
        this.elementName = elementName;
        this.timeoutInMilliseconds = parent.timeoutInMilliseconds;
    }
    
    private String generateElementName(String selector) {
        // Extract meaningful name from selector
        if (selector.contains("id=")) {
            String id = selector.substring(selector.indexOf("id=") + 3);
            if (id.contains("'") || id.contains("\"")) {
                id = id.substring(1, id.indexOf(id.charAt(0), 1));
            }
            return "Element[" + id + "]";
        }
        
        if (selector.contains("text('") || selector.contains("text(\"")) {
            int start = selector.indexOf("text(") + 6;
            int end = selector.indexOf(selector.charAt(start - 1), start);
            String text = selector.substring(start, end);
            return "Element[" + text + "]";
        }
        
        if (selector.contains(":has(span:text('") || selector.contains(":has(span:text(\"")) {
            int start = selector.indexOf(":has(span:text(") + 16;
            int end = selector.indexOf(selector.charAt(start - 1), start);
            String text = selector.substring(start, end);
            return "Element[" + text + "]";
        }
        
        if (selector.startsWith("#")) {
            return "Element[" + selector.substring(1) + "]";
        }
        
        if (selector.contains("class=") || selector.contains(".")) {
            String className = selector.contains(".") ? 
                selector.substring(selector.indexOf(".") + 1).split("[\\s\\[\\:]")[0] :
                selector.substring(selector.indexOf("class=") + 6).split("'\"")[0];
            return "Element[" + className + "]";
        }
        
        if (selector.contains("input")) return "InputField";
        if (selector.contains("button")) return "Button";
        if (selector.contains("div")) return "Container";
        if (selector.contains("span")) return "TextElement";
        if (selector.contains("li")) return "ListItem";
        
        return "Element[" + selector.substring(0, Math.min(20, selector.length())) + "]";
    }
    
    // Core Actions
    
    public void click() {
        LOGGER.info("Clicking {}", elementName);
        locator.click();
    }
    
    @Deprecated
    public void click(long timeoutInSeconds) {
        LOGGER.info("Clicking {} (deprecated timeout method)", elementName);
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
    
    // Format functionality
    
    public PlaywrightWebElement format(Object... args) {
        String formattedSelector = String.format(this.selector, args);
        LOGGER.info("Formatting selector for {}", elementName);
        return new PlaywrightWebElement(this.page, formattedSelector);
    }
    
    // Utility methods
    
    public void clear() {
        LOGGER.info("Clearing {}", elementName);
        locator.clear();
    }
    
    public void sendKeys(CharSequence... keysToSend) {
        String text = String.join("", keysToSend);
        LOGGER.info("Sending keys '{}' to {}", text, elementName);
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