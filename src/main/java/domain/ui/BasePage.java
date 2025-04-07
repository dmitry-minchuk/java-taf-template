package domain.ui;

import configuration.appcontainer.AppContainerPool;
import configuration.core.SmartElementFactory;
import configuration.driver.DriverPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public abstract class BasePage {
    protected static final Logger LOGGER = LogManager.getLogger(BasePage.class);
    protected String absoluteUrl = null;
    protected String urlAppender = "";

    public BasePage() {
        LOGGER.info(this.getClass().getName() + " was opened.");
        SmartElementFactory.initElements(DriverPool.getDriver(), this);
    }

    public BasePage(String urlAppender) {
        this.urlAppender = urlAppender;
        LOGGER.info(this.getClass().getName() + " was opened.");
        SmartElementFactory.initElements(DriverPool.getDriver(), this);
    }

    public void open() {
        DriverPool.getDriver().get(AppContainerPool.get().getAppHostUrl() + urlAppender);
        DriverPool.getDriver().manage().window().maximize();
    }

    public boolean isPageOpened() {
        String urlExpected = Objects.requireNonNullElseGet(absoluteUrl, () -> AppContainerPool.get().getAppHostUrl() + urlAppender);
        return DriverPool.getDriver().getCurrentUrl().equalsIgnoreCase(urlExpected);
    }
}
