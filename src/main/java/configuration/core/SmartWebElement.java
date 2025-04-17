package configuration.core;

import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import helpers.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.util.function.Consumer;
import java.util.function.Function;

public class SmartWebElement {

    protected final static Logger LOGGER = LogManager.getLogger(SmartWebElement.class);
    private final int timeoutInSeconds = Integer.parseInt(ProjectConfiguration.getProperty(PropertyNameSpace.WEB_ELEMENT_EXPLICIT_WAIT));
    private final WebDriver driver;
    private final By locator;
    private WebElement parentElement;
    private By parentLocator;

    public SmartWebElement(WebDriver driver, By locator) {
        this.driver = driver;
        this.locator = locator;
    }

    public SmartWebElement(WebDriver driver, By locator, By parentLocator) {
        this.driver = driver;
        this.locator = locator;
        this.parentLocator = parentLocator;
    }

    public SmartWebElement(WebDriver driver, By locator, WebElement parentElement, By parentLocator) {
        this.driver = driver;
        this.locator = locator;
        this.parentElement = parentElement;
        this.parentLocator = parentLocator;
    }

    // Basic logic for element lookup
    public WebElement getUnwrappedElement() {
        if (parentLocator != null) {
            WaitUtil.waitUntil(driver, ExpectedConditions.elementToBeClickable(parentLocator), timeoutInSeconds);
            WebElement parent = driver.findElement(parentLocator);
            WaitUtil.waitUntil(parent, locator, timeoutInSeconds);
            return parent.findElement(locator);
        } else {
            WaitUtil.waitUntil(driver, ExpectedConditions.elementToBeClickable(locator), timeoutInSeconds);
            return driver.findElement(locator);
        }
    }

    // Retry logic for applying several attempts to do something with the element
    protected <T> T performWithRetry(Function<WebElement, T> action, String actionName) {
        WaitUtil.waitUntilPageIsReady(driver, timeoutInSeconds);
        int attempts = 0;
        int retryCount = 3;
        WebElement element = null;
        while (attempts < retryCount) {
            try {
                element = getUnwrappedElement();
                return action.apply(element);
            } catch (NoSuchElementException | StaleElementReferenceException e) {
                LOGGER.warn("Element not found or stale during '{}', retrying... (Attempt {}/{})", actionName, attempts + 1, retryCount, e);
                attempts++;
                WaitUtil.sleep(500);
            }
        }
        element = getUnwrappedElement();
        return action.apply(element);
    }

    // Retry logic for applying several attempts to do something with the element
    protected void performWithRetry(Consumer<WebElement> action, String actionName) {
        WaitUtil.waitUntilPageIsReady(driver, timeoutInSeconds);
        int attempts = 0;
        int retryCount = 3;
        WebElement element = null;
        while (attempts < retryCount) {
            try {
                element = getUnwrappedElement();
                action.accept(element);
                return;
            } catch (NoSuchElementException | StaleElementReferenceException e) {
                LOGGER.warn("Element not found or stale during '{}', retrying... (Attempt {}/{})", actionName, attempts + 1, retryCount, e);
                attempts++;
                WaitUtil.sleep(500);
            }
        }
        element = getUnwrappedElement();
        action.accept(element);
    }

    // Actions for SmartWebElement
    public void click() {
        performWithRetry(WebElement::click, "click");
    }

    public void sendKeys(CharSequence... keysToSend) {
        performWithRetry(element -> {
            element.clear();
            element.sendKeys(keysToSend);
            return null;
        }, "sendKeys");
    }

    public String getText() {
        return performWithRetry(WebElement::getText, "getText");
    }

    public boolean isDisplayed() {
        return performWithRetry(WebElement::isDisplayed, "isDisplayed");
    }

    public String getAttribute(String name) {
        return performWithRetry((Function<WebElement, String>) element -> element.getDomAttribute(name), "getAttribute(" + name + ")");
    }

    public void clear() {
        performWithRetry(WebElement::clear, "clear");
    }

    public void selectByVisibleText(String text) {
        performWithRetry(element -> {
            Select select = new Select(element);
            select.selectByVisibleText(text);
            return null; // Consumer не возвращает значение
        }, "selectByVisibleText(" + text + ")");
    }

    // SmartWebElement.format(locator) logic here:
    public SmartWebElement format(Object... args) {
        String formattedLocatorString = formatByToString(this.locator, args);
        By formattedBy = createByFromLocatorString(formattedLocatorString);
        return new SmartWebElement(this.driver, formattedBy, this.parentElement, this.parentLocator);
    }

    private String formatByToString(By by, Object... args) {
        String locatorString = by.toString();
        String value = locatorString.substring(locatorString.indexOf(':') + 2); // Extract the actual locator value
        return String.format(value, args);
    }

    private By createByFromLocatorString(String locatorString) {
        if (locatorString.startsWith("//") || locatorString.startsWith("./") || locatorString.startsWith("../")) {
            return By.xpath(locatorString);
        } else if (locatorString.matches("^[.#]?[a-zA-Z_][a-zA-Z0-9_-]*$") || locatorString.contains("[")) { // Simple CSS check
            return By.cssSelector(locatorString);
        } else if (locatorString.startsWith("id=")) {
            return By.id(locatorString.substring(3));
        } else if (locatorString.startsWith("name=")) {
            return By.name(locatorString.substring(5));
        } else if (locatorString.startsWith("class=")) {
            return By.className(locatorString.substring(6));
        } else if (locatorString.startsWith("tag=")) {
            return By.tagName(locatorString.substring(4));
        } else if (locatorString.startsWith("link=")) {
            return By.linkText(locatorString.substring(5));
        } else if (locatorString.startsWith("partial link=")) {
            return By.partialLinkText(locatorString.substring(13));
        }
        return By.xpath(locatorString); // Default to XPath if type is not clearly identified
    }
}
