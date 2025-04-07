package domain.ui;

import configuration.ProjectConfiguration;
import configuration.PropertyNameSpace;
import configuration.core.SmartElementFactory;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class BasePageComponent {

    @Getter
    private By rootLocatorBy;
    @Getter
    private WebElement rootElement;
    private WebDriver driver;
    private int timeoutInSeconds;

    // Protected constructor to be called by subclasses
    protected BasePageComponent() {
        this.timeoutInSeconds = Integer.parseInt(ProjectConfiguration.getProperty(PropertyNameSpace.WEB_ELEMENT_EXPLICIT_WAIT));
    }

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
    public boolean isDisplayed() {
        try {
            if (rootElement != null) {
                return rootElement.isDisplayed();
            } else if (rootLocatorBy != null) {
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
                return wait.until(ExpectedConditions.presenceOfElementLocated(rootLocatorBy)).isDisplayed();
            }
            return false; // If nor rootElement neither rootLocatorBy not set
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    // Allows getting the driver in component subclasses
    protected WebDriver getDriver() {
        return driver;
    }
}
