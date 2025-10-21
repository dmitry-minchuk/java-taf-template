package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerPool;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.serviceclasses.models.UserData;
import domain.ui.webstudio.components.admincomponents.SystemSettingsPageComponent;
import domain.ui.webstudio.components.admincomponents.UsersPageComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.LoginPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testcontainers.containers.Container.ExecResult;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSystemSettingsDBValidation extends BaseTest {

    private static final String TEST_USER = "testdbuser";
    private static final String TEST_PASSWORD = "Test123!";
    private static final String TEST_EMAIL = "testdbuser@example.com";
    private static final String TEST_FIRSTNAME = "TestDB";
    private static final String TEST_LASTNAME = "User";

    @Test
    @TestCaseId("IPBQA-TBD")
    @Description("System Settings - Database Configuration validation with real DB interaction")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testSystemSettingsDBValidation() throws Exception {
        // Step 1: Login as admin and verify default DB settings
        EditorPage editorPage = new LoginService(LocalDriverPool.getPage()).login(UserService.getUser(User.ADMIN));
        SystemSettingsPageComponent systemSettings = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToSystemSettingsPage();

        String originalDbUrl = systemSettings.getDatabaseUrl();
        String originalDbUser = systemSettings.getDatabaseUser();
        int originalMaxPoolSize = systemSettings.getDatabaseMaxPoolSize();

        LOGGER.info("Original DB URL: {}", originalDbUrl);
        LOGGER.info("Original DB User: {}", originalDbUser);
        LOGGER.info("Original Max Pool Size: {}", originalMaxPoolSize);

        assertThat(originalDbUrl).contains("jdbc:h2:");
        assertThat(originalDbUrl).contains("/opt/openl/local/users-db/db");

        // Step 2: Verify we can query the database (check admin user exists)
        String adminCheckQuery = String.format("SELECT COUNT(*) FROM OPENL_USERS WHERE USERNAME = 'admin'");
        String adminCount = executeH2Query(adminCheckQuery);
        assertThat(adminCount).as("Admin user should exist in database").isEqualTo("1");
        LOGGER.info("Verified admin user exists in database");

        // Step 3: Change MaxPoolSize setting (just to verify we can modify settings)
        int newMaxPoolSize = originalMaxPoolSize + 5;
        systemSettings.setDatabaseMaxPoolSize(newMaxPoolSize);
        systemSettings.clickApplyButton();

        // Verify the change was applied (refresh page and check)
        systemSettings = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToSystemSettingsPage();
        assertThat(systemSettings.getDatabaseMaxPoolSize()).isEqualTo(newMaxPoolSize);
        LOGGER.info("Successfully changed MaxPoolSize from {} to {}", originalMaxPoolSize, newMaxPoolSize);

        // Restore original MaxPoolSize
        systemSettings.setDatabaseMaxPoolSize(originalMaxPoolSize);
        systemSettings.applySettingsAndRelogin(User.ADMIN);

        // Step 4: Create new user via Admin UI
        UsersPageComponent usersPage = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();

        usersPage.clickAddUser()
                .setUsername(TEST_USER)
                .setPassword(TEST_PASSWORD)
                .setEmail(TEST_EMAIL)
                .setFirstName(TEST_FIRSTNAME)
                .setLastName(TEST_LASTNAME)
                .saveUser();

        LOGGER.info("Created user via UI: {}", TEST_USER);

        // Verify user appears in UI table
        assertThat(usersPage.isUserInList(TEST_USER)).isTrue();
        assertThat(usersPage.getEmailFromRow(usersPage.getUserRow(TEST_USER))).isEqualTo(TEST_EMAIL);

        // Step 5: Verify user exists in database via SQL query
        String userCheckQuery = String.format("SELECT COUNT(*) FROM OPENL_USERS WHERE USERNAME = '%s'", TEST_USER);
        String userCount = executeH2Query(userCheckQuery);
        assertThat(userCount).as("User should exist in database after creation").isEqualTo("1");
        LOGGER.info("Verified user '{}' exists in database via SQL query", TEST_USER);

        // Verify user details in database
        String userDetailsQuery = String.format(
                "SELECT USERNAME, FIRSTNAME, LASTNAME, EMAIL FROM OPENL_USERS WHERE USERNAME = '%s'",
                TEST_USER
        );
        String userDetails = executeH2Query(userDetailsQuery);
        assertThat(userDetails).contains(TEST_USER);
        assertThat(userDetails).contains(TEST_FIRSTNAME);
        assertThat(userDetails).contains(TEST_LASTNAME);
        assertThat(userDetails).contains(TEST_EMAIL);
        LOGGER.info("Verified user details in database: {}", userDetails);

        // Step 6: Test login as new user
        editorPage.openUserMenu().signOut();
        UserData user = new UserData(TEST_USER, TEST_PASSWORD);
        new LoginPage().login(user);
        assertThat(editorPage.openUserMenu().navigateToAdministration().navigateToSystemSettingsPage().getDatabaseUser()).isEqualTo(TEST_USER);
        LOGGER.info("Successfully logged in as user '{}'", TEST_USER);

        // Logout and login back as admin
        editorPage.openUserMenu().signOut();
        new LoginPage().login(UserService.getUser(User.ADMIN));

        // Step 7: Delete user
        usersPage = editorPage.openUserMenu()
                .navigateToAdministration()
                .navigateToUsersPage();

        usersPage.clickDeleteUser(TEST_USER);
        LOGGER.info("Deleted user via UI: {}", TEST_USER);

        // Verify user is removed from UI table
        assertThat(usersPage.isUserInList(TEST_USER)).isFalse();

        // Step 8: Verify user is deleted from database via SQL query
        String deletedUserCheckQuery = String.format("SELECT COUNT(*) FROM OPENL_USERS WHERE USERNAME = '%s'", TEST_USER);
        String deletedUserCount = executeH2Query(deletedUserCheckQuery);
        assertThat(deletedUserCount).as("User should be deleted from database").isEqualTo("0");
        LOGGER.info("Verified user '{}' is deleted from database via SQL query", TEST_USER);
    }

    private String executeH2Query(String query) throws Exception {
        // H2 database file path in container
        String dbPath = "/opt/openl/users-db/db";

        // Use H2's shell tool to execute query
        // -url: database URL
        // -sql: SQL query to execute
        String h2Command = String.format(
                "java -cp /opt/openl/tomcat/webapps/webstudio/WEB-INF/lib/h2-*.jar org.h2.tools.Shell " +
                "-url 'jdbc:h2:%s' -sql \"%s\"",
                dbPath, query
        );

        ExecResult execResult = AppContainerPool.get()
                .getAppContainer()
                .execInContainer("sh", "-c", h2Command);

        if (execResult.getExitCode() != 0) {
            LOGGER.error("H2 query execution failed. Exit code: {}", execResult.getExitCode());
            LOGGER.error("STDOUT: {}", execResult.getStdout());
            LOGGER.error("STDERR: {}", execResult.getStderr());
            throw new RuntimeException("H2 query execution failed: " + execResult.getStderr());
        }

        String result = execResult.getStdout().trim();
        LOGGER.debug("H2 query result: {}", result);

        // Parse the result - H2 Shell outputs table format, we need to extract the actual value
        // For COUNT queries, the result looks like:
        // COUNT(*)
        // 1
        String[] lines = result.split("\n");
        if (lines.length >= 2) {
            // Return the last non-empty line which contains the actual value
            for (int i = lines.length - 1; i >= 0; i--) {
                String line = lines[i].trim();
                if (!line.isEmpty() && !line.startsWith("-") && !line.equals("COUNT(*)")
                    && !line.startsWith("USERNAME") && !line.startsWith("(")) {
                    return line;
                }
            }
        }

        return result;
    }
}
