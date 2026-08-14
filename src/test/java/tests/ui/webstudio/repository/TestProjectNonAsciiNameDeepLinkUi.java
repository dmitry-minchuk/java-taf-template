package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.ProjectDetailPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestProjectNonAsciiNameDeepLinkUi extends BaseTest {

    private static final String PROJECT = "Ставки 🚀 Проверка";

    @Test
    @TestCaseId("IPBQA-33035")
    @Description("EPBDS-16402: a project named with Cyrillic and an emoji must render correctly on the list, "
            + "the detail header and the Overview panel, and its deep link must survive a fresh login session.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testNonAsciiNameRendersAndDeepLinkSurvivesRelogin() {
        EditorPage editorPage = new LoginService(DriverPool.getPage()).login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, PROJECT, "Sample Project");

        repositoryPage.openProjectsList();
        assertThat(repositoryPage.isProjectPresent(PROJECT))
                .as("The projects list must show the non-ASCII project name exactly as entered")
                .isTrue();

        repositoryPage.filterByName("Ставки");
        assertThat(repositoryPage.isProjectPresent(PROJECT))
                .as("The list search must find the project by a Cyrillic name fragment")
                .isTrue();
        repositoryPage.filterByName("НетТакогоПроекта");
        assertThat(repositoryPage.isProjectPresent(PROJECT))
                .as("A non-matching Cyrillic query must hide the project from the list")
                .isFalse();
        repositoryPage.clearNameFilter();

        ProjectDetailPage detail = repositoryPage.openProjectDetail(PROJECT);
        String deepLink = DriverPool.getPage().url();
        assertThat(detail.getOverviewPath())
                .as("The Overview path must carry the non-ASCII project name without mangling")
                .isEqualTo(PROJECT);

        detail.openHistoryTab();
        assertThat(detail.getRevisionsCount())
                .as("The Revisions tab must load for the non-ASCII project")
                .isGreaterThanOrEqualTo(1);
        assertThat(detail.getRevisionDescriptions().toString())
                .as("The creation revision comment must render the non-ASCII name unmangled")
                .contains(PROJECT);

        detail.openUserMenu().signOut();
        new LoginService(DriverPool.getPage()).login(UserService.getUser(User.ADMIN));
        DriverPool.getPage().navigate(deepLink);
        detail = new ProjectDetailPage();
        assertThat(detail.getStatus())
                .as("The deep link with the non-ASCII project's id must open in a fresh session, not HTTP 400")
                .isNotBlank();
        assertThat(detail.getOverviewPath())
                .as("The reopened project must still show the exact non-ASCII name")
                .isEqualTo(PROJECT);
    }
}
