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
import domain.ui.webstudio.pages.mainpages.PlaywrightEditorPage;
import helpers.service.PlaywrightLoginService;
import helpers.service.UserService;
import helpers.utils.TestDataUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.Properties;

/**
 * Playwright version of TestAdminEmail - Admin UI Email verification configuration test
 * Migrated from Selenium to use native Playwright wait strategies and element interactions
 */
public class TestPlaywrightAdminEmail extends BaseTest {

    private static final Logger LOGGER = LogManager.getLogger(TestPlaywrightAdminEmail.class);

    @Test
    @TestCaseId("IPBQA-32798-PW")
    @Description("Playwright - Admin UI 'Email' page - Email verification configuration test")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testPlaywrightAdminEmail() {
        
        // Initialize Playwright page for test execution
        Page page = PlaywrightDriverPool.getPage();
        
        // Step 1: Login with admin user using Playwright login service (same as Selenium)
        // The BaseTest container setup automatically provides the correct URL
        PlaywrightLoginService loginService = new PlaywrightLoginService(page);
        PlaywrightEditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        // Step 2: Navigate to Administration (exact same as Selenium: editorPage.getCurrentUserComponent().navigateToAdministration())
        PlaywrightAdminPage adminPage = editorPage.getCurrentUserComponent().navigateToAdministration();
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

        // Step 5: Login again after applying settings (following exact Selenium TestAdminEmail logic)
        // User should be logged out after applying settings, so login again
        loginService = new PlaywrightLoginService(page);
        editorPage = loginService.login(UserService.getUser(User.ADMIN));
        adminPage = editorPage.getCurrentUserComponent().navigateToAdministration();
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
    }
}