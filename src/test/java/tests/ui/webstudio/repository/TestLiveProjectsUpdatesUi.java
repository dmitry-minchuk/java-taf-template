package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import domain.api.RepositoryProjectsMethod;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.WaitUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class TestLiveProjectsUpdatesUi extends BaseTest {

    private static final String LIVE_PROJECT = "LivePushProject";
    private static final String BURST_PREFIX = "LiveBurst";
    private static final String DESIGN_REPO = "design";
    private static final String MASTER = "master";
    private static final String TEMPLATE = "Sample Project";

    @Test
    @TestCaseId("IPBQA-33042")
    @Description("EPBDS-16314: the Projects list must refresh itself when a project is created through another "
            + "origin (REST) - a change notification arrives over the WebSocket and the new row appears without "
            + "any reload action, and a manual reload then shows the very same list.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testProjectsListPicksUpRestCreatedProjectWithoutReload() {
        List<String> socketFrames = captureWebSocketFrames();
        new LoginService(DriverPool.getPage()).login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = new EditorPage().getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        assertThat(repositoryPage.isProjectPresent(LIVE_PROJECT))
                .as("Precondition: the project must not exist before the push")
                .isFalse();

        assertThat(new RepositoryProjectsMethod()
                .createProjectFromTemplate(DESIGN_REPO, LIVE_PROJECT, TEMPLATE, MASTER).getStatusCode())
                .as("Fixture: the REST-origin project creation must succeed")
                .isEqualTo(200);

        boolean appeared = WaitUtil.waitForCondition(() -> repositoryPage.isProjectPresent(LIVE_PROJECT),
                20000, 500, "Waiting for the projects list to pick up the REST-created project without a reload");
        assertThat(appeared)
                .as("The Projects list must show the REST-created project without any reload (EPBDS-16314)")
                .isTrue();
        assertThat(socketFrames)
                .as("A change notification must have arrived over the WebSocket, not through client polling")
                .anyMatch(frame -> frame.contains("changed"));

        repositoryPage.reloadPage();
        assertThat(repositoryPage.isProjectPresent(LIVE_PROJECT))
                .as("The pushed state must match what a manual reload reads from the server")
                .isTrue();
    }

    @Test
    @TestCaseId("IPBQA-33043")
    @Description("EPBDS-16384 guard: change pings arriving while the Projects screen reloads must leave it "
            + "interactive - every pushed project lands in the list and the name filter still narrows it.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testChangesDuringReloadLeaveScreenInteractive() {
        new LoginService(DriverPool.getPage()).login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = new EditorPage().getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        RepositoryProjectsMethod projectsApi = new RepositoryProjectsMethod();
        for (int index = 1; index <= 3; index++) {
            assertThat(projectsApi
                    .createProjectFromTemplate(DESIGN_REPO, BURST_PREFIX + index, TEMPLATE, MASTER).getStatusCode())
                    .as("Fixture: burst project %s must be created over REST", BURST_PREFIX + index)
                    .isEqualTo(200);
            repositoryPage.openProjectsList();
        }

        for (int index = 1; index <= 3; index++) {
            String name = BURST_PREFIX + index;
            boolean appeared = WaitUtil.waitForCondition(() -> repositoryPage.isProjectPresent(name),
                    20000, 500, "Waiting for burst project " + name + " to land in the list");
            assertThat(appeared)
                    .as("Burst project %s must land in the list despite the reload it raced", name)
                    .isTrue();
        }

        repositoryPage.filterByName(BURST_PREFIX + "2");
        assertThat(repositoryPage.getAllVisibleProjectsInTable())
                .as("The screen must stay interactive after the burst - the filter must narrow the list to one row")
                .containsExactly(BURST_PREFIX + "2");
        repositoryPage.clearNameFilter();
    }

    private List<String> captureWebSocketFrames() {
        List<String> frames = new CopyOnWriteArrayList<>();
        DriverPool.getPage().onWebSocket(socket -> socket.onFrameReceived(frame -> frames.add(frame.text())));
        return frames;
    }
}
