package domain.ui.webstudio.pages.mainpages;

import configuration.core.ui.WebElement;
import domain.ui.webstudio.components.admincomponents.AdminNavigationComponent;
import domain.ui.webstudio.components.admincomponents.EmailPageComponent;
import domain.ui.webstudio.components.admincomponents.MyProfilePageComponent;
import domain.ui.webstudio.components.admincomponents.MySettingsPageComponent;
import domain.ui.webstudio.components.admincomponents.NotificationPageComponent;
import domain.ui.webstudio.components.admincomponents.RepositoriesPageComponent;
import domain.ui.webstudio.components.admincomponents.SecurityPageComponent;
import domain.ui.webstudio.components.admincomponents.SystemSettingsPageComponent;
import domain.ui.webstudio.components.admincomponents.TagsPageComponent;
import domain.ui.webstudio.components.admincomponents.UsersPageComponent;
import domain.ui.webstudio.pages.BasePage;
import helpers.utils.WaitUtil;
import lombok.Getter;

public class AdminPage extends BasePage {

    @Getter
    private AdminNavigationComponent adminNavigationComponent;
    private EmailPageComponent emailPageComponent;
    private SystemSettingsPageComponent systemSettingsPageComponent;
    private UsersPageComponent usersPageComponent;
    private SecurityPageComponent securityPageComponent;
    private MyProfilePageComponent myProfilePageComponent;
    private MySettingsPageComponent mySettingsPageComponent;
    private RepositoriesPageComponent repositoriesPageComponent;
    private NotificationPageComponent notificationPageComponent;
    private TagsPageComponent tagsPageComponent;
    
    private WebElement adminNavigation;
    private WebElement contentContainer;

    public AdminPage() {
        super();
        initializeAdminComponents();
    }

    private void initializeAdminComponents() {
        // Define root locators for component scoping - EXACT SAME as legacy AdminPage
        adminNavigation = new WebElement(page, "xpath=//div[@id='main-menu']", "Admin Navigation Container");
        contentContainer = new WebElement(page, "xpath=//div[@id='content']", "Content Container");
        
        // Initialize components with proper root locators
        adminNavigationComponent = new AdminNavigationComponent(adminNavigation);
        emailPageComponent = new EmailPageComponent(contentContainer);
        systemSettingsPageComponent = new SystemSettingsPageComponent(contentContainer);
        usersPageComponent = new UsersPageComponent(contentContainer);
        securityPageComponent = new SecurityPageComponent(contentContainer);
        myProfilePageComponent = new MyProfilePageComponent(contentContainer);
        mySettingsPageComponent = new MySettingsPageComponent(contentContainer);
        repositoriesPageComponent = new RepositoriesPageComponent(contentContainer);
        notificationPageComponent = new NotificationPageComponent(contentContainer);
        tagsPageComponent = new TagsPageComponent(contentContainer);
    }

    public EmailPageComponent navigateToEmailPage() {
        adminNavigationComponent.clickMail();
        return emailPageComponent;
    }

    public SystemSettingsPageComponent navigateToSystemSettingsPage() {
        adminNavigationComponent.clickSystem();
        return systemSettingsPageComponent;
    }

    public UsersPageComponent navigateToUsersPage() {
        adminNavigationComponent.clickUsers();
        WaitUtil.sleep(1000, "Wait for users list to load");
        return usersPageComponent;
    }

    public SecurityPageComponent navigateToSecurityPage() {
        adminNavigationComponent.clickSecurity();
        return securityPageComponent;
    }

    public MyProfilePageComponent navigateToMyProfilePage() {
        adminNavigationComponent.clickMyProfile();
        return myProfilePageComponent;
    }

    public MySettingsPageComponent navigateToMySettingsPage() {
        adminNavigationComponent.clickMySettings();
        return mySettingsPageComponent;
    }

    public RepositoriesPageComponent navigateToRepositoriesPage() {
        adminNavigationComponent.clickRepositories();
        return repositoriesPageComponent;
    }

    public NotificationPageComponent navigateToNotificationPage() {
        adminNavigationComponent.clickNotification();
        return notificationPageComponent;
    }

    public TagsPageComponent navigateToTagsPage() {
        adminNavigationComponent.clickTags();
        return tagsPageComponent;
    }
}