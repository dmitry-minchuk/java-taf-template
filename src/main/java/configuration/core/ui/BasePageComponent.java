package configuration.core.ui;

import com.microsoft.playwright.Page;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import helpers.utils.WaitUtil;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Updated BasePageComponent to support both Selenium and Playwright during migration
 * Phase 1: Dual support for backward compatibility
 */
public abstract class BasePageComponent {

    protected static final Logger LOGGER = LogManager.getLogger(BasePageComponent.class);
    protected final int timeoutInSeconds = Integer.parseInt(ProjectConfiguration.getProperty(PropertyNameSpace.WEB_ELEMENT_EXPLICIT_WAIT));
    
    // Selenium-related fields (for backward compatibility)
    @Getter
    private By rootLocatorBy;
    @Getter
    private WebElement rootElement;
    @Getter
    private WebDriver driver;
    
    // Playwright-related fields
    @Getter
    private Page page;
    @Getter
    private String rootSelector;

    @Getter // We want confirmationPopup available from BasePage and from BasePageComponent - but it's impossible at the same time with the same name in @FindBy
    protected ConfirmationPopupComponent confirmationPopup;

    protected BasePageComponent() {
        // Default constructor - initialization will happen via init() methods
    }

    /**
     * SELENIUM MIGRATION: Original Selenium initialization method (kept for backward compatibility)
     */
    public void init(WebDriver driver, By rootLocatorBy) {
        this.driver = driver;
        this.rootLocatorBy = rootLocatorBy;
        this.rootElement = null;
        LOGGER.debug("Initializing component with Selenium WebDriver: {}", this.getClass().getSimpleName());
        
        SmartPageFactory.initElements(driver, this);
        confirmationPopup = new ConfirmationPopupComponent();
        SmartPageFactory.initElements(driver, confirmationPopup);
    }
    
    /**
     * PLAYWRIGHT MIGRATION: New Playwright initialization method
     */
    public void initPlaywright(Page page, String rootSelector) {
        this.page = page;
        this.rootSelector = rootSelector;
        LOGGER.debug("Initializing component with Playwright: {} (selector: {})", 
                    this.getClass().getSimpleName(), rootSelector);
        
        PlaywrightPageFactory.initElements(page, this);
        confirmationPopup = new ConfirmationPopupComponent();
        confirmationPopup.initPlaywright(page, ""); // Global popup selector
        PlaywrightPageFactory.initElements(page, confirmationPopup);
    }

    /**
     * SELENIUM MIGRATION: Original presence check using Selenium
     */
    public boolean isPresent() {
        if (driver != null && rootLocatorBy != null) {
            // Selenium mode
            return WaitUtil.waitUntil(driver, ExpectedConditions.visibilityOfElementLocated(rootLocatorBy), timeoutInSeconds);
        } else if (page != null && rootSelector != null && !rootSelector.isEmpty()) {
            // Playwright mode
            return isPresentPlaywright();
        } else {
            LOGGER.warn("Component not properly initialized - neither Selenium nor Playwright context available");
            return false;
        }
    }
    
    /**
     * PLAYWRIGHT MIGRATION: New presence check using Playwright
     */
    public boolean isPresentPlaywright() {
        if (page == null) {
            throw new IllegalStateException("Playwright page not initialized. Call initPlaywright() first.");
        }
        
        if (rootSelector == null || rootSelector.isEmpty()) {
            // If no root selector, assume component is always present
            return true;
        }
        
        try {
            return page.locator(rootSelector).isVisible();
        } catch (Exception e) {
            LOGGER.debug("Component presence check failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Wait for component to be visible (Playwright mode)
     */
    public void waitForVisible() {
        waitForVisible(timeoutInSeconds * 1000); // Convert to milliseconds
    }
    
    /**
     * Wait for component to be visible with timeout (Playwright mode)
     */
    public void waitForVisible(int timeoutMs) {
        if (page == null) {
            throw new IllegalStateException("Playwright page not initialized. Call initPlaywright() first.");
        }
        
        if (rootSelector != null && !rootSelector.isEmpty()) {
            page.locator(rootSelector).waitFor(new com.microsoft.playwright.Locator.WaitForOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.VISIBLE)
                    .setTimeout(timeoutMs));
        }
    }
    
    /**
     * Check if component is using Playwright mode
     */
    public boolean isPlaywrightMode() {
        return page != null;
    }
    
    /**
     * Check if component is using Selenium mode
     */
    public boolean isSeleniumMode() {
        return driver != null;
    }
}
