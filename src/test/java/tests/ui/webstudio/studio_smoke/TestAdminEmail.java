package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.EmailPageComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.LoginPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.WaitUtil;
import org.testng.SkipException;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestAdminEmail extends BaseTest {

    @Test
    @TestCaseId("IPBQA-32798")
    @Description("Admin UI 'Email' page - Email verification configuration and persistence test.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAdminEmail() {
        // Step 3 prerequisites: SMTP credentials are secrets and come from .env / -D properties, never from VCS
        String emailUrl = ProjectConfiguration.getProperty(PropertyNameSpace.EMAIL_SMTP_URL);
        String emailUsername = ProjectConfiguration.getProperty(PropertyNameSpace.EMAIL_SMTP_USERNAME);
        String emailPassword = ProjectConfiguration.getProperty(PropertyNameSpace.EMAIL_SMTP_PASSWORD);
        if (emailUsername == null || emailUsername.isEmpty() || emailPassword == null || emailPassword.isEmpty()) {
            throw new SkipException("SMTP credentials are not configured: set email.smtp.username / "
                    + "email.smtp.password in .env or as -D system properties");
        }

        LoginService loginService = new LoginService(DriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        // Step 1-2: Navigate to Administration → Email page
        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        EmailPageComponent emailPageComponent = adminPage.navigateToEmailPage();

        // Step 3: Verify "Email" tab contains inactive checkbox "Enable email address verification"
        assertThat(emailPageComponent.isEmailVerificationEnabled())
                .as("Email verification checkbox should be initially disabled")
                .isFalse();

        // Step 3: Enable email verification and set credentials, then click Apply
        // Expected: User should be logged out after applying email settings
        emailPageComponent.enableEmailVerificationWithCredentials(emailUrl, emailUsername, emailPassword);

        // Step 4: Login again with admin user to verify settings persistence
        editorPage = new LoginPage().login(UserService.getUser(User.ADMIN));

        // Step 4: Navigate back to Email page to verify settings were saved
        adminPage = editorPage.openUserMenu().navigateToAdministration();
        emailPageComponent = adminPage.navigateToEmailPage();

        // Step 4: Verify that email verification is still enabled and credentials persisted
        WaitUtil.waitForCondition(emailPageComponent::isEmailVerificationEnabled, 2000, 100, "Waiting for Email verification checkbox to load its state");
        assertThat(emailPageComponent.isEmailVerificationEnabled())
                .as("Email verification should remain enabled after logout/login")
                .isTrue();
        assertThat(emailPageComponent.getEmailUrl())
                .as("Email URL should persist after logout/login")
                .isEqualTo(emailUrl);
        assertThat(emailPageComponent.getEmailUsername())
                .as("Email username should persist after logout/login")
                .isEqualTo(emailUsername);

        // Verify password field is empty
        assertThat(emailPageComponent.getEmailPassword())
                .as("Saved password field should be empty after relogin")
                .isEmpty();

        // Step 5: Test password visibility toggle (JIRA Step 5: "Press eye on password field for email")
        // According to JIRA requirements: "Email password is not shown"
        // Verify password is initially hidden (type="password")
        emailPageComponent.setEmailPassword("qwerty");
        assertThat(emailPageComponent.getPasswordFieldType())
                .as("Password field should have type='password' initially (hidden)")
                .isEqualTo("password");
        assertThat(emailPageComponent.isPasswordVisible())
                .as("Password should be hidden initially")
                .isFalse();

        // Step 5: Click the eye icon to toggle password visibility
        emailPageComponent.togglePasswordVisibility();
        assertThat(emailPageComponent.getPasswordFieldType())
                .as("Password field should be type='text' after clicking eye icon")
                .isEqualTo("text");
        assertThat(emailPageComponent.isPasswordVisible())
                .as("Password should not be hidden after clicking eye icon")
                .isTrue();
    }
}
