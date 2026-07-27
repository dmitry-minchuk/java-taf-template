package tests.ui.webstudio.repository;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.RepositoriesPageComponent;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.editortabcomponents.EditProjectDialogComponent;
import domain.ui.webstudio.components.repositorytabcomponents.CopyProjectDialogComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.ProjectDetailPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.DeployInfrastructureService;
import helpers.service.LoginService;
import helpers.service.UserService;
import helpers.utils.DbVerificationUtil;
import helpers.utils.WaitUtil;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import tests.BaseTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Ticket: IPBQA-30859 — Multiple Design Repositories (Git flat + Git non-flat + JDBC security DB).
 *
 * React repository (build 032c60a664ce+): create/copy target a specific repository via the wizard/copy-dialog
 * repository selector + path field; status lives in the project detail ("No Changes"/"Closed");
 * two same-name projects across repos are disambiguated by open/closed state (openProjectDetailByState). The
 * legacy branch/separate-project copy options were removed.
 */
public class TestMultipleDesignRepositoriesWithPostgres extends BaseTest {

    private static final Map<String, String> additionalContainerConfig = new HashMap<>();
    private static final Map<String, String> additionalContainerFiles = new HashMap<>();

    private DeployInfrastructureService deployInfra;

    private final String nameProjectDesign = "ProjectDesignRepo";
    private final String nameProjectDesign1 = "ProjectDesign1Repo";

    @Override
    @BeforeMethod
    public void beforeMethod(ITestResult result) {
        additionalContainerConfig.clear();
        additionalContainerFiles.clear();
        deployInfra = DeployInfrastructureService.builder().withPostgresAsSecurityDb().build();
        deployInfra.start();
        additionalContainerConfig.putAll(deployInfra.getContainerConfig());
        additionalContainerFiles.putAll(deployInfra.getFilesToCopy());
        super.beforeMethod(result);
    }

    @Override
    @AfterMethod
    public void afterMethod(ITestResult result) {
        super.afterMethod(result);
        if (deployInfra != null) {
            deployInfra.cleanup();
        }
    }

    @Test
    @TestCaseId("IPBQA-30859")
    @Description("Multiple Design Repositories: create/copy across Git repos with JDBC security DB")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testMultipleDesignRepositoriesWithPostgres() {
        // Step 0: Login and verify PostgreSQL is used as the security DB
        EditorPage editorPage = new LoginService(LocalDriverPool.getPage()).login(UserService.getUser(User.ADMIN));
        verifyPostgresContainsOpenLTables();

        // Step 1: Create a project in the default Design repository
        editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        RepositoryPage repositoryPage = new RepositoryPage();
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, nameProjectDesign, "Example 1 - Bank Rating");

        // Step 2: Add a second design repository (Design1) and check its defaults
        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        RepositoriesPageComponent reposPage = adminPage.navigateToRepositoriesPage();
        reposPage.addDesignRepository();
        assertThat(reposPage.getDesignRepositoryNameValue()).isEqualTo("Design1");
        assertThat(reposPage.getDesignRepositoryType()).isEqualTo("Git");
        assertThat(reposPage.getDesignRepositoryLocalPath()).contains("repositories/design1");
        reposPage.applyChangesAndRelogin(User.ADMIN);

        // Step 3: Create-project wizard shows a repository selector + path field once >1 repo exists
        editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage = new RepositoryPage();
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, nameProjectDesign + "_check", "", false);
        CreateNewProjectComponent wizard = repositoryPage.getCreateNewProjectComponent();
        wizard.selectRepository("Design");
        assertThat(wizard.isPathInRepositoryVisible()).as("Path field should be shown for the Design repo").isTrue();
        wizard.selectRepository("Design1");
        assertThat(wizard.isPathInRepositoryVisible()).as("Path field should be shown for the Design1 repo").isTrue();
        repositoryPage.getCreateNewProjectComponent().cancelCreation();

        // Step 4: Create ProjectDesign1Repo in Design1 at path /new/
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, nameProjectDesign1, "Example 2 - Corporate Rating", false);
        wizard = repositoryPage.getCreateNewProjectComponent();
        wizard.selectRepository("Design1").setPathInRepository("new").clickCreate();
        repositoryPage.fillCommitInfo();
        repositoryPage.waitUntilSpinnerLoaded();
        // 6.4.0 lists a project under its own name wherever it lives; the path is a field of its own, not a
        // prefix on the row name as the 6.3.1 React build showed it. Both repos' projects are listed by
        // default (repo filters unchecked = show-all), so no filter toggling.
        String design1RowName = nameProjectDesign1;
        ProjectDetailPage detail = repositoryPage.openProjectsList().openProjectDetail(design1RowName);
        assertThat(detail.getOverviewPath()).as("ProjectDesign1Repo path").contains("new");
        assertThat(detail.getOverviewRepository()).as("ProjectDesign1Repo repository").isEqualTo("Design1");
        repositoryPage.openProjectsList();

        // Step 5: Copy ProjectDesignRepo (flat) to Design1 at "copied" as nameCopiedProjectToDesign1
        // COPY into a path yields a BARE row name (just the new name), unlike CREATE (which path-prefixes).
        String nameCopiedProjectToDesign1 = "nameCopiedProjectToDesign1";
        CopyProjectDialogComponent copyDialog = repositoryPage.clickCopyAction(nameProjectDesign);
        copyDialog.setAsNewProject().setNewProjectName(nameCopiedProjectToDesign1).selectRepository("Design1").setProjectFolder("copied").clickCopyButton();
        repositoryPage.fillCommitInfo();
        repositoryPage.waitUntilSpinnerLoaded();
        detail = repositoryPage.openProjectsList().openProjectDetail(nameCopiedProjectToDesign1);
        assertThat(detail.getOverviewRepository()).isEqualTo("Design1");
        repositoryPage.openProjectsList();

        // Step 6: Copy ProjectDesign1Repo to Design (flat) as nameCopiedProjectFromDesign1
        String nameCopiedProjectFromDesign1 = "nameCopiedProjectFromDesign1";
        copyDialog = repositoryPage.clickCopyAction(design1RowName);
        copyDialog.setAsNewProject().setNewProjectName(nameCopiedProjectFromDesign1).selectRepository("Design").clickCopyButton();
        repositoryPage.fillCommitInfo();
        repositoryPage.waitUntilSpinnerLoaded();
        detail = repositoryPage.openProjectsList().openProjectDetail(nameCopiedProjectFromDesign1);
        assertThat(detail.getOverviewRepository()).isEqualTo("Design");
        repositoryPage.openProjectsList();

        // Step 7: Copy ProjectDesignRepo to Design1 at "step7" with the SAME name. COPY yields a BARE row name,
        // so there are now TWO rows named "ProjectDesignRepo" (Design original = Opened, Design1 copy = Closed) —
        // disambiguate by state.
        copyDialog = repositoryPage.clickCopyAction(nameProjectDesign);
        copyDialog.setAsNewProject().setNewProjectName(nameProjectDesign).selectRepository("Design1").setProjectFolder("step7").clickCopyButton();
        repositoryPage.fillCommitInfo();
        repositoryPage.waitUntilSpinnerLoaded();

        // The opened original (Design) still shows Opened
        detail = repositoryPage.openProjectsList().openProjectDetailByState(nameProjectDesign, true);
        assertThat(detail.getStatus()).as("Opened original status").isEqualTo("No Changes");
        // The closed copy (Design1)
        detail = repositoryPage.openProjectsList().openProjectDetailByState(nameProjectDesign, false);
        assertThat(detail.getOverviewRepository()).isEqualTo("Design1");
        assertThat(detail.getStatus()).isEqualTo("Closed");

        // Step 7.1: Opening the closed same-name copy is blocked with a duplicate-name error notification
        repositoryPage.openProjectsList().openProjectByState(nameProjectDesign, false);
        assertThat(waitForDuplicateNameError())
                .as("Opening a second project with the same name must be blocked").isTrue();
        repositoryPage.closeAllMessages();

        // Step 8: Copy the opened original again to Design1 at "copied" with the same name → duplicate error
        copyDialog = repositoryPage.clickCopyActionByState(nameProjectDesign, true);
        copyDialog.setAsNewProject().setNewProjectName(nameProjectDesign).selectRepository("Design1").setProjectFolder("copied").clickCopyButton(false);
        assertThat(copyDialog.waitForErrors(5000)).anyMatch(e -> e.contains("already exists"));
        copyDialog.clickCancelButton();

        // Step 11: Permanently delete the opened ProjectDesignRepo original (a same-name closed copy also exists)
        repositoryPage.openProjectsList();
        repositoryPage.deleteProjectByState(nameProjectDesign, true)
                .enterDeletionComment("Removed by automated regression test")
                .acknowledgePermanentDeletion()
                .clickDelete();
        repositoryPage.openProjectsList();

        // Step 12: Permanently delete ProjectDesign1Repo (Design1 at path → row name is path-prefixed)
        repositoryPage.deleteProject(design1RowName)
                .enterDeletionComment("Removed by automated regression test")
                .acknowledgePermanentDeletion()
                .clickDelete();
    }

    private boolean waitForDuplicateNameError() {
        return WaitUtil.waitForCondition(
                () -> LocalDriverPool.getPage().locator("xpath=//div[contains(@class,'ant-notification-notice')]"
                        + "[contains(normalize-space(.),'Cannot open two projects with the same name')]").count() > 0,
                10000, 500, "Waiting for the duplicate-name error notification");
    }

    private void verifyPostgresContainsOpenLTables() {
        PostgreSQLContainer<?> pg = deployInfra.getPostgresContainer();
        List<String> tables = DbVerificationUtil.queryTableNames(
                pg.getJdbcUrl(), pg.getUsername(), pg.getPassword(),
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_name LIKE 'openl_%' ORDER BY table_name");
        assertThat(tables)
                .as("PostgreSQL should contain OpenL security tables")
                .isNotEmpty()
                .anyMatch(t -> t.contains("openl_users"))
                .anyMatch(t -> t.contains("openl_groups"));
    }
}
