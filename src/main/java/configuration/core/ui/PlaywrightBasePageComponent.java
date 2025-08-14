package configuration.core.ui;

import com.microsoft.playwright.Page;
import configuration.core.ui.factory.PlaywrightComponentFactory;
import configuration.core.ui.factory.PlaywrightComponentFactoryImpl;
import configuration.core.ui.factory.PlaywrightListFactory;
import configuration.core.ui.factory.LazyPlaywrightComponentsList;
import configuration.core.ui.factory.LazyPlaywrightElementsList;

import java.util.List;

public abstract class PlaywrightBasePageComponent implements PlaywrightComponentFactory {
    
    protected final Page page;
    protected final PlaywrightWebElement rootLocator;
    
    /**
     * Creates a page-level component without scoping boundaries.
     * 
     * <p>This constructor is used for top-level components that need to search the entire page DOM.
     * Elements created with {@code createScopedElement()} will search from the page root.</p>
     * 
     * <h3>When to use:</h3>
     * <ul>
     *   <li>Main page components (e.g., AdminPage, EditorPage)</li>
     *   <li>Components that span multiple page sections</li>
     *   <li>Initial component creation from page classes</li>
     * </ul>
     */
    public PlaywrightBasePageComponent(Page page) {
        this.page = page;
        this.rootLocator = null;
    }
    
    /**
     * Creates a root-scoped component with automatic page extraction.
     * 
     * <p>This is the most commonly used constructor for child components. The page instance is
     * automatically extracted from the root locator, ensuring consistency and simplifying component creation.</p>
     * 
     * <h3>When to use:</h3>
     * <ul>
     *   <li><b>Most common pattern:</b> Creating child components from parent containers</li>
     *   <li>Component composition and nesting</li>
     *   <li>Dynamic component creation with {@code createScopedComponent()}</li>
     * </ul>
     */
    public PlaywrightBasePageComponent(PlaywrightWebElement rootLocator) {
        this.page = rootLocator.getPage();
        this.rootLocator = rootLocator;
    }

    protected Page getPage() {
        return page;
    }

    protected PlaywrightWebElement getRootLocator() {
        return rootLocator;
    }

    protected boolean hasRootLocator() {
        return rootLocator != null;
    }
    
    /**
     * Creates a scoped element with automatic boundary detection.
     * 
     * <p>This is the core method for creating child elements within components. The element will be
     * scoped to the component's root locator if present, or to the entire page if this is a page-level component.</p>
     * 
     * <h3>Scoping Behavior:</h3>
     * <ul>
     *   <li><b>Root-scoped component:</b> Creates child element within root locator boundary</li>
     *   <li><b>Page-level component:</b> Creates element searching entire page DOM</li>
     * </ul>
     * 
     * <h3>Selector Best Practices:</h3>
     * <ul>
     *   <li><b>Scoped selectors:</b> Use ".//" prefix for relative XPath (e.g., ".//button[@class='save']")</li>
     *   <li><b>Avoid absolute paths:</b> Don't use "//" in scoped components as it searches from document root</li>
     * </ul>
     *
     */
    protected PlaywrightWebElement createScopedElement(String selector) {
        if (hasRootLocator()) {
            return new PlaywrightWebElement(rootLocator, selector);
        } else {
            return new PlaywrightWebElement(page, selector);
        }
    }
    
    /**
     * Creates a named scoped element with automatic boundary detection.
     * 
     * <p>This method creates a scoped element with a custom name for enhanced logging and debugging.
     * The element name appears in all log messages, making test debugging more efficient.</p>
     * 
     * <h3>Benefits of Named Elements:</h3>
     * <ul>
     *   <li><b>Enhanced Logging:</b> "Clicking saveButton" instead of "Clicking Element"</li>
     *   <li><b>Debugging Support:</b> Clear identification in test failures</li>
     * </ul>
     */
    protected PlaywrightWebElement createScopedElement(String selector, String elementName) {
        if (hasRootLocator()) {
            return new PlaywrightWebElement(rootLocator, selector, elementName);
        } else {
            return new PlaywrightWebElement(page, selector, elementName);
        }
    }
    
    // Implementation of PlaywrightComponentFactory interface
    
    /**
     * Creates a scoped child component using reflection with automatic element creation.
     * 
     * <p>For components, this method creates child components within the component's boundary.
     * The child component will be scoped to an element found within this component's root locator.</p>
     */
    @Override
    public <T extends PlaywrightBasePageComponent> T createScopedComponent(Class<T> componentClass, String selector, String componentName) {
        return PlaywrightComponentFactoryImpl.createScopedComponent(componentClass, selector, componentName, page, rootLocator);
    }
    
    /**
     * Creates a scoped child component from an existing PlaywrightWebElement.
     */
    @Override
    public <T extends PlaywrightBasePageComponent> T createScopedComponent(Class<T> componentClass, PlaywrightWebElement childLocator) {
        return PlaywrightComponentFactoryImpl.createScopedComponent(componentClass, childLocator);
    }
    
    //Creates list of components from selector using lazy initialization (implementation for interface).
    @Override
    public <T extends PlaywrightBasePageComponent> List<T> findComponents(Class<T> componentClass, String selector, String baseName) {
        return new LazyPlaywrightComponentsList<>(componentClass, page, rootLocator, selector, baseName);
    }
    
    //Creates list of components from selector using lazy initialization (implementation for interface).
    @Override
    public <T extends PlaywrightBasePageComponent> List<T> findComponents(Class<T> componentClass, String selector) {
        return new LazyPlaywrightComponentsList<>(componentClass, page, rootLocator, selector);
    }

    //Finds a child element within this component's boundary.
    protected PlaywrightWebElement findChildElement(String selector) {
        return createScopedElement(selector);
    }
    
    //Finds list of child elements within this component's boundary using lazy initialization.
    protected List<PlaywrightWebElement> findChildElements(String selector, String baseName) {
        return new LazyPlaywrightElementsList(page, rootLocator, selector, baseName);
    }
    
    //Finds list of child elements within this component's boundary using lazy initialization.
    protected List<PlaywrightWebElement> findChildElements(String selector) {
        return new LazyPlaywrightElementsList(page, rootLocator, selector);
    }
    

    //Finds a named child element within this component's boundary.
    protected PlaywrightWebElement findChildElement(String selector, String elementName) {
        return createScopedElement(selector, elementName);
    }
    
    //Finds scoped elements within component.
    protected List<PlaywrightWebElement> findScopedElements(String selector, String baseName) {
        return PlaywrightListFactory.createElementsList(page, rootLocator, selector, baseName);
    }
    
    //Finds scoped elements within component.
    protected List<PlaywrightWebElement> findScopedElements(String selector) {
        return PlaywrightListFactory.createElementsList(page, rootLocator, selector);
    }
    
    //Finds scoped components within component.
    protected <T extends PlaywrightBasePageComponent> List<T> findScopedComponents(Class<T> componentClass, String selector, String baseName) {
        return PlaywrightListFactory.createComponentsList(componentClass, page, rootLocator, selector, baseName);
    }
    
    //Finds scoped components within component.
    protected <T extends PlaywrightBasePageComponent> List<T> findScopedComponents(Class<T> componentClass, String selector) {
        return PlaywrightListFactory.createComponentsList(componentClass, page, rootLocator, selector);
    }
}