package configuration.core.ui.factory;

import com.microsoft.playwright.Page;
import configuration.core.ui.CoreComponent;
import configuration.core.ui.PlaywrightWebElement;

import java.util.List;

//Utility implementation of the PlaywrightComponentFactory interface.
public final class PlaywrightComponentFactoryImpl {
    
    // Private constructor to prevent instantiation - this is a utility class
    private PlaywrightComponentFactoryImpl() {
        throw new UnsupportedOperationException("PlaywrightComponentFactoryImpl is a utility class and should not be instantiated");
    }
    
    /**
     * Creates a scoped child component using reflection with automatic element creation.
     * 
     * <p>This method creates a PlaywrightWebElement from the provided selector and page context,
     * then uses reflection to instantiate the component class with the created element as root locator.</p>
     */
    public static <T extends CoreComponent> T createScopedComponent(
            Class<T> componentClass, 
            String selector, 
            String componentName,
            Page page,
            PlaywrightWebElement parentElement) {
        
        try {
            // Create the scoped element based on whether we have a parent element
            PlaywrightWebElement childLocator;
            if (parentElement != null) {
                // Component-scoped: create element within parent boundary
                childLocator = new PlaywrightWebElement(parentElement, selector, componentName);
            } else {
                // Page-scoped: create element at page level
                childLocator = new PlaywrightWebElement(page, selector, componentName);
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
     * Creates a scoped child component from an existing PlaywrightWebElement.
     * 
     * <p>This method uses reflection to instantiate the component class using the provided
     * element as the root locator. The component class must have a constructor that accepts
     * a PlaywrightWebElement parameter.</p>
     */
    public static <T extends CoreComponent> T createScopedComponent(Class<T> componentClass, PlaywrightWebElement childLocator) {
        try {
            return componentClass.getConstructor(PlaywrightWebElement.class).newInstance(childLocator);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to create scoped component %s with provided locator. Ensure the component has a public constructor accepting PlaywrightWebElement parameter.",
                    componentClass.getSimpleName()), e);
        }
    }
    
    /**
     * Creates list of components from selector.
     */
    public static <T extends CoreComponent> List<T> createComponentsList(
            Class<T> componentClass,
            String selector,
            String baseName,
            Page page,
            PlaywrightWebElement parentElement) {
        return PlaywrightListFactory.createComponentsList(componentClass, page, parentElement, selector, baseName);
    }
    
    /**
     * Creates list of components from selector.
     */
    public static <T extends CoreComponent> List<T> createComponentsList(
            Class<T> componentClass,
            String selector,
            Page page,
            PlaywrightWebElement parentElement) {
        return PlaywrightListFactory.createComponentsList(componentClass, page, parentElement, selector);
    }
}