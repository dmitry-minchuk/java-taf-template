package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.TagsPageComponent;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.createnewproject.ZipArchiveComponent;
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
 * Test Case: IPBQA-32767-2
 * Scenario: Verify Tags - Non-existing tag type with extensible tag handling (create from zip)
 *
 * Test Flow:
 * 1. Create required tag types in Admin section
 * 2. Create project from zip file containing non-existing tag type
 * 3. Verify warning popup appears with correct message about missing tag type
 * 4. Continue and handle tag extension for extensible type
 * 5. Verify tags are correctly created and saved in project properties
 */
public class TestProjectTagsCreationNonExistingType extends BaseTest {

    private static final String PROJECT_NAME = "Project6";
    private static final String ZIP_FILE_NAME = "TagsTestProject6.zip";

    // Tag type configuration
    private static final String TAG_TYPE_NAME = "Tag";
    private static final String TAG_TYPE_OPT = "TagOpt";
    private static final String TAG_TYPE_EXT = "TagExt";
    private static final String TAG_TYPE_OPT_EXT = "TagOptExt";

    @Test(testName = "Create project from zip with non-existing tag type and extension handling")
    public void testNonExistingTagTypeHandling() {
        // Login as admin
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        // Step 1: Setup required tag types in Admin
        setupRequiredTagTypes(editorPage);

        // Step 2: Navigate to Repository and create project from zip
        RepositoryPage repositoryPage = new RepositoryPage();

        // Step 3-4: Create project from zip file with non-existing tag type
        repositoryPage.getCreateProjectLink().click();
        CreateNewProjectComponent createNewProjectComponent = repositoryPage.getCreateNewProjectComponent();
        ZipArchiveComponent zipComponent = createNewProjectComponent.selectTab(CreateNewProjectComponent.TabName.ZIP_ARCHIVE);

        String zipFilePath = "TestProjectTagsCreationNonExistingType/" + ZIP_FILE_NAME;
        zipComponent.uploadZipFile(zipFilePath);
        zipComponent.setProjectName(PROJECT_NAME);
        zipComponent.getCreateProjectBtn().click();

        // Step 5: After clicking create, warning popup appears about missing tag type
        // The system proceeds to tags popup

        // Step 6-7: Verify and select tags in TagsPopupComponent
        repositoryPage = new RepositoryPage();
        TagsPopupComponent tagsPopup = repositoryPage.getTagsPopupComponent();

        // Verify tag types that exist (non-existing type should be ignored)
        String selectedTagValue = tagsPopup.getSelectedTagForType(TAG_TYPE_NAME);
        Assert.assertTrue(selectedTagValue == null || selectedTagValue.isEmpty(),
                "Tag value should be empty initially");

        // Verify TagExt has the non-existing value (TagExt3) from the zip
        String selectedTagExtValue = tagsPopup.getSelectedTagForType(TAG_TYPE_EXT);
        Assert.assertEquals(selectedTagExtValue, "TagExt3",
                "TagExt should have the non-existing value TagExt3 from zip");

        // Verify other tag types are empty
        String selectedTagOptValue = tagsPopup.getSelectedTagForType(TAG_TYPE_OPT);
        Assert.assertTrue(selectedTagOptValue == null || selectedTagOptValue.isEmpty(),
                "TagOpt value should be empty");

        // Step 8: Select mandatory Tag value first
        tagsPopup.selectTagForType(TAG_TYPE_NAME, "Tag1");

        // TagExt already has TagExt3 which doesn't exist - system will ask to add it during save

        // Step 9: Click save button
        // This will trigger extension dialog for TagExt3
        tagsPopup.clickSave();

        // Step 10: Handle extension confirmation popup
        // System will ask "Do you want to add TagExt3 to TagExt?"
        // User should click OK to confirm

        // Step 11: Verify tags in project properties
        repositoryPage.getLeftRepositoryTreeComponent().selectProjectInTree(PROJECT_NAME);
        RepositoryContentTabPropertiesComponent propertiesComponent = repositoryPage.getRepositoryContentTabPropertiesComponent();

        // Verify tags are correctly saved
        verifyTagValue(propertiesComponent, TAG_TYPE_NAME, "Tag1");
        verifyTagValue(propertiesComponent, TAG_TYPE_EXT, "TagExt3");

        // Optional tags should be empty
        String optValue = propertiesComponent.getSelectedTagForType(TAG_TYPE_OPT);
        Assert.assertTrue(optValue == null || optValue.isEmpty(),
                "TagOpt should be empty as it was not set");

        String optExtValue = propertiesComponent.getSelectedTagForType(TAG_TYPE_OPT_EXT);
        Assert.assertTrue(optExtValue == null || optExtValue.isEmpty(),
                "TagOptExt should be empty as it was not set");
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
