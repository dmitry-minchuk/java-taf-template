package tests.ui.webstudio.ad;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.api.PersonalAccessTokenApiMethod;
import domain.ui.webstudio.components.admincomponents.PersonalAccessTokenPageComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.SambaAdInfrastructureService;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestPersonalAccessTokenAdUi extends AbstractAdUiTest {

    private static final String TOKEN_NAME = "aqa-pat-ad";

    @Test
    @TestCaseId("EPBDS-16168")
    @Description("AD mode: a personal access token created in the UI authenticates a REST call, and revoking it removes access.")
    @AppContainerConfig(startParams = AppContainerStartParameters.STUDIO_AD_PARAMS)
    public void testPersonalAccessTokenAuthenticatesRestInAdMode() {
        EditorPage editorPage = adLogin(SambaAdInfrastructureService.ADMIN_USER, SambaAdInfrastructureService.USER_PASSWORD);
        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        PersonalAccessTokenPageComponent tokensPage = adminPage.navigateToPersonalAccessTokensPage();

        String token = tokensPage.createToken(TOKEN_NAME, "No expiration");
        assertThat(token)
                .as("Generated token should carry the OpenL PAT prefix")
                .startsWith("openl_pat_");
        assertThat(tokensPage.isTokenListed(TOKEN_NAME))
                .as("Created token should appear in the tokens list")
                .isTrue();

        PersonalAccessTokenApiMethod restApi = new PersonalAccessTokenApiMethod();
        assertThat(restApi.getProfileStatusWithToken(token))
                .as("PAT must authenticate a REST call in AD mode")
                .isEqualTo(200);
        assertThat(restApi.getProfileStatusWithoutAuthorization())
                .as("REST call without a token must be unauthorized")
                .isEqualTo(401);

        tokensPage.revokeToken(TOKEN_NAME);
        assertThat(tokensPage.isTokenListed(TOKEN_NAME))
                .as("Revoked token should be removed from the list")
                .isFalse();
        assertThat(restApi.getProfileStatusWithToken(token))
                .as("Revoked token must no longer authenticate a REST call")
                .isEqualTo(401);
    }
}
