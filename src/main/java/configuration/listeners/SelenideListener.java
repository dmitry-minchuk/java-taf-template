package configuration.listeners;

import com.codeborne.selenide.Screenshots;
import com.codeborne.selenide.logevents.LogEvent;
import com.codeborne.selenide.logevents.LogEventListener;
import com.epam.reportportal.service.ReportPortal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

public class SelenideListener implements LogEventListener {
    protected static final Logger LOGGER = LogManager.getLogger(SelenideListener.class);

    @Override
    public void afterEvent(LogEvent logEvent) {
        String logStr = logEvent.getElement() + " : " + logEvent.getSubject() + " : " + logEvent.getStatus();
        // Logging into console
        LOGGER.info(logStr);
        // Logging into Report Portal
        if(logEvent.getStatus().equals(LogEvent.EventStatus.FAIL)) {
            ReportPortal.emitLog(logStr, "INFO", new Date(), Screenshots.takeScreenShotAsFile());
        } else {
            ReportPortal.emitLog(logStr, "INFO", new Date());
        }
    }

    @Override
    public void beforeEvent(LogEvent logEvent) {

    }
}
