package configuration.core.ui;

import com.microsoft.playwright.Page;
import configuration.core.ui.factory.ComponentFactory;
import configuration.core.ui.factory.ComponentFactoryImpl;
import configuration.core.ui.factory.LazyComponentsList;
import configuration.core.ui.factory.LazyElementsList;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import domain.ui.webstudio.components.BaseComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public abstract class CoreComponent implements ComponentFactory {

    protected static final Logger LOGGER = LogManager.getLogger(CoreComponent.class);
    protected static final int DEFAULT_TIMEOUT_MS = Integer.parseInt(ProjectConfiguration.getProperty(PropertyNameSpace.PLAYWRIGHT_DEFAULT_TIMEOUT));
    protected final Page page;
    protected final WebElement rootLocator;

    // Creates a page-level component without scoping boundaries.
    public CoreComponent(Page page) {
        this.page = page;
        this.rootLocator = null;
    }

    // Creates a root-scoped component with automatic page extraction.
    public CoreComponent(WebElement rootLocator) {
        this.page = rootLocator.getPage();
        this.rootLocator = rootLocator;
    }

    protected Page getPage() {
        return page;
    }

    public boolean isVisible() {
        return rootLocator.isVisible();
    }

    public boolean isVisible(int timeoutInMillis) {
        return rootLocator.isVisible(timeoutInMillis);
    }

    protected WebElement getRootLocator() {
        return rootLocator;
    }

    protected boolean hasRootLocator() {
        return rootLocator != null;
    }

    // Creates a scoped element with automatic boundary detection.
    protected WebElement createScopedElement(String selector) {
        if (hasRootLocator()) {
            return new WebElement(rootLocator, selector);
        } else {
            return new WebElement(page, selector);
        }
    }

    //Creates a named scoped element with automatic boundary detection.
    protected WebElement createScopedElement(String selector, String elementName) {
        if (hasRootLocator()) {
            return new WebElement(rootLocator, selector, elementName);
        } else {
            return new WebElement(page, selector, elementName);
        }
    }

    // Implementation of ComponentFactory interface

    // Creates a scoped child component using reflection with automatic element creation.
    @Override
    public <T extends BaseComponent> T createScopedComponent(Class<T> componentClass, String selector, String componentName) {
        return ComponentFactoryImpl.createScopedComponent(componentClass, selector, componentName, page, rootLocator);
    }

    // Creates a scoped child component from an existing WebElement.
    @Override
    public <T extends BaseComponent> T createScopedComponent(Class<T> componentClass, WebElement childLocator) {
        return ComponentFactoryImpl.createScopedComponent(componentClass, childLocator);
    }

    //Creates list of components from selector using lazy initialization (implementation for interface).
    @Override
    public <T extends BaseComponent> List<T> createComponentList(Class<T> componentClass, String selector, String baseName) {
        return new LazyComponentsList<>(componentClass, page, rootLocator, selector, baseName);
    }

    //Creates list of components from selector using lazy initialization (implementation for interface).
    @Override
    public <T extends BaseComponent> List<T> createComponentList(Class<T> componentClass, String selector) {
        return new LazyComponentsList<>(componentClass, page, rootLocator, selector);
    }

    //Finds scoped elements within component.
    protected List<WebElement> createScopedElementList(String selector, String baseName) {
        return new LazyElementsList(page, rootLocator, selector, baseName);
    }

    //Finds scoped elements within component.
    protected List<WebElement> createScopedElementList(String selector) {
        return new LazyElementsList(page, rootLocator, selector);
    }

    //Finds scoped components within component.
    protected <T extends BaseComponent> List<T> createScopedComponentList(Class<T> componentClass, String selector, String baseName) {
        return new LazyComponentsList<>(componentClass, page, rootLocator, selector, baseName);
    }

    //Finds scoped components within component.
    protected <T extends BaseComponent> List<T> createScopedComponentList(Class<T> componentClass, String selector) {
        return new LazyComponentsList<>(componentClass, page, rootLocator, selector);
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
