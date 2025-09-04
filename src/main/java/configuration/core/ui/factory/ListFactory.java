package configuration.core.ui.factory;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import domain.ui.webstudio.components.BaseComponent;
import configuration.core.ui.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for creating lists of Playwright elements and components.
 */
public final class ListFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(ListFactory.class);
    
    private ListFactory() {
        throw new UnsupportedOperationException("ListFactory is a utility class and should not be instantiated");
    }
    
    /**
     * Creates indexed selector that works correctly for both XPath and CSS selectors.
     */
    private static String createIndexedSelector(String selector, int index) {
        if (selector.startsWith("xpath=")) {
            // For XPath: add position predicate
            String xpathPart = selector.substring(6); // Remove "xpath=" prefix
            return "xpath=(" + xpathPart + ")[" + (index + 1) + "]";
        } else {
            // For CSS: use :nth-match()
            return selector + ":nth-match(" + (index + 1) + ")";
        }
    }
    
    /**
     * Creates list of PlaywrightWebElements from selector.
     */
    public static List<WebElement> createElementsList(
            Page page, 
            WebElement parentElement,
            String selector, 
            String baseName) {
        
        logger.info("[DEBUG] createElementsList called with selector='{}', parentElement={}, baseName='{}'", 
                   selector, (parentElement != null ? "present" : "null"), baseName);
        
        // Create base locator (scoped or page-level)
        Locator baseLocator = (parentElement != null) ? 
            parentElement.getLocator().locator(selector) : 
            page.locator(selector);
            
        int count = baseLocator.count();
        logger.info("[DEBUG] baseLocator.count() returned: {}", count);
        
        // Additional debug: try direct page locator for comparison
        int directCount = page.locator(selector).count();
        logger.info("[DEBUG] direct page.locator(selector).count() returned: {}", directCount);
        
        List<WebElement> elements = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            String elementName = (baseName != null) ? baseName + "_" + i : "Element_" + i;
            
            // Create indexed selector for each element (supports both XPath and CSS)
            String indexedSelector = createIndexedSelector(selector, i);
            logger.info("[DEBUG] Creating element {}: indexedSelector='{}', elementName='{}'", i, indexedSelector, elementName);
            
            WebElement element = (parentElement != null) ?
                new WebElement(parentElement, indexedSelector, elementName) :
                new WebElement(page, indexedSelector, elementName);
                
            elements.add(element);
        }
        
        logger.info("[DEBUG] createElementsList finished: created {} elements", elements.size());
        return elements;
    }
    
    /**
     * Creates list of PlaywrightWebElements from selector.
     */
    public static List<WebElement> createElementsList(
            Page page, 
            WebElement parentElement,
            String selector) {
        return createElementsList(page, parentElement, selector, null);
    }
    
    /**
     * Creates list of PlaywrightBasePageComponents from selector.
     */
    public static <T extends BaseComponent> List<T> createComponentsList(
            Class<T> componentClass,
            Page page, 
            WebElement parentElement,
            String selector, 
            String baseName) {
        
        // Create base locator (scoped or page-level)
        Locator baseLocator = (parentElement != null) ? 
            parentElement.getLocator().locator(selector) : 
            page.locator(selector);
            
        int count = baseLocator.count();
        List<T> components = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            String componentName = (baseName != null) ? baseName + "_" + i : componentClass.getSimpleName() + "_" + i;
            
            // Create indexed selector for each component (supports both XPath and CSS)
            String indexedSelector = createIndexedSelector(selector, i);
            
            WebElement rootElement = (parentElement != null) ?
                new WebElement(parentElement, indexedSelector, componentName) :
                new WebElement(page, indexedSelector, componentName);
                
            try {
                T component = componentClass.getConstructor(WebElement.class).newInstance(rootElement);
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
    public static <T extends BaseComponent> List<T> createComponentsList(
            Class<T> componentClass,
            Page page, 
            WebElement parentElement,
            String selector) {
        return createComponentsList(componentClass, page, parentElement, selector, null);
    }
}