package helpers.utils;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.WaitForSelectorState;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Playwright-based replacement for WaitUtil
 * Uses Playwright's native expect() patterns and wait strategies
 * Phase 2: Eliminate custom wait logic in favor of Playwright's built-in capabilities
 */

// DEPRECATED: This utility class should be avoided - use native Playwright expect() and waitFor() methods instead.
// Use only in emergency cases where native Playwright capabilities are insufficient.
public class PlaywrightExpectUtil {
    
    protected static final Logger LOGGER = LogManager.getLogger(PlaywrightExpectUtil.class);
    private static final int DEFAULT_TIMEOUT_MS = Integer.parseInt(
        ProjectConfiguration.getProperty(PropertyNameSpace.WEB_ELEMENT_EXPLICIT_WAIT)
    ) * 1000; // Convert to milliseconds
    
    /**
     * PLAYWRIGHT MIGRATION: Replace WaitUtil.waitUntil() with expect().toBeVisible()
     */
    public static boolean expectVisible(Page page, String selector) {
        return expectVisible(page, selector, DEFAULT_TIMEOUT_MS);
    }
    
    public static boolean expectVisible(Page page, String selector, int timeoutMs) {
        try {
            PlaywrightAssertions.assertThat(page.locator(selector))
                .isVisible(new com.microsoft.playwright.assertions.LocatorAssertions.IsVisibleOptions()
                    .setTimeout(timeoutMs));
            LOGGER.debug("Element visible: {}", selector);
            return true;
        } catch (Exception e) {
            LOGGER.debug("Element not visible within timeout: {} ({}ms)", selector, timeoutMs);
            return false;
        }
    }
    
    /**
     * PLAYWRIGHT MIGRATION: Overloaded method for Locator parameter
     */
    public static boolean expectVisible(Page page, Locator locator) {
        return expectVisible(page, locator, DEFAULT_TIMEOUT_MS);
    }
    
    public static boolean expectVisible(Page page, Locator locator, int timeoutMs) {
        try {
            PlaywrightAssertions.assertThat(locator)
                .isVisible(new com.microsoft.playwright.assertions.LocatorAssertions.IsVisibleOptions()
                    .setTimeout(timeoutMs));
            LOGGER.debug("Element visible: {}", locator);
            return true;
        } catch (Exception e) {
            LOGGER.debug("Element not visible within timeout: {} ({}ms)", locator, timeoutMs);
            return false;
        }
    }
    
    /**
     * PLAYWRIGHT MIGRATION: Replace WaitUtil.waitUntil() for element presence
     */
    public static boolean expectAttached(Page page, String selector) {
        return expectAttached(page, selector, DEFAULT_TIMEOUT_MS);
    }
    
    public static boolean expectAttached(Page page, String selector, int timeoutMs) {
        try {
            PlaywrightAssertions.assertThat(page.locator(selector))
                .isAttached(new com.microsoft.playwright.assertions.LocatorAssertions.IsAttachedOptions()
                    .setTimeout(timeoutMs));
            LOGGER.debug("Element attached: {}", selector);
            return true;
        } catch (Exception e) {
            LOGGER.debug("Element not attached within timeout: {} ({}ms)", selector, timeoutMs);
            return false;
        }
    }
    
    /**
     * PLAYWRIGHT MIGRATION: Replace WaitUtil.waitForElementsList() with count-based expectations
     */
    public static List<Locator> expectElements(Page page, String selector) {
        return expectElements(page, selector, DEFAULT_TIMEOUT_MS);
    }
    
    public static List<Locator> expectElements(Page page, String selector, int timeoutMs) {
        Locator locator = page.locator(selector);
        
        // Wait for at least one element to be attached
        try {
            locator.first().waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.ATTACHED)
                .setTimeout(timeoutMs));
        } catch (Exception e) {
            LOGGER.debug("No elements found for selector: {} ({}ms)", selector, timeoutMs);
            return List.of(); // Return empty list if no elements found
        }
        
        // Return all matching locators
        return locator.all();
    }
    
    /**
     * PLAYWRIGHT MIGRATION: Replace WaitUtil.waitForElementsList() with count verification
     */
    public static boolean expectElementCount(Page page, String selector, int expectedCount) {
        return expectElementCount(page, selector, expectedCount, DEFAULT_TIMEOUT_MS);
    }
    
    public static boolean expectElementCount(Page page, String selector, int expectedCount, int timeoutMs) {
        try {
            PlaywrightAssertions.assertThat(page.locator(selector))
                .hasCount(expectedCount, new com.microsoft.playwright.assertions.LocatorAssertions.HasCountOptions()
                    .setTimeout(timeoutMs));
            LOGGER.debug("Element count verified: {} has {} elements", selector, expectedCount);
            return true;
        } catch (Exception e) {
            LOGGER.debug("Element count mismatch: {} expected {} elements ({}ms)", selector, expectedCount, timeoutMs);
            return false;
        }
    }
    
    /**
     * PLAYWRIGHT MIGRATION: Replace WaitUtil.waitUntilPageIsReady() with load state waiting
     */
    public static void expectPageReady(Page page) {
        expectPageReady(page, DEFAULT_TIMEOUT_MS);
    }
    
    public static void expectPageReady(Page page, int timeoutMs) {
        LOGGER.debug("Waiting for page to be ready");
        
        // Wait for DOM content to be loaded
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED, 
            new Page.WaitForLoadStateOptions().setTimeout(timeoutMs));
        
        // Wait for network to be idle (no ongoing requests)
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE,
            new Page.WaitForLoadStateOptions().setTimeout(timeoutMs));
        
        LOGGER.debug("Page ready");
    }
    
    /**
     * PLAYWRIGHT MIGRATION: Replace WaitUtil.waitUntilElementStable() with expect patterns
     */
    public static boolean expectElementStable(Page page, String selector) {
        return expectElementStable(page, selector, DEFAULT_TIMEOUT_MS);
    }
    
    public static boolean expectElementStable(Page page, String selector, int timeoutMs) {
        try {
            Locator locator = page.locator(selector);
            
            // Wait for element to be visible and enabled (stable for interaction)
            PlaywrightAssertions.assertThat(locator)
                .isVisible(new com.microsoft.playwright.assertions.LocatorAssertions.IsVisibleOptions()
                    .setTimeout(timeoutMs));
            PlaywrightAssertions.assertThat(locator)
                .isEnabled(new com.microsoft.playwright.assertions.LocatorAssertions.IsEnabledOptions()
                    .setTimeout(timeoutMs));
            
            LOGGER.debug("Element stable: {}", selector);
            return true;
        } catch (Exception e) {
            LOGGER.debug("Element not stable within timeout: {} ({}ms)", selector, timeoutMs);
            return false;
        }
    }
    
    /**
     * PLAYWRIGHT MIGRATION: Wait for element to be hidden/disappear
     */
    public static boolean expectHidden(Page page, String selector) {
        return expectHidden(page, selector, DEFAULT_TIMEOUT_MS);
    }
    
    public static boolean expectHidden(Page page, String selector, int timeoutMs) {
        try {
            PlaywrightAssertions.assertThat(page.locator(selector))
                .isHidden(new com.microsoft.playwright.assertions.LocatorAssertions.IsHiddenOptions()
                    .setTimeout(timeoutMs));
            LOGGER.debug("Element hidden: {}", selector);
            return true;
        } catch (Exception e) {
            LOGGER.debug("Element still visible after timeout: {} ({}ms)", selector, timeoutMs);
            return false;
        }
    }
    
    /**
     * PLAYWRIGHT MIGRATION: Wait for text content to match
     */
    public static boolean expectText(Page page, String selector, String expectedText) {
        return expectText(page, selector, expectedText, DEFAULT_TIMEOUT_MS);
    }
    
    public static boolean expectText(Page page, String selector, String expectedText, int timeoutMs) {
        try {
            PlaywrightAssertions.assertThat(page.locator(selector))
                .hasText(expectedText, new com.microsoft.playwright.assertions.LocatorAssertions.HasTextOptions()
                    .setTimeout(timeoutMs));
            LOGGER.debug("Text verified: {} contains '{}'", selector, expectedText);
            return true;
        } catch (Exception e) {
            LOGGER.debug("Text mismatch: {} does not contain '{}' ({}ms)", selector, expectedText, timeoutMs);
            return false;
        }
    }
    
    /**
     * PLAYWRIGHT MIGRATION: Replace complex conditional waits with simple expect patterns
     */
    public static boolean expectAnyCondition(Page page, String selector, Condition... conditions) {
        return expectAnyCondition(page, selector, DEFAULT_TIMEOUT_MS, conditions);
    }
    
    public static boolean expectAnyCondition(Page page, String selector, int timeoutMs, Condition... conditions) {
        for (Condition condition : conditions) {
            try {
                switch (condition) {
                    case VISIBLE -> {
                        if (expectVisible(page, selector, timeoutMs)) return true;
                    }
                    case HIDDEN -> {
                        if (expectHidden(page, selector, timeoutMs)) return true;
                    }
                    case ATTACHED -> {
                        if (expectAttached(page, selector, timeoutMs)) return true;
                    }
                    case STABLE -> {
                        if (expectElementStable(page, selector, timeoutMs)) return true;
                    }
                }
            } catch (Exception e) {
                // Continue to next condition
                LOGGER.debug("Condition {} failed for {}: {}", condition, selector, e.getMessage());
            }
        }
        return false;
    }
    
    /**
     * Conditions enum for flexible waiting strategies
     */
    public enum Condition {
        VISIBLE, HIDDEN, ATTACHED, STABLE
    }
    
    /**
     * PLAYWRIGHT MIGRATION: Page URL expectation
     */
    public static boolean expectUrl(Page page, String expectedUrl) {
        return expectUrl(page, expectedUrl, DEFAULT_TIMEOUT_MS);
    }
    
    public static boolean expectUrl(Page page, String expectedUrl, int timeoutMs) {
        try {
            PlaywrightAssertions.assertThat(page)
                .hasURL(expectedUrl, new com.microsoft.playwright.assertions.PageAssertions.HasURLOptions()
                    .setTimeout(timeoutMs));
            LOGGER.debug("URL verified: {}", expectedUrl);
            return true;
        } catch (Exception e) {
            LOGGER.debug("URL mismatch: expected '{}' ({}ms)", expectedUrl, timeoutMs);
            return false;
        }
    }
}