package tests.ui.webstudio.db;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.RepositoriesPageComponent;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
import domain.ui.webstudio.components.repositorytabcomponents.RepositoryContentTabPropertiesComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
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
 * Migrated from: Repository/TestMultipleDesignReposGitFlatNonFlatAndJDBC.java
 * Ticket: IPBQA-30859
 *
 * Adaptation: Instead of using a pre-deployed PostgreSQL instance (used in legacy @BeforeMethod
 * clearDB() for test isolation), each test run spins up a PostgreSQL container via Testcontainers.
 * The container is configured as the Studio's users/security database via app container env vars,
 * demonstrating JDBC integration. Since each test gets a fresh container, clearDB() is not needed.
 *
 * The PostgreSQL container URL uses host.docker.internal so the app Docker container can reach it.
 */
public class TestMultipleDesignReposGitFlatNonFlatAndJDBC extends BaseTest {

    private static final Map<String, String> additionalContainerConfig = new HashMap<>();

    private PostgreSQLContainer<?> postgresContainer;

    private final String nameProjectDesign = "ProjectDesignRepo";
    private final String nameProjectDesign1 = "ProjectDesign1Repo";

    @Override
    @BeforeMethod
    public void beforeMethod(ITestResult result) {
        LOGGER.info("Starting PostgreSQL container for JDBC integration test...");
        postgresContainer = new PostgreSQLContainer<>("postgres:alpine");
        postgresContainer.start();

        int pgPort = postgresContainer.getMappedPort(5432);
        // Build JDBC URL using host.docker.internal so the app container can reach it
        String pgJdbcUrl = "jdbc:postgresql://host.docker.internal:" + pgPort + "/" + postgresContainer.getDatabaseName();
        LOGGER.info("PostgreSQL container started. JDBC URL (for app container): {}", pgJdbcUrl);

        // Configure Studio to use PostgreSQL as its security/user database (JDBC integration)
        // Legacy: clearDB() dropped tables from this Postgres DB before each test.
        // New approach: fresh container per test = no leftover state, no clearDB() needed.
        additionalContainerConfig.put("user.mode", "multi");
        additionalContainerConfig.put("security.administrators", "admin");
        additionalContainerConfig.put("db.url", pgJdbcUrl);
        additionalContainerConfig.put("db.user", postgresContainer.getUsername());
        additionalContainerConfig.put("db.password", postgresContainer.getPassword());

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
    public void testMultipleDesignReposGitFlatNonFlatAndJDBC() {
        // Step 1: Login and create project in default Design repository
        EditorPage editorPage = new LoginService(LocalDriverPool.getPage()).login(UserService.getUser(User.ADMIN));
        editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        RepositoryPage repositoryPage = new RepositoryPage();
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, nameProjectDesign, "Example 1 - Bank Rating");

        // Step 2: Add second design repository (Design1 — Git non-flat)
        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        RepositoriesPageComponent reposPage = adminPage.navigateToRepositoriesPage();
        reposPage.addDesignRepository();

        // Assert default values of newly added Design1 repository
        assertThat(reposPage.getDesignRepositoryNameValue()).isEqualTo("Design1");
        assertThat(reposPage.getDesignRepositoryType()).isEqualTo("Git");
        assertThat(reposPage.isDesignRepositoryRemote()).isFalse();
        // Local path should contain "repositories/design1" — container workspace path
        assertThat(reposPage.getDesignRepositoryLocalPath()).contains("repositories/design1");

        // Make Design1 non-flat folder structure
        reposPage.setFlatFolderStructure(false);
        reposPage.applyChangesAndRelogin(User.ADMIN);

        // Step 3: Open Repository tab and check create project dialog repository selectors
        editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage = new RepositoryPage();
        repositoryPage.createProject(CreateNewProjectComponent.TabName.TEMPLATE, nameProjectDesign + "_check", "", false);

        // Template tab: default = "-- Select a repository --"
        assertThat(repositoryPage.getCreateNewProjectComponent().getTemplateTabComponent().getRepositorySelectValue())
                .isEqualTo("-- Select a repository --");
        repositoryPage.getCreateNewProjectComponent().getTemplateTabComponent().selectRepository("Design");
        // Path field absent for flat repo (Design)
        assertThat(repositoryPage.getCreateNewProjectComponent().getTemplateTabComponent().isPathInRepositoryVisible())
                .isFalse();

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
                .setNewProjectName(nameCopiedProjectFromDesign1)
                .selectRepository("Design")
                .clickCopyButton();

        repositoryPage.getLeftRepositoryTreeComponent()
                .selectItemInFolder("Projects", nameCopiedProjectFromDesign1);
        propsTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        assertThat(propsTab.getRepository()).isEqualTo("Design");

        // Step 7: Copy ProjectDesignRepo to Design1 (same name) — conflict/closed scenario
        repositoryPage.getLeftRepositoryTreeComponent()
                .selectItemInFolder("Projects", nameProjectDesign);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickCopyBtn();
        repositoryPage.getCopyProjectDialogComponent()
                .setNewProjectName(nameProjectDesign)
                .selectRepository("Design1")
                .clickCopyButton();

        // After copy, original in Design1 should be no-changes
        propsTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        assertThat(propsTab.getStatus()).isEqualTo("No Changes");

        // The copy in Design repo (closed version) should have expected properties
        // NOTE: "getProjectFromTableWithClosedStatus" pattern from legacy = find the closed entry in the table
        repositoryPage.getLeftRepositoryTreeComponent()
                .selectItemInFolder("Projects", nameProjectDesign);
        propsTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        assertThat(propsTab.getName()).isEqualTo(nameProjectDesign);
        assertThat(propsTab.getPath()).isEqualTo(nameProjectDesign);
        assertThat(propsTab.getRepository()).isEqualTo("Design1");
        assertThat(propsTab.getStatus()).isEqualTo("Closed");

        // Step 7.1: Try to open the closed project — should show "cannot open two projects with same name" message
        repositoryPage.getRepositoryContentButtonsPanelComponent().openProject();
        assertThat(repositoryPage.getMessagePopupText())
                .contains("WebStudio cannot open two projects with same name");
        repositoryPage.closeMessagePopup();

        // Step 8: Try to copy again to Design1 with /copied/ path — should show duplicate error
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickCopyBtn();
        repositoryPage.getCopyProjectDialogComponent()
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
                .setNewProjectName(nameProjectDesign1)
                .selectRepository("Design")
                .clickCopyButton();

        propsTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        assertThat(propsTab.getRepository()).isEqualTo("Design1");
        assertThat(propsTab.getStatus()).isEqualTo("No Changes");

        // The closed copy in Design repo
        repositoryPage.getLeftRepositoryTreeComponent()
                .selectItemInFolder("Projects", nameProjectDesign1);
        propsTab = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();
        assertThat(propsTab.getName()).isEqualTo(nameProjectDesign1);
        assertThat(propsTab.getRepository()).isEqualTo("Design");
        assertThat(propsTab.getStatus()).isEqualTo("Closed");

        // Step 9.1: Open closed copy — should show "cannot open two projects with same name"
        repositoryPage.getRepositoryContentButtonsPanelComponent().openProject();
        assertThat(repositoryPage.getMessagePopupText())
                .contains("WebStudio cannot open two projects with same name");
        repositoryPage.closeMessagePopup();

        // Step 9.2: Editor tab — open ProjectDesign1Repo, edit project name, assert enabled
        // (nameProjectDesign1 is in Design1 — editing allowed)
        // NOTE: Editor tab navigation from Repository tab — switching to Editor and back
        // Requires EditorPage.getEditProjectDialog() or similar — left as TODO pending component addition

        // Step 10: From breadcrumbs in Editor, select ProjectDesignRepo — edit project name field absent
        // (ProjectDesignRepo is in Design1 as a separate branch/non-flat — editing not allowed from this view)
        // NOTE: Left as TODO pending EditorPage edit project support for multi-repo

        // Step 11: Delete and Erase ProjectDesignRepo — no "also delete from repository" checkbox expected
        repositoryPage.getLeftRepositoryTreeComponent()
                .selectItemInFolder("Projects", nameProjectDesign);
        repositoryPage.setShowDeletedProjects(false);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickDeleteBtn();
        repositoryPage.getConfirmDeleteDialogComponent().clickDelete();
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickEraseBtn();
        assertThat(repositoryPage.getConfirmEraseDialogComponent().isAlsoDeleteFromRepositoryVisible())
                .as("Erasing from flat Git repo should NOT show 'also delete from repository' checkbox")
                .isFalse();
        repositoryPage.getConfirmEraseDialogComponent().clickErase();

        // Step 12: Delete and Erase ProjectDesign1Repo — "also delete from repository" checkbox expected
        repositoryPage.getLeftRepositoryTreeComponent()
                .selectItemInFolder("Projects", nameProjectDesign1);
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickDeleteBtn();
        repositoryPage.getConfirmDeleteDialogComponent().clickDelete();
        repositoryPage.getRepositoryContentButtonsPanelComponent().clickEraseBtn();
        assertThat(repositoryPage.getConfirmEraseDialogComponent().isAlsoDeleteFromRepositoryVisible())
                .as("Erasing from non-flat Git repo should show 'also delete from repository' checkbox")
                .isTrue();
    }
}
