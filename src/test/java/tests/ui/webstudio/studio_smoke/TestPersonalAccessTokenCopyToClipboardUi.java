package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import configuration.driver.ExecutionMode;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.PersonalAccessTokenPageComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestPersonalAccessTokenCopyToClipboardUi extends BaseTest {

    private static final String TOKEN_NAME = "aqa-pat-clipboard";

    @Test
    @TestCaseId("EPBDS-16264")
    @Description("EPBDS-16210 verification ordered by EPBDS-16264: in multi-user mode the copy icon of a freshly "
            + "created personal access token copies it without the 'Failed to copy to clipboard' error. "
            + "The defect only exists on a plain HTTP origin that is not localhost, where the browser exposes no "
            + "Clipboard API, so this test requires PLAYWRIGHT_DOCKER where the browser reaches Studio by container name.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testGeneratedTokenIsCopiedToClipboardInMultiUserMode() {
        requireInsecureOriginMode();

        EditorPage editorPage = new LoginService(DriverPool.getPage()).login(UserService.getUser(User.ADMIN));
        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        PersonalAccessTokenPageComponent tokensPage = adminPage.navigateToPersonalAccessTokensPage();

        String token = tokensPage.createTokenKeepingResultOpen(TOKEN_NAME, "No expiration");

        tokensPage.closeAllMessages();
        tokensPage.copyGeneratedToken();

        boolean copyConfirmed = tokensPage.isCopyConfirmationDisplayed();
        boolean copyFailed = tokensPage.isCopyFailureDisplayed();

        assertThat(copyFailed)
                .as("Copying the token must not report 'Failed to copy to clipboard' (EPBDS-16210)")
                .isFalse();
        assertThat(copyConfirmed)
                .as("Copying the token must confirm with 'Token copied to clipboard'")
                .isTrue();

        tokensPage.confirmGeneratedToken();
        assertThat(tokensPage.pasteIntoNewTokenName())
                .as("The clipboard must hold the generated token after the copy icon is used")
                .isEqualTo(token);
    }

    private void requireInsecureOriginMode() {
        ExecutionMode mode = DriverPool.getCurrentExecutionMode();
        if (mode != ExecutionMode.PLAYWRIGHT_DOCKER) {
            throw new IllegalStateException("This test reproduces a defect that only exists on a plain HTTP origin "
                    + "that is not localhost. " + mode + " serves Studio over localhost, which browsers treat as a "
                    + "secure context, so the Clipboard API is present and the defect cannot be reproduced. "
                    + "Run with -Dexecution.mode=PLAYWRIGHT_DOCKER.");
        }
    }
}
