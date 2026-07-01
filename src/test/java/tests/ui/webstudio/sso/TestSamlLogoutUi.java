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
 * PLAYWRIGHT_DOCKER test — SAML Single Logout (EPBDS-16182). Studio runs in saml mode against the
 * ephemeral Keycloak SAML IdP. Signing out must trigger an <em>SP-initiated</em> SAML LogoutRequest
 * to the IdP's Single Logout Service — before the fix the {@code /logout} chain used a no-op handler
 * and never contacted the IdP, so the still-live IdP session silently re-authenticated the user.
 * <p>
 * The check is that Sign Out sends a request to the IdP SAML endpoint (the LogoutRequest). The full
 * round-trip (IdP ends the session → SP shows the login form again) is NOT asserted here: Keycloak
 * cannot post the LogoutResponse back because the ephemeral Studio container has a random hostname
 * ({@code appcontainer-<id>}) and the SAML client has no stable SingleLogoutService URL, so the
 * round-trip 500s in this harness. Issuing the LogoutRequest is the product behaviour under test;
 * the full round-trip is covered by the product's own Keycloak integration tests.
 */
public class TestSamlLogoutUi extends AbstractSsoUiTest {

    @Test
    @Description("SAML logout (EPBDS-16182): Sign Out issues an SP-initiated SAML LogoutRequest to the "
            + "IdP Single Logout Service (a request to Keycloak's /protocol/saml endpoint) instead of "
            + "the pre-fix no-op that left the IdP session alive.")
    @AppContainerConfig(startParams = AppContainerStartParameters.STUDIO_SAML_PARAMS)
    public void testSamlSingleLogout() {
        ssoLogin("studiouser", "studiouser");

        // Fully settle the authenticated SAML session before logging out (avoids a login->logout race
        // where Sign Out happens before the SP has persisted the SAML authentication).
        Page page = LocalDriverPool.getPage();
        page.navigate(LocalDriverPool.getAppUrl());
        page.waitForLoadState();

        // waitForRequest arms the listener BEFORE running Sign Out, so the LogoutRequest cannot be
        // missed. After login the only reason to hit the IdP SAML endpoint again is an SP-initiated
        // LogoutRequest, so capturing it proves the /logout chain contacts the IdP (EPBDS-16182).
        Request logoutRequest = page.waitForRequest(
                request -> request.url().contains("/realms/openlstudio/protocol/saml"),
                new Page.WaitForRequestOptions().setTimeout(20000),
                () -> new EditorPage().openUserMenu().signOut());

        assertThat(logoutRequest)
                .as("Sign Out issues an SP-initiated SAML LogoutRequest to the IdP (EPBDS-16182); "
                        + "a request must reach Keycloak's /protocol/saml endpoint after Sign Out")
                .isNotNull();
    }
}
