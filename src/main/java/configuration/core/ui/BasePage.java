package configuration.core.ui;

import configuration.appcontainer.AppContainerPool;
import configuration.driver.DriverPool;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.support.FindBy;

import java.util.Objects;

public abstract class BasePage {
    protected static final Logger LOGGER = LogManager.getLogger(BasePage.class);
    protected String absoluteUrl = null;
    protected String urlAppender = "";

    @Getter
    @FindBy(xpath = "//*[contains(text(), '%s')]")
    protected SmartWebElement universalTextElement;

    public BasePage() {
        LOGGER.info(this.getClass().getName() + " was opened.");
        SmartPageFactory.initElements(DriverPool.getDriver(), this);
    }

    public BasePage(String urlAppender) {
        this.urlAppender = urlAppender;
        LOGGER.info(this.getClass().getName() + " was opened.");
        SmartPageFactory.initElements(DriverPool.getDriver(), this);
    }

    public void open() {
        DriverPool.getDriver().get(AppContainerPool.get().getAppHostUrl() + urlAppender);
        DriverPool.getDriver().manage().window().maximize();
    }

    public boolean isPageOpened() {
        String urlExpected = Objects.requireNonNullElseGet(absoluteUrl, () -> AppContainerPool.get().getAppHostUrl() + urlAppender);
        return Objects.requireNonNull(DriverPool.getDriver().getCurrentUrl()).equalsIgnoreCase(urlExpected);
    }
}
