package domain.ui;

import configuration.ProjectConfiguration;
import configuration.PropertyNameSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

import java.util.Objects;

public abstract class BasePage {
    protected static final Logger LOGGER = LogManager.getLogger(BasePage.class);
    protected String baseUrl = ProjectConfiguration.getPropertyByEnv(PropertyNameSpace.BASE_URL);
    protected String absoluteUrl = null;
    protected String urlAppender = "";
    protected WebDriver driver = null;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        LOGGER.info(this.getClass().getName() + " was opened.");
        PageFactory.initElements(this.driver, this);
    }

    public BasePage(WebDriver driver, String urlAppender) {
        this.driver = driver;
        this.urlAppender = urlAppender;
        LOGGER.info(this.getClass().getName() + " was opened.");
        PageFactory.initElements(this.driver, this);
    }

    public void open() {
        if(absoluteUrl == null)
            driver.get(baseUrl + urlAppender);
        else
            driver.get(absoluteUrl);
        driver.manage().window().maximize();
    }

    public boolean isPageOpened() {
        String urlExpected = Objects.requireNonNullElseGet(absoluteUrl, () -> baseUrl + urlAppender);
        return driver.getCurrentUrl().equalsIgnoreCase(urlExpected);
    }
}
