package domain.ui.webstudio.components;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import domain.ui.webstudio.pages.mainpages.PlaywrightAdminPage;

public class PlaywrightCurrentUserComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement myProfileMenuItem;
    private PlaywrightWebElement mySettingsMenuItem;
    private PlaywrightWebElement administrationMenuItem;
    private PlaywrightWebElement helpMenuItem;
    private PlaywrightWebElement signOutMenuItem;

    public PlaywrightCurrentUserComponent() {
        super(configuration.driver.PlaywrightDriverPool.getPage());
        initializeComponents();
    }

    private void initializeComponents() {
        myProfileMenuItem = new PlaywrightWebElement(page, "li.ant-menu-item:has(span:text('My Profile'))");
        mySettingsMenuItem = new PlaywrightWebElement(page, "li.ant-menu-item:has(span:text('My Settings'))");
        administrationMenuItem = new PlaywrightWebElement(page, "li.ant-menu-item:has(span:text('Administration'))");
        helpMenuItem = new PlaywrightWebElement(page, "li.ant-menu-item:has(span:text('Help'))");
        signOutMenuItem = new PlaywrightWebElement(page, "li.ant-menu-item:has(span:text('Sign Out'))");
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