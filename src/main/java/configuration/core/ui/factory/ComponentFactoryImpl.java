package configuration.core.ui.factory;

import com.microsoft.playwright.Page;
import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;

import java.util.List;

//Utility implementation of the ComponentFactory interface.
public final class ComponentFactoryImpl {
    
    // Private constructor to prevent instantiation - this is a utility class
    private ComponentFactoryImpl() {
        throw new UnsupportedOperationException("ComponentFactoryImpl is a utility class and should not be instantiated");
    }
    
    /**
     * Creates a scoped child component using reflection with automatic element creation.
     * 
     * <p>This method creates a WebElement from the provided selector and page context,
     * then uses reflection to instantiate the component class with the created element as root locator.</p>
     */
    public static <T extends BaseComponent> T createScopedComponent(
            Class<T> componentClass, 
            String selector, 
            String componentName,
            Page page,
            WebElement parentElement) {
        
        try {
            // Create the scoped element based on whether we have a parent element
            WebElement childLocator;
            if (parentElement != null) {
                // Component-scoped: create element within parent boundary
                childLocator = new WebElement(parentElement, selector, componentName);
            } else {
                // Page-scoped: create element at page level
                childLocator = new WebElement(page, selector, componentName);
            }
            
            // Use the other overload to create the component from the element
            return createScopedComponent(componentClass, childLocator);
            
        } catch (Exception e) {
            throw new RuntimeException(
                String.format("Failed to create scoped component %s with selector: %s", 
                    componentClass.getSimpleName(), selector), e);
        }
    }
    
    /**
     * Creates a scoped child component from an existing WebElement.
     * 
     * <p>This method uses reflection to instantiate the component class using the provided
     * element as the root locator. The component class must have a constructor that accepts
     * a WebElement parameter.</p>
     */
    public static <T extends BaseComponent> T createScopedComponent(Class<T> componentClass, WebElement childLocator) {
        try {
            return componentClass.getConstructor(WebElement.class).newInstance(childLocator);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to create scoped component %s with provided locator. Ensure the component has a public constructor accepting WebElement parameter.",
                    componentClass.getSimpleName()), e);
        }
    }
    
    /**
     * Creates list of components from selector.
     */
    public static <T extends BaseComponent> List<T> createComponentsList(
            Class<T> componentClass,
            String selector,
            String baseName,
            Page page,
            WebElement parentElement) {
        return ListFactory.createComponentsList(componentClass, page, parentElement, selector, baseName);
    }
    
    /**
     * Creates list of components from selector.
     */
    public static <T extends BaseComponent> List<T> createComponentsList(
            Class<T> componentClass,
            String selector,
            Page page,
            WebElement parentElement) {
        return ListFactory.createComponentsList(componentClass, page, parentElement, selector);
    }
}