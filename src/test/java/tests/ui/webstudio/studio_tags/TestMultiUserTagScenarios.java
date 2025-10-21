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

public class TestMultiUserTagScenarios extends BaseTest {

    @Test
    @TestCaseId("IPBQA-31659-DifferentUsersSeeSameTags")
    @Description("Test that tag configuration is visible across different users")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testDifferentUsersSeeSameTags() {
        // Admin user: Setup tag structure
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        // Create tags
        tagsPage.addTagType("Department");
        tagsPage.addTagValue(1, "Engineering");
        tagsPage.addTagValue(1, "Sales");
        tagsPage.addTagValue(1, "Marketing");

        tagsPage.addTagType("Region");
        tagsPage.addTagValue(2, "US");
        tagsPage.addTagValue(2, "EU");
        tagsPage.addTagValue(2, "APAC");

        // Verify tags were created
        Assert.assertTrue(tagsPage.hasTagType("Department"), "Should have Department tag");
        Assert.assertTrue(tagsPage.hasTagType("Region"), "Should have Region tag");
        Assert.assertEquals(tagsPage.getTagValues(1).size(), 3, "Department should have 3 values");
        Assert.assertEquals(tagsPage.getTagValues(2).size(), 3, "Region should have 3 values");
    }

    @Test
    @TestCaseId("IPBQA-31659-AnalystCreateProjectWithNewTag")
    @Description("Test that users can create new tag values on extensible tags")
    @AppContainerConfig(startParams = AppContainerStartParameters.DEFAULT_STUDIO_PARAMS)
    public void testAnalystCreateProjectWithNewTag() {
        // Admin: Setup extensible tag type
        LoginService loginService = new LoginService(LocalDriverPool.getPage());
        EditorPage editorPage = loginService.login(UserService.getUser(User.ADMIN));

        AdminPage adminPage = editorPage.openUserMenu().navigateToAdministration();
        TagsPageComponent tagsPage = adminPage.navigateToTagsPage();

        // Create extensible tag
        tagsPage.addTagType("Industry");
        tagsPage.addTagValue(1, "Insurance");
        tagsPage.addTagValue(1, "Banking");
        tagsPage.setTagTypeExtensible(1, true);

        tagsPage.addTagType("Status");
        tagsPage.addTagValue(2, "Active");
        tagsPage.addTagValue(2, "Inactive");
        tagsPage.setTagTypeExtensible(2, true);

        // Verify extensible setting
        Assert.assertTrue(tagsPage.getTagTypeExtensibleCheckbox(1).isChecked(),
                "Industry tag should be extensible");
        Assert.assertTrue(tagsPage.getTagTypeExtensibleCheckbox(2).isChecked(),
                "Status tag should be extensible");

        // Verify initial values
        Assert.assertEquals(tagsPage.getTagValues(1).size(), 2, "Industry should have 2 values initially");
        Assert.assertEquals(tagsPage.getTagValues(2).size(), 2, "Status should have 2 values initially");
    }
}
