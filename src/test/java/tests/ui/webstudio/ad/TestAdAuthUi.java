package tests.ui.webstudio.ad;

import com.epam.reportportal.annotations.Description;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.components.common.UserSlidingRightMenuComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.LoginPage;
import helpers.service.SambaAdInfrastructureService;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PLAYWRIGHT_DOCKER test — Active Directory (LDAP) authentication. WebStudio runs in
 * {@code user.mode=ad} against an ephemeral Samba AD DC (no real Microsoft infrastructure).
 * Covers the full form-auth lifecycle: admin login, sign-out returning to the login form,
 * switching to a non-admin AD user with different privileges, and rejection of bad credentials.
 */
public class TestAdAuthUi extends AbstractAdUiTest {

    @Test
    @Description("AD auth: an admin AD user logs in via the Studio form and sees Administration; sign-out "
            + "returns to the login form; switching to a non-admin AD user yields no Administration; "
            + "invalid credentials are rejected.")
    @AppContainerConfig(startParams = AppContainerStartParameters.STUDIO_AD_PARAMS)
    public void testAdLoginLogoutAndUserSwitch() {
        // 1. Admin AD user logs in via the Studio form and has admin rights.
        EditorPage editorPage = adLogin(SambaAdInfrastructureService.ADMIN_USER, SambaAdInfrastructureService.USER_PASSWORD);
        UserSlidingRightMenuComponent adminMenu = editorPage.openUserMenu();
        assertThat(adminMenu.isAdministrationMenuItemVisible())
                .as("AD admin (%s) sees the Administration menu", SambaAdInfrastructureService.ADMIN_USER)
                .isTrue();

        // 2. Sign out — AD is form-based, so the Studio login form must reappear (session ended).
        adminMenu.signOut();
        LocalDriverPool.getPage().waitForLoadState();
        LocalDriverPool.getPage().navigate(LocalDriverPool.getAppUrl());
        assertThat(new LoginPage().isLoginFormDisplayed(15000))
                .as("after AD Sign Out the Studio login form reappears")
                .isTrue();

        // 3. Switch user: a non-admin AD user logs in and does NOT get Administration.
        EditorPage userPage = adLogin(SambaAdInfrastructureService.REGULAR_USER, SambaAdInfrastructureService.USER_PASSWORD);
        assertThat(userPage.openUserMenu().isAdministrationMenuItemVisible())
                .as("non-admin AD user (%s) does NOT see the Administration menu", SambaAdInfrastructureService.REGULAR_USER)
                .isFalse();

        // 4. Invalid credentials are rejected.
        LocalDriverPool.getBrowserContext().clearCookies();
        LocalDriverPool.getPage().navigate(LocalDriverPool.getAppUrl());
        LoginPage loginPage = new LoginPage();
        loginPage.login(new UserData(SambaAdInfrastructureService.ADMIN_USER, "WrongPassword!"));
        assertThat(loginPage.isLoginErrorDisplayed(10000))
                .as("invalid AD credentials are rejected with a login error")
                .isTrue();
    }
}
