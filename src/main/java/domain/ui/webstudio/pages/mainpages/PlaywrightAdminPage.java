package domain.ui.webstudio.pages.mainpages;

import domain.ui.webstudio.components.admincomponents.PlaywrightAdminNavigationComponent;
import domain.ui.webstudio.components.admincomponents.PlaywrightEmailPageComponent;
import domain.ui.webstudio.components.admincomponents.PlaywrightMyProfilePageComponent;
import domain.ui.webstudio.components.admincomponents.PlaywrightMySettingsPageComponent;
import domain.ui.webstudio.components.admincomponents.PlaywrightNotificationPageComponent;
import domain.ui.webstudio.components.admincomponents.PlaywrightRepositoriesPageComponent;
import domain.ui.webstudio.components.admincomponents.PlaywrightSecurityPageComponent;
import domain.ui.webstudio.components.admincomponents.PlaywrightSystemSettingsPageComponent;
import domain.ui.webstudio.components.admincomponents.PlaywrightTagsPageComponent;
import domain.ui.webstudio.components.admincomponents.PlaywrightUsersPageComponent;
import lombok.Getter;
import org.openqa.selenium.support.FindBy;

/**
 * Playwright version of AdminPage - Admin UI navigation and component access
 * Uses exact same architecture as Selenium version with @FindBy annotations
 */
public class PlaywrightAdminPage extends PlaywrightProxyMainPage {

    @Getter
    @FindBy(xpath = "//div[@id='main-menu']")
    private PlaywrightAdminNavigationComponent adminNavigationComponent;

    @FindBy(xpath = "//div[@id='content']")
    private PlaywrightEmailPageComponent emailPageComponent;

    @FindBy(xpath = "//div[@id='content']")
    private PlaywrightSystemSettingsPageComponent systemSettingsPageComponent;

    @FindBy(xpath = "//div[@id='content']")
    private PlaywrightUsersPageComponent usersPageComponent;

    @FindBy(xpath = "//div[@id='content']")
    private PlaywrightSecurityPageComponent securityPageComponent;

    @FindBy(xpath = "//div[@id='content']")
    private PlaywrightMyProfilePageComponent myProfilePageComponent;

    @FindBy(xpath = "//div[@id='content']")
    private PlaywrightMySettingsPageComponent mySettingsPageComponent;

    @FindBy(xpath = "//div[@id='content']")
    private PlaywrightRepositoriesPageComponent repositoriesPageComponent;

    @FindBy(xpath = "//div[@id='content']")
    private PlaywrightNotificationPageComponent notificationPageComponent;

    @FindBy(xpath = "//div[@id='content']")
    private PlaywrightTagsPageComponent tagsPageComponent;

    public PlaywrightAdminPage() {
        super("/");
    }

    /**
     * Navigate to Email page by using admin navigation component
     * Same pattern as Selenium: adminNavigationComponent.clickMail(); return emailPageComponent;
     * @return PlaywrightEmailPageComponent for email configuration
     */
    public PlaywrightEmailPageComponent navigateToEmailPage() {
        adminNavigationComponent.clickMail();
        return emailPageComponent;
    }

    /**
     * Navigate to System Settings page
     * @return PlaywrightSystemSettingsPageComponent for system settings
     */
    public PlaywrightSystemSettingsPageComponent navigateToSystemSettingsPage() {
        adminNavigationComponent.clickSystemSettings();
        return systemSettingsPageComponent;
    }

    /**
     * Navigate to Users page
     * @return PlaywrightUsersPageComponent for user management
     */
    public PlaywrightUsersPageComponent navigateToUsersPage() {
        adminNavigationComponent.clickUsers();
        return usersPageComponent;
    }

    /**
     * Navigate to Security page
     * @return PlaywrightSecurityPageComponent for security settings
     */
    public PlaywrightSecurityPageComponent navigateToSecurityPage() {
        adminNavigationComponent.clickSecurity();
        return securityPageComponent;
    }

    /**
     * Navigate to My Profile page
     * @return PlaywrightMyProfilePageComponent for profile management
     */
    public PlaywrightMyProfilePageComponent navigateToMyProfilePage() {
        adminNavigationComponent.clickMyProfile();
        return myProfilePageComponent;
    }

    /**
     * Navigate to My Settings page
     * @return PlaywrightMySettingsPageComponent for personal settings
     */
    public PlaywrightMySettingsPageComponent navigateToMySettingsPage() {
        adminNavigationComponent.clickMySettings();
        return mySettingsPageComponent;
    }

    /**
     * Navigate to Repositories page
     * @return PlaywrightRepositoriesPageComponent for repository management
     */
    public PlaywrightRepositoriesPageComponent navigateToRepositoriesPage() {
        adminNavigationComponent.clickRepositories();
        return repositoriesPageComponent;
    }

    /**
     * Navigate to Notification page
     * @return PlaywrightNotificationPageComponent for notification settings
     */
    public PlaywrightNotificationPageComponent navigateToNotificationPage() {
        adminNavigationComponent.clickNotification();
        return notificationPageComponent;
    }

    /**
     * Navigate to Tags page
     * @return PlaywrightTagsPageComponent for tag management
     */
    public PlaywrightTagsPageComponent navigateToTagsPage() {
        adminNavigationComponent.clickTags();
        return tagsPageComponent;
    }
}