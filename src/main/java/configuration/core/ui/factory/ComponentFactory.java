package configuration.core.ui.factory;

import configuration.core.ui.CoreComponent;
import configuration.core.ui.WebElement;

import java.util.List;

//Factory interface for creating Playwright components with proper scoping.
public interface ComponentFactory {
    
    /**
     * Creates a scoped child component using reflection with automatic element creation.
     * 
     * <p>This method dynamically creates child components by first creating a scoped element with the
     * provided selector, then instantiating the component class using its WebElement constructor.
     * This is the preferred method for dynamic component composition.</p>
     * 
     * <h3>Requirements for Component Class:</h3>
     * <ul>
     *   <li>Must extend CoreComponent</li>
     *   <li>Must have a constructor accepting WebElement parameter</li>
     *   <li>Constructor must be public and accessible</li>
     * </ul>
     */
    <T extends CoreComponent> T createScopedComponent(Class<T> componentClass, String selector, String componentName);
    
    /**
     * Creates a scoped child component from an existing WebElement.
     */
    <T extends CoreComponent> T createScopedComponent(Class<T> componentClass, WebElement childLocator);
    
    /**
     * Creates list of components from selector.
     */
    <T extends CoreComponent> List<T> createComponentList(Class<T> componentClass, String selector, String baseName);
    
    /**
     * Creates list of components from selector.
     */
    <T extends CoreComponent> List<T> createComponentList(Class<T> componentClass, String selector);
}