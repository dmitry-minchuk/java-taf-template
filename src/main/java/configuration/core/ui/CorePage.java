package configuration.core.ui;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import configuration.appcontainer.AppContainerPool;
import configuration.core.ui.factory.ComponentFactoryImpl;
import configuration.core.ui.factory.LazyComponentsList;
import configuration.core.ui.factory.LazyElementsList;
import configuration.core.ui.factory.ComponentFactory;
import configuration.driver.LocalDriverPool;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public abstract class CorePage implements ComponentFactory {
    protected static final Logger LOGGER = LogManager.getLogger(CorePage.class);
    private static final int DEFAULT_TIMEOUT_MS = Integer.parseInt(ProjectConfiguration.getProperty(PropertyNameSpace.PLAYWRIGHT_DEFAULT_TIMEOUT));
    protected String absoluteUrl = null;
    protected String urlAppender = "";
    @Getter
    protected Page page;

    public CorePage() {
        this.page = LocalDriverPool.getPage();
        LOGGER.info("{} was opened.", this.getClass().getName());
    }

    public CorePage(Page page) {
        this.page = page;
        LOGGER.info("{} was opened", this.getClass().getName());
    }

    public void open() {
        String url = AppContainerPool.get().getAppHostUrl() + urlAppender;
        LOGGER.info("Opening page: {}", url);

        // Navigate with Playwright's built-in wait conditions
        page.navigate(url, new Page.NavigateOptions()
                .setWaitUntil(com.microsoft.playwright.options.WaitUntilState.DOMCONTENTLOADED)
                .setTimeout(DEFAULT_TIMEOUT_MS));

        // Set viewport to maximize equivalent
        page.setViewportSize(1920, 1080);
    }

    public String getTitle() {
        return page.title();
    }

    public Locator locator(String selector) {
        return page.locator(selector);
    }

    // Implementation of ComponentFactory interface

    //Creates a scoped child component at page level using selector.
    @Override
    public <T extends CoreComponent> T createScopedComponent(Class<T> componentClass, String selector, String componentName) {
        return ComponentFactoryImpl.createScopedComponent(componentClass, selector, componentName, page, null);
    }

    //Creates a scoped child component from an existing element.
    @Override
    public <T extends CoreComponent> T createScopedComponent(Class<T> componentClass, WebElement childLocator) {
        return ComponentFactoryImpl.createScopedComponent(componentClass, childLocator);
    }
    
    //Creates list of components from selector using lazy initialization.
    @Override
    public <T extends CoreComponent> List<T> createComponentList(Class<T> componentClass, String selector, String baseName) {
        return new LazyComponentsList<>(componentClass, page, null, selector, baseName);
    }
    
    //Creates list of components from selector using lazy initialization.
    @Override
    public <T extends CoreComponent> List<T> createComponentList(Class<T> componentClass, String selector) {
        return new LazyComponentsList<>(componentClass, page, null, selector);
    }
    
    //Finds list of elements on page using lazy initialization.
    public List<WebElement> createElementList(String selector, String baseName) {
        return new LazyElementsList(page, null, selector, baseName);
    }
    
    //Finds list of elements on page using lazy initialization.
    public List<WebElement> createElementList(String selector) {
        return new LazyElementsList(page, null, selector);
    }
}