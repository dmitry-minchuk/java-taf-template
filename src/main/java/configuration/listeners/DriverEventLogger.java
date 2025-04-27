package configuration.listeners;

import com.epam.reportportal.service.ReportPortal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverListener;

import java.util.Arrays;
import java.util.Date;

public class DriverEventLogger implements WebDriverListener {
    protected static final Logger LOGGER = LogManager.getLogger(DriverEventLogger.class);

    @Override
    public void beforeGet(WebDriver driver, String url) {
        String logMessage = String.format("Opening url: %s", url);
        LOGGER.info(logMessage);
        reportPortalLogger(logMessage);
    }

    @Override
    public void beforeClick(WebElement element) {
        String logMessage = String.format("Click on element: '%s'", getElementIdentifier(element));
        LOGGER.info(logMessage);
        reportPortalLogger(logMessage);
    }

    @Override
    public void beforeSendKeys(WebElement element, CharSequence... keysToSend) {
        String logMessage = String.format("Type text '%s' into '%s'", Arrays.toString(keysToSend), getElementIdentifier(element));
        LOGGER.info(logMessage);
        reportPortalLogger(logMessage);
    }

    @Override
    public void beforeClear(WebElement element) {
        String logMessage = String.format("Clear '%s'", getElementIdentifier(element));
        LOGGER.info(logMessage);
        reportPortalLogger(logMessage);
    }

    @Override
    public void beforeAccept(Alert alert) {
        String logMessage = String.format("Accept alert '%s'", alert.getText());
        LOGGER.info(logMessage);
        reportPortalLogger(logMessage);
    }

    @Override
    public void beforeMaximize(WebDriver.Window window) {
        String logMessage = String.format("Maximize window to height:'%s' width:'%s'", window.getSize().height, window.getSize().width);
        LOGGER.info(logMessage);
        reportPortalLogger(logMessage);
    }

    @Override
    public void beforeQuit(WebDriver driver) {
        String logMessage = "Closing browser";
        LOGGER.info(logMessage);
        reportPortalLogger(logMessage);
    }

    @Override
    public void beforeClose(WebDriver driver) {
        String logMessage = String.format("Closing browser tab '%s' with title '%s'", driver.getWindowHandle(), driver.getTitle());
        LOGGER.info(logMessage);
        reportPortalLogger(logMessage);
    }

    private String getElementIdentifier(WebElement element) {
        if(!element.getAccessibleName().isBlank() && !element.getAccessibleName().isBlank())
            return element.getAccessibleName();
        return element.getTagName();
    }

    private void reportPortalLogger(String logMessage) {
        ReportPortal.emitLog(logMessage, "INFO", new Date());
    }
}
