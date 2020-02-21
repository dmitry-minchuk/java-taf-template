package listeners;

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
        LOGGER.info(logEvent.getElement() + " : " + logEvent.getSubject() + " : " + logEvent.getStatus());
        ReportPortal.emitLog(logEvent.getElement() + " : " + logEvent.getSubject() + " : " + logEvent.getStatus(), "INFO", new Date(), new File("C:\\Users\\dminchuk\\Downloads\\photo_2020-01-27_15-39-13.jpg"));
    }

    @Override
    public void beforeEvent(LogEvent logEvent) {

    }
}
