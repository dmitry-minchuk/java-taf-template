package tests.ui.webstudio.sso;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import org.testng.annotations.Test;

/**
 * PLAYWRIGHT_DOCKER test — run with {@code -Dexecution.mode=PLAYWRIGHT_DOCKER}.
 * <p>
 * IPBQA-32788 — Admin 'Users' view with a SAML external user management system. Studio runs in
 * saml mode against the ephemeral Keycloak SAML IdP; the admin assigns Manager then Viewer to
 * the external user via the Admin Users view, and the user's repository access matches after
 * SAML SSO re-login.
 */
public class TestUsersViewRolesSamlUi extends AbstractUsersViewRolesSsoTest {

    @Test
    @TestCaseId("IPBQA-32788")
    @Description("SAML external auth: admin assigns Manager then Viewer to an SSO user via the "
            + "Admin 'Users' view; repository access matches the assigned role after re-login.")
    @AppContainerConfig(startParams = AppContainerStartParameters.STUDIO_SAML_PARAMS)
    public void testUsersViewRolesUnderSaml() {
        runUsersViewRoleFlow();
    }
}
