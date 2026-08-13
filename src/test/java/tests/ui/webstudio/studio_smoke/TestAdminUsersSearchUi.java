package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import domain.api.UsersMethod;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.AdminNavigationComponent;
import domain.ui.webstudio.components.admincomponents.AdminTableSearchComponent;
import domain.ui.webstudio.components.admincomponents.UsersPageComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestAdminUsersSearchUi extends BaseTest {

    private static final String SECOND_USER = "searchable_15806";

    @Test
    @TestCaseId("EPBDS-16214")
    @Description("EPBDS-15806 ordered by EPBDS-16233: the admin Users table must filter by the search input - a "
            + "matching fragment keeps only the matching user, a garbage query hides every user, and clearing "
            + "the search restores the full list.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testUsersTableFiltersBySearch() {
        EditorPage editorPage = new LoginService(DriverPool.getPage()).login(UserService.getUser(User.ADMIN));
        assertThat(new UsersMethod().createUser(SECOND_USER, SECOND_USER).getStatusCode())
                .as("Fixture: a second user must exist so the search has something to filter out")
                .isEqualTo(204);

        editorPage.openUserMenu().navigateToAdministration();
        new AdminNavigationComponent().clickNavigationItem(AdminNavigationComponent.NavigationItem.USERS);
        UsersPageComponent usersPage = new UsersPageComponent();
        assertThat(usersPage.isUserInList(SECOND_USER))
                .as("Precondition: the second user must be listed before searching")
                .isTrue();

        AdminTableSearchComponent usersSearch = new AdminTableSearchComponent("users-search-input");
        usersSearch.search("searchable");
        assertThat(usersPage.isUserInList(SECOND_USER))
                .as("A matching fragment must keep the matching user visible")
                .isTrue();
        assertThat(usersPage.isUserInList("admin"))
                .as("A non-matching user must be filtered out of the table")
                .isFalse();

        usersSearch.search("no-such-user-xyz");
        assertThat(usersPage.isUserInList("admin"))
                .as("A garbage query must hide every user from the table")
                .isFalse();
        assertThat(usersPage.isUserInList(SECOND_USER))
                .as("A garbage query must hide the second user as well")
                .isFalse();

        usersSearch.clear();
        assertThat(usersPage.isUserInList("admin"))
                .as("Clearing the search must restore the full users list")
                .isTrue();
        assertThat(usersPage.isUserInList(SECOND_USER))
                .as("Clearing the search must restore the second user as well")
                .isTrue();
    }
}
