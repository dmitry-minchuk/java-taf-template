package tests.ui.webstudio.studio_tags;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.TagsPageComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.Assert;
import org.testng.annotations.Test;
import tests.BaseTest;

public class TestImportProjectWithTags extends BaseTest {

    @Test
    @TestCaseId("IPBQA-31659-CreateProjectFromWorkspaceWithTags")
    @Description("Test creating project from workspace into repository with tag assignment")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCreateProjectFromWorkspaceWithTags() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        // Setup tag types
        tagsPage.addTagType("Type");
        tagsPage.addTagValue(1, "Workspace");
        tagsPage.addTagValue(1, "Repository");
        tagsPage.addTagValue(1, "Shared");

        tagsPage.addTagType("Purpose");
        tagsPage.addTagValue(2, "Testing");
        tagsPage.addTagValue(2, "Production");
        tagsPage.addTagValue(2, "Development");

        // Create a project in workspace (local)
        editorPage = tagsPage.returnToEditorPage();
        editorPage.createProjectInWorkspace("LocalWorkspaceProject");

        // Verify project exists in workspace
        Assert.assertTrue(editorPage.projectExistsInWorkspace("LocalWorkspaceProject"),
                "Project should be created in workspace");

        // Open project to verify it works
        editorPage.openProject("LocalWorkspaceProject");
        Assert.assertTrue(editorPage.isProjectOpen("LocalWorkspaceProject"),
                "Project should be openable from workspace");

        // Now save/publish this project to repository with tags
        editorPage.saveProjectToRepository("LocalWorkspaceProject");

        // Repository creation dialog appears with tag selection
        Assert.assertTrue(editorPage.isRepositoryCreationDialogVisible(),
                "Repository creation dialog should appear");

        // Fill in project name in repository
        editorPage.enterRepositoryProjectName("PublishedProject");

        // Assign tags
        editorPage.selectTagValue("Type", "Repository");
        editorPage.selectTagValue("Purpose", "Production");

        // Confirm publish to repository
        editorPage.confirmRepositoryPublish();

        // Verify project now appears in repository
        editorPage.openRepository();
        Assert.assertTrue(editorPage.projectExists("PublishedProject"),
                "Project should exist in repository after publish");

        // Verify tags were applied
        editorPage.openProjectProperties("PublishedProject");
        Assert.assertEquals(editorPage.getSelectedTagValue("Type"), "Repository",
                "Published project should have Type=Repository");
        Assert.assertEquals(editorPage.getSelectedTagValue("Purpose"), "Production",
                "Published project should have Purpose=Production");
        editorPage.closeProjectProperties();

        // Workspace project may still exist locally
        Assert.assertTrue(editorPage.projectExistsInWorkspace("LocalWorkspaceProject"),
                "Original workspace project should still exist");
    }

    @Test
    @TestCaseId("IPBQA-31659-ImportProjectFromRepositoryWithTags")
    @Description("Test importing project from repository with tag assignment")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testImportProjectFromRepositoryWithTags() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        // Setup tag types
        tagsPage.addTagType("Source");
        tagsPage.addTagValue(1, "Migrated");
        tagsPage.addTagValue(1, "Legacy");
        tagsPage.addTagValue(1, "New");

        tagsPage.addTagType("Owner");
        tagsPage.addTagValue(2, "TeamA");
        tagsPage.addTagValue(2, "TeamB");
        tagsPage.addTagValue(2, "TeamC");

        // Create initial project in repository with some tags
        editorPage = tagsPage.returnToEditorPage();
        editorPage.openRepository();
        editorPage.createProject("RepositorySource");

        // Add tags to source project
        editorPage.openProjectProperties("RepositorySource");
        editorPage.selectTagValue("Source", "Legacy");
        editorPage.selectTagValue("Owner", "TeamA");
        editorPage.saveProjectProperties();
        editorPage.closeProjectProperties();

        // Verify initial state
        editorPage.openProjectProperties("RepositorySource");
        Assert.assertEquals(editorPage.getSelectedTagValue("Source"), "Legacy",
                "Source project should have Legacy tag");
        editorPage.closeProjectProperties();

        // Delete the project from one location but keep in source
        editorPage.openRepository();
        editorPage.deleteProjectFromLocal("RepositorySource");

        // Verify it's deleted locally
        Assert.assertFalse(editorPage.projectExistsLocally("RepositorySource"),
                "Project should be deleted from local storage");

        // Import project back from repository
        editorPage.importProjectFromRepository("RepositorySource");

        // Import dialog should appear with tag reassignment capability
        Assert.assertTrue(editorPage.isProjectImportDialogVisible(),
                "Import dialog should be visible");

        // During import, change tags to different values
        editorPage.selectTagValue("Source", "Migrated");  // Different from original
        editorPage.selectTagValue("Owner", "TeamB");      // Different from original

        // Confirm import
        editorPage.confirmImport();

        // Verify imported project has new tags
        editorPage.openProjectProperties("RepositorySource");
        Assert.assertEquals(editorPage.getSelectedTagValue("Source"), "Migrated",
                "Imported project should have updated Source tag");
        Assert.assertEquals(editorPage.getSelectedTagValue("Owner"), "TeamB",
                "Imported project should have updated Owner tag");
        editorPage.closeProjectProperties();

        // Verify project is now available locally
        Assert.assertTrue(editorPage.projectExistsLocally("RepositorySource"),
                "Project should exist locally after import");

        // Verify project still exists in repository
        editorPage.openRepository();
        Assert.assertTrue(editorPage.projectExists("RepositorySource"),
                "Project should still exist in repository");
    }
}
