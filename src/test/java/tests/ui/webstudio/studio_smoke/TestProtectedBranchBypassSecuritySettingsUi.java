package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.SecurityPageComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EPBDS-15960 Section G — Security settings page "Allow Managers to bypass
 * protected branches" checkbox: visibility, tooltip icon, persistence.
 */
public class TestProtectedBranchBypassSecuritySettingsUi extends BaseTest {

    @Test
    @TestCaseId("EPBDS-15960")
    @Description("Section G: 'Allow Managers to bypass protected branches' checkbox is rendered "
            + "with its info-tooltip icon, defaults to OFF, toggles on click, and the saved value "
            + "persists after Apply + restart + re-login.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAllowBypassCheckboxRenderTogglePersistence() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        SecurityPageComponent security = adminPage.navigateToSecurityPage();

        assertThat(security.isAllowBypassVisible())
                .as("G.1 — checkbox 'Allow Managers to bypass protected branches' is rendered")
                .isTrue();
        assertThat(security.isAllowBypassTooltipIconVisible())
                .as("G.2 — checkbox label exposes the info-circle tooltip icon")
                .isTrue();
        assertThat(security.isAllowBypassChecked())
                .as("G.1 — checkbox defaults to OFF on a fresh container")
                .isFalse();

        security.toggleAllowBypass();
        assertThat(security.isAllowBypassChecked())
                .as("G.1 — checkbox flips to ON after click")
                .isTrue();

        security.clickApplyAndRelogin(User.ADMIN);

        SecurityPageComponent securityAfter = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToSecurityPage();
        assertThat(securityAfter.isAllowBypassChecked())
                .as("G.3 — toggled value persists after Apply + restart + re-login")
                .isTrue();
    }
}
