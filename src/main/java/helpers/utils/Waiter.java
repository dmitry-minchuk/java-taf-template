package helpers.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class Waiter {
    protected final static Logger LOGGER = LogManager.getLogger(Waiter.class);
    private static final int WAIT_POLL_INTERVAL_MS = 200;

    public static boolean waitUntil(WebDriver driver, ExpectedCondition<?> condition, long timeoutSec) {
        boolean result;
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSec), Duration.ofMillis(WAIT_POLL_INTERVAL_MS)).ignoring(WebDriverException.class)
                .ignoring(NoSuchSessionException.class);
        try {
            wait.until(condition);
            result = true;
            LOGGER.debug("waitUntil: finished true...");
        } catch (NoSuchElementException | TimeoutException e) {
            LOGGER.debug("waitUntil: NoSuchElementException | TimeoutException e..." + condition.toString());
            result = false;
        } catch (Exception e) {
            LOGGER.error("waitUntil: " + condition.toString(), e);
            result = false;
        }
        return result;
    }

    public static void sleep(int timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
