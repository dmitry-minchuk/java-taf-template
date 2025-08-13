package configuration.core.ui;

//Factory interface for creating Playwright components with proper scoping.
public interface PlaywrightComponentFactory {
    
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
    <T extends PlaywrightBasePageComponent> T createScopedComponent(Class<T> componentClass, String selector, String componentName);
    
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
     */
    <T extends PlaywrightBasePageComponent> T createScopedComponent(Class<T> componentClass, PlaywrightWebElement childLocator);
}