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

public class TestProjectTagsCreationFromWorkspace extends BaseTest {

    private static final String PROJECT_NAME_5 = "Project5";
    private static final String PROJECT_NAME_6 = "Project6";
    private static final String ZIP_FILE_NAME_5 = "TagsTestProject5.zip";
    private static final String ZIP_FILE_NAME_6 = "TagsTestProject5.zip";

    // Tag type configuration
    private static final String TAG_TYPE_NAME = "Tag";
    private static final String TAG_TYPE_OPT = "TagOpt";
    private static final String TAG_TYPE_EXT = "TagExt";
    private static final String TAG_TYPE_OPT_EXT = "TagOptExt";

    @Test
    @TestCaseId("IPBQA-32767")
    @Description("Create project from workspace with tags")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCreateProjectFromWorkspaceWithTags() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        setupRequiredTagTypes(editorPage);
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, PROJECT_NAME_5, ZIP_FILE_NAME_5, false);
        repositoryPage.fillCommitInfo();
        repositoryPage.getMissingTagsPopupComponent().clickContinue();
        TagsPopupComponent tagsPopup = repositoryPage.getTagsPopupComponent();
        tagsPopup.selectTagForType(TAG_TYPE_NAME, "Tag2")
                .selectTagForType(TAG_TYPE_EXT, "TagExt2")
                .selectTagForType(TAG_TYPE_OPT, "TagOpt2")
                .selectTagForType(TAG_TYPE_OPT_EXT, "TagOptExt2")
                .clickSave();

        repositoryPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, PROJECT_NAME_6, ZIP_FILE_NAME_6, false);
        repositoryPage.getMissingTagsPopupComponent().clickContinue();
        tagsPopup.selectTagForType(TAG_TYPE_NAME, "Tag2")
                .selectTagForType(TAG_TYPE_EXT, "TagExt2")
                .selectTagForType(TAG_TYPE_OPT, "TagOpt2")
                .selectTagForType(TAG_TYPE_OPT_EXT, "TagOptExt2")
                .clickSave();

        repositoryPage.openUserMenu()
                .navigateToAdministration()
                .navigateToRepositoriesPage()
                .setRepositoryPath("/opt/openl/local/repositories/design1")
                .applyChangesAndRelogin(User.ADMIN);

        editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProjectFromWorkSpace(null, null, true);

        Assert.assertEquals(tagsPopup.getSelectedTagForType(TAG_TYPE_NAME), "[None]", "Tag value should be empty for new project");
        Assert.assertEquals(tagsPopup.getSelectedTagForType(TAG_TYPE_EXT), "[None]", "Tag value should be empty for new project");
        Assert.assertEquals(tagsPopup.getSelectedTagForType(TAG_TYPE_OPT), "[None]", "Tag value should be empty for new project");
        Assert.assertEquals(tagsPopup.getSelectedTagForType(TAG_TYPE_OPT_EXT), "[None]", "Tag value should be empty for new project");

        // Select tags for the project
        tagsPopup.selectTagForType(TAG_TYPE_NAME, "Tag1")
                .selectTagForType(TAG_TYPE_EXT, "TagExt1")
                .selectTagForType(TAG_TYPE_OPT, "TagOpt1")
                .selectTagForType(TAG_TYPE_OPT_EXT, "TagOptExt1")
                .clickSave();

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME_5);
        RepositoryContentTabPropertiesComponent propertiesComponent = repositoryPage.getRepositoryContentTabPropertiesComponent();

        repositoryPage.getLeftRepositoryTreeComponent()
                .expandFolderInTree("Projects")
                .selectItemInFolder("Projects", PROJECT_NAME_6);
        propertiesComponent = repositoryPage.getRepositoryContentTabPropertiesComponent();

        // Verify all tags are correctly saved
        verifyTagValue(propertiesComponent, TAG_TYPE_NAME, "Tag1");
        verifyTagValue(propertiesComponent, TAG_TYPE_EXT, "TagExt1");
        verifyTagValue(propertiesComponent, TAG_TYPE_OPT, "TagOpt1");
        verifyTagValue(propertiesComponent, TAG_TYPE_OPT_EXT, "TagOptExt1");
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
