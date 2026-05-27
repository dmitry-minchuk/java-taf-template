package tests.ui.webstudio.sso;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import org.testng.annotations.Test;

/**
 * PLAYWRIGHT_DOCKER test — run with {@code -Dexecution.mode=PLAYWRIGHT_DOCKER}.
 * <p>
 * IPBQA-32789 — Admin 'Users' view with an OAuth2 external user management system. Studio runs
 * in oauth2 mode against the ephemeral Keycloak; the admin assigns Manager then Viewer to the
 * external user via the Admin Users view, and the user's repository access matches after SSO
 * re-login.
 */
public class TestUsersViewRolesOauth2Ui extends AbstractUsersViewRolesSsoTest {

    @Test
    @TestCaseId("IPBQA-32789")
    @Description("OAuth2 external auth: admin assigns Manager then Viewer to an SSO user via the "
            + "Admin 'Users' view; repository access matches the assigned role after re-login.")
    @AppContainerConfig(startParams = AppContainerStartParameters.STUDIO_OIDC_PARAMS)
    public void testUsersViewRolesUnderOauth2() {
        runUsersViewRoleFlow();
    }
}
