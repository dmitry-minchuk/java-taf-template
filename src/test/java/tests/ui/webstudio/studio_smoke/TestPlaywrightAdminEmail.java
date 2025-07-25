package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import com.microsoft.playwright.Page;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.PlaywrightDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.PlaywrightEmailPageComponent;
import domain.ui.webstudio.pages.mainpages.PlaywrightAdminPage;
import helpers.service.PlaywrightLoginService;
import helpers.service.UserService;
import helpers.utils.TestDataUtil;
import org.testng.Assert;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.Properties;

/**
 * Playwright version of TestAdminEmail - Admin UI Email verification configuration test
 * Migrated from Selenium to use native Playwright wait strategies and element interactions
 */
public class TestPlaywrightAdminEmail extends BaseTest {

    @Test
    @TestCaseId("IPBQA-32798-PW")
    @Description("Playwright - Admin UI 'Email' page - Email verification configuration test")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testPlaywrightAdminEmail() {
        
        // Initialize Playwright page for test execution
        Page page = PlaywrightDriverPool.getPage();
        
        // Navigate to local application (for LOCAL mode)
        PlaywrightDriverPool.navigateTo("http://localhost:8090");
        
        // Step 1: Login with admin user using Playwright login service
        PlaywrightLoginService loginService = new PlaywrightLoginService(page);
        // TODO: loginService.login() must lead us to EditorPage
        loginService.login(UserService.getUser(User.ADMIN));

        // TODO: Getting to AdminPage must be done the same way as it is done in Selenium version of the test: AdminPage adminPage = editorPage.getCurrentUserComponent().navigateToAdministration();
        PlaywrightAdminPage adminPage = loginService.navigateToAdministration();
        PlaywrightEmailPageComponent emailPageComponent = adminPage.navigateToEmailPage();

        // Step 3: Verify "Email" tab contains inactive checkbox "Enable email address verification"
        Assert.assertFalse(emailPageComponent.isEmailVerificationEnabled(),
            "Email verification checkbox should be initially disabled");

        // Step 4: Load email credentials from test data properties file
        Properties emailProperties = TestDataUtil.makePropertiesFromFile("TestAdminEmailWebStudio.properties");
        String emailUrl = emailProperties.getProperty("mail.url");
        String emailUsername = emailProperties.getProperty("mail.username");
        String emailPassword = emailProperties.getProperty("mail.password");
        
        // Enable email verification and set credentials
        emailPageComponent.enableEmailVerificationWithCredentials(emailUrl, emailUsername, emailPassword);

        // Step 5: Login again (user gets logged out after applying settings)
        loginService = new PlaywrightLoginService(page);
        adminPage = loginService.login(UserService.getUser(User.ADMIN));
        adminPage = loginService.navigateToAdministration();
        emailPageComponent = adminPage.navigateToEmailPage();
        
        // Step 6: Verify settings persistence after restart
        Assert.assertTrue(emailPageComponent.isEmailVerificationEnabled(),
            "Email verification should remain enabled after restart");
        Assert.assertEquals(emailPageComponent.getEmailUrl(), emailUrl,
            "Email URL should be persisted after restart");
        Assert.assertEquals(emailPageComponent.getEmailUsername(), emailUsername,
            "Email username should be persisted after restart");
        Assert.assertNotEquals(emailPageComponent.getEmailPassword(), emailPassword,
                "Password should not be displayed in plain text");

        // Step 7: Test password visibility toggle - password should remain hidden
        emailPageComponent.togglePasswordVisibility();
        Assert.assertNotEquals(emailPageComponent.getEmailPassword(), emailPassword,
            "Password should not be displayed in plain text even after toggle");
            
        System.out.println("✅ Playwright Admin Email test completed successfully!");
        System.out.println("   - Email verification configuration: PASSED");
        System.out.println("   - Settings persistence validation: PASSED");
        System.out.println("   - Password security verification: PASSED");
        System.out.println("   - Using native Playwright wait strategies throughout");
    }
}