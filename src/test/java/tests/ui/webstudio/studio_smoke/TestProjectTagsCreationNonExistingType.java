package tests.ui.webstudio.studio_smoke;

import com.epam.reportportal.annotations.Description;
import com.epam.reportportal.annotations.TestCaseId;
import configuration.annotations.AppContainerConfig;
import configuration.appcontainer.AppContainerStartParameters;
import configuration.driver.LocalDriverPool;
import domain.serviceclasses.constants.User;
import domain.ui.webstudio.components.admincomponents.TagsPageComponent;
import domain.ui.webstudio.components.common.CreateNewProjectComponent;
import domain.ui.webstudio.components.common.TabSwitcherComponent;
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

public class TestProjectTagsCreationNonExistingType extends BaseTest {

    private static final String PROJECT_NAME = "Project6";
    private static final String ZIP_FILE_NAME = "TagsTestProject6.zip";

    // Tag type configuration
    private static final String TAG_TYPE_NAME = "Tag";
    private static final String TAG_TYPE_OPT = "TagOpt";
    private static final String TAG_TYPE_EXT = "TagExt";
    private static final String TAG_TYPE_OPT_EXT = "TagOptExt";

    @Test
    @TestCaseId("IPBQA-32767")
    @Description("Create project from zip with non-existing tag type")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testNonExistingTagTypeHandling() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        setupRequiredTagTypes(editorPage);
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, PROJECT_NAME, ZIP_FILE_NAME, false);
        repositoryPage.fillCommitInfo();

        // Step 5: After clicking create, warning popup appears and system proceeds to tags popup
        Assert.assertTrue(repositoryPage.getMissingTagsPopupComponent().isVisible(500), "Missing Tags PopupComponent should be visible!");
        Assert.assertTrue(repositoryPage.getMissingTagsPopupComponent().getAllWarnings().contains("TagNonExisging: Tag3"), "'TagNonExisging: Tag3' warning should be visible!");
        Assert.assertTrue(repositoryPage.getMissingTagsPopupComponent().getAllWarnings().contains("TagExt: TagExt3"), "'TagExt: TagExt3' warning should be visible!");
        repositoryPage.getMissingTagsPopupComponent().clickContinue();

        // Step 6-7: Verify and select tags in TagsPopupComponent
        TagsPopupComponent tagsPopup = repositoryPage.getTagsPopupComponent();
        String selectedTagValue = tagsPopup.getSelectedTagForType(TAG_TYPE_NAME);
        Assert.assertTrue(selectedTagValue.equals("[None]"), "Tag value should be empty for non-existing tag value");

        // Verify TagExt is set to first available value (TagExt1)
        String selectedTagExtValue = tagsPopup.getSelectedTagForType(TAG_TYPE_EXT);
        Assert.assertNotNull(selectedTagExtValue, "TagExt should have a value");

        // Select tags for the project
        tagsPopup.selectTagForType(TAG_TYPE_NAME, "Tag2")
                .selectTagForType(TAG_TYPE_EXT, "TagExt2")
                .selectTagForType(TAG_TYPE_OPT, "TagOpt2")
                .selectTagForType(TAG_TYPE_OPT_EXT, "TagOptExt2")
                .clickSave();

        // Step 9: Verify tags in project properties
        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME);
        RepositoryContentTabPropertiesComponent propertiesComponent = repositoryPage.getRepositoryContentTabSwitcherComponent().selectPropertiesTab();

        // Verify all tags are correctly saved
        verifyTagValue(propertiesComponent, TAG_TYPE_NAME, "Tag2");
        verifyTagValue(propertiesComponent, TAG_TYPE_EXT, "TagExt2");
        verifyTagValue(propertiesComponent, TAG_TYPE_OPT, "TagOpt2");
        verifyTagValue(propertiesComponent, TAG_TYPE_OPT_EXT, "TagOptExt2");
    }

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

    private void verifyTagValue(RepositoryContentTabPropertiesComponent propertiesComponent, String tagTypeName, String expectedValue) {
        String actualValue = propertiesComponent.getSelectedTagForType(tagTypeName);
        Assert.assertEquals(actualValue, expectedValue, String.format("Tag type '%s' should have value '%s', but got '%s'", tagTypeName, expectedValue, actualValue));
    }
}
