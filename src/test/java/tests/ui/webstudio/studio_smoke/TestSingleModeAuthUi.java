package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.LoginPage;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Single-user mode ({@code user.mode=single}). The app auto-authenticates every request as the
 * configured {@code security.single.username} with no login step — opening the app lands directly in
 * the editor, and (being listed in {@code security.administrators}) the single user has admin access.
 */
public class TestSingleModeAuthUi extends BaseTest {

    @Test
    @Description("Single-user mode: opening the app auto-authenticates with no login form, and the "
            + "configured single user has admin access (Administration menu visible).")
    @AppContainerConfig(startParams = AppContainerStartParameters.STUDIO_SINGLE_PARAMS)
    public void testSingleModeAutoAuthentication() {
        LocalDriverPool.getPage().navigate(LocalDriverPool.getAppUrl());
        LocalDriverPool.getPage().waitForLoadState();

        assertThat(new LoginPage().isLoginFormDisplayed(5000))
                .as("single mode auto-authenticates — no login form is shown")
                .isFalse();

        assertThat(new EditorPage().openUserMenu().isAdministrationMenuItemVisible())
                .as("the single user has admin access (Administration menu visible)")
                .isTrue();
    }
}
