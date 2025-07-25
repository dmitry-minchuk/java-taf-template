package domain.ui.webstudio.components;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import domain.ui.webstudio.pages.mainpages.PlaywrightAdminPage;
import org.openqa.selenium.support.FindBy;

/**
 * Playwright version of CurrentUserComponent - User menu dropdown component
 * Uses exact same architecture as Selenium version with @FindBy annotations
 */
public class PlaywrightCurrentUserComponent extends PlaywrightBasePageComponent {

    @FindBy(xpath = ".//li[@class='ant-menu-item' and ./span[text()='My Profile']]")
    private PlaywrightWebElement myProfileMenuItem;

    @FindBy(xpath = ".//li[@class='ant-menu-item' and ./span[text()='My Settings']]")
    private PlaywrightWebElement mySettingsMenuItem;

    @FindBy(xpath = ".//li[@class='ant-menu-item' and ./span[text()='Administration']]")
    private PlaywrightWebElement administrationMenuItem;

    @FindBy(xpath = ".//li[@class='ant-menu-item' and ./span[text()='Help']]")
    private PlaywrightWebElement helpMenuItem;

    @FindBy(xpath = ".//li[@class='ant-menu-item' and ./span[text()='Sign Out']]")
    private PlaywrightWebElement signOutMenuItem;

    /**
     * Navigate to My Profile page
     * Note: PlaywrightMyProfilePage not implemented yet
     */
    public void navigateToMyProfile() {
        select(MenuElements.MY_PROFILE);
        // TODO: return new PlaywrightMyProfilePage() when implemented
    }

    /**
     * Navigate to Administration page
     * Exact same logic as Selenium version: select(MenuElements.ADMINISTRATION); return new AdminPage();
     * @return PlaywrightAdminPage for administration operations
     */
    public PlaywrightAdminPage navigateToAdministration() {
        select(MenuElements.ADMINISTRATION);
        return new PlaywrightAdminPage();
    }

    /**
     * Open help page
     */
    public void openHelp() {
        select(MenuElements.HELP);
    }

    /**
     * Sign out current user
     */
    public void signOut() {
        select(MenuElements.SIGN_OUT);
    }

    /**
     * Select a menu element from the user dropdown
     * @param element Menu element to select
     */
    private void select(MenuElements element) {
        switch (element) {
            case MY_PROFILE:
                myProfileMenuItem.click();
                break;
            case MY_SETTINGS:
                mySettingsMenuItem.click();
                break;
            case ADMINISTRATION:
                administrationMenuItem.click();
                break;
            case HELP:
                helpMenuItem.click();
                break;
            case SIGN_OUT:
                signOutMenuItem.click();
                break;
        }
    }

    /**
     * Menu elements enum - exact same as Selenium version
     */
    public enum MenuElements {
        MY_PROFILE("My Profile"),
        MY_SETTINGS("My Settings"),
        ADMINISTRATION("Administration"),
        HELP("Help"),
        SIGN_OUT("Sign Out");

        private String value;

        MenuElements(String value) {
            this.value = value;
        }
    }
}