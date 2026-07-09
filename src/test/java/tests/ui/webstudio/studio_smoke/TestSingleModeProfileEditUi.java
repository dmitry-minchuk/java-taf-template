package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.ui.webstudio.components.admincomponents.MyProfilePageComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Single-user mode ({@code user.mode=single}). EPBDS-16213 made the profile fields editable in this
 * mode (they used to be disabled and silently reset on every startup). This verifies an edit to the
 * name and email survives a full page reload.
 */
public class TestSingleModeProfileEditUi extends BaseTest {

    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Single";
    private static final String EMAIL = "john.single@example.com";

    @Test
    @TestCaseId("EPBDS-16220")
    @Description("Single-user mode: the user profile (first/last name, email) is editable through the "
            + "Studio UI and the changes persist after a full page reload (EPBDS-16213).")
    @AppContainerConfig(startParams = AppContainerStartParameters.STUDIO_SINGLE_PARAMS)
    public void testSingleModeProfileEditPersists() {
        LocalDriverPool.getPage().navigate(LocalDriverPool.getAppUrl());
        LocalDriverPool.getPage().waitForLoadState();

        new EditorPage().openUserMenu()
                .navigateToAdministration()
                .navigateToMyProfilePage()
                .setFirstName(FIRST_NAME)
                .setLastName(LAST_NAME)
                .setEmail(EMAIL)
                .saveProfile();

        LocalDriverPool.getPage().navigate(LocalDriverPool.getAppUrl());
        LocalDriverPool.getPage().waitForLoadState();

        MyProfilePageComponent profile = new EditorPage().openUserMenu()
                .navigateToAdministration()
                .navigateToMyProfilePage();

        assertThat(profile.getFirstName()).as("first name persists in single-user mode").isEqualTo(FIRST_NAME);
        assertThat(profile.getLastName()).as("last name persists in single-user mode").isEqualTo(LAST_NAME);
        assertThat(profile.getEmail()).as("email persists in single-user mode").isEqualTo(EMAIL);
    }
}
