package configuration.core;

import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import helpers.utils.WaitUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

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

    public void click() {
        getUnwrappedElement().click();
    }

    public void sendKeys(CharSequence... keysToSend) {
        getUnwrappedElement().clear();
        getUnwrappedElement().sendKeys(keysToSend);
    }

    public String getText() {
        return getUnwrappedElement().getText();
    }

    public boolean isDisplayed() {
        return getUnwrappedElement().isDisplayed();
    }

    public String getAttribute(String name) {
        return getUnwrappedElement().getDomAttribute(name);
    }

    public void clear() {
        getUnwrappedElement().getText();
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
