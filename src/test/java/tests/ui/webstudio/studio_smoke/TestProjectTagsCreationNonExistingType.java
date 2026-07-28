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
import domain.ui.webstudio.pages.mainpages.AdminPage;
import domain.ui.webstudio.pages.mainpages.EditorPage;
import domain.ui.webstudio.pages.mainpages.ProjectDetailPage;
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
    @Description("Create project from zip with a non-existing tag type. KNOWN-FAILING on 6.4.0: the React UI "
            + "dropped the tag reconciliation dialogs AND does not apply a zip's tags at all — a project "
            + "created from TagsTestProject6.zip carries no tags. Asserts the intended behaviour (the zip's "
            + "tags are applied); needs confirming with the team whether the loss is intended.")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testNonExistingTagTypeHandling() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        setupRequiredTagTypes(editorPage);
        RepositoryPage repositoryPage = editorPage.getTabSwitcherComponent().selectTab(TabSwitcherComponent.TabName.REPOSITORY);
        repositoryPage.createProject(CreateNewProjectComponent.TabName.ZIP_ARCHIVE, PROJECT_NAME, ZIP_FILE_NAME, false);
        repositoryPage.fillCommitInfo();

        // The React UI no longer reconciles a zip's tags through popups (the old "Missing tags" and "Tags"
        // dialogs are gone): whatever the zip declares is applied as-is, so this reads the result instead.
        ProjectDetailPage projectDetail = repositoryPage.openProjectsList().openProjectDetail(PROJECT_NAME);
        Assert.assertEquals(projectDetail.getTagValueForType(TAG_TYPE_NAME), "Tag3",
                "The tag value declared by the zip should be applied");
        Assert.assertEquals(projectDetail.getTagValueForType(TAG_TYPE_EXT), "TagExt3",
                "An extensible type should take the zip's value");
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

}
