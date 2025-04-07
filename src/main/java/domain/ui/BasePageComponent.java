package domain.ui;

import configuration.ProjectConfiguration;
import configuration.PropertyNameSpace;
import configuration.core.SmartElementFactory;
import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public abstract class BasePageComponent {

    @Getter
    private By rootLocator;
    private WebDriver driver;
    private int timeoutInSeconds;

    // Protected constructor to be called by subclasses
    protected BasePageComponent() {
        // Will be initialized by factory
        this.timeoutInSeconds = Integer.parseInt(ProjectConfiguration.getProperty(PropertyNameSpace.WEB_ELEMENT_EXPLICIT_WAIT));
    }

    // This will be called by the SmartElementFactory during initialization
    public void init(WebDriver driver, By rootLocator) {
        this.driver = driver;
        this.rootLocator = rootLocator;
        // Initialize all @FindBy elements inside the component
        SmartElementFactory.initElements(driver, this);
    }

    // Check if component is displayed
    public boolean isDisplayed() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
            return wait.until(ExpectedConditions.presenceOfElementLocated(rootLocator)).isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    // Allows getting the driver in component subclasses
    protected WebDriver getDriver() {
        return driver;
    }
}
