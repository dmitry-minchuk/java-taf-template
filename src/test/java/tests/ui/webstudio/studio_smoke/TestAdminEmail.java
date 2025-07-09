package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincpmponents.EmailPageComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.TestDataUtil;
import org.testng.Assert;
import org.testng.annotations.Test;
import tests.BaseTest;
import java.util.Properties;

public class TestAdminEmail extends BaseTest {

    @Test
    @TestCaseId("IPBQA-32798")
    @Description("New Admin UI 'Email' page - Email verification configuration test")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAdminEmail() {
        LoginService loginService = new LoginService();
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        // Step 2: From admin user click on user menu and select "Administration"
        AdminPage adminPage = editorPage.getCurrentUserComponent().navigateToAdministration();
        EmailPageComponent emailPageComponent = adminPage.navigateToEmailPage();

        // Verify "Email" tab contains non active checkbox "Enable email address verification"
        Assert.assertFalse(emailPageComponent.isEmailVerificationEnabled(),
            "Email verification checkbox should be initially disabled");

        Properties emailProperties = TestDataUtil.makePropertiesFromFile("TestAdminEmailWebStudio.properties");
        String emailUrl = emailProperties.getProperty("mail.url");
        String emailUsername = emailProperties.getProperty("mail.username");
        String emailPassword = emailProperties.getProperty("mail.password");
        emailPageComponent.enableEmailVerificationWithCredentials(emailUrl, emailUsername, emailPassword);

        // Step 4: Login one more time with admin user
        // User should be logged out after applying settings, so login again
        editorPage = loginService.login(UserService.getUser(User.ADMIN));
        adminPage = editorPage.getCurrentUserComponent().navigateToAdministration();
        emailPageComponent = adminPage.navigateToEmailPage();
        Assert.assertTrue(emailPageComponent.isEmailVerificationEnabled(),
            "Email verification should remain enabled after restart");
        Assert.assertEquals(emailPageComponent.getEmailUrl(), emailUrl,
            "Email URL should be persisted after restart");
        Assert.assertEquals(emailPageComponent.getEmailUsername(), emailUsername,
            "Email username should be persisted after restart");
        Assert.assertNotEquals(emailPageComponent.getEmailPassword(), emailPassword,
                "Password should not be displayed in plain text");

        // Step 5: Press eye on password field for email - Email password is not shown
        emailPageComponent.togglePasswordVisibility();
        // Note: Password field behavior verification depends on the actual implementation
        // The password should not be visible in plain text even after toggle
        Assert.assertNotEquals(emailPageComponent.getEmailPassword(), emailPassword,
            "Password should not be displayed in plain text");
    }
}
