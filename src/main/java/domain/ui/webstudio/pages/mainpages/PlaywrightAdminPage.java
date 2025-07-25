package domain.ui.webstudio.pages.mainpages;

import com.microsoft.playwright.Page;
import domain.ui.webstudio.components.admincomponents.PlaywrightEmailPageComponent;
import helpers.utils.PlaywrightExpectUtil;

/**
 * Playwright version of AdminPage for Admin UI navigation and component access
 * Provides navigation to various admin components using Playwright locators
 */
public class PlaywrightAdminPage {
    
    private final Page page;
    
    public PlaywrightAdminPage(Page page) {
        this.page = page;
    }
    
    /**
     * Navigate to Email page by clicking on the Mail navigation item
     * Based on AdminNavigationComponent.clickMail() implementation
     * @return PlaywrightEmailPageComponent for email configuration
     */
    public PlaywrightEmailPageComponent navigateToEmailPage() {
        // Click on the Mail menu item using exact same pattern as AdminNavigationComponent
        // Original Selenium: .//li[contains(@class,'ant-menu-item') and ./span[text()='Mail']]
        var mailMenuItem = page.locator("li.ant-menu-item:has(span:text('Mail'))");
        PlaywrightExpectUtil.expectVisible(page, mailMenuItem);
        mailMenuItem.click();
        
        // Wait for the email page content to load
        PlaywrightExpectUtil.expectVisible(page, page.locator("div#content"));
        
        return new PlaywrightEmailPageComponent(page);
    }
    
    /**
     * Get the current page instance
     * @return Playwright page
     */
    public Page getPage() {
        return page;
    }
}