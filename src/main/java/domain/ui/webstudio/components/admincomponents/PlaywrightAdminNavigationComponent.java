package domain.ui.webstudio.components.admincomponents;

import configuration.core.ui.PlaywrightBasePageComponent;
import configuration.core.ui.PlaywrightWebElement;
import lombok.Getter;

public class PlaywrightAdminNavigationComponent extends PlaywrightBasePageComponent {

    private PlaywrightWebElement navigationItemTemplate;

    public PlaywrightAdminNavigationComponent() {
        super(configuration.driver.PlaywrightDriverPool.getPage());
        initializeNavigationComponents();
    }

    public PlaywrightAdminNavigationComponent(PlaywrightWebElement rootLocator) {
        super(rootLocator);
        initializeNavigationComponents();
    }

    private void initializeNavigationComponents() {
        navigationItemTemplate = createScopedElement("xpath=.//li[contains(@class,'ant-menu-item') and ./span[text()='%s']]", "navigationItem");
    }

    public void clickNavigationItem(NavigationItem item) {
        navigationItemTemplate.format(item.getValue()).click();
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