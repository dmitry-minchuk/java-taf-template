package configuration.core.ui;

import com.microsoft.playwright.Page;
import configuration.core.ui.factory.LazyPlaywrightComponentsList;
import configuration.core.ui.factory.LazyPlaywrightElementsList;
import configuration.core.ui.factory.PlaywrightComponentFactory;
import configuration.core.ui.factory.PlaywrightComponentFactoryImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public abstract class PlaywrightBasePageComponent implements PlaywrightComponentFactory {

    protected static final Logger LOGGER = LogManager.getLogger(PlaywrightBasePageComponent.class);
    protected final Page page;
    protected final PlaywrightWebElement rootLocator;

    // Creates a page-level component without scoping boundaries.
    public PlaywrightBasePageComponent(Page page) {
        this.page = page;
        this.rootLocator = null;
    }

    // Creates a root-scoped component with automatic page extraction.
    public PlaywrightBasePageComponent(PlaywrightWebElement rootLocator) {
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

    protected PlaywrightWebElement getRootLocator() {
        return rootLocator;
    }

    protected boolean hasRootLocator() {
        return rootLocator != null;
    }
    
    // Creates a scoped element with automatic boundary detection.
    protected PlaywrightWebElement createScopedElement(String selector) {
        if (hasRootLocator()) {
            return new PlaywrightWebElement(rootLocator, selector);
        } else {
            return new PlaywrightWebElement(page, selector);
        }
    }

    //Creates a named scoped element with automatic boundary detection.
    protected PlaywrightWebElement createScopedElement(String selector, String elementName) {
        if (hasRootLocator()) {
            return new PlaywrightWebElement(rootLocator, selector, elementName);
        } else {
            return new PlaywrightWebElement(page, selector, elementName);
        }
    }
    
    // Implementation of PlaywrightComponentFactory interface

    // Creates a scoped child component using reflection with automatic element creation.
    @Override
    public <T extends PlaywrightBasePageComponent> T createScopedComponent(Class<T> componentClass, String selector, String componentName) {
        return PlaywrightComponentFactoryImpl.createScopedComponent(componentClass, selector, componentName, page, rootLocator);
    }

    // Creates a scoped child component from an existing PlaywrightWebElement.
    @Override
    public <T extends PlaywrightBasePageComponent> T createScopedComponent(Class<T> componentClass, PlaywrightWebElement childLocator) {
        return PlaywrightComponentFactoryImpl.createScopedComponent(componentClass, childLocator);
    }
    
    //Creates list of components from selector using lazy initialization (implementation for interface).
    @Override
    public <T extends PlaywrightBasePageComponent> List<T> createComponentList(Class<T> componentClass, String selector, String baseName) {
        return new LazyPlaywrightComponentsList<>(componentClass, page, rootLocator, selector, baseName);
    }
    
    //Creates list of components from selector using lazy initialization (implementation for interface).
    @Override
    public <T extends PlaywrightBasePageComponent> List<T> createComponentList(Class<T> componentClass, String selector) {
        return new LazyPlaywrightComponentsList<>(componentClass, page, rootLocator, selector);
    }
    
    //Finds scoped elements within component.
    protected List<PlaywrightWebElement> createScopedElementList(String selector, String baseName) {
        return new LazyPlaywrightElementsList(page, rootLocator, selector, baseName);
    }
    
    //Finds scoped elements within component.
    protected List<PlaywrightWebElement> createScopedElementList(String selector) {
        return new LazyPlaywrightElementsList(page, rootLocator, selector);
    }
    
    //Finds scoped components within component.
    protected <T extends PlaywrightBasePageComponent> List<T> createScopedComponentList(Class<T> componentClass, String selector, String baseName) {
        return new LazyPlaywrightComponentsList<>(componentClass, page, rootLocator, selector, baseName);
    }
    
    //Finds scoped components within component.
    protected <T extends PlaywrightBasePageComponent> List<T> createScopedComponentList(Class<T> componentClass, String selector) {
        return new LazyPlaywrightComponentsList<>(componentClass, page, rootLocator, selector);
    }
}