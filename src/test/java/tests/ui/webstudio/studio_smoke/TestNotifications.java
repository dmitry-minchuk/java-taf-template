package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.components.admincomponents.NotificationPageComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNotifications extends BaseTest {

    private String testMessage = "Check message!";
    private String message256 = "aultdsswodeisanalytic12skallodOOOlickslickdbsdefaultdsswodeisanalytic12skallodOOOlickslickdbsdefausanalytic12skallodOOOlickslickdbsdefaultdsswodeisanalytic12skallodOOOlicklytic12skallodOOOlickslickdbsdefausanalytic12skallodOOOlickslickdbsdefaultdsswodei256";

    @Test
    @TestCaseId("IPBQA-30617")
    @Description("Test notifications: send to all users, display, delete, validate message length and empty messages")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testNotifications() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        UserData newUser = new UserData("newUser", "password123");
        editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage()
                .clickAddUser()
                .setUsername(newUser.getLogin())
                .setPassword(newUser.getPassword())
                .inviteUser();
        NotificationPageComponent notificationComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToNotificationPage();

        notificationComponent.sendNotification(testMessage);
        assertThat(notificationComponent.isNotificationVisible()).isTrue();
        assertThat(notificationComponent.getNotificationText()).isEqualTo(testMessage);

        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(newUser);
        assertThat(editorPage.isNotificationVisible()).isTrue();
        assertThat(editorPage.getNotificationText()).isEqualTo(testMessage);

        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(UserService.getUser(User.ADMIN));
        assertThat(editorPage.isNotificationVisible()).isTrue();
        assertThat(editorPage.getNotificationText()).isEqualTo(testMessage);

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        assertThat(repositoryPage.isNotificationVisible()).isTrue();

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        assertThat(editorPage.isNotificationVisible()).isTrue();

        notificationComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToNotificationPage();
        notificationComponent.clearNotification();
        assertThat(notificationComponent.isNotificationVisible()).isFalse();

        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(newUser);
        assertThat(editorPage.isNotificationVisible()).isFalse();

        editorPage.openUserMenu().signOut();
        editorPage = loginService.login(UserService.getUser(User.ADMIN));

        notificationComponent = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToNotificationPage();

        notificationComponent.sendNotification(message256);
        assertThat(notificationComponent.getNotificationText()).isEqualTo(message256); // No limitation currently
        assertThat(notificationComponent.getNotificationText().length()).isEqualTo(256);

        notificationComponent.clearNotification();
        notificationComponent.sendNotification("");
        assertThat(notificationComponent.isNotificationVisible()).isFalse();

        notificationComponent.sendNotification("   ");
        assertThat(notificationComponent.isNotificationVisible()).isFalse(); //should not be visible
    }
}
