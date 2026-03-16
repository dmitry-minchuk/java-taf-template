package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerPool;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.RepositoriesPageComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.LogsUtil;
import helpers.utils.StringUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

public class TestAddDeleteDesignRepository extends BaseTest {

    @Test
    @TestCaseId("IPBQA-30682")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAddDeleteDesignRepository() {
        String nameProject = StringUtil.generateUniqueName("TestAddDeleteDesignRepo");
        String nameProjectDesign1 = nameProject + "InDesign1";
        String nameProjectDesign2 = nameProject + "InDesign2";

        // Login and create project in default repo
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(
                domain.ui.webstudio.components.common.CreateNewProjectComponent.TabName.TEMPLATE,
                nameProject, "Sample Project");

        // Navigate to Admin → Repositories and add Design1
        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        RepositoriesPageComponent repositories = adminPage.navigateToRepositoriesPage();
        repositories.addDesignRepository();
        repositories.applyChangesAndRelogin(User.ADMIN);

        // Create project in Design1
        editorPage = new EditorPage();
        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProjectFromTemplateWithSelectRepo(nameProjectDesign1, "Sample Project", "Design1");

        // Navigate to Admin → Repositories, delete first additional repo, add new one (becomes Design2)
        adminPage = editorPage.openUserMenu().navigateToAdministration();
        repositories = adminPage.navigateToRepositoriesPage();
        repositories.deleteRepository("Design1");
        repositories.addDesignRepository();
        repositories.applyChangesAndRelogin(User.ADMIN);

        // Create project in Design2
        editorPage = new EditorPage();
        repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProjectFromTemplateWithSelectRepo(nameProjectDesign2, "Empty Project", "Design2");

        // Verify no errors in application logs
        LogsUtil.inspectLogFile(AppContainerPool.get());
    }
}
