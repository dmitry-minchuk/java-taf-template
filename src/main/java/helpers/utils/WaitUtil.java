package helpers.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class WaitUtil {
    protected final static Logger LOGGER = LogManager.getLogger(WaitUtil.class);
    private static final int WAIT_POLL_INTERVAL_MS = 500;

    public static boolean waitUntil(WebDriver driver, ExpectedCondition<?> condition, long timeoutSec) {
        boolean result;
        sleep(100);
        Wait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeoutSec))
                .pollingEvery(Duration.ofMillis(WAIT_POLL_INTERVAL_MS))
                .ignoring(NoSuchElementException.class, StaleElementReferenceException.class);

        try {
            wait.until(condition);
            result = true;
            LOGGER.debug("WaitUntil: finished successfully");
        } catch (Exception e) {
            LOGGER.error("WaitUntil: {}", condition.toString(), e);
            result = false;
        }
        return result;
    }

    public static boolean waitUntil(WebElement rootElement, By locator, long timeoutSec) {
        boolean result;
        sleep(100);
        Wait<WebElement> wait = new FluentWait<>(rootElement)
                .withTimeout(Duration.ofSeconds(timeoutSec))
                .pollingEvery(Duration.ofMillis(WAIT_POLL_INTERVAL_MS))
                .ignoring(NoSuchElementException.class, StaleElementReferenceException.class);

        try {
            wait.until(webElement -> {
                WebElement found = webElement.findElement(locator);
                return found.isDisplayed() ? found : null;
            });
            result = true;
            LOGGER.debug("WaitUntil: lookup inside rootElement finished successfully");
        } catch (Exception e) {
            LOGGER.debug("WaitUntil: {}", locator.toString(), e);
            result = false;
        }
        return result;
    }

    public static List<WebElement> waitForElementsList(WebDriver driver, By locator, long timeoutSec) {
        Wait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(java.time.Duration.ofSeconds(timeoutSec))
                .pollingEvery(java.time.Duration.ofMillis(WAIT_POLL_INTERVAL_MS))
                .ignoring(NoSuchElementException.class, StaleElementReferenceException.class);

        try {
            return wait.until(d -> d.findElements(locator));
        } catch (TimeoutException e) {
            LOGGER.error("WaitUtil: Timeout waiting for elements: {}", locator.toString());
            return Collections.emptyList();
        }
    }

    public static List<WebElement> waitForElementsList(WebElement rootElement, By locator, long timeoutSec) {
        Wait<WebElement> wait = new FluentWait<>(rootElement)
                .withTimeout(java.time.Duration.ofSeconds(timeoutSec))
                .pollingEvery(java.time.Duration.ofMillis(WAIT_POLL_INTERVAL_MS))
                .ignoring(NoSuchElementException.class, StaleElementReferenceException.class);

        try {
            return wait.until(element -> element.findElements(locator));
        } catch (TimeoutException e) {
            LOGGER.error("WaitUtil: Timeout waiting for elements inside rootElement: {}", locator.toString());
            return Collections.emptyList();
        }
    }

    public static void waitUntilPageIsReady(WebDriver driver, int timeoutInSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
        wait.until(wd -> ((JavascriptExecutor) wd).executeScript("return document.readyState").equals("complete"));
        ExpectedCondition<Boolean> jQueryInactive = wd -> {
            try {
                return (Boolean) ((JavascriptExecutor) wd).executeScript("return typeof jQuery == 'undefined' || jQuery.active == 0");
            } catch (org.openqa.selenium.JavascriptException e) {
                return true;
            }
        };
        wait.until(jQueryInactive);
    }

    public static void sleep(int timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
