package helpers.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class WaitUtil {
    protected final static Logger LOGGER = LogManager.getLogger(WaitUtil.class);
    private static final int WAIT_POLL_INTERVAL_MS = 200;

    public static boolean waitUntil(WebDriver driver, ExpectedCondition<?> condition, long timeoutSec) {
        boolean result;
        sleep(100);
        Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSec), Duration.ofMillis(WAIT_POLL_INTERVAL_MS))
                .ignoring(Exception.class);
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

    public static void sleep(int timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
