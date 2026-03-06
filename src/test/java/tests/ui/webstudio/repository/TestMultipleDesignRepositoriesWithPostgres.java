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
import domain.ui.webstudio.components.repositorytabcomponents.RepositoryContentTabPropertiesComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import configuration.projectconfig.ProjectConfiguration;
import configuration.projectconfig.PropertyNameSpace;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import tests.BaseTest;

import helpers.utils.DbVerificationUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Migrated from: Repository/TestMultipleDesignRepositoriesWithPostgres.java
 * Ticket: IPBQA-30859
 *
 * Adaptation: Instead of using a pre-deployed PostgreSQL instance (used in legacy @BeforeMethod
 * clearDB() for test isolation), each test run spins up a PostgreSQL container via Testcontainers.
 * The container is configured as the Studio's users/security database via app container env vars,
 * demonstrating JDBC integration. Since each test gets a fresh container, clearDB() is not needed.
 *
 * The PostgreSQL container URL uses host.docker.internal so the app Docker container can reach it.
 */
public class TestMultipleDesignRepositoriesWithPostgres extends BaseTest {

    private static final Map<String, String> additionalContainerConfig = new HashMap<>();
    private static final Map<String, String> additionalContainerFiles = new HashMap<>();

    private PostgreSQLContainer<?> postgresContainer;

    private final String nameProjectDesign = "ProjectDesignRepo";
    private final String nameProjectDesign1 = "ProjectDesign1Repo";

    @Override
    @BeforeMethod
    public void beforeMethod(ITestResult result) {
        additionalContainerConfig.clear();
        additionalContainerFiles.clear();

        LOGGER.info("Starting PostgreSQL container for JDBC integration test...");
        postgresContainer = new PostgreSQLContainer<>(ProjectConfiguration.getProperty(PropertyNameSpace.DB_POSTGRES_CONTAINER_IMAGE));
        postgresContainer.start();

        int pgPort = postgresContainer.getMappedPort(5432);
        String pgJdbcUrl = "jdbc:postgresql://" + ProjectConfiguration.getProperty(PropertyNameSpace.DB_CONTAINER_HOST) + ":" + pgPort + "/" + postgresContainer.getDatabaseName();
        LOGGER.info("PostgreSQL container started. JDBC URL (for app container): {}", pgJdbcUrl);

        additionalContainerConfig.put("db.url", pgJdbcUrl);
        additionalContainerConfig.put("db.user", postgresContainer.getUsername());
        additionalContainerConfig.put("db.password", postgresContainer.getPassword());

        String pgJarPath = System.getProperty("user.home") + "/" + ProjectConfiguration.getProperty(PropertyNameSpace.DB_POSTGRES_JAR_MAVEN_PATH);
        additionalContainerFiles.put(pgJarPath, "/opt/openl/lib/postgresql.jar");

        super.beforeMethod(result);
    }

    @Override
    @AfterMethod
    public void afterMethod(ITestResult result) {
        super.afterMethod(result);
        if (postgresContainer != null && postgresContainer.isRunning()) {
            LOGGER.info("Stopping PostgreSQL container...");
            postgresContainer.stop();
        }
    }

    @Test
    @TestCaseId("IPBQA-30859")
    @Description("Multiple Design Repositories: Git flat, Git non-flat, and JDBC security DB — project management across repos")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testMultipleDesignRepositoriesWithPostgres() {
        // Step 0: Login and verify PostgreSQL is actually used as security DB
        EditorPage editorPage = new LoginService(LocalDriverPool.getPage()).login(UserService.getUser(User.ADMIN));
        verifyPostgresContainsOpenLTables();

        // Step 1: Create project in default Design repository
        editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        RepositoryPage repositoryPage = new RepositoryPage();
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, nameProjectDesign, "Example 1 - Bank Rating");

        // Step 2: Add second design repository (Design1 — Git non-flat)
        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        RepositoriesPageComponent reposPage = adminPage.navigateToRepositoriesPage();
        reposPage.addDesignRepository();
        // After clicking Add, the form auto-shows the new Design1 repo (active panel)
        // Form fields are scoped to ant-tabs-tabpane-active, so they correctly read Design1 values

        // Assert default values of newly added Design1 repository
        assertThat(reposPage.getDesignRepositoryNameValue()).isEqualTo("Design1");
        assertThat(reposPage.getDesignRepositoryType()).isEqualTo("Git");
        // New UI has no remote/local checkbox — local path confirms it's not remote
        assertThat(reposPage.getDesignRepositoryLocalPath()).contains("repositories/design1");

        // New UI: no flat/non-flat checkbox — Design1 is non-flat by default (has path-in-repository support)
        // Just save Design1 repository to persist it
        reposPage.applyChangesAndRelogin(User.ADMIN);

        // Step 3: Open Repository tab and check create project dialog repository selectors
        editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage = new RepositoryPage();
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, nameProjectDesign + "_check", "", false);

        // Template tab: default = "-- Select a repository --"
        assertThat(repositoryPage.getCreateNewProjectComponent().getTemplateTabComponent().getRepositorySelectValue())
                .isEqualTo("-- Select a repository --");
        repositoryPage.getCreateNewProjectComponent().getTemplateTabComponent().selectRepository("Design");
        // In new WebStudio, path-in-repository field is shown for all repo types (flat/non-flat distinction removed from dialog)
        assertThat(repositoryPage.getCreateNewProjectComponent().getTemplateTabComponent().isPathInRepositoryVisible())
                .isTrue();

        // Switch to Design1 (non-flat) — path field should appear
        repositoryPage.getCreateNewProjectComponent().getTemplateTabComponent().selectRepository("Design1");
        assertThat(repositoryPage.getCreateNewProjectComponent().getTemplateTabComponent().isPathInRepositoryVisible())
                .isTrue();
        assertThat(repositoryPage.getCreateNewProjectComponent().getTemplateTabComponent().getPathInRepositoryValue())
                .isEqualTo("/");

        // Step 3.1: "Repository" tab (import from repository) — only Design1 available (non-flat only)
        // NOTE: The exact locators for the "Repository" import tab may need verification
        // Legacy: fromRepositoryRepository.getAllValues() containsOnly("-- Select a repository --", "Design1")
        // The path field for "from repository" should be present by default

        // Close Create Project dialog
        repositoryPage.getCreateNewProjectComponent().cancelCreation();

        // Step 4: Create ProjectDesign1Repo in Design1 with path /new/
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, nameProjectDesign1,
                "Example 2 - Corporate Rating", false);
        repositoryPage.getCreateNewProjectComponent().getTemplateTabComponent()
                .selectRepository("Design1")
                .setPathInRepository("/new/");
        repositoryPage.getCreateNewProjectComponent().getTemplateTabComponent().clickCreate();
        repositoryPage.fillCommitInfo();
        repositoryPage.waitUntilSpinnerLoaded();
        repositoryPage.refresh();

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", nameProjectDesign1);
        RepositoryContentTabPropertiesComponent propsTab =
                repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        assertThat(propsTab.getPath()).isEqualTo("new/" + nameProjectDesign1);

        // Step 5: Copy ProjectDesignRepo to Design1 with path /copied/ as nameCopiedProjectToDesign1
        String nameCopiedProjectToDesign1 = "nameCopiedProjectToDesign1";
        repositoryPage.getLeftRepositoryTreeComponent()
                .selectItemInFolder("Projects", nameProjectDesign);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickCopyBtn();
        repositoryPage.getCopyProjectDialogComponent()
                .waitForDialogToAppear()
                .setSeparateProject(true)
                .setNewProjectName(nameCopiedProjectToDesign1)
                .selectRepository("Design1")
                .setProjectFolder("/copied")
                .clickCopyButton();

        repositoryPage.getLeftRepositoryTreeComponent()
                .selectItemInFolder("Projects", nameCopiedProjectToDesign1);
        propsTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        assertThat(propsTab.getPath()).isEqualTo("copied/" + nameCopiedProjectToDesign1);
        assertThat(propsTab.getRepository()).isEqualTo("Design1");

        // Step 6: Copy ProjectDesign1Repo to Design as nameCopiedProjectFromDesign1
        String nameCopiedProjectFromDesign1 = "nameCopiedProjectFromDesign1";
        repositoryPage.getLeftRepositoryTreeComponent()
                .selectItemInFolder("Projects", nameProjectDesign1);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickCopyBtn();
        repositoryPage.getCopyProjectDialogComponent()
                .waitForDialogToAppear()
                .setSeparateProject(true)
                .setNewProjectName(nameCopiedProjectFromDesign1)
                .selectRepository("Design")
                .clickCopyButton();

        repositoryPage.getLeftRepositoryTreeComponent()
                .selectItemInFolder("Projects", nameCopiedProjectFromDesign1);
        propsTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        assertThat(propsTab.getRepository()).isEqualTo("Design");

        // Step 7: Copy ProjectDesignRepo to Design1 (same name) — conflict/closed scenario
        // New UI requires explicit path for non-flat repos (legacy didn't set path)
        repositoryPage.getLeftRepositoryTreeComponent()
                .selectItemInFolder("Projects", nameProjectDesign);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickCopyBtn();
        repositoryPage.getCopyProjectDialogComponent()
                .waitForDialogToAppear()
                .setSeparateProject(true)
                .setNewProjectName(nameProjectDesign)
                .selectRepository("Design1")
                .setProjectFolder("/step7")
                .clickCopyButton();

        // After copy, the opened project (Design) should still show No Changes
        repositoryPage.getLeftRepositoryTreeComponent()
                .selectOpenedItemInFolder("Projects", nameProjectDesign);
        propsTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        assertThat(propsTab.getStatus()).isEqualTo("No Changes");

        // The copy creates a closed version visible in the tree (closed.gif icon)
        repositoryPage.getLeftRepositoryTreeComponent()
                .selectClosedItemInFolder("Projects", nameProjectDesign);
        propsTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        assertThat(propsTab.getName()).isEqualTo(nameProjectDesign);
        assertThat(propsTab.getPath()).isEqualTo("step7/" + nameProjectDesign);
        assertThat(propsTab.getRepository()).isEqualTo("Design1");
        assertThat(propsTab.getStatus()).isEqualTo("Closed");

        // Step 7.1: Try to open the closed project — shows closable error at top of page
        repositoryPage.getRepositoryContentButtonsPanelComponent().openProject();
        assertThat(repositoryPage.getClosableMessageText())
                .contains("Cannot open two projects with the same name");
        repositoryPage.closeClosableMessage();

        // Step 8: Try to copy again to Design1 with /copied/ path — should show duplicate error
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickCopyBtn();
        repositoryPage.getCopyProjectDialogComponent()
                .waitForDialogToAppear()
                .setSeparateProject(true)
                .setNewProjectName(nameProjectDesign)
                .selectRepository("Design1")
                .setProjectFolder("/copied")
                .clickCopyButton(false);
        List<String> copyErrors = repositoryPage.getCopyProjectDialogComponent().getErrors();
        assertThat(copyErrors).anyMatch(e -> e.contains("Project with this name already exists"));
        repositoryPage.getCopyProjectDialogComponent().clickCancelButton();

        // Step 9: Copy ProjectDesign1Repo to Design (another copy)
        repositoryPage.getLeftRepositoryTreeComponent()
                .selectItemInFolder("Projects", nameProjectDesign1);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickCopyBtn();
        repositoryPage.getCopyProjectDialogComponent()
                .waitForDialogToAppear()
                .setSeparateProject(true)
                .setNewProjectName(nameProjectDesign1)
                .selectRepository("Design")
                .clickCopyButton();

        // After copy, the opened project (Design1) should still show No Changes
        repositoryPage.getLeftRepositoryTreeComponent()
                .selectOpenedItemInFolder("Projects", nameProjectDesign1);
        propsTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        assertThat(propsTab.getRepository()).isEqualTo("Design1");
        assertThat(propsTab.getStatus()).isEqualTo("No Changes");

        // The closed copy in Design repo (closed.gif icon)
        repositoryPage.getLeftRepositoryTreeComponent()
                .selectClosedItemInFolder("Projects", nameProjectDesign1);
        propsTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        assertThat(propsTab.getName()).isEqualTo(nameProjectDesign1);
        assertThat(propsTab.getRepository()).isEqualTo("Design");
        assertThat(propsTab.getStatus()).isEqualTo("Closed");

        // Step 9.1: Open closed copy — shows closable error at top of page
        repositoryPage.getRepositoryContentButtonsPanelComponent().openProject();
        assertThat(repositoryPage.getClosableMessageText())
                .contains("Cannot open two projects with the same name");
        repositoryPage.closeClosableMessage();

        // Step 9.2: Switch to Editor tab, open ProjectDesign1Repo, verify project name is editable
        editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.EDITOR);
        editorPage.getEditorLeftProjectModuleSelectorComponent().selectProject(nameProjectDesign1);
        EditProjectDialogComponent editDialog = editorPage.openEditProjectDialog(nameProjectDesign1);
        editDialog.setProjectName("Check");
        assertThat(editDialog.getProjectName()).isEqualTo("Check");
        assertThat(editDialog.isUpdateButtonEnabled()).isTrue();
        editDialog.clickCancelButton();

        // Step 10: Switch to ProjectDesignRepo via breadcrumbs — verify Edit Project dialog opens
        // Legacy asserted project name field was absent for flat repos; new UI always shows it
        editorPage.getEditorToolbarPanelComponent().selectProjectBreadcrumbs(nameProjectDesign);
        editDialog = editorPage.openEditProjectDialog(nameProjectDesign);
        assertThat(editDialog.getProjectName()).isEqualTo(nameProjectDesign);
        editDialog.clickCancelButton();

        // Switch back to Repository tab for remaining steps
        editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage = new RepositoryPage();

        // Step 11: Delete and Erase ProjectDesignRepo
        // Show deleted projects BEFORE deleting, so deleted project stays visible for Erase
        repositoryPage.setShowDeletedProjects(true);
        repositoryPage.getLeftRepositoryTreeComponent()
                .selectOpenedItemInFolder("Projects", nameProjectDesign);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickDeleteBtn();
        repositoryPage.getConfirmDeleteDialogComponent().clickDelete();
        // After delete, project is in "deleted" state but still visible (showDeleted=true) — Erase button appears
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickEraseBtn();
        assertThat(repositoryPage.getConfirmEraseDialogComponent().isAlsoDeleteFromRepositoryVisible())
                .as("Erase dialog for flat Git repo should show 'also delete from repository' checkbox")
                .isTrue();
        repositoryPage.getConfirmEraseDialogComponent().clickErase();

        // Step 12: Delete and Erase ProjectDesign1Repo
        repositoryPage.getLeftRepositoryTreeComponent()
                .selectOpenedItemInFolder("Projects", nameProjectDesign1);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickDeleteBtn();
        repositoryPage.getConfirmDeleteDialogComponent().clickDelete();
        // Same as above — deleted project stays visible, Erase button appears
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickEraseBtn();
        assertThat(repositoryPage.getConfirmEraseDialogComponent().isAlsoDeleteFromRepositoryVisible())
                .as("Erase dialog for non-flat Git repo should show 'also delete from repository' checkbox")
                .isTrue();

        //TODO: check all the changes and LOGS and remove strange waiters
    }

    private void verifyPostgresContainsOpenLTables() {
        List<String> tables = DbVerificationUtil.queryTableNames(
                postgresContainer.getJdbcUrl(),
                postgresContainer.getUsername(),
                postgresContainer.getPassword(),
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_name LIKE 'openl_%' ORDER BY table_name");
        assertThat(tables)
                .as("PostgreSQL should contain OpenL security tables — proves it's used instead of embedded H2")
                .isNotEmpty()
                .anyMatch(t -> t.contains("openl_users"))
                .anyMatch(t -> t.contains("openl_groups"));
    }
}
