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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestProjectOverviewEditKeepsModulesUi extends BaseTest {

    private static final String TEMPLATE_NAME = "Example 2 - Corporate Rating";
    private static final String MODULE_NAME = "Corporate Rating";

    @Test
    @TestCaseId("EPBDS-16327")
    @Description("Validates the EPBDS-16327 fix. A template project keeps its workbook in the project "
            + "root, so the Overview offers Migrate instead of Edit; after migrating, Edit -> Save "
            + "without changes must keep the project's modules both on Overview and in the Editor.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testOverviewEditSaveKeepsModules() {
        String projectName = "OverviewEditModules_" + System.currentTimeMillis();

        LoginService loginService = new LoginService(DriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);

        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, projectName, TEMPLATE_NAME);

        ProjectDetailPage projectDetail = repositoryPage.openProjectsList().openProjectDetail(projectName);
        assertThat(projectDetail.getOverviewModuleNames())
                .as("The template's module should be listed before the descriptor is migrated")
                .anyMatch(name -> name.contains(MODULE_NAME));

        projectDetail.migrateOverviewDescriptor();

        assertThat(projectDetail.getOverviewMatchedModuleNames())
                .as("Migrating the descriptor must keep the workbook matched as a module")
                .anyMatch(name -> name.contains(MODULE_NAME));

        projectDetail.editOverviewAndSave();

        assertThat(projectDetail.getOverviewMatchedModuleNames())
                .as("Saving the Overview tab must not empty the module list")
                .anyMatch(name -> name.contains(MODULE_NAME));

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        List<String> modulesInEditor = editorPage.getEditorLeftProjectModuleSelectorComponent()
                .getAllModuleNames(projectName);
        assertThat(modulesInEditor)
                .as("The Editor must still list the module after the Overview tab was saved")
                .contains(MODULE_NAME);
    }
}
