package domain.ui.webstudio.pages.mainpages;

import configuration.core.ui.PlaywrightWebElement;
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

public class PlaywrightAdminPage extends PlaywrightProxyMainPage {

    @Getter
    private PlaywrightAdminNavigationComponent adminNavigationComponent;
    private PlaywrightEmailPageComponent emailPageComponent;
    private PlaywrightSystemSettingsPageComponent systemSettingsPageComponent;
    private PlaywrightUsersPageComponent usersPageComponent;
    private PlaywrightSecurityPageComponent securityPageComponent;
    private PlaywrightMyProfilePageComponent myProfilePageComponent;
    private PlaywrightMySettingsPageComponent mySettingsPageComponent;
    private PlaywrightRepositoriesPageComponent repositoriesPageComponent;
    private PlaywrightNotificationPageComponent notificationPageComponent;
    private PlaywrightTagsPageComponent tagsPageComponent;
    
    private PlaywrightWebElement adminNavigation;
    private PlaywrightWebElement contentContainer;

    public PlaywrightAdminPage() {
        super("/");
        initializeAdminComponents();
    }

    private void initializeAdminComponents() {
        // Define root locators for component scoping - EXACT SAME as legacy AdminPage
        adminNavigation = new PlaywrightWebElement(page, "xpath=//div[@id='main-menu']", "Admin Navigation Container");
        contentContainer = new PlaywrightWebElement(page, "xpath=//div[@id='content']", "Content Container");
        
        // Initialize components with proper root locators
        adminNavigationComponent = new PlaywrightAdminNavigationComponent(adminNavigation);
        emailPageComponent = new PlaywrightEmailPageComponent(contentContainer);
        systemSettingsPageComponent = new PlaywrightSystemSettingsPageComponent(contentContainer);
        usersPageComponent = new PlaywrightUsersPageComponent(contentContainer);
        securityPageComponent = new PlaywrightSecurityPageComponent(contentContainer);
        myProfilePageComponent = new PlaywrightMyProfilePageComponent(contentContainer);
        mySettingsPageComponent = new PlaywrightMySettingsPageComponent(contentContainer);
        repositoriesPageComponent = new PlaywrightRepositoriesPageComponent(contentContainer);
        notificationPageComponent = new PlaywrightNotificationPageComponent(contentContainer);
        tagsPageComponent = new PlaywrightTagsPageComponent(contentContainer);
    }

    public PlaywrightEmailPageComponent navigateToEmailPage() {
        adminNavigationComponent.clickMail();
        return emailPageComponent;
    }

    public PlaywrightSystemSettingsPageComponent navigateToSystemSettingsPage() {
        adminNavigationComponent.clickSystem();
        return systemSettingsPageComponent;
    }

    public PlaywrightUsersPageComponent navigateToUsersPage() {
        adminNavigationComponent.clickUsers();
        return usersPageComponent;
    }

    public PlaywrightSecurityPageComponent navigateToSecurityPage() {
        adminNavigationComponent.clickSecurity();
        return securityPageComponent;
    }

    public PlaywrightMyProfilePageComponent navigateToMyProfilePage() {
        adminNavigationComponent.clickMyProfile();
        return myProfilePageComponent;
    }

    public PlaywrightMySettingsPageComponent navigateToMySettingsPage() {
        adminNavigationComponent.clickMySettings();
        return mySettingsPageComponent;
    }

    public PlaywrightRepositoriesPageComponent navigateToRepositoriesPage() {
        adminNavigationComponent.clickRepositories();
        return repositoriesPageComponent;
    }

    public PlaywrightNotificationPageComponent navigateToNotificationPage() {
        adminNavigationComponent.clickNotification();
        return notificationPageComponent;
    }

    public PlaywrightTagsPageComponent navigateToTagsPage() {
        adminNavigationComponent.clickTags();
        return tagsPageComponent;
    }
}