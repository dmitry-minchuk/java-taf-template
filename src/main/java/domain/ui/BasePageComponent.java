package domain.ui;

import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import configuration.core.SmartElementFactory;
import lombok.Getter;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class BasePageComponent {

    private final int timeoutInSeconds = Integer.parseInt(ProjectConfiguration.getProperty(PropertyNameSpace.WEB_ELEMENT_EXPLICIT_WAIT));
    @Getter
    private By rootLocatorBy;
    @Getter
    private WebElement rootElement;
    @Getter
    private WebDriver driver;

    // Protected constructor to be called by subclasses
    protected BasePageComponent() {}

    // Overloaded init method to accept By locator
    public void init(WebDriver driver, By rootLocatorBy) {
        this.driver = driver;
        this.rootLocatorBy = rootLocatorBy;
        this.rootElement = null; // nulls rootElement, if initialization uses By
        SmartElementFactory.initElements(driver, this);
    }

    // Overloaded init method to accept WebElement
    public void init(WebDriver driver, WebElement rootElement) {
        this.driver = driver;
        this.rootElement = rootElement;
        this.rootLocatorBy = null; // nulls rootLocatorBy, if initialization uses WebElement
        SmartElementFactory.initElements(driver, this);
    }

    // Check if component is displayed
    public boolean isPresent() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
        try {
            if (rootElement != null) {
                wait.until(ExpectedConditions.visibilityOf(rootElement));
                return rootElement.isDisplayed();
            } else if (rootLocatorBy != null) {
                WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(rootLocatorBy));
                return element.isDisplayed();
            }
            return false;
        } catch (NoSuchElementException | StaleElementReferenceException | TimeoutException e) {
            return false;
        }
    }
}
