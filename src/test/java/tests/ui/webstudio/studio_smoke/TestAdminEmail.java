package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.EmailPageComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.LoginPage;
import helpers.service.LoginService;
import helpers.service.MailMockService;
import helpers.service.UserService;
import helpers.utils.WaitUtil;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestAdminEmail extends BaseTest {

    private final MailMockService mailMock = new MailMockService();

    @BeforeMethod
    public void startMailMock() {
        mailMock.start();
    }

    @AfterMethod(alwaysRun = true)
    public void stopMailMock() {
        mailMock.stop();
    }

    @Test
    @TestCaseId("IPBQA-32798")
    @Description("Admin UI 'Email' page - email verification configuration and persistence, "
            + "backed by an ephemeral in-network SMTP mock (Mailpit) instead of an external mail provider.")
    @AppContainerConfig(startParams = AppContainerStartParameters.STUDIO_MAIL_MOCK_PARAMS)
    public void testAdminEmail() {
        LoginService loginService = new LoginService(DriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        EmailPageComponent emailPageComponent = adminPage.navigateToEmailPage();

        assertThat(emailPageComponent.isEmailVerificationEnabled())
                .as("Email verification checkbox should be initially disabled")
                .isFalse();

        emailPageComponent.enableEmailVerificationWithCredentials(
                MailMockService.SMTP_URL, MailMockService.USERNAME, MailMockService.PASSWORD);

        DriverPool.getPage().navigate(DriverPool.getAppUrl());
        editorPage = new LoginPage().login(UserService.getUser(User.ADMIN));

        adminPage = editorPage.openUserMenu().navigateToAdministration();
        emailPageComponent = adminPage.navigateToEmailPage();

        WaitUtil.waitForCondition(emailPageComponent::isEmailVerificationEnabled, 2000, 100, "Waiting for Email verification checkbox to load its state");
        assertThat(emailPageComponent.isEmailVerificationEnabled())
                .as("Email verification should remain enabled after logout/login")
                .isTrue();
        assertThat(emailPageComponent.getEmailUrl())
                .as("Email URL should persist after logout/login")
                .isEqualTo(MailMockService.SMTP_URL);
        assertThat(emailPageComponent.getEmailUsername())
                .as("Email username should persist after logout/login")
                .isEqualTo(MailMockService.USERNAME);

        assertThat(emailPageComponent.getEmailPassword())
                .as("Saved password field should be empty after relogin")
                .isEmpty();

        emailPageComponent.setEmailPassword("qwerty");
        assertThat(emailPageComponent.getPasswordFieldType())
                .as("Password field should have type='password' initially (hidden)")
                .isEqualTo("password");
        assertThat(emailPageComponent.isPasswordVisible())
                .as("Password should be hidden initially")
                .isFalse();

        emailPageComponent.togglePasswordVisibility();
        assertThat(emailPageComponent.getPasswordFieldType())
                .as("Password field should be type='text' after clicking eye icon")
                .isEqualTo("text");
        assertThat(emailPageComponent.isPasswordVisible())
                .as("Password should not be hidden after clicking eye icon")
                .isTrue();
    }
}
