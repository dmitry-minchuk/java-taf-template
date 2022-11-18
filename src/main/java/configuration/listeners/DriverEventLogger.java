package configuration.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverListener;

import java.util.Arrays;

public class DriverEventLogger implements WebDriverListener {
    protected static final Logger LOGGER = LogManager.getLogger(DriverEventLogger.class);

    @Override
    public void beforeGet(WebDriver driver, String url) {
        LOGGER.info(String.format("Opening url: %s", url));
    }

    @Override
    public void beforeClick(WebElement element) {
        LOGGER.info(String.format("Click on element: '%s'", getElementIdentifier(element)));
    }

    @Override
    public void beforeSendKeys(WebElement element, CharSequence... keysToSend) {
        LOGGER.info(String.format("Type text '%s' into '%s'", Arrays.toString(keysToSend), getElementIdentifier(element)));
    }

    @Override
    public void beforeClear(WebElement element) {
        LOGGER.info(String.format("Clear '%s'", getElementIdentifier(element)));
    }

    @Override
    public void beforeAccept(Alert alert) {
        LOGGER.info(String.format("Accept alert '%s'", alert.getText()));
    }

    @Override
    public void beforeMaximize(WebDriver.Window window) {
        LOGGER.info(String.format("Maximize window to height:'%s' width:'%s'", window.getSize().height, window.getSize().width));
    }

    @Override
    public void beforeQuit(WebDriver driver) {
        LOGGER.info("Closing browser");
    }

    @Override
    public void beforeClose(WebDriver driver) {
        LOGGER.info(String.format("Closing browser tab '%s' with title '%s'", driver.getWindowHandle(), driver.getTitle()));
    }

    private String getElementIdentifier(WebElement element) {
        if(!element.getAccessibleName().isBlank() && !element.getAccessibleName().isBlank())
            return element.getAccessibleName();
        return element.getTagName();
    }
}
