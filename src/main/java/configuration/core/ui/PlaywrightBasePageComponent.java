package configuration.core.ui;

import com.microsoft.playwright.Page;

public abstract class PlaywrightBasePageComponent {
    
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
     *   <li><b>CSS selectors:</b> Use standard CSS syntax (e.g., "button.save", "#username")</li>
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
     *   <li><b>Maintenance:</b> Self-documenting element purpose</li>
     * </ul>
     */
    protected PlaywrightWebElement createScopedElement(String selector, String elementName) {
        if (hasRootLocator()) {
            return new PlaywrightWebElement(rootLocator, selector, elementName);
        } else {
            return new PlaywrightWebElement(page, selector, elementName);
        }
    }
    
    /**
     * Creates a scoped child component using reflection with automatic element creation.
     * 
     * <p>This method dynamically creates child components by first creating a scoped element with the
     * provided selector, then instantiating the component class using its PlaywrightWebElement constructor.
     * This is the preferred method for dynamic component composition.</p>
     * 
     * <h3>Requirements for Component Class:</h3>
     * <ul>
     *   <li>Must extend PlaywrightBasePageComponent</li>
     *   <li>Must have a constructor accepting PlaywrightWebElement parameter</li>
     *   <li>Constructor must be public and accessible</li>
     * </ul>
     */
    protected <T extends PlaywrightBasePageComponent> T createScopedComponent(Class<T> componentClass, String selector, String componentName) {
        try {
            PlaywrightWebElement childLocator = createScopedElement(selector, componentName);
            return componentClass.getConstructor(PlaywrightWebElement.class).newInstance(childLocator);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create scoped component " + componentClass.getSimpleName() + 
                    " with selector: " + selector, e);
        }
    }
    
    /**
     * Creates a scoped child component from an existing PlaywrightWebElement.
     * 
     * <p>This method instantiates a component class using an already-created element as the root locator.
     * Useful when you have pre-existing elements or want to reuse elements across multiple components.</p>
     * 
     * <h3>When to use:</h3>
     * <ul>
     *   <li>Reusing elements across multiple component instances</li>
     *   <li>Component creation from dynamically located elements</li>
     *   <li>Advanced component composition patterns</li>
     * </ul>
     *
     */
    protected <T extends PlaywrightBasePageComponent> T createScopedComponent(Class<T> componentClass, PlaywrightWebElement childLocator) {
        try {
            return componentClass.getConstructor(PlaywrightWebElement.class).newInstance(childLocator);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create scoped component " + componentClass.getSimpleName() + 
                    " with provided locator", e);
        }
    }
    
    /**
     * Finds a child element within this component's boundary.
     * 
     * <p>This is a convenience method that delegates to {@code createScopedElement(selector)}.
     * Use this when the method name better expresses intent (finding vs creating).</p>
     */
    protected PlaywrightWebElement findChildElement(String selector) {
        return createScopedElement(selector);
    }
    
    /**
     * Finds a named child element within this component's boundary.
     * 
     * <p>This is a convenience method that delegates to {@code createScopedElement(selector, elementName)}.
     * Use this when the method name better expresses intent and you want enhanced logging.</p>
     *
     */
    protected PlaywrightWebElement findChildElement(String selector, String elementName) {
        return createScopedElement(selector, elementName);
    }
}