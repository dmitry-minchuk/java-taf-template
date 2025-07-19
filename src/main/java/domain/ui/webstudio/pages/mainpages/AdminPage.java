package domain.ui.webstudio.pages.mainpages;

import domain.ui.webstudio.components.admincpmponents.AdminNavigationComponent;
import lombok.Getter;
import domain.ui.webstudio.components.admincpmponents.EmailPageComponent;
import domain.ui.webstudio.components.admincpmponents.MyProfilePageComponent;
import domain.ui.webstudio.components.admincpmponents.MySettingsPageComponent;
import domain.ui.webstudio.components.admincpmponents.NotificationPageComponent;
import domain.ui.webstudio.components.admincpmponents.RepositoriesPageComponent;
import domain.ui.webstudio.components.admincpmponents.SecurityPageComponent;
import domain.ui.webstudio.components.admincpmponents.SystemSettingsPageComponent;
import domain.ui.webstudio.components.admincpmponents.TagsPageComponent;
import domain.ui.webstudio.components.admincpmponents.UsersPageComponent;
import org.openqa.selenium.support.FindBy;

public class AdminPage extends ProxyMainPage {

    @Getter
    @FindBy(xpath = "//div[@id='main-menu']")
    private AdminNavigationComponent adminNavigationComponent;

    @FindBy(xpath = "//div[@id='content']")
    private EmailPageComponent emailPageComponent;

    @FindBy(xpath = "//div[@id='content']")
    private SystemSettingsPageComponent systemSettingsPageComponent;

    @FindBy(xpath = "//div[@id='content']")
    private UsersPageComponent usersPageComponent;

    @FindBy(xpath = "//div[@id='content']")
    private SecurityPageComponent securityPageComponent;

    @FindBy(xpath = "//div[@id='content']")
    private MyProfilePageComponent myProfilePageComponent;

    @FindBy(xpath = "//div[@id='content']")
    private MySettingsPageComponent mySettingsPageComponent;

    @FindBy(xpath = "//div[@id='content']")
    private RepositoriesPageComponent repositoriesPageComponent;

    @FindBy(xpath = "//div[@id='content']")
    private NotificationPageComponent notificationPageComponent;

    @FindBy(xpath = "//div[@id='content']")
    private TagsPageComponent tagsPageComponent;

    public AdminPage() {
        super("");
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
