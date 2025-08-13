package configuration.core.ui;

import com.microsoft.playwright.Page;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

// Playwright-based factory for creating element lists
public class PlaywrightPageFactory {
    
    protected final static Logger LOGGER = LogManager.getLogger(PlaywrightPageFactory.class);
    
    /**
     * Creates a list of PlaywrightWebElements from a selector
     * @param page Playwright page instance
     * @param selector CSS/XPath selector to find elements
     * @param parent Parent object for scoping (can be null)
     * @return List of PlaywrightWebElements
     */
    public static List<PlaywrightWebElement> createPlaywrightElementList(Page page, String selector, Object parent) {
        List<PlaywrightWebElement> elements = new ArrayList<>();
        
        // Get count of elements matching the selector
        int count = page.locator(selector).count();
        
        // Create PlaywrightWebElement for each match using nth() selector
        for (int i = 0; i < count; i++) {
            String indexedSelector = selector + " >> nth=" + i;
            PlaywrightWebElement element = new PlaywrightWebElement(page, indexedSelector);
            elements.add(element);
        }
        
        return elements;
    }
}