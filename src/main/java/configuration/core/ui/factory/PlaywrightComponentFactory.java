package configuration.core.ui.factory;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;

import java.util.List;

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
     */
    <T extends PlaywrightBasePageComponent> T createScopedComponent(Class<T> componentClass, PlaywrightWebElement childLocator);
    
    /**
     * Creates list of components from selector.
     */
    <T extends PlaywrightBasePageComponent> List<T> findComponents(Class<T> componentClass, String selector, String baseName);
    
    /**
     * Creates list of components from selector.
     */
    <T extends PlaywrightBasePageComponent> List<T> findComponents(Class<T> componentClass, String selector);
}