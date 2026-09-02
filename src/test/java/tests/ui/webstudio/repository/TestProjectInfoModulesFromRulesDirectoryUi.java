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
import helpers.utils.ZipUtil;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class TestProjectInfoModulesFromRulesDirectoryUi extends BaseTest {

    private static final String PROJECT = "EmptyModulesDescriptorProject";
    private static final String FIXTURE_ZIP = "EmptyModulesDescriptorProject.zip";
    private static final String FIRST_MODULE = "BugReproducing";
    private static final String SECOND_MODULE = "generalProject";
    private static final String NEW_DESCRIPTION = "Description edited by AQA";

    @Test
    @TestCaseId("EPBDS-16272")
    @Description("EPBDS-16224 verification ordered by EPBDS-16272: a descriptor that declares no modules while the "
            + "rules directory holds them must still show those modules on Project Info, and editing project "
            + "information other than the modules must leave the descriptor's modules block alone.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testModulesAreShownAndDescriptorKeepsNoDeclaredModules() {
        EditorPage editorPage = new LoginService(DriverPool.getPage()).login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, PROJECT, FIXTURE_ZIP);

        ProjectDetailPage detail = repositoryPage.openProjectsList().openProjectDetail(PROJECT);

        assertThat(detail.getOverviewMatchedModuleNames())
                .as("Modules found in the rules directory must be shown even though rules.xml declares none "
                        + "(EPBDS-16224)")
                .contains(FIRST_MODULE, SECOND_MODULE);

        assertThat(detail.isOverviewEditOffered())
                .as("Precondition: Project Info must offer Edit for this descriptor")
                .isTrue();

        detail.editOverviewDescriptionAndSave(NEW_DESCRIPTION);

        assertThat(detail.getOverviewMatchedModuleNames())
                .as("Saving an edit to other project information must not empty the module list (EPBDS-16224)")
                .contains(FIRST_MODULE, SECOND_MODULE);

        File exportedZip = detail.openExportDialog().clickExportAndDownload();
        String rulesXml = ZipUtil.readFileFromZip(exportedZip, "rules.xml");

        assertThat(rulesXml)
                .as("Precondition: the saved descriptor must carry the edited project information")
                .contains(NEW_DESCRIPTION);
        assertThat(rulesXml)
                .as("Editing project information other than the modules must not declare modules in rules.xml "
                        + "(EPBDS-16224)")
                .doesNotContainPattern("<module[\\s/>]");
    }
}
