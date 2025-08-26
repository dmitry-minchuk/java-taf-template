package configuration.core.ui;

import com.microsoft.playwright.Page;
import configuration.appcontainer.AppContainerPool;
import configuration.core.ui.factory.LazyPlaywrightComponentsList;
import configuration.core.ui.factory.LazyPlaywrightElementsList;
import configuration.core.ui.factory.PlaywrightComponentFactory;
import configuration.core.ui.factory.PlaywrightComponentFactoryImpl;
import configuration.driver.PlaywrightDriverPool;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public abstract class PlaywrightBasePage implements PlaywrightComponentFactory {
    protected static final Logger LOGGER = LogManager.getLogger(PlaywrightBasePage.class);
    private static final int DEFAULT_TIMEOUT_MS = Integer.parseInt(ProjectConfiguration.getProperty(PropertyNameSpace.PLAYWRIGHT_DEFAULT_TIMEOUT));
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
                .setTimeout(DEFAULT_TIMEOUT_MS)); // 5 seconds timeout

        // Set viewport to maximize equivalent
        page.setViewportSize(1920, 1080);
    }

    public String getTitle() {
        return page.title();
    }

    // Implementation of PlaywrightComponentFactory interface

    //Creates a scoped child component at page level using selector.
    @Override
    public <T extends PlaywrightBasePageComponent> T createScopedComponent(Class<T> componentClass, String selector, String componentName) {
        return PlaywrightComponentFactoryImpl.createScopedComponent(componentClass, selector, componentName, page, null);
    }

    //Creates a scoped child component from an existing element.
    @Override
    public <T extends PlaywrightBasePageComponent> T createScopedComponent(Class<T> componentClass, PlaywrightWebElement childLocator) {
        return PlaywrightComponentFactoryImpl.createScopedComponent(componentClass, childLocator);
    }
    
    //Creates list of components from selector using lazy initialization.
    @Override
    public <T extends PlaywrightBasePageComponent> List<T> createComponentList(Class<T> componentClass, String selector, String baseName) {
        return new LazyPlaywrightComponentsList<>(componentClass, page, null, selector, baseName);
    }
    
    //Creates list of components from selector using lazy initialization.
    @Override
    public <T extends PlaywrightBasePageComponent> List<T> createComponentList(Class<T> componentClass, String selector) {
        return new LazyPlaywrightComponentsList<>(componentClass, page, null, selector);
    }
    
    //Finds list of elements on page using lazy initialization.
    public List<PlaywrightWebElement> createElementList(String selector, String baseName) {
        return new LazyPlaywrightElementsList(page, null, selector, baseName);
    }
    
    //Finds list of elements on page using lazy initialization.
    public List<PlaywrightWebElement> createElementList(String selector) {
        return new LazyPlaywrightElementsList(page, null, selector);
    }
}