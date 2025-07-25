package configuration.core.ui;

import com.microsoft.playwright.Page;

/**
 * Base class for all Playwright page components
 * Provides common functionality and page reference for component classes
 */
public abstract class PlaywrightBasePageComponent {
    
    protected final Page page;
    
    /**
     * Initialize component with Playwright page reference
     * @param page Playwright page instance
     */
    public PlaywrightBasePageComponent(Page page) {
        this.page = page;
    }
    
    /**
     * Get the current page instance
     * @return Playwright page
     */
    protected Page getPage() {
        return page;
    }
}