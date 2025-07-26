package configuration.core.ui;

import com.microsoft.playwright.Page;

public abstract class PlaywrightBasePageComponent {
    
    protected final Page page;
    protected final PlaywrightWebElement rootLocator;
    
    public PlaywrightBasePageComponent(Page page) {
        this.page = page;
        this.rootLocator = null;
    }
    
    public PlaywrightBasePageComponent(Page page, PlaywrightWebElement rootLocator) {
        this.page = page;
        this.rootLocator = rootLocator;
    }
    
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
    
    protected PlaywrightWebElement createScopedElement(String selector) {
        if (hasRootLocator()) {
            return new PlaywrightWebElement(rootLocator, selector);
        } else {
            return new PlaywrightWebElement(page, selector);
        }
    }
    
    protected PlaywrightWebElement createScopedElement(String selector, String elementName) {
        if (hasRootLocator()) {
            return new PlaywrightWebElement(rootLocator, selector, elementName);
        } else {
            return new PlaywrightWebElement(page, selector, elementName);
        }
    }
    
    protected <T extends PlaywrightBasePageComponent> T createScopedComponent(Class<T> componentClass, String selector, String componentName) {
        try {
            PlaywrightWebElement childLocator = createScopedElement(selector, componentName);
            return componentClass.getConstructor(PlaywrightWebElement.class).newInstance(childLocator);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create scoped component " + componentClass.getSimpleName() + 
                    " with selector: " + selector, e);
        }
    }
    
    protected <T extends PlaywrightBasePageComponent> T createScopedComponent(Class<T> componentClass, PlaywrightWebElement childLocator) {
        try {
            return componentClass.getConstructor(PlaywrightWebElement.class).newInstance(childLocator);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create scoped component " + componentClass.getSimpleName() + 
                    " with provided locator", e);
        }
    }
    
    protected PlaywrightWebElement findChildElement(String selector) {
        return createScopedElement(selector);
    }
    
    protected PlaywrightWebElement findChildElement(String selector, String elementName) {
        return createScopedElement(selector, elementName);
    }
}