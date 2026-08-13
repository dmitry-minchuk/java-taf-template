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

public class TestMigrateBlockedByUndeclaredWorkbookUi extends BaseTest {

    private static final String PROJECT = "MigrateBlockedProject";
    private static final String FIXTURE_ZIP = "MigrateBlockedProject.zip";

    @Test
    @TestCaseId("EPBDS-16363")
    @Description("EPBDS-16363: a project with an undeclared root workbook must not be silently widened - the "
            + "Migrate offer is withheld because the rewrite would turn Extra.xls into a module, Edit stays "
            + "available, and rules.xml keeps its original explicit module declaration.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testMigrateWithheldWhenRewriteWouldWidenModules() {
        EditorPage editorPage = new LoginService(DriverPool.getPage()).login(UserService.getUser(User.ADMIN));
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent()
                .selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, PROJECT, FIXTURE_ZIP);

        ProjectDetailPage detail = repositoryPage.openProjectsList().openProjectDetail(PROJECT);
        assertThat(detail.isOverviewEditOffered())
                .as("Edit must stay available for the project with an undeclared workbook")
                .isTrue();
        assertThat(detail.isOverviewMigrateOffered())
                .as("Migrate must be withheld while an undeclared workbook would become a module (EPBDS-16363)")
                .isFalse();

        editorPage = repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectModule(PROJECT, "MainModule");
        editorPage.getEditorToolbarPanelComponent().navigateToProjectRoot(PROJECT);
        editorPage.getEditorToolbarPanelComponent().clickExport();
        File exportedZip = editorPage.getExportProjectDialogComponent().clickExportAndDownload();
        String rulesXml = ZipUtil.readFileFromZip(exportedZip, "rules.xml");
        assertThat(rulesXml)
                .as("The withheld migration must leave rules.xml with its original explicit declaration")
                .contains("Main.xls")
                .doesNotContain("Extra");
    }
}
