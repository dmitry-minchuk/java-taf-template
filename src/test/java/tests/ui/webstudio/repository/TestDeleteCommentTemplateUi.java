package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.RepositoriesPageComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.annotations.Test;
import tests.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDeleteCommentTemplateUi extends BaseTest {

    @Test
    @TestCaseId("EPBDS-16230")
    @Description("Known-failing regression for EPBDS-16230: the repository 'Customize comments' User Message Templates must include a Delete-project template, so delete commits are consistent with create/save/copy. Red until EPBDS-16230 is fixed.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testDeleteProjectMessageTemplateExists() {
        EditorPage editorPage = new LoginService(LocalDriverPool.getPage()).login(UserService.getUser(User.ADMIN));
        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        RepositoriesPageComponent repositories = adminPage.navigateToRepositoriesPage();
        repositories.enableCustomizeComments();

        assertThat(repositories.isDeleteMessageTemplatePresent())
                .as("Customize comments must offer a Delete-project message template (EPBDS-16230)")
                .isTrue();
    }
}
