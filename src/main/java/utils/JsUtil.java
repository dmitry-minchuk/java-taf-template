package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class JsUtil {
    protected static final Logger LOGGER = LogManager.getLogger(JsUtil.class);

    public static void clickElement(WebDriver driver, WebElement element) {
        JavascriptExecutor executor = (JavascriptExecutor) driver;
        executor.executeScript("arguments[0].click();", element);
        LOGGER.info("Click with JS for element " + element.toString() + " was successfully completed.");
    }
}
