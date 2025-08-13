package configuration.core.ui;

import com.microsoft.playwright.Page;
import configuration.appcontainer.AppContainerPool;
import configuration.driver.PlaywrightDriverPool;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public abstract class PlaywrightBasePage {
    protected static final Logger LOGGER = LogManager.getLogger(PlaywrightBasePage.class);
    protected String absoluteUrl = null;
    protected String urlAppender = "";
    @Getter
    protected Page page;

    public PlaywrightBasePage() {
        this.page = PlaywrightDriverPool.getPage();
        LOGGER.info("{} was opened.", this.getClass().getName());
    }

    public PlaywrightBasePage(String urlAppender) {
        this.urlAppender = urlAppender;
        this.page = PlaywrightDriverPool.getPage();
        LOGGER.info("{} was opened with URL appender: {}", this.getClass().getName(), urlAppender);
    }

    public void open() {
        String url = AppContainerPool.get().getAppHostUrl() + urlAppender;
        LOGGER.info("Opening page: {}", url);
        
        // Navigate with Playwright's built-in wait conditions
        page.navigate(url, new Page.NavigateOptions()
                .setWaitUntil(com.microsoft.playwright.options.WaitUntilState.DOMCONTENTLOADED)
                .setTimeout(5000)); // 5 seconds timeout
        
        // Set viewport to maximize equivalent
        page.setViewportSize(1920, 1080);
    }

    public String getTitle() {
        return page.title();
    }
}