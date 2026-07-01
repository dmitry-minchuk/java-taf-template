package tests.ui.webstudio.sso;

import com.epam.reportportal.annotations.Description;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PLAYWRIGHT_DOCKER test — OIDC (OAuth2) RP-initiated logout (EPBDS-16182). Studio runs in oauth2
 * mode against the ephemeral Keycloak OIDC IdP. Signing out must redirect to the IdP's end-session
 * endpoint (with the {@code id_token_hint}) so the IdP ends its session — before the fix the
 * {@code /logout} chain used a no-op handler and never contacted the IdP.
 * <p>
 * The check is that Sign Out sends a request to Keycloak's {@code /protocol/openid-connect/logout}
 * endpoint. Mirrors {@link TestSamlLogoutUi}; the full round-trip is covered by the product's own
 * Keycloak integration tests.
 */
public class TestOidcLogoutUi extends AbstractSsoUiTest {

    @Test
    @Description("OIDC logout (EPBDS-16182): Sign Out issues an RP-initiated logout to the IdP "
            + "end-session endpoint (a request to Keycloak's /protocol/openid-connect/logout) instead "
            + "of the pre-fix no-op that left the IdP session alive.")
    @AppContainerConfig(startParams = AppContainerStartParameters.STUDIO_OIDC_PARAMS)
    public void testOidcRpInitiatedLogout() {
        ssoLogin("admin", "admin");

        // Fully settle the authenticated OIDC session before logging out (avoids a login->logout race).
        Page page = LocalDriverPool.getPage();
        page.navigate(LocalDriverPool.getAppUrl());
        page.waitForLoadState();

        // waitForRequest arms the listener BEFORE running Sign Out, so the logout redirect cannot be
        // missed. After login the only reason to hit the IdP end-session endpoint is an RP-initiated
        // logout, so capturing it proves the /logout chain contacts the IdP (EPBDS-16182).
        Request logoutRequest = page.waitForRequest(
                request -> request.url().contains("/protocol/openid-connect/logout"),
                new Page.WaitForRequestOptions().setTimeout(20000),
                () -> new EditorPage().openUserMenu().signOut());

        assertThat(logoutRequest)
                .as("Sign Out issues an RP-initiated OIDC logout to the IdP (EPBDS-16182); "
                        + "a request must reach Keycloak's /protocol/openid-connect/logout endpoint after Sign Out")
                .isNotNull();
    }
}
