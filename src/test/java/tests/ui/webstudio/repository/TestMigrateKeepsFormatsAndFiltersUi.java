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

public class TestMigrateKeepsFormatsAndFiltersUi extends BaseTest {

    private static final String PROJECT = "MigrateXlsProject";
    private static final String FIXTURE_ZIP = "MigrateXlsProject.zip";
    private static final String MODULE_FILE = "LegacyOld.xls";
    private static final String FILTER_PATTERN = "foo*";

    @Test
    @TestCaseId("IPBQA-33029")
    @Description("EPBDS-16364 and EPBDS-16365: migrating a legacy descriptor with a declared .xls module and a "
            + "module method-filter must keep the .xls module reachable and must not drop the method "
            + "restriction - the filter pattern survives in the rewritten rules.xml.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testMigrateKeepsXlsModuleAndMethodFilter() {
        EditorPage editorPage = new LoginService(DriverPool.getPage()).login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, PROJECT, FIXTURE_ZIP);

        ProjectDetailPage detail = repositoryPage.openProjectsList().openProjectDetail(PROJECT);
        assertThat(detail.isOverviewMigrateOffered())
                .as("Precondition: the legacy .xls descriptor must offer Migrate")
                .isTrue();

        detail.migrateOverviewDescriptor();

        assertThat(detail.getOverviewModuleNames())
                .as("The .xls module must survive the migration (EPBDS-16364)")
                .anyMatch(name -> name.contains(MODULE_FILE) || name.contains("LegacyOld"));
        assertThat(detail.isFilePresent(MODULE_FILE))
                .as("The declared .xls workbook must still be reachable after the migration")
                .isTrue();

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(PROJECT, "LegacyOld");
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(PROJECT);
        editorPage.getEditorToolbarPanelComponent().clickExport();
        File exportedZip = editorPage.getExportProjectDialogComponent().clickExportAndDownload();
        String rulesXml = ZipUtil.readFileFromZip(exportedZip, "rules.xml");
        assertThat(rulesXml)
                .as("The method restriction must survive the migration (EPBDS-16365)")
                .contains(FILTER_PATTERN);
        assertThat(rulesXml)
                .as("The migrated descriptor must still declare the .xls workbook (EPBDS-16364)")
                .contains(MODULE_FILE);
    }
}
