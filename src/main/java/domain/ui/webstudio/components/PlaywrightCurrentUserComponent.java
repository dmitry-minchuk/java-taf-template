package domain.ui.webstudio.components;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import configuration.driver.PlaywrightDriverPool;
import domain.ui.webstudio.pages.mainpages.PlaywrightAdminPage;

public class PlaywrightCurrentUserComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement myProfileMenuItem;
    private PlaywrightWebElement mySettingsMenuItem;
    private PlaywrightWebElement administrationMenuItem;
    private PlaywrightWebElement helpMenuItem;
    private PlaywrightWebElement signOutMenuItem;

    public PlaywrightCurrentUserComponent() {
        super(PlaywrightDriverPool.getPage());
        initializeComponents();
    }
    
    public PlaywrightCurrentUserComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeComponents();
    }

    private void initializeComponents() {
        myProfileMenuItem = createScopedElement("xpath=.//li[contains(@class,'ant-menu-item')]//span[text()='My Profile']", "My Profile Menu Item");
        mySettingsMenuItem = createScopedElement("xpath=.//li[contains(@class,'ant-menu-item')]//span[text()='My Settings']", "My Settings Menu Item");
        administrationMenuItem = createScopedElement("xpath=.//li[contains(@class,'ant-menu-item')]//span[text()='Administration']", "Administration Menu Item");
        helpMenuItem = createScopedElement("xpath=.//li[contains(@class,'ant-menu-item')]//span[text()='Help']", "Help Menu Item");
        signOutMenuItem = createScopedElement("xpath=.//li[contains(@class,'ant-menu-item')]//span[text()='Sign Out']", "Sign Out Menu Item");
    }

    public void navigateToMyProfile() {
        select(MenuElements.MY_PROFILE);
    }

    public PlaywrightAdminPage navigateToAdministration() {
        select(MenuElements.ADMINISTRATION);
        return new PlaywrightAdminPage();
    }

    public void openHelp() {
        select(MenuElements.HELP);
    }

    public void signOut() {
        select(MenuElements.SIGN_OUT);
    }

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