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

    @Getter // We want confirmationPopup available from BasePage and from BasePageComponent - but it's impossible at the same time with the same name in @FindBy
    protected ConfirmationPopupComponent confirmationPopup;

    public BasePage() {
        LOGGER.info(this.getClass().getName() + " was opened.");
        SmartPageFactory.initElements(DriverPool.getDriver(), this);
        confirmationPopup = new ConfirmationPopupComponent();
        SmartPageFactory.initElements(DriverPool.getDriver(), confirmationPopup);
    }

    public BasePage(String urlAppender) {
        this.urlAppender = urlAppender;
        LOGGER.info(this.getClass().getName() + " was opened.");
        SmartPageFactory.initElements(DriverPool.getDriver(), this);
        confirmationPopup = new ConfirmationPopupComponent();
        SmartPageFactory.initElements(DriverPool.getDriver(), confirmationPopup);
    }

    public void open() {
        DriverPool.getDriver().get(AppContainerPool.get().getAppHostUrl() + urlAppender);
        DriverPool.getDriver().manage().window().maximize();
    }

    public boolean isPageOpened() {
        String urlExpected = Objects.requireNonNullElseGet(absoluteUrl, () -> AppContainerPool.get().getAppHostUrl() + urlAppender);
        return Objects.requireNonNull(DriverPool.getDriver().getCurrentUrl()).equalsIgnoreCase(urlExpected);
    }

    public void refresh() {
        String url = DriverPool.getDriver().getCurrentUrl();
        DriverPool.getDriver().get(url);
    }
}
