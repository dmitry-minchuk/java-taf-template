package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
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
    @Description("Studio pages must fit the window width. KNOWN-FAILING: the main page is wider than the "
            + "viewport, so a horizontal scroll bar appears."
            + " Known bug: EPBDS-16274.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testStudioPagesFitTheWindowWidth() {
        EditorPage editorPage = new LoginService(LocalDriverPool.getPage())
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
