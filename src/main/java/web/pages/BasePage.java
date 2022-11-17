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
import java.time.Duration;

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

    protected String getPageUrl() {
        if (absoluteUrl == null) {
            return baseUrl + urlAppender;
        } else {
            return absoluteUrl.toString();
        }
    }
}
