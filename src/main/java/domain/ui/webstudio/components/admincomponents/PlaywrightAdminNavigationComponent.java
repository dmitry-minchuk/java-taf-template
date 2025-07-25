package domain.ui.webstudio.components.admincomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Playwright version of AdminNavigationComponent - Admin navigation menu
 * Uses exact same architecture as Selenium version with @FindBy annotations
 */
public class PlaywrightAdminNavigationComponent extends PlaywrightBasePageComponent {

    @FindBy(xpath = ".//li[contains(@class,'ant-menu-item') and ./span[text()='Mail']]")
    private PlaywrightWebElement mailMenuItem;

    @FindBy(xpath = ".//li[contains(@class,'ant-menu-item') and ./span[text()='System Settings']]")
    private PlaywrightWebElement systemSettingsMenuItem;

    @FindBy(xpath = ".//li[contains(@class,'ant-menu-item') and ./span[text()='Users']]")
    private PlaywrightWebElement usersMenuItem;

    @FindBy(xpath = ".//li[contains(@class,'ant-menu-item') and ./span[text()='Security']]")
    private PlaywrightWebElement securityMenuItem;

    @FindBy(xpath = ".//li[contains(@class,'ant-menu-item') and ./span[text()='My Profile']]")
    private PlaywrightWebElement myProfileMenuItem;

    @FindBy(xpath = ".//li[contains(@class,'ant-menu-item') and ./span[text()='My Settings']]")
    private PlaywrightWebElement mySettingsMenuItem;

    @FindBy(xpath = ".//li[contains(@class,'ant-menu-item') and ./span[text()='Repositories']]")
    private PlaywrightWebElement repositoriesMenuItem;

    @FindBy(xpath = ".//li[contains(@class,'ant-menu-item') and ./span[text()='Notification']]")
    private PlaywrightWebElement notificationMenuItem;

    @FindBy(xpath = ".//li[contains(@class,'ant-menu-item') and ./span[text()='Tags']]")
    private PlaywrightWebElement tagsMenuItem;

    /**
     * Click on Mail navigation item
     * Same as Selenium: clickNavigationItem(NavigationItem.MAIL)
     */
    public void clickMail() {
        mailMenuItem.click();
    }

    /**
     * Click on System Settings navigation item
     */
    public void clickSystemSettings() {
        systemSettingsMenuItem.click();
    }

    /**
     * Click on Users navigation item
     */
    public void clickUsers() {
        usersMenuItem.click();
    }

    /**
     * Click on Security navigation item
     */
    public void clickSecurity() {
        securityMenuItem.click();
    }

    /**
     * Click on My Profile navigation item
     */
    public void clickMyProfile() {
        myProfileMenuItem.click();
    }

    /**
     * Click on My Settings navigation item
     */
    public void clickMySettings() {
        mySettingsMenuItem.click();
    }

    /**
     * Click on Repositories navigation item
     */
    public void clickRepositories() {
        repositoriesMenuItem.click();
    }

    /**
     * Click on Notification navigation item
     */
    public void clickNotification() {
        notificationMenuItem.click();
    }

    /**
     * Click on Tags navigation item
     */
    public void clickTags() {
        tagsMenuItem.click();
    }
}