package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.DriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestMainPageHasNoHorizontalScrollUi extends BaseTest {

    @Test
    @TestCaseId("EPBDS-16274")
    @Description("Studio pages must fit the window width. Guards the horizontal scroll bar reported in "
            + "EPBDS-16274, which carries no steps beyond a screenshot; measured at 1280 and below it does not "
            + "appear on 6.4.0-ef53e0bec1d7, so the test is green here and turns red if the page outgrows the "
            + "window.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testStudioPagesFitTheWindowWidth() {
        EditorPage editorPage = new LoginService(DriverPool.getPage())
                .login(UserService.getUser(User.ADMIN));

        assertThat(editorPage.hasHorizontalScroll())
                .as("The Editor page should not scroll sideways")
                .isFalse();

        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        assertThat(repositoryPage.hasHorizontalScroll())
                .as("The Projects page should not scroll sideways")
                .isFalse();
    }
}
