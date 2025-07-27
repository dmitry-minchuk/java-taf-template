package configuration.core.ui;

import com.microsoft.playwright.Page;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import helpers.utils.WaitUtil;
import helpers.utils.PlaywrightExpectUtil;
import configuration.driver.PlaywrightDriverPool;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

// Base component supporting both Selenium and Playwright during migration
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

    // Selenium initialization method (kept for backward compatibility)
    public void init(WebDriver driver, By rootLocatorBy) {
        this.driver = driver;
        this.rootLocatorBy = rootLocatorBy;
        this.rootElement = null;
        LOGGER.debug("Initializing component with Selenium WebDriver: {}", this.getClass().getSimpleName());
        
        SmartPageFactory.initElements(driver, this);
        confirmationPopup = new ConfirmationPopupComponent();
        SmartPageFactory.initElements(driver, confirmationPopup);
    }
    
    // Playwright initialization method
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

    // Presence check supporting both Selenium and Playwright modes
    public boolean isPresent() {
        if (driver != null && rootLocatorBy != null) {
            // Selenium mode
            return WaitUtil.waitUntil(driver, ExpectedConditions.visibilityOfElementLocated(rootLocatorBy), timeoutInSeconds);
        } else if (page != null && rootSelector != null && !rootSelector.isEmpty()) {
            // PLAYWRIGHT MIGRATION: Use PlaywrightExpectUtil instead of separate method
            return PlaywrightExpectUtil.expectVisible(page, rootSelector, timeoutInSeconds * 1000);
        } else {
            LOGGER.warn("Component not properly initialized - neither Selenium nor Playwright context available");
            return false;
        }
    }
    
    // Playwright-specific presence check
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
    
    public void waitForVisible() {
        waitForVisible(timeoutInSeconds * 1000); // Convert to milliseconds
    }
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
    
    public boolean isPlaywrightMode() {
        return page != null;
    }
    
    public boolean isSeleniumMode() {
        return driver != null;
    }
    
    // ============================================
    // PHASE 2.5: DUAL-MODE WAIT STRATEGY UTILITIES
    // ============================================
    
    // Dual-mode element visibility wait using appropriate strategy based on current mode
    protected boolean waitForElementVisible(By seleniumLocator, String playwrightSelector) {
        return waitForElementVisible(seleniumLocator, playwrightSelector, timeoutInSeconds);
    }
    
    protected boolean waitForElementVisible(By seleniumLocator, String playwrightSelector, int timeoutSeconds) {
        if (PlaywrightDriverPool.isInitialized() && page != null) {
            // Playwright mode: Use PlaywrightExpectUtil
            return PlaywrightExpectUtil.expectVisible(page, playwrightSelector, timeoutSeconds * 1000);
        } else if (driver != null) {
            // Selenium mode: Use WaitUtil
            return WaitUtil.waitUntil(driver, org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated(seleniumLocator), timeoutSeconds);
        } else {
            LOGGER.warn("Component not properly initialized - neither Selenium nor Playwright context available");
            return false;
        }
    }
    
    // Dual-mode element presence wait
    protected boolean waitForElementPresent(By seleniumLocator, String playwrightSelector) {
        return waitForElementPresent(seleniumLocator, playwrightSelector, timeoutInSeconds);
    }
    
    protected boolean waitForElementPresent(By seleniumLocator, String playwrightSelector, int timeoutSeconds) {
        if (PlaywrightDriverPool.isInitialized() && page != null) {
            // Playwright mode: Use PlaywrightExpectUtil
            return PlaywrightExpectUtil.expectAttached(page, playwrightSelector, timeoutSeconds * 1000);
        } else if (driver != null) {
            // Selenium mode: Use WaitUtil
            return WaitUtil.waitUntil(driver, org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated(seleniumLocator), timeoutSeconds);
        } else {
            LOGGER.warn("Component not properly initialized - neither Selenium nor Playwright context available");
            return false;
        }
    }
    
    // Dual-mode page ready wait
    protected void waitForPageReady() {
        if (PlaywrightDriverPool.isInitialized() && page != null) {
            // Playwright mode: Use PlaywrightExpectUtil
            PlaywrightExpectUtil.expectPageReady(page);
        } else if (driver != null) {
            // Selenium mode: Use WaitUtil
            WaitUtil.waitUntilPageIsReady(driver, timeoutInSeconds);
        }
    }
    
    // Dual-mode element stable wait
    protected boolean waitForElementStable(By seleniumLocator, String playwrightSelector) {
        return waitForElementStable(seleniumLocator, playwrightSelector, timeoutInSeconds);
    }
    
    protected boolean waitForElementStable(By seleniumLocator, String playwrightSelector, int timeoutSeconds) {
        if (PlaywrightDriverPool.isInitialized() && page != null) {
            // Playwright mode: Use PlaywrightExpectUtil
            return PlaywrightExpectUtil.expectElementStable(page, playwrightSelector, timeoutSeconds * 1000);
        } else if (driver != null) {
            // Selenium mode: Use WaitUtil
            org.openqa.selenium.WebElement element = driver.findElement(seleniumLocator);
            WaitUtil.waitUntilElementStable(driver, element, timeoutSeconds);
            return true;
        } else {
            LOGGER.warn("Component not properly initialized - neither Selenium nor Playwright context available");
            return false;
        }
    }
}
