package configuration.core;

import configuration.ProjectConfiguration;
import configuration.PropertyNameSpace;
import helpers.utils.Waiter;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class SmartWebElement {

    private final int timeoutInSeconds = Integer.parseInt(ProjectConfiguration.getProperty(PropertyNameSpace.WEB_ELEMENT_EXPLICIT_WAIT));
    private final WebDriver driver;
    private final By locator;
    private final int numberOfAttempts = 3;
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

    public WebElement getUnwrappedElement() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
        Waiter.sleep(50);
        if (parentLocator != null) {
            WebElement parent = wait.until(ExpectedConditions.presenceOfElementLocated(parentLocator));
            return parent.findElement(locator);
        } else {
            return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        }
    }

    public void click() {
        retryOnStale(WebElement::click);
    }

    public void sendKeys(CharSequence... keysToSend) {
        retryOnStale(el -> {
            el.clear();
            el.sendKeys(keysToSend);
            return null;
        });
    }

    public String getText() {
        return retryOnStale(WebElement::getText);
    }

    public boolean isDisplayed() {
        return retryOnStale(WebElement::isDisplayed);
    }

    public String getAttribute(String name) {
        return retryOnStale((SupplierWithException<String>) el -> el.getDomAttribute(name));
    }

    public void clear() {
        retryOnStale(WebElement::getText);
    }

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

    private void retryOnStale(VoidConsumerWithException action) {
        int attempts = numberOfAttempts;
        while (attempts-- > 0) {
            try {
                action.accept(getUnwrappedElement());
                return;
            } catch (StaleElementReferenceException e) {
                // retry
            }
        }
        throw new StaleElementReferenceException("Element still stale after multiple attempts: " + locator);
    }

    @FunctionalInterface
    private interface VoidConsumerWithException {
        void accept(WebElement el);
    }

    private <T> T retryOnStale(SupplierWithException<T> action) {
        int attempts = numberOfAttempts;
        while (attempts-- > 0) {
            try {
                return action.get(getUnwrappedElement());
            } catch (StaleElementReferenceException e) {
                // retry
            }
        }
        throw new StaleElementReferenceException("Element still stale after multiple attempts: " + locator);
    }

    @FunctionalInterface
    private interface SupplierWithException<T> {
        T get(WebElement el);
    }
}
