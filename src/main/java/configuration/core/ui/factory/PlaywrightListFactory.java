package configuration.core.ui.factory;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for creating lists of Playwright elements and components.
 */
public final class PlaywrightListFactory {
    
    private PlaywrightListFactory() {
        throw new UnsupportedOperationException("PlaywrightListFactory is a utility class and should not be instantiated");
    }
    
    /**
     * Creates list of PlaywrightWebElements from selector.
     */
    public static List<PlaywrightWebElement> createElementsList(
            Page page, 
            PlaywrightWebElement parentElement,
            String selector, 
            String baseName) {
        
        if (page == null) {
            throw new IllegalArgumentException("Page cannot be null");
        }
        if (selector == null || selector.trim().isEmpty()) {
            throw new IllegalArgumentException("Selector cannot be null or empty");
        }
        
        // Create base locator (scoped or page-level)
        Locator baseLocator = (parentElement != null) ? 
            parentElement.getLocator().locator(selector) : 
            page.locator(selector);
            
        int count = baseLocator.count();
        List<PlaywrightWebElement> elements = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            String elementName = (baseName != null) ? baseName + "_" + i : "Element_" + i;
            
            // Create indexed selector for each element
            String indexedSelector = selector + ":nth-match(" + (i + 1) + ")";
            
            PlaywrightWebElement element = (parentElement != null) ?
                new PlaywrightWebElement(parentElement, indexedSelector, elementName) :
                new PlaywrightWebElement(page, indexedSelector, elementName);
                
            elements.add(element);
        }
        
        return elements;
    }
    
    /**
     * Creates list of PlaywrightWebElements from selector.
     */
    public static List<PlaywrightWebElement> createElementsList(
            Page page, 
            PlaywrightWebElement parentElement,
            String selector) {
        return createElementsList(page, parentElement, selector, null);
    }
    
    /**
     * Creates list of PlaywrightBasePageComponents from selector.
     */
    public static <T extends PlaywrightBasePageComponent> List<T> createComponentsList(
            Class<T> componentClass,
            Page page, 
            PlaywrightWebElement parentElement,
            String selector, 
            String baseName) {
        
        if (componentClass == null) {
            throw new IllegalArgumentException("Component class cannot be null");
        }
        if (page == null) {
            throw new IllegalArgumentException("Page cannot be null");
        }
        if (selector == null || selector.trim().isEmpty()) {
            throw new IllegalArgumentException("Selector cannot be null or empty");
        }
        
        // Create base locator (scoped or page-level)
        Locator baseLocator = (parentElement != null) ? 
            parentElement.getLocator().locator(selector) : 
            page.locator(selector);
            
        int count = baseLocator.count();
        List<T> components = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            String componentName = (baseName != null) ? baseName + "_" + i : componentClass.getSimpleName() + "_" + i;
            
            // Create indexed selector for each component
            String indexedSelector = selector + ":nth-match(" + (i + 1) + ")";
            
            PlaywrightWebElement rootElement = (parentElement != null) ?
                new PlaywrightWebElement(parentElement, indexedSelector, componentName) :
                new PlaywrightWebElement(page, indexedSelector, componentName);
                
            try {
                T component = componentClass.getConstructor(PlaywrightWebElement.class).newInstance(rootElement);
                components.add(component);
            } catch (Exception e) {
                throw new RuntimeException(
                    String.format("Failed to create component %s with selector: %s", 
                        componentClass.getSimpleName(), indexedSelector), e);
            }
        }
        
        return components;
    }
    
    /**
     * Creates list of PlaywrightBasePageComponents from selector.
     */
    public static <T extends PlaywrightBasePageComponent> List<T> createComponentsList(
            Class<T> componentClass,
            Page page, 
            PlaywrightWebElement parentElement,
            String selector) {
        return createComponentsList(componentClass, page, parentElement, selector, null);
    }
}