package configuration.listeners;

import com.codeborne.selenide.Screenshots;
import com.codeborne.selenide.logevents.LogEvent;
import com.codeborne.selenide.logevents.LogEventListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class SelenideListener implements LogEventListener {
    protected static final Logger LOGGER = LogManager.getLogger(SelenideListener.class);

    @Override
    public void afterEvent(LogEvent logEvent) {
        String logStr = logEvent.getElement() + " : " + logEvent.getSubject() + " : " + logEvent.getStatus();
        // Logging into console
        LOGGER.info(logStr);
        File screenshot = Screenshots.takeScreenShotAsFile();
        // Logging into report
        if(logEvent.getStatus().equals(LogEvent.EventStatus.FAIL)) {
            //do smth
        } else {
            //do smth
        }
    }

    @Override
    public void beforeEvent(LogEvent logEvent) {
    }
}
