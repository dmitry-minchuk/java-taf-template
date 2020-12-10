package configuration.listeners;

import com.codeborne.selenide.Screenshots;
import com.codeborne.selenide.logevents.LogEvent;
import com.codeborne.selenide.logevents.LogEventListener;
import com.epam.reportportal.service.ReportPortal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Date;

public class SelenideListener implements LogEventListener {
    protected static final Logger LOGGER = LogManager.getLogger(SelenideListener.class);

    @Override
    public void afterEvent(LogEvent logEvent) {
        String logStr = logEvent.getElement() + " : " + logEvent.getSubject() + " : " + logEvent.getStatus();
        // Logging into console
        LOGGER.info(logStr);
        File screenshot = Screenshots.takeScreenShotAsFile();
        // Logging into Report Portal
        if(logEvent.getStatus().equals(LogEvent.EventStatus.FAIL)) {
            sendScreenShotWithLogs(screenshot, logStr);
        } else {
            // ReportPortal.emitLog(logStr, "INFO", new Date());
            sendScreenShotWithLogs(screenshot, logStr);
        }
    }

    @Override
    public void beforeEvent(LogEvent logEvent) {

    }

    private void sendScreenShotWithLogs(File file, String logStr) {
        if(file != null && file.exists()) {
            ReportPortal.emitLog(logStr, "INFO", new Date(), Screenshots.takeScreenShotAsFile());
        } else {
            LOGGER.info("Cannot take a Screenshot!");
            ReportPortal.emitLog(logStr, "INFO", new Date());
        }
    }
}
