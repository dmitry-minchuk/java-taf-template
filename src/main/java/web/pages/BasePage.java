package web.pages;

import configuration.ProjectConfiguration;
import configuration.domain.PropertyNameSpace;
import configuration.driver.DriverFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

import java.net.MalformedURLException;
import java.net.URL;

public abstract class BasePage {
    protected static final Logger LOGGER = LogManager.getLogger(BasePage.class);
    protected String baseUrl = ProjectConfiguration.getPropertyByEnv(PropertyNameSpace.BASE_URL);
    protected String absoluteUrl = null;
    protected String urlAppender = "";
    private static WebDriver driver = null;

    public BasePage() {
        LOGGER.info(this.getClass().getName() + " was opened.");
        PageFactory.initElements(getDriver(), this);
    }

    public BasePage(String urlAppender) {
        this.urlAppender = urlAppender;
        LOGGER.info(this.getClass().getName() + " was opened.");
        PageFactory.initElements(getDriver(), this);
    }

    public void open() {
        if (absoluteUrl == null)
            getDriver().get(baseUrl + urlAppender);
        else
            getDriver().get(absoluteUrl);
        getDriver().manage().window().maximize();
    }

    public WebDriver getDriver() {
        if(driver != null)
            return driver;
        else
            driver = DriverFactory.getDriver();
        return driver;
    }

    protected String getPageUrl() {
        if (absoluteUrl == null) {
            return baseUrl + urlAppender;
        } else {
            return absoluteUrl.toString();
        }
    }
}
