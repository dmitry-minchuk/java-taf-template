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

public class TestMigrateNotOfferedForFormattingOnlyUi extends BaseTest {

    private static final String PROJECT = "MigrateModernProject";
    private static final String FIXTURE_ZIP = "MigrateModernProject.zip";

    @Test
    @TestCaseId("EPBDS-16408")
    @Description("EPBDS-16408: a modern project whose rules.xml differs from the canonical form only by "
            + "formatting must get Edit, not a Migrate offer - migration is about declarations, not bytes.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testFormattingOnlyDescriptorGetsEditNotMigrate() {
        EditorPage editorPage = new LoginService(DriverPool.getPage()).login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, PROJECT, FIXTURE_ZIP);

        ProjectDetailPage detail = repositoryPage.openProjectsList().openProjectDetail(PROJECT);
        assertThat(detail.isOverviewEditOffered())
                .as("A formatting-only descriptor difference must leave Edit available")
                .isTrue();
        assertThat(detail.isOverviewMigrateOffered())
                .as("Migrate must not be offered when nothing it declares would change (EPBDS-16408)")
                .isFalse();
    }
}
