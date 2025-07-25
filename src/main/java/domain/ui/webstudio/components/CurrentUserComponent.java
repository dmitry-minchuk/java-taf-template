package domain.ui.webstudio.components;

import configuration.core.ui.SmartWebElement;
import configuration.core.ui.BasePageComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import helpers.utils.WaitUtil;
import lombok.Getter;
import org.openqa.selenium.support.FindBy;

public class CurrentUserComponent extends BasePageComponent {

    @FindBy(xpath = ".//li[@class='ant-menu-item' and ./span[text()='%s']]")
    private SmartWebElement menuOption;

    public CurrentUserComponent() {
        super();
        // PLAYWRIGHT MIGRATION: Removed WaitUtil.sleep() - components should use proper element waiting
    }

    public void select(MenuElements element) {
        menuOption.format(element.getValue()).click();
        // PLAYWRIGHT MIGRATION: Removed WaitUtil.sleep() - Playwright's click() waits for element to be actionable
    }

    // Navigation Methods
    
    public AdminPage navigateToMyProfile() {
        select(MenuElements.MY_PROFILE);
        return new AdminPage();
    }

    
    public AdminPage navigateToMySettings() {
        select(MenuElements.MY_SETTINGS);
        return new AdminPage();
    }

    
    public AdminPage navigateToAdministration() {
        select(MenuElements.ADMINISTRATION);
        return new AdminPage();
    }

    
    public void openHelp() {
        select(MenuElements.HELP);
    }

    
    public void signOut() {
        select(MenuElements.SIGN_OUT);
    }

    // Utility Methods
    
    public boolean isMenuDisplayed() {
        return menuOption.format(MenuElements.MY_PROFILE.getValue()).isDisplayed(2);
    }

    
    public boolean isMenuElementAvailable(MenuElements element) {
        return menuOption.format(element.getValue()).isDisplayed(2);
    }

    
    public String getMenuElementText(MenuElements element) {
        return menuOption.format(element.getValue()).getText();
    }

    
    public boolean clickMenuElementIfAvailable(MenuElements element) {
        if (isMenuElementAvailable(element)) {
            select(element);
            return true;
        }
        return false;
    }

    @Getter
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
