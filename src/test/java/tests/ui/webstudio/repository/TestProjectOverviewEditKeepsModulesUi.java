package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestProjectOverviewEditKeepsModulesUi extends BaseTest {

    private static final String TEMPLATE_NAME = "Example 2 - Corporate Rating";
    private static final String MODULE_NAME = "Corporate Rating";

    @Test
    @TestCaseId("EPBDS-16327")
    @Description("Saving the Overview tab without changing anything must keep the project's modules. "
            + "KNOWN-FAILING: Edit -> Save writes a rules.xml whose modules block is empty, so the module "
            + "list comes out empty both on Overview and in the Editor."
            + " Known bug: EPBDS-16327.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testOverviewEditSaveKeepsModules() {
        String projectName = "OverviewEditModules_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, projectName, TEMPLATE_NAME);

        ProjectDetailPage projectDetail = repositoryPage.openProjectsList().openProjectDetail(projectName);
        assertThat(projectDetail.getOverviewModuleNames())
                .as("The template's module should be listed before the Overview tab is saved")
                .anyMatch(name -> name.contains(MODULE_NAME));

        projectDetail.editOverviewAndSave();

        assertThat(projectDetail.getOverviewModuleNames())
                .as("Saving the Overview tab must not empty the module list")
                .anyMatch(name -> name.contains(MODULE_NAME));

        // The Editor reads the same descriptor, so it must still offer the module.
        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        List<String> modulesInEditor = editorPage.getEditorLeftProjectModuleSelectorComponent()
                .getAllModuleNames(projectName);
        assertThat(modulesInEditor)
                .as("The Editor must still list the module after the Overview tab was saved")
                .contains(MODULE_NAME);
    }
}
