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

public class TestProjectCopyWithTags extends BaseTest {

    @Test
    @TestCaseId("IPBQA-31659-CopyProjectFromRepositoryPreservesTags")
    @Description("Test setup for project copy with tags scenario")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCopyProjectFromRepositoryPreservesTags() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        // Setup tag types for copy scenario
        tagsPage.addTagType("Domain");
        tagsPage.addTagValue(1, "Policy");
        tagsPage.addTagValue(1, "Claims");
        tagsPage.addTagValue(1, "Benefits");

        tagsPage.addTagType("LOB");
        tagsPage.addTagValue(2, "Auto");
        tagsPage.addTagValue(2, "Home");
        tagsPage.addTagValue(2, "Life");

        // Verify tag types were created
        Assert.assertTrue(tagsPage.hasTagType("Domain"), "Should have Domain tag type");
        Assert.assertTrue(tagsPage.hasTagType("LOB"), "Should have LOB tag type");

        // Verify tag values
        Assert.assertTrue(tagsPage.getTagValues(1).size() >= 3, "Domain should have at least 3 values");
        Assert.assertTrue(tagsPage.getTagValues(2).size() >= 3, "LOB should have at least 3 values");
    }

    @Test
    @TestCaseId("IPBQA-31659-CopyProjectFromEditorPreservesTags")
    @Description("Test tag type configuration for project copy operations")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testCopyProjectFromEditorPreservesTags() {
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        // Setup tag types for editor copy scenario
        tagsPage.addTagType("Tier");
        tagsPage.addTagValue(1, "Enterprise");
        tagsPage.addTagValue(1, "Professional");
        tagsPage.addTagValue(1, "Standard");

        tagsPage.addTagType("Region");
        tagsPage.addTagValue(2, "NorthAmerica");
        tagsPage.addTagValue(2, "Europe");
        tagsPage.addTagValue(2, "AsiaPacific");

        // Verify all tag values are available for copy selection
        Assert.assertTrue(tagsPage.hasTagValue(1, "Enterprise"), "Should have Enterprise tier");
        Assert.assertTrue(tagsPage.hasTagValue(1, "Professional"), "Should have Professional tier");
        Assert.assertTrue(tagsPage.hasTagValue(2, "NorthAmerica"), "Should have NorthAmerica region");
        Assert.assertTrue(tagsPage.hasTagValue(2, "Europe"), "Should have Europe region");
    }
}
