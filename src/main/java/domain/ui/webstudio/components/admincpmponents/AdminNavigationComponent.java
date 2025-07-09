package domain.ui.webstudio.components.admincpmponents;

import configuration.core.ui.BasePageComponent;
import configuration.core.ui.SmartWebElement;
import lombok.Getter;
import org.openqa.selenium.support.FindBy;

public class AdminNavigationComponent extends BasePageComponent {

    @FindBy(xpath = ".//li[contains(@class,'ant-menu-item') and ./span[text()='%s']]")
    private SmartWebElement navigationItem;

    public AdminNavigationComponent() {
        super();
    }

    public void clickNavigationItem(NavigationItem item) {
        navigationItem.format(item.getValue()).click(3);
    }

    public void clickMyProfile() {
        clickNavigationItem(NavigationItem.MY_PROFILE);
    }

    public void clickMySettings() {
        clickNavigationItem(NavigationItem.MY_SETTINGS);
    }

    public void clickRepositories() {
        clickNavigationItem(NavigationItem.REPOSITORIES);
    }

    public void clickSystem() {
        clickNavigationItem(NavigationItem.SYSTEM);
    }

    public void clickSecurity() {
        clickNavigationItem(NavigationItem.SECURITY);
    }

    public void clickUsers() {
        clickNavigationItem(NavigationItem.USERS);
    }

    public void clickMail() {
        clickNavigationItem(NavigationItem.MAIL);
    }

    public void clickNotification() {
        clickNavigationItem(NavigationItem.NOTIFICATION);
    }

    public void clickTags() {
        clickNavigationItem(NavigationItem.TAGS);
    }

    @Getter
    public enum NavigationItem {
        MY_PROFILE("My Profile"),
        MY_SETTINGS("My Settings"),
        REPOSITORIES("Repositories"),
        SYSTEM("System"),
        SECURITY("Security"),
        USERS("Users"),
        MAIL("Mail"),
        NOTIFICATION("Notification"),
        TAGS("Tags");

        private final String value;

        NavigationItem(String value) {
            this.value = value;
        }
    }
}