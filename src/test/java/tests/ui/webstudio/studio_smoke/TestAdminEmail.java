package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.EmailPageComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.LoginPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.WaitUtil;
import org.apache.http.impl.conn.LoggingSessionInputBuffer;
import org.testng.Assert;
import org.testng.annotations.Test;
import tests.BaseTest;

public class TestAdminEmail extends BaseTest {

    @Test
    @TestCaseId("IPBQA-32798")
    @Description("Admin UI 'Email' page - Email verification configuration and persistence test.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAdminEmail() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        // Step 1-2: Navigate to Administration → Email page
        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        EmailPageComponent emailPageComponent = adminPage.navigateToEmailPage();

        // Step 3: Verify "Email" tab contains inactive checkbox "Enable email address verification"
        Assert.assertFalse(emailPageComponent.isEmailVerificationEnabled(), "Email verification checkbox should be initially disabled");

        // Step 3: Load email credentials from test data properties file and enable email verification
        String emailUrl = "smtps://smtp.gmail.com";
        String emailUsername = "webstudiotest21@gmail.com";
        String emailPassword = "pcukwundeupvipfo";

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
        Assert.assertTrue(emailPageComponent.isEmailVerificationEnabled(), "Email verification should remain enabled after logout/login");
        Assert.assertEquals(emailPageComponent.getEmailUrl(), emailUrl, "Email URL should persist after logout/login");
        Assert.assertEquals(emailPageComponent.getEmailUsername(), emailUsername, "Email username should persist after logout/login");

        // Verify password field is empty
        String savedPassword = emailPageComponent.getEmailPassword();
        Assert.assertTrue(savedPassword.isEmpty(), "Saved password field should be empty after relogin");

        // Step 5: Test password visibility toggle (JIRA Step 5: "Press eye on password field for email")
        // According to JIRA requirements: "Email password is not shown"
        // Verify password is initially hidden (type="password")
        emailPageComponent.setEmailPassword("qwerty");
        Assert.assertEquals(emailPageComponent.getPasswordFieldType(), "password", "Password field should have type='password' initially (hidden)");
        Assert.assertFalse(emailPageComponent.isPasswordVisible(), "Password should be hidden initially");

        // Step 5: Click the eye icon to toggle password visibility
        emailPageComponent.togglePasswordVisibility();
        Assert.assertEquals(emailPageComponent.getPasswordFieldType(), "text", "Password field should be type='text' after clicking eye icon");
        Assert.assertTrue(emailPageComponent.isPasswordVisible(), "Password should not be hidden after clicking eye icon");

    }
}