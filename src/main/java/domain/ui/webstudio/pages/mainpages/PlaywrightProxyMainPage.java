package domain.ui.webstudio.pages.mainpages;

import configuration.core.ui.PlaywrightBasePage;
import configuration.core.ui.PlaywrightWebElement;
import domain.ui.webstudio.components.PlaywrightCurrentUserComponent;
import domain.ui.webstudio.components.PlaywrightTabSwitcherComponent;
import lombok.Getter;
import org.openqa.selenium.support.FindBy;

/**
 * Playwright version of ProxyMainPage - Base class for main application pages
 * Uses exact same architecture as Selenium version with @FindBy annotations
 */
public abstract class PlaywrightProxyMainPage extends PlaywrightBasePage {

    @FindBy(xpath = "//div[@class='ant-drawer-content-wrapper']")
    private PlaywrightCurrentUserComponent currentUserComponent;

    @FindBy(xpath = "//div[@class='user-logo']/span")
    private PlaywrightWebElement userLogo;

    @Getter
    @FindBy(xpath = "//ul[@role='menu' and contains(@class,'ant-menu-horizontal')]")
    private PlaywrightTabSwitcherComponent tabSwitcherComponent;

    @FindBy(xpath = "//div[@class='message closable']")
    private PlaywrightWebElement message;

    public PlaywrightProxyMainPage(String urlAppender) {
        super(urlAppender);
    }

    /**
     * Get studio message text if displayed
     * @return Message text or null if not displayed
     */
    public String getStudioMessage() {
        if (message.isDisplayed()) {
            return message.getText();
        } else {
            return null;
        }
    }

    /**
     * Check if studio message is displayed with specific text
     * @param text Message text to check for
     * @return true if message is displayed and contains the specified text
     */
    public boolean isStudioMessageDisplayed(String text) {
        return message.isDisplayed() && message.getText().contains(text);
    }

    /**
     * Get the current user component by clicking user logo and returning the menu
     * Exact same logic as Selenium version: userLogo.click(); currentUserComponent.isPresent(); return currentUserComponent;
     * @return PlaywrightCurrentUserComponent for user menu operations
     */
    public PlaywrightCurrentUserComponent getCurrentUserComponent() {
        userLogo.click();
        currentUserComponent.isPresent();
        return currentUserComponent;
    }
}