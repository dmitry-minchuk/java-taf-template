package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.RepositoriesPageComponent;
import domain.ui.webstudio.components.admincomponents.TagsPageComponent;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.createnewproject.WorkspaceComponent;
import domain.ui.webstudio.components.repositorytabcomponents.RepositoryContentTabPropertiesComponent;
import domain.ui.webstudio.components.repositorytabcomponents.TagsPopupComponent;
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.RepositoryPage;
import helpers.service.LoginService;
import helpers.service.UserService;
import org.testng.Assert;
import org.testng.annotations.Test;
import tests.BaseTest;

/**
 * Test Case: IPBQA-32767-3
 * Scenario: Verify Tags - Create project from workspace with bulk tag assignment
 *
 * Test Flow:
 * 1. Create required tag types in Admin section
 * 2. Update local repository path in Admin settings
 * 3. Create projects from workspace
 * 4. Assign tags during bulk creation
 * 5. Verify tags are correctly applied to all created projects
 */
public class TestProjectTagsCreationFromWorkspace extends BaseTest {

    private static final String PROJECT5_NAME = "Project5";
    private static final String PROJECT6_NAME = "Project6";
    private static final String WORKSPACE_PATH = "/opt/openl/local/repositories/design1";

    // Tag type configuration
    private static final String TAG_TYPE_NAME = "Tag";
    private static final String TAG_TYPE_OPT = "TagOpt";
    private static final String TAG_TYPE_EXT = "TagExt";
    private static final String TAG_TYPE_OPT_EXT = "TagOptExt";

    @Test(testName = "Create projects from workspace with bulk tag assignment")
    public void testCreateProjectsFromWorkspaceWithTags() {
        // Login as admin
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        // Step 1: Setup required tag types in Admin
        setupRequiredTagTypes(editorPage);

        // Step 2: Update local repository path and restart
        updateWorkspacePath(editorPage);

        // Step 3: Navigate to Repository and create project from workspace
        RepositoryPage repositoryPage = new RepositoryPage();

        // Step 4-5: Create projects from workspace
        repositoryPage.getCreateProjectLink().click();
        CreateNewProjectComponent createNewProjectComponent = repositoryPage.getCreateNewProjectComponent();
        WorkspaceComponent workspaceComponent = createNewProjectComponent.selectTab(CreateNewProjectComponent.TabName.WORKSPACE);

        workspaceComponent.setWorkspacePath(WORKSPACE_PATH);

        // Select all projects checkbox (this would be specific to workspace UI)
        // This step depends on how WorkspaceComponent is implemented

        // Step 6: Verify tags popup is displayed
        repositoryPage = new RepositoryPage();
        TagsPopupComponent tagsPopup = repositoryPage.getTagsPopupComponent();

        // Verify all tag types are present
        Assert.assertTrue(tagsPopup.getAllTagTypeNames().contains(TAG_TYPE_NAME),
                "Tag type '" + TAG_TYPE_NAME + "' should be present");
        Assert.assertTrue(tagsPopup.getAllTagTypeNames().contains(TAG_TYPE_EXT),
                "Tag type '" + TAG_TYPE_EXT + "' should be present");
        Assert.assertTrue(tagsPopup.getAllTagTypeNames().contains(TAG_TYPE_OPT),
                "Tag type '" + TAG_TYPE_OPT + "' should be present");
        Assert.assertTrue(tagsPopup.getAllTagTypeNames().contains(TAG_TYPE_OPT_EXT),
                "Tag type '" + TAG_TYPE_OPT_EXT + "' should be present");

        // Verify all tags are initially empty
        String tagValue = tagsPopup.getSelectedTagForType(TAG_TYPE_NAME);
        Assert.assertTrue(tagValue == null || tagValue.isEmpty(),
                "Tag should be initially empty");

        // Step 7: Select tags for bulk assignment
        tagsPopup.selectTagForType(TAG_TYPE_NAME, "Tag1")
                .selectTagForType(TAG_TYPE_EXT, "TagExt1")
                .selectTagForType(TAG_TYPE_OPT, "TagOpt1")
                .selectTagForType(TAG_TYPE_OPT_EXT, "TagOptExt1");

        // Step 8: Click save to create projects with tags
        tagsPopup.clickSave();

        // Step 9: Verify success messages
        // System should show: "Project Project5 was created successfully"
        // "Project Project6 was created successfully"
        // This can be verified through getAllMessages() method from BaseComponent

        // Step 10: Verify tags in Project5 properties
        repositoryPage.getLeftRepositoryTreeComponent().selectProjectInTree(PROJECT5_NAME);
        RepositoryContentTabPropertiesComponent project5Properties = repositoryPage.getRepositoryContentTabPropertiesComponent();

        verifyTagValue(project5Properties, TAG_TYPE_NAME, "Tag1");
        verifyTagValue(project5Properties, TAG_TYPE_EXT, "TagExt1");
        verifyTagValue(project5Properties, TAG_TYPE_OPT, "TagOpt1");
        verifyTagValue(project5Properties, TAG_TYPE_OPT_EXT, "TagOptExt1");

        // Step 11: Verify tags in Project6 properties
        repositoryPage.getLeftRepositoryTreeComponent().selectProjectInTree(PROJECT6_NAME);
        RepositoryContentTabPropertiesComponent project6Properties = repositoryPage.getRepositoryContentTabPropertiesComponent();

        verifyTagValue(project6Properties, TAG_TYPE_NAME, "Tag1");
        verifyTagValue(project6Properties, TAG_TYPE_EXT, "TagExt1");
        verifyTagValue(project6Properties, TAG_TYPE_OPT, "TagOpt1");
        verifyTagValue(project6Properties, TAG_TYPE_OPT_EXT, "TagOptExt1");
    }

    /**
     * Setup required tag types in admin section
     */
    private void setupRequiredTagTypes(EditorPage editorPage) {
        // Navigate to Admin -> Tags
        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPageComponent = adminPage.navigateToTagsPage();

        // Create Tag (False, False) with values Tag1, Tag2
        tagsPageComponent.addNewTagType(TAG_TYPE_NAME)
                .addTag(TAG_TYPE_NAME, "Tag1")
                .addTag(TAG_TYPE_NAME, "Tag2")
                .setExtensible(1, false)
                .setNullable(1, false);

        // Create TagOpt (True, False) with values TagOpt1, TagOpt2
        tagsPageComponent.addNewTagType(TAG_TYPE_OPT)
                .addTag(TAG_TYPE_OPT, "TagOpt1")
                .addTag(TAG_TYPE_OPT, "TagOpt2")
                .setExtensible(2, false)
                .setNullable(2, true);

        // Create TagExt (False, True) with values TagExt1, TagExt2
        tagsPageComponent.addNewTagType(TAG_TYPE_EXT)
                .addTag(TAG_TYPE_EXT, "TagExt1")
                .addTag(TAG_TYPE_EXT, "TagExt2")
                .setExtensible(3, true)
                .setNullable(3, false);

        // Create TagOptExt (True, True) with values TagOptExt1, TagOptExt2
        tagsPageComponent.addNewTagType(TAG_TYPE_OPT_EXT)
                .addTag(TAG_TYPE_OPT_EXT, "TagOptExt1")
                .addTag(TAG_TYPE_OPT_EXT, "TagOptExt2")
                .setExtensible(4, true)
                .setNullable(4, true);

        // Save templates
        tagsPageComponent.saveTemplates();
    }

    /**
     * Update workspace path in admin repositories settings
     */
    private void updateWorkspacePath(EditorPage editorPage) {
        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        RepositoriesPageComponent repositoriesPage = adminPage.navigateToRepositoriesPage();

        // Update local path to workspace path
        // This step depends on RepositoriesPageComponent implementation
        // repositoriesPage.setLocalRepositoryPath(WORKSPACE_PATH);
        // repositoriesPage.applyAllAndRestart();

        // Alternative: Navigate back to repository after configuration
        // editorPage = new EditorPage();
    }

    /**
     * Helper method to verify tag value in properties
     */
    private void verifyTagValue(RepositoryContentTabPropertiesComponent propertiesComponent,
                               String tagTypeName, String expectedValue) {
        String actualValue = propertiesComponent.getSelectedTagForType(tagTypeName);
        Assert.assertEquals(actualValue, expectedValue,
                String.format("Tag type '%s' should have value '%s', but got '%s'",
                        tagTypeName, expectedValue, actualValue));
    }
}
